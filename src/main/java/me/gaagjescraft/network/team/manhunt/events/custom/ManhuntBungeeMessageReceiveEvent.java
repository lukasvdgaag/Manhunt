package me.gaagjescraft.network.team.manhunt.events.custom;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ManhuntBungeeMessageReceiveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final String subChannel;
    private final String message;

    public ManhuntBungeeMessageReceiveEvent(String subChannel, String message) {
        this.subChannel = subChannel;
        this.message = message;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public String getSubChannel() {
        return subChannel;
    }

    public String getMessage() {
        return message;
    }
}
