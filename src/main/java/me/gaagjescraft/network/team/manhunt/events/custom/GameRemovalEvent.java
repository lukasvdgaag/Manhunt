package me.gaagjescraft.network.team.manhunt.events.custom;

import me.gaagjescraft.network.team.manhunt.games.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameRemovalEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Game game;

    public GameRemovalEvent(Game game) {
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

    public Game getGame() {
        return game;
    }

}
