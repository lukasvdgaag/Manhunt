package me.gaagjescraft.network.team.manhunt.menus;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.GameSetup;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ManhuntPlayerAmountSetupMenu implements Listener {

    public void openMenu(Player player, GameSetup setup) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1);
        Inventory inventory = Bukkit.createInventory(null, 27, Util.c(Manhunt.get().getCfg().menuMaxHuntersTitle));
        player.openInventory(inventory);
        updateItems(player, setup);
    }

    public void updateItems(Player player, GameSetup setup) {
        player.getOpenInventory();
        player.getOpenInventory().getTopInventory();
        Inventory inventory = player.getOpenInventory().getTopInventory();

        inventory.setItem(9, getLoredItem(false, 25, setup.getMaxPlayers()));
        inventory.setItem(10, getLoredItem(false, 10, setup.getMaxPlayers()));
        inventory.setItem(11, getLoredItem(false, 1, setup.getMaxPlayers()));

        inventory.setItem(13, Itemizer.createItem(
                Manhunt.get().getCfg().maxHuntersMenuSaveMaterial, 1,
                Manhunt.get().getCfg().maxHuntersMenuSaveDisplayname,
                Util.r(Manhunt.get().getCfg().maxHuntersMenuSaveLore, "%amount%", setup.getMaxPlayers() + "")
        ));

        inventory.setItem(15, getLoredItem(true, 1, setup.getMaxPlayers()));
        inventory.setItem(16, getLoredItem(true, 10, setup.getMaxPlayers()));
        inventory.setItem(17, getLoredItem(true, 25, setup.getMaxPlayers()));

    }

    private ItemStack getLoredItem(boolean plus, int number, int currentSize) {
        ItemStack item = plus ? Itemizer.HEAD_PLUS : Itemizer.HEAD_MINUS;
        item.setAmount(number);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Util.c(
                plus ? Manhunt.get().getCfg().maxHuntersMenuAddDisplayname : Manhunt.get().getCfg().maxHuntersMenuRemoveDisplayname
        ).replaceAll("%number%", number + "").replaceAll("%amount%", currentSize + ""));
        List<String> lore = plus ? Manhunt.get().getCfg().maxHuntersMenuAddLore : Manhunt.get().getCfg().maxHuntersMenuRemoveLore;
        lore = new ArrayList<>(lore);
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, Util.c(lore.get(i)
                    .replaceAll("%number%", number + "")
                    .replaceAll("%amount%", currentSize + "")));
        }
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals(Util.c(Manhunt.get().getCfg().menuMaxHuntersTitle))) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player player = (Player) e.getWhoClicked();
        GameSetup setup = Manhunt.get().getManhuntGameSetupMenu().gameSetups.get(player);
        if (setup == null) return;

        int current = setup.getMaxPlayers();

        int max = Manhunt.get().getCfg().maximumPlayers;
        int min = Manhunt.get().getCfg().minimumPlayers;

        if (e.getSlot() == 9) {
            if (current > min)
                Util.playSound(player, Manhunt.get().getCfg().menuMaxHuntersChangeAmountSound, 1, 1);
            else player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            setup.setMaxPlayers(Math.max(current - 25, min));
            updateItems(player, setup);
        } else if (e.getSlot() == 10) {
            if (current > min)
                Util.playSound(player, Manhunt.get().getCfg().menuMaxHuntersChangeAmountSound, 1, 1);
            else player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            setup.setMaxPlayers(Math.max(current - 10, min));
            updateItems(player, setup);
        } else if (e.getSlot() == 11) {
            if (current > min)
                Util.playSound(player, Manhunt.get().getCfg().menuMaxHuntersChangeAmountSound, 1, 1);
            else player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            setup.setMaxPlayers(Math.max(current - 1, min));
            updateItems(player, setup);
        } else if (e.getSlot() == 13) {
            // continue setup
            Manhunt.get().getManhuntGameSetupMenu().openMenu(player, setup.getGame());
        } else if (e.getSlot() == 15) {
            if (current < max)
                Util.playSound(player, Manhunt.get().getCfg().menuMaxHuntersChangeAmountSound, 1, 2);
            else player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            setup.setMaxPlayers(Math.min(current + 1, max));
            updateItems(player, setup);
        } else if (e.getSlot() == 16) {
            if (current < max)
                Util.playSound(player, Manhunt.get().getCfg().menuMaxHuntersChangeAmountSound, 1, 2);
            else player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            setup.setMaxPlayers(Math.min(current + 10, max));
            updateItems(player, setup);
        } else if (e.getSlot() == 17) {
            if (current < max)
                Util.playSound(player, Manhunt.get().getCfg().menuMaxHuntersChangeAmountSound, 1, 2);
            else player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            setup.setMaxPlayers(Math.min(current + 25, max));
            updateItems(player, setup);
        }

    }

}
