package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
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

        Game game = Game.getGame(e.getPlayer());
        if (game == null) {
            for (Game g : Game.getGames()) {
                if (g.addPlayer(e.getPlayer())) {
                    e.getPlayer().sendMessage("§5You automatically joined " + g.getIdentifier() + "'s Manhunt!");
                    e.getPlayer().sendMessage("§7If you wish to leave this game, type §e/leave§7.");
                    return;
                }
            }
        } else {
            if (!game.getPlayer(e.getPlayer()).isOnline()) {
                if (game.addPlayer(e.getPlayer())) {
                    e.getPlayer().sendMessage("§5You have been put into your old game.");
                    return;
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> e.getPlayer().teleport(Manhunt.get().getLobby()), 10L);

    }

}
