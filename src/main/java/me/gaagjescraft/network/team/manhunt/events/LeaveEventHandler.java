package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveEventHandler implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        Game game = Game.getGame(e.getPlayer());
        if (game == null) return;

        game.removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        if (Manhunt.get().getCfg().joinGameOnServerJoin || Manhunt.get().getCfg().teleportLobbyOnServerJoin) {
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> {
                if (Manhunt.get().getCfg().joinGameOnServerJoin) {
                    Game game = Game.getGame(e.getPlayer());
                    if (game == null) {
                        for (Game g : Game.getGames()) {
                            if (g.addPlayer(e.getPlayer())) {
                                for (String s : Manhunt.get().getCfg().autoRejoinMessage) {
                                    e.getPlayer().sendMessage(Util.c(s.replace("%host%", g.getIdentifier())));
                                }
                                return;
                            }
                        }
                    } else {
                        if (!game.getPlayer(e.getPlayer()).isOnline()) {
                            if (game.addPlayer(e.getPlayer())) {
                                e.getPlayer().sendMessage(Util.c(Manhunt.get().getCfg().playerRejoinedMessage));
                                return;
                            }
                        }
                    }
                }
                if (Manhunt.get().getCfg().teleportLobbyOnServerJoin && Manhunt.get().getCfg().lobby != null) {
                    e.getPlayer().teleport(Manhunt.get().getCfg().lobby);
                }
            }, 10L);
        }

    }

}
