package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.GameSetup;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ManhuntPlayerAmountSetupMenu implements Listener {

    public void openMenu(Player player, GameSetup setup) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1);
        Inventory inventory = Bukkit.createInventory(null, 27, "Manhunt Max Hunters");
        player.openInventory(inventory);
        updateItems(player, setup);
    }

    public void updateItems(Player player, GameSetup setup) {
        if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null) return;
        Inventory inventory = player.getOpenInventory().getTopInventory();

        inventory.setItem(9, getLoredItem(false, 25, setup.getMaxPlayers()));
        inventory.setItem(10, getLoredItem(false, 10, setup.getMaxPlayers()));
        inventory.setItem(11, getLoredItem(false, 1, setup.getMaxPlayers()));
        //inventory.setItem(12, getLoredItem(false, 1, setup.getMaxPlayers()));

        ItemStack item = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bFinish Amount Setup");
        meta.setLore(Lists.newArrayList("", "§7Finish the max hunters amount", "§7setup for your game.", "", "§bCurrent amount: §a" + setup.getMaxPlayers(), "", "§6Click§e to finish."));
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        inventory.setItem(13, item);

        //inventory.setItem(14, getLoredItem(true, 1, setup.getMaxPlayers()));
        inventory.setItem(15, getLoredItem(true, 1, setup.getMaxPlayers()));
        inventory.setItem(16, getLoredItem(true, 10, setup.getMaxPlayers()));
        inventory.setItem(17, getLoredItem(true, 25, setup.getMaxPlayers()));

    }

    private ItemStack getLoredItem(boolean plus, int number, int currentSize) {
        ItemStack item = plus ? Itemizer.HEAD_PLUS : Itemizer.HEAD_MINUS;
        item.setAmount(number);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plus ? "§a§lAdd +" + number : "§c§lRemove -" + number);
        meta.setLore(Lists.newArrayList("", (plus ? "§7Add " + number + " to " : "§7Remove " + number + " from ") + "the maximum", "§7amount of hunters that can join.", "",
                "§bCurrent amount: §a" + currentSize, "",
                "§6Click§e to " + (plus ? "add +" + number : "remove -" + number) + "."));
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals("Manhunt Max Hunters")) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player player = (Player) e.getWhoClicked();
        GameSetup setup = Manhunt.get().getManhuntGameSetupMenu().gameSetups.get(player);
        if (setup == null) return;

        int current = setup.getMaxPlayers();

        if (e.getSlot() == 9) {
            setup.setMaxPlayers(Math.max(current - 25, 4));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            updateItems(player, setup);
        } else if (e.getSlot() == 10) {
            setup.setMaxPlayers(Math.max(current - 10, 4));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            updateItems(player, setup);
        } else if (e.getSlot() == 11) {
            setup.setMaxPlayers(Math.max(current - 1, 4));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            updateItems(player, setup);
        }
        /*else if (e.getSlot() == 12) {
            setup.setMaxPlayers(Math.max(current-1,4));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,1);
            updateItems(player, setup);
        }*/
        else if (e.getSlot() == 13) {
            // continue setup
            Manhunt.get().getManhuntGameSetupMenu().openMenu(player, setup.getGame());
        }
       /* else if (e.getSlot() == 14) {
            setup.setMaxPlayers(Math.min(current+1, 100));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,2);
            updateItems(player, setup);
        }*/
        else if (e.getSlot() == 15) {
            setup.setMaxPlayers(Math.min(current + 1, 100));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            updateItems(player, setup);
        } else if (e.getSlot() == 16) {
            setup.setMaxPlayers(Math.min(current + 10, 100));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            updateItems(player, setup);
        } else if (e.getSlot() == 17) {
            setup.setMaxPlayers(Math.min(current + 25, 100));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            updateItems(player, setup);
        }


    }

}
