package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.*;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class DeathEventHandler implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
        Player player = e.getEntity();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);
        doCheckThingForDeath(player, game, gp, player.getLastDamageCause().getCause(), null);
        e.getDrops().clear();
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() == EntityType.ENDER_DRAGON) {
            if (e.getDamager().getType() == EntityType.PLAYER) {
                Player damager = (Player) e.getDamager();
                Game game = Game.getGame(damager);
                if (game == null) return;
                GamePlayer dgp = game.getPlayer(damager);
                if (dgp.isDead() || dgp.getPlayerType() == PlayerType.HUNTER) {
                    e.setCancelled(true);
                    e.setDamage(0);
                } else if (dgp.getPlayerType() == PlayerType.RUNNER) {
                    EnderDragon dragon = ((EnderDragon) e.getEntity());
                    if (dragon.getHealth() - e.getFinalDamage() <= 0) {
                        dragon.getBossBar().removeAll();
                        game.sendMessage(null, Util.c(Manhunt.get().getCfg().dragonDefeatedMessage).replaceAll("%prefix%", dgp.getPrefix()).replaceAll("%player%", damager.getName()));
                        for (GamePlayer gp : game.getPlayers(null)) {
                            Player p = Bukkit.getPlayer(gp.getUuid());
                            if (p == null || !gp.isOnline()) continue;
                            Util.sendTitle(p, Util.c(Manhunt.get().getCfg().dragonDefeatedTitle).replaceAll("%prefix%", dgp.getPrefix()).replaceAll("%player%", damager.getName()), 20, 50, 20);
                        }
                        game.setDragonDefeated(true);
                        game.checkForWin(true);
                    }
                }
            }
            return;
        }

        if (e.getEntity().getType() == EntityType.PLAYER) {
            Player player = (Player) e.getEntity();
            Game game = Game.getGame(player);
            if (game == null) return;
            GamePlayer gp = game.getPlayer(player);
            if (gp == null || gp.isDead()) {
                e.setDamage(0);
                e.setCancelled(true);
                return;
            }
            if (player.getHealth() - e.getFinalDamage() <= 0) {
                // player got killed by an entity.
                // todo stuff here
                e.setCancelled(true);
                doCheckThingForDeath(player, game, gp, EntityDamageEvent.DamageCause.ENTITY_ATTACK, e.getDamager());
                return;
            }

        }

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
        if (game.isTwistsAllowed() && game.isEventActive() && game.getSelectedTwist() == TwistVote.HARDCORE) {
            e.setCancelled(true);
            e.setAmount(0);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity().getType() != EntityType.PLAYER) return;
        Player player = (Player) e.getEntity();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING || (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds() + 15) ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.RUNNER && game.getTimer() <= 20)) {
            e.setCancelled(true);
            player.setHealth(20);
            player.setMaxHealth(20);
            player.setFoodLevel(20);
            e.setDamage(0);
            return;
        }

        if (player.getHealth() - e.getFinalDamage() > 0) {
            player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getLocation().add(0, 1, 0), 4, Material.REDSTONE_BLOCK.createBlockData());
        }
        // dropping inventory items before clearing inventory.
        //doCheckThingForDeath(player, game, gp, e.getCause(), player.getLastDamageCause() == null ? null : player.getLastDamageCause().getEntity());
    }

    private void doCheckThingForDeath(Player player, Game game, GamePlayer gp, EntityDamageEvent.DamageCause cause, Entity killer) {
        player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getLocation().add(0, 1, 0), 6, Material.REDSTONE_BLOCK.createBlockData());
        player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getLocation(), 6, Material.REDSTONE_BLOCK.createBlockData());

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType() == Material.COMPASS && item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', Manhunt.get().getCfg().generalTrackerDisplayname)))
                continue;
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

        player.setHealth(20);
        player.setMaxHealth(20);
        player.setFireTicks(0);
        player.setFoodLevel(20);

        gp.addDeath();
        gp.setDead(true);
        gp.prepareForRespawn();


        if (gp.getPlayerType() == PlayerType.RUNNER && gp.getDeaths() == 1) {
            String killMsg = determineDeathMessage(player, cause, killer);

            Util.sendTitle(player, Util.c(Manhunt.get().getCfg().deathTitle).replaceAll("%lives%", "0"), 20, 60, 20);

            for (GamePlayer gp1 : game.getPlayers()) {
                Player p = Bukkit.getPlayer(gp1.getUuid());

                if (gp1.getTracking() != null && gp1.getTracking().getUuid().equals(player.getUniqueId())) {
                    if (p != null && gp1.isOnline())
                        p.sendMessage(Util.c(Manhunt.get().getCfg().trackerResetDiedMessage));
                    gp1.setTracking(null);
                }

                if (p == null) continue;

                p.sendMessage(Util.c(Manhunt.get().getCfg().runnerDownMessage));
                p.sendMessage(killMsg);
                if (!p.equals(player)) {
                    Util.sendTitle(p, Util.c(Manhunt.get().getCfg().runnerDownTitle).replaceAll("%player%", player.getName()), 20, 60, 20);
                }
            }
        } else if (gp.getPlayerType() == PlayerType.HUNTER && gp.getDeaths() <= 3) {
            String killMsg = determineDeathMessage(player, cause, killer);
            int livesLeft = gp.getDeaths() >= 3 ? 0 : (3 - gp.getDeaths());
            Util.sendTitle(player, Util.c(Manhunt.get().getCfg().deathTitle).replaceAll("%lives%", livesLeft + ""), 20, 60, 20);

            game.sendMessage(null, killMsg);
        }

        game.checkForWin(false);
    }

    private String determineDeathMessage(Player player, EntityDamageEvent.DamageCause cause, @Nullable Entity killer) {
        Game game = Game.getGame(player);
        if (game == null) return "";

        GamePlayer gp = game.getPlayer(player);

        String prefix;

        if (killer == null && player.getKiller() != null) {
            killer = player.getKiller();
        }

        if (killer != null && killer.getType() == EntityType.PLAYER) {
            GamePlayer killerGp = game.getPlayer((Player) killer);
            if (killerGp != null) {
                if (gp.getPlayerType() != killerGp.getPlayerType()) {
                    killerGp.addKill();
                }
                return Util.c(gp.getPrefix() + " " + player.getName() + " §cwas murdered by " + killerGp.getPrefix() + " " + killer.getName());
            }
        }
        if (killer != null) {
            if (killer.getType() == EntityType.ZOMBIE) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwas too weak to fight a zombie!";
            } else if (killer.getType() == EntityType.ENDERMAN) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cmade an enderman mad and had to suffer the consequences.";
            } else if (killer.getType() == EntityType.ARROW) {
                Arrow arrow = (Arrow) killer;
                if (arrow.getShooter() != null) {
                    if (arrow.getShooter() instanceof Player) {
                        GamePlayer killerGP = game.getPlayer(((Player) arrow.getShooter()));
                        if (killerGP != null) {
                            prefix = gp.getPrefix() + " " + player.getName() + " §cwas shot by " + killerGP.getPrefix() + " " + ((Player) arrow.getShooter()).getName();
                        } else {
                            prefix = gp.getPrefix() + " " + player.getName() + " §cwas shot to death.";
                        }
                    } else {
                        prefix = gp.getPrefix() + " " + player.getName() + " §cwas shot to death by a " + ((Entity) arrow.getShooter()).getType().getName();
                    }
                } else {
                    prefix = gp.getPrefix() + " " + player.getName() + " §cwas shot to death.";
                }
            } else if (killer.getType() == EntityType.BEE) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwas stung to death by a bee.";
            } else if (killer.getType() == EntityType.BLAZE) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwas turned into a pile of ash by a blaze.";
            } else if (killer.getType() == EntityType.SPIDER) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwas mistaken for a fly by a spider and died.";
            } else if (killer.getType() == EntityType.CREEPER) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwas yeeted into the air by a creeper.";
            } else if (killer.getType() == EntityType.CAVE_SPIDER) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwas sneaked upon by a sneaky cave spider.";
            } else if (killer.getType() == EntityType.GUARDIAN || killer.getType() == EntityType.ELDER_GUARDIAN) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cdidn't manage to survive the beam from a guardian.";
            } else if (killer.getType() == EntityType.ENDER_DRAGON) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwas defeated by the §5Ender Dragon§c! Should've been the other way around lol.";
            } else if (killer.getType() == EntityType.DROWNED) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwas overpowered by a drowned and is now one of them.";
            } else if (killer.getType() == EntityType.SILVERFISH) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwas attacked by a gross silverfish. Those nasty creatures...";
            } else if (killer.getType() == EntityType.WITCH) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwas enchanted by a witch and died.";
            } else if (killer.getType() == EntityType.HOGLIN) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cgot his ass swooped by a hoglin.";
            } else if (killer.getType() == EntityType.PIGLIN || killer.getType() == EntityType.PIGLIN_BRUTE) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cdidn't have enough gold on them, so a piglin killed them. Welp.";
            } else if (killer.getType() == EntityType.ZOMBIFIED_PIGLIN) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cmade a pigman mad, and well... yk.";
            } else if (killer.getType() == EntityType.IRON_GOLEM) {
                prefix = gp.getPrefix() + " " + player.getName() + " §ctouched one of the villagers and angrified the iron golem.";
            } else if (killer.getType() == EntityType.WOLF) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cmade mother wolf angry and couldn't defend themselves.";
            } else if (killer.getType() == EntityType.CAT) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwas looking at this not so cute kitty and got scratched to death.";
            } else if (killer.getType() == EntityType.FOX) {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwoke up daddy fox and got bitten to death.";
            } else {
                prefix = gp.getPrefix() + " " + player.getName() + " §cwas killed by a " + killer.getType().getName();
            }
        } else {
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
        }

        if (gp.isFullyDead()) {
            prefix += " §6§lFINAL DEATH!";
        }

        return Util.c(prefix);

    }

}
