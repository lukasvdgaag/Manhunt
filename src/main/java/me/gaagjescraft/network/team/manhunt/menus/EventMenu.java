package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EventMenu implements Listener {

    public void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 36, "§6§lEvents");

        ItemStack manhunt = new ItemStack(Material.COMPASS);
        ItemMeta manhuntMeta = manhunt.getItemMeta();
        manhuntMeta.setDisplayName("§bManhunt Event");

        int players = 0;
        int games = 0;
        for (Game g : Game.getGames()) {
            games++;
            players += g.getPlayers().size();
        }

        manhuntMeta.setLore(Lists.newArrayList("", "§7Hunt down the speed runners",
                "§7and kill them without dying yourself.",
                "§7Hunters win if all runners are dead.", "",
                "§bThere are §e" + players + "§b players online in §e" + games + "§b games.", "",
                "§6Click §eto view all games."));
        manhuntMeta.addItemFlags(ItemFlag.values());
        manhunt.setItemMeta(manhuntMeta);
        inventory.setItem(13, manhunt);

        inventory.setItem(31, Itemizer.CLOSE_ITEM);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR)
                inventory.setItem(i, Itemizer.FILL_ITEM);
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals("§6§lEvents")) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        if (e.getSlot() == 13) {
            Manhunt.get().getManhuntGamesMenu().openMenu((Player) e.getWhoClicked());
        } else if (e.getSlot() == 31) {
            e.getWhoClicked().closeInventory();
        }

    }

}
