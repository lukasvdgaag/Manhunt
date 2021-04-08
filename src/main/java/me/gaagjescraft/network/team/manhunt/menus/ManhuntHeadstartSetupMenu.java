package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.GameSetup;
import me.gaagjescraft.network.team.manhunt.games.HeadstartType;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ManhuntHeadstartSetupMenu implements Listener {

    public void openMenu(Player player, GameSetup setup) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1);
        Inventory inventory = Bukkit.createInventory(null, 18, "§6§lManHunt Headstart");
        player.openInventory(inventory);
        updateItems(player, setup);
    }

    public void updateItems(Player player, GameSetup setup) {
        if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null) return;
        Inventory inventory = player.getOpenInventory().getTopInventory();

        for (int i=0;i<inventory.getSize(); i++) {
            inventory.setItem(i, Itemizer.FILL_ITEM);
        }

        inventory.setItem(1,Itemizer.createItem(Material.LEVER,1, "§730 seconds" + (setup.getHeadstart()== HeadstartType.HALF_MINUTE?" §a(selected)":""), Lists.newArrayList("", "§7The runners will have a", "§e30 seconds§7 headstart.", "",
                "§6Click§e to select.")));
        inventory.setItem(3,Itemizer.createItem(Material.TORCH,1, "§e1 minute" + (setup.getHeadstart()== HeadstartType.ONE_MINUTE?" §a(selected)":""), Lists.newArrayList("", "§7The runners will have a", "§e1 minute§7 headstart.", "",
                "§6Click§e to select.")));
        inventory.setItem(5,Itemizer.createItem(Material.SOUL_TORCH,1, "§b1.5 minute" + (setup.getHeadstart()== HeadstartType.ONE_HALF_MINUTE?" §a(selected)":""), Lists.newArrayList("", "§7The runners will have a", "§e1.5 minutes§7 headstart.", "",
                "§6Click§e to select.")));
        inventory.setItem(7,Itemizer.createItem(Material.REDSTONE_TORCH,1, "§c2 minutes"  + (setup.getHeadstart()== HeadstartType.TWO_MINUTES?" §a(selected)":""), Lists.newArrayList("", "§7The runners will have a", "§e2 minutes§7 headstart.", "",
                "§6Click§e to select.")));

        inventory.setItem(13, Itemizer.CLOSE_ITEM);

        int slot = 1;
        if (setup.getHeadstart() == HeadstartType.ONE_MINUTE) slot = 3;
        else if (setup.getHeadstart() == HeadstartType.ONE_HALF_MINUTE) slot = 5;
        else if (setup.getHeadstart() == HeadstartType.TWO_MINUTES) slot = 7;

        ItemStack item = inventory.getItem(slot);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        item.setItemMeta(meta);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals("§6§lManHunt Headstart")) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player player = (Player) e.getWhoClicked();
        GameSetup setup = Manhunt.get().getManhuntGameSetupMenu().gameSetups.get(player);
        if (setup == null) return;

        if (e.getSlot() == 1 && setup.getHeadstart() != HeadstartType.HALF_MINUTE) {
            setup.setHeadstart(HeadstartType.HALF_MINUTE, setup.getGame()!=null);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            updateItems(player, setup);
        } else if (e.getSlot() == 3 && setup.getHeadstart() != HeadstartType.ONE_MINUTE) {
            setup.setHeadstart(HeadstartType.ONE_MINUTE, setup.getGame()!=null);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            updateItems(player, setup);
        } else if (e.getSlot() == 5 && setup.getHeadstart() != HeadstartType.ONE_HALF_MINUTE) {
            setup.setHeadstart(HeadstartType.ONE_HALF_MINUTE, setup.getGame()!=null);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            updateItems(player, setup);
        } else if (e.getSlot() == 7 && setup.getHeadstart() != HeadstartType.TWO_MINUTES) {
            setup.setHeadstart(HeadstartType.TWO_MINUTES, setup.getGame()!=null);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            updateItems(player, setup);
        }
        else if (e.getSlot() == 13) {
            // continue setup
            Manhunt.get().getManhuntGameSetupMenu().openMenu(player, setup.getGame());
        }
    }

}
