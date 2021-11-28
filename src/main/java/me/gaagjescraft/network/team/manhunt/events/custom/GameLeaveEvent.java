package me.gaagjescraft.network.team.manhunt.events.custom;

import me.gaagjescraft.network.team.manhunt.games.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameLeaveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Game game;

    public GameLeaveEvent(Player player, Game game) {
        this.player = player;
        this.game = game;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public Game getGame() {
        return game;
    }
}
