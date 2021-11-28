package me.gaagjescraft.network.team.manhunt.events.custom;

import me.gaagjescraft.network.team.manhunt.games.GameSetup;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class GameSetupMenuOpenEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final GameSetup gameSetup;

    private final Inventory inventory;

    public GameSetupMenuOpenEvent(Player player, GameSetup gameSetup, Inventory inventory) {
        this.player = player;
        this.gameSetup = gameSetup;
        this.inventory = inventory;
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

    public GameSetup getGameSetup() {
        return gameSetup;
    }

    public Inventory getInventory() {
        return inventory;
    }

}
