package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.games.Game;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveEventHandler implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Game game = Game.getGame(e.getPlayer());
        if (game == null) return;

        game.removePlayer(e.getPlayer());
    }

}
