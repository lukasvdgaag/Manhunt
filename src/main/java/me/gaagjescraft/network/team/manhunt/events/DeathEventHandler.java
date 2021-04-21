package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.games.*;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DeathEventHandler implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() != EntityType.PLAYER) return;
        Player damager = (Player) e.getDamager();
        Game game = Game.getGame(damager);
        if (game == null) return;
        GamePlayer dgp = game.getPlayer(damager);

        if (dgp.isDead()) {
            e.setCancelled(true);
            e.setDamage(0);
            return;
        }

        if (e.getEntity().getType() != EntityType.PLAYER) return;

        Player player = (Player) e.getEntity();
        GamePlayer gp = game.getPlayer(player);

        if (gp == null || gp.isDead()) {
            e.setCancelled(true);
            e.setDamage(0);
            return;
        }

        if (gp.getPlayerType() == dgp.getPlayerType() && (gp.getPlayerType() == PlayerType.RUNNER || !game.isAllowFriendlyFire())) {
            e.setCancelled(true);
            e.setDamage(0);
        }
    }

    @EventHandler
    public void onHeal(EntityRegainHealthEvent e) {
        if (e.getEntity().getType() != EntityType.PLAYER) return;
        Player player = (Player) e.getEntity();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);
        if (game.isTwistsAllowed() && game.isEventActive() && game.getSelectedTwist() == TwistVote.HARDCORE) {
            e.setCancelled(true);
            e.setAmount(0);
        }
        gp.updateHealthTag();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity().getType() != EntityType.PLAYER) return;
        Player player = (Player) e.getEntity();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        gp.updateHealthTag();

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING || (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds() + 15) ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.RUNNER && game.getTimer() <= 20)) {
            e.setCancelled(true);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            e.setDamage(0);
            return;
        }

        if (player.getHealth() - e.getFinalDamage() > 0) {
            player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getLocation().add(0, 1, 0), 2, Material.REDSTONE_WIRE.createBlockData());
            return;
        }

        player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getLocation().add(0, 1, 0), 1, Material.REDSTONE_BLOCK.createBlockData());
        player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getLocation(), 1, Material.REDSTONE_BLOCK.createBlockData());

        // dropping inventory items before clearing inventory.
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.isSimilar(Itemizer.MANHUNT_RUNNER_TRACKER)) continue;
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);

        gp.addDeath();
        gp.setDead(true);
        gp.prepareForRespawn();


        if (gp.getPlayerType() == PlayerType.RUNNER && gp.getDeaths() == 1) {
            String killMsg = determineDeathMessage(player, e.getCause());

            player.sendTitle("§c§lYOU DIED!", "§7You have 0 lives left.", 20, 60, 20);

            for (GamePlayer gp1 : game.getPlayers()) {
                Player p = Bukkit.getPlayer(gp1.getUuid());

                if (gp1.getTracking() != null && gp1.getTracking().getUuid().equals(player.getUniqueId())) {
                    if (p != null)
                        p.sendMessage("§cYour tracker has been reset because the runner you were tracking died.");
                    gp1.setTracking(null);
                }

                if (p == null) continue;

                p.sendMessage("§7§lSPEED RUNNER DOWN!");
                p.sendMessage(killMsg);
                if (!p.equals(player)) {
                    p.sendTitle("§7§lSPEED RUNNER DOWN!", "§c" + player.getName() + " died!", 20, 60, 20);
                }
            }
        } else if (gp.getPlayerType() == PlayerType.HUNTER && gp.getDeaths() <= 3) {
            String killMsg = determineDeathMessage(player, e.getCause());

            if (gp.getDeaths() >= 3) {
                // out of lives
                player.sendTitle("§c§lYOU DIED!", "§7You have 0 lives left.", 20, 60, 20);
            } else {
                player.sendTitle("§c§lYOU DIED!", "§7You have " + (3 - gp.getDeaths()) + " lives left.", 5, 30, 5);
            }

            game.sendMessage(null, killMsg);
        }

        game.checkForWin(false);
    }

    private String determineDeathMessage(Player player, EntityDamageEvent.DamageCause cause) {
        Game game = Game.getGame(player);
        if (game == null) return "";

        GamePlayer gp = game.getPlayer(player);

        String prefix = gp.getPrefix() + " " + player.getName() + " §cdied!";

        if (player.getKiller() != null) {
            GamePlayer killerGp = game.getPlayer(player.getKiller());
            if (killerGp != null) {
                return gp.getPrefix() + " " + player.getName() + " §cwas murdered by " + killerGp.getPrefix() + " " + player.getKiller().getName();
            }
        }

        // no killer
        if (game.isEventActive() && game.getSelectedTwist() == TwistVote.ACID_RAIN && cause == EntityDamageEvent.DamageCause.CUSTOM) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cperished from the effects of the §2Acid Rain";
        } else if (game.isEventActive() && game.getSelectedTwist() == TwistVote.HARDCORE) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cfailed to survive during the §4Hardcore mode§c!";
        } else if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cwas blown up into a million pieces.";
        } else if (cause == EntityDamageEvent.DamageCause.DROWNING) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cforgot they couldn't breath under water.";
        } else if (cause == EntityDamageEvent.DamageCause.FIRE) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cburnt to a crisp.";
        } else if (cause == EntityDamageEvent.DamageCause.FALL) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cfell to their death.";
        } else if (cause == EntityDamageEvent.DamageCause.LAVA) {
            prefix = gp.getPrefix() + " " + player.getName() + " §ctried to take a swim in lava.";
        } else if (cause == EntityDamageEvent.DamageCause.LIGHTNING) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cwas stuck by lightning.";
        } else if (cause == EntityDamageEvent.DamageCause.SUICIDE) {
            prefix = gp.getPrefix() + " " + player.getName() + " §ccommitted suicide. Can we get an F in chat?";
        } else if (cause == EntityDamageEvent.DamageCause.PROJECTILE) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cwas pierces by a projectile.";
        } else if (cause == EntityDamageEvent.DamageCause.POISON) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cdied of poison. Stop eating them rotten flesh!";
        } else if (cause == EntityDamageEvent.DamageCause.DRYOUT) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cforgot to drink and dried out.";
        } else if (cause == EntityDamageEvent.DamageCause.FALLING_BLOCK) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cwas crushed by a falling block.";
        } else if (cause == EntityDamageEvent.DamageCause.WITHER) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cwas defeated by the wither.";
        } else if (cause == EntityDamageEvent.DamageCause.MAGIC) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cdied of a magical spell.";
        } else if (cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cwas blown up into peaces by a potential creeper.";
        } else if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            prefix = gp.getPrefix() + " " + player.getName() + " §cwas murdered by an entity.";
        } else {
            prefix = gp.getPrefix() + " " + player.getName() + " §cdied!";
        }

        if (gp.isFullyDead()) {
            prefix += " §6§lFINAL DEATH!";
        }

        return prefix;

    }

}
