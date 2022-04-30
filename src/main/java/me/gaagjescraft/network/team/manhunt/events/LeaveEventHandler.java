package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class LeaveEventHandler implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        Game game = Game.getGame(e.getPlayer());
        if (game == null) return;

        GamePlayer gp = game.getPlayer(e.getPlayer());
        if (!gp.isOnline()) return; // already removed the player (or they're just not in the game).

        game.removePlayer(e.getPlayer());
        e.getPlayer().setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());

        Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> {
            // removing the user after 5 minutes if they haven't rejoined yet.
            Player p = Bukkit.getPlayer(e.getPlayer().getUniqueId());
            if (p == null || !p.isOnline()) {
                Manhunt.get().getPlayerStorage().unloadUser(e.getPlayer().getUniqueId());
            }
        }, 20 * 300L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);

        Manhunt.get().getPlayerStorage().loadUser(e.getPlayer().getUniqueId());

        if (Manhunt.get().getCfg().joinGameOnServerJoin || Manhunt.get().getCfg().teleportLobbyOnServerJoin) {
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> {
                if (Manhunt.get().getCfg().teleportLobbyOnServerJoin && Manhunt.get().getCfg().lobby != null) {
                    e.getPlayer().teleport(Manhunt.get().getCfg().lobby);
                }
                if (Manhunt.get().getCfg().joinGameOnServerJoin && !Manhunt.get().getCfg().isLobbyServer) {
                    Game game = Game.getGame(e.getPlayer());
                    if (game == null) {
                        for (Game g : Game.getGames()) {
                            if (g.addPlayer(e.getPlayer())) {
                                for (String s : Manhunt.get().getCfg().autoRejoinMessage) {
                                    e.getPlayer().sendMessage(Util.c(s.replaceAll("%host%", g.getIdentifier())));
                                }
                                return;
                            }
                        }
                    } else {
                        if (!game.getPlayer(e.getPlayer()).isOnline()) {
                            if (game.addPlayer(e.getPlayer()) && game.getPlayer(e.getPlayer()).isJoinedBefore()) {
                                e.getPlayer().sendMessage(Util.c(Manhunt.get().getCfg().playerRejoinedMessage));
                            }
                        }
                    }
                }
            }, 1L);
        }

    }

}
