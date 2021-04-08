package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEventHandler implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() != EntityType.PLAYER || e.getDamager().getType() != EntityType.PLAYER) return;
        Player player = (Player) e.getEntity();
        Player damager = (Player) e.getDamager();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);
        GamePlayer dgp = game.getPlayer(damager);
        if (dgp == null) return;

        if (gp.getPlayerType() == dgp.getPlayerType() && (gp.getPlayerType() == PlayerType.RUNNER || !game.isAllowFriendlyFire())) {
            e.setCancelled(true);
            e.setDamage(0);
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

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING || (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= 45) ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.RUNNER && game.getTimer() <= 35)) {
            e.setCancelled(true);
            player.setHealth(player.getMaxHealth());
            e.setDamage(0);
            return;
        }

        if (player.getHealth() - e.getFinalDamage() > 0) return;

        player.setHealth(player.getMaxHealth());
        player.setGameMode(GameMode.SPECTATOR);

        gp.addDeath();
        gp.setDead(true);

        if (gp.getPlayerType() == PlayerType.RUNNER && gp.getDeaths() == 1) {
            String killMsg = gp.getPrefix() + " " + player.getName() + " §cdied.";
            if (player.getKiller() != null) {
                GamePlayer gp1 = game.getPlayer(player.getKiller());
                gp1.addKill();
                killMsg = player.getName() + " §cwas killed by " + gp1.getPrefix() + " " + player.getKiller().getName();
            }

            player.sendTitle("§c§lYOU DIED!", "§7You have 0 lives left.", 20, 60, 20);

            for (GamePlayer gp1 : game.getPlayers()) {
                Player p = Bukkit.getPlayer(gp1.getUuid());
                if (p == null) continue;

                p.sendMessage("§7§lSPEED RUNNER DOWN! " + killMsg);
                if (!p.equals(player)) {
                    p.sendTitle("§7§lSPEED RUNNER DOWN!", killMsg, 20, 60, 20);
                }
            }
        } else if (gp.getPlayerType() == PlayerType.HUNTER && gp.getDeaths() <= 3) {
            String killMsg = "§e" + player.getName() + " §cdied.";
            if (player.getKiller() != null) {
                GamePlayer gp1 = game.getPlayer(player.getKiller());
                gp1.addKill();
                killMsg = player.getName() + " §cwas killed by " + gp1.getPrefix() + " " + player.getKiller().getName();
            }

            if (gp.getDeaths() >= 3) {
                // out of lives
                player.sendTitle("§c§lYOU DIED!", "§7You have 0 lives left.", 20, 60, 20);
            } else {
                player.sendTitle("§c§lYOU DIED!", "§7You have " + (3 - gp.getDeaths()) + " lives left.", 5, 30, 5);
                gp.prepareForRespawn();
            }

            for (GamePlayer gp1 : game.getPlayers()) {
                Player p = Bukkit.getPlayer(gp1.getUuid());
                if (p == null) continue;
                p.sendMessage(gp.getPrefix() + " " + killMsg);
            }
        }

        game.checkForWin(false);
    }

}
