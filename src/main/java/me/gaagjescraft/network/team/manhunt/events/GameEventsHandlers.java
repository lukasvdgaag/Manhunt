package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class GameEventsHandlers implements Listener {

    @EventHandler
    public void onBedSpawn(PlayerBedEnterEvent e) {
        Player player = e.getPlayer();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (gp.isDead()) {
            e.setCancelled(true);
            return;
        }
        gp.setBedSpawn(e.getBed().getLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled() || Manhunt.get().getManhuntRunnerManageMenu().chatPlayers.contains(e.getPlayer())) return;
        Player player = e.getPlayer();

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        e.setCancelled(true);
        String message = Util.c(Manhunt.get().getCfg().chatFormat
                .replace("%prefix%", gp.getPrefix(true))
                .replace("%color%", gp.getColor())
                .replace("%message%", e.getMessage()));

        if (Manhunt.get().getCfg().chatPerTeam) {
            for (GamePlayer gps : game.getOnlinePlayers(null)) {
                Player p = Bukkit.getPlayer(gps.getUuid());
                if (p == null) continue;
                if (Manhunt.get().getCfg().separateDeadChat) {
                    if (gp.isFullyDead()) {
                        if (gps.isFullyDead()) {
                            p.sendMessage(message);
                        }
                        continue;
                    } else if (!gp.isFullyDead() && gp.isFullyDead()) continue;
                }
                if (gp.getPlayerType() == gps.getPlayerType()) {
                    // same team
                    p.sendMessage(message);
                }
            }
        } else {
            for (GamePlayer gps : game.getOnlinePlayers(null)) {
                Player p = Bukkit.getPlayer(gps.getUuid());
                if (p == null) continue;
                if (Manhunt.get().getCfg().separateDeadChat) {
                    if (gp.isFullyDead()) {
                        if (gps.isFullyDead()) {
                            p.sendMessage(message);
                        }
                    } else {
                        if (!gps.isFullyDead()) {
                            p.sendMessage(message);
                        }
                    }
                } else {
                    p.sendMessage(message);
                }
            }
        }
    }

}
