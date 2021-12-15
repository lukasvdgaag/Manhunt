package me.gaagjescraft.network.team.manhunt.events.custom;

import me.gaagjescraft.network.team.manhunt.games.GameSetup;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class GameSetupMenuClickEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final GameSetup gameSetup;
    private int slot;
    private final ClickType clickType;
    private final Inventory inventory;

    private boolean cancel;

    public GameSetupMenuClickEvent(Player player, GameSetup gameSetup, Inventory inventory, int slot, ClickType clickType) {
        this.player = player;
        this.gameSetup = gameSetup;
        this.inventory = inventory;
        this.slot = slot;
        this.clickType = clickType;
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

    public GameSetup getGame() {
        return gameSetup;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public ClickType getClickType() {
        return clickType;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancel = b;
    }
}
