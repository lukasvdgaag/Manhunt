package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
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

import java.util.List;

public class EventMenu implements Listener {

    public void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 36, Util.c(Manhunt.get().getCfg().menuEventsTitle));

        ItemStack manhunt = new ItemStack(Material.valueOf(Manhunt.get().getCfg().eventMenuManhuntMaterial));
        ItemMeta manhuntMeta = manhunt.getItemMeta();
        manhuntMeta.setDisplayName(Util.c(Manhunt.get().getCfg().eventMenuManhuntDisplayname));

        int players = 0;
        int games = 0;
        for (Game g : Game.getGames()) {
            games++;
            players += g.getOnlinePlayers(null).size();
        }

        List<String> lore = Lists.newArrayList();
        for (String s : Manhunt.get().getCfg().eventMenuManhuntLore) {
            lore.add(Util.c(s).replace("%totalplayers%", players + "").replace("%totalgames%", games + ""));
        }

        manhuntMeta.setLore(lore);
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
        if (!e.getView().getTitle().equals(Util.c(Manhunt.get().getCfg().menuEventsTitle))) return;
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
