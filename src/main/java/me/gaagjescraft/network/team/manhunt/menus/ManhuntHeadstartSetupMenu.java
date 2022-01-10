package me.gaagjescraft.network.team.manhunt.menus;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.GameSetup;
import me.gaagjescraft.network.team.manhunt.games.HeadstartType;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
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
        Util.playSound(player, Manhunt.get().getCfg().openMenuHeadstartSound, .5f, 1);
        Inventory inventory = Bukkit.createInventory(null, 18, Util.c(Manhunt.get().getCfg().menuHeadstartTitle));
        player.openInventory(inventory);
        updateItems(player, setup);
    }

    public void updateItems(Player player, GameSetup setup) {
        if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null) return;
        Inventory inventory = player.getOpenInventory().getTopInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, Itemizer.FILL_ITEM);
        }

        String selected = " " + Manhunt.get().getCfg().selectedPrefix;

        inventory.setItem(1, Itemizer.createItem(
                Manhunt.get().getCfg().headstartMenuHalfMinuteHeadstartMaterial, 1,
                Manhunt.get().getCfg().headstartMenuHalfMinuteHeadstartDisplayname + (setup.getHeadStart() == HeadstartType.HALF_MINUTE ? selected : ""),
                Util.r(Manhunt.get().getCfg().headstartMenuHeadstartLore, "%time%", Manhunt.get().getUtil().secondsToTimeString(HeadstartType.HALF_MINUTE.getSeconds(), "string")))
        );
        inventory.setItem(3, Itemizer.createItem(
                Manhunt.get().getCfg().headstartMenuOneMinuteHeadstartMaterial, 1,
                Manhunt.get().getCfg().headstartMenuOneMinuteHeadstartDisplayname + (setup.getHeadStart() == HeadstartType.ONE_MINUTE ? selected : ""),
                Util.r(Manhunt.get().getCfg().headstartMenuHeadstartLore, "%time%", Manhunt.get().getUtil().secondsToTimeString(HeadstartType.ONE_MINUTE.getSeconds(), "string")))
        );
        inventory.setItem(5, Itemizer.createItem(
                Manhunt.get().getCfg().headstartMenuOneHalfMinuteHeadstartMaterial, 1,
                Manhunt.get().getCfg().headstartMenuOneHalfMinuteHeadstartDisplayname + (setup.getHeadStart() == HeadstartType.ONE_HALF_MINUTE ? selected : ""),
                Util.r(Manhunt.get().getCfg().headstartMenuHeadstartLore, "%time%", Manhunt.get().getUtil().secondsToTimeString(HeadstartType.ONE_HALF_MINUTE.getSeconds(), "string")))
        );
        inventory.setItem(7, Itemizer.createItem(
                Manhunt.get().getCfg().headstartMenuTwoMinutesHeadstartMaterial, 1,
                Manhunt.get().getCfg().headstartMenuTwoMinutesHeadstartDisplayname + (setup.getHeadStart() == HeadstartType.TWO_MINUTES ? selected : ""),
                Util.r(Manhunt.get().getCfg().headstartMenuHeadstartLore, "%time%", Manhunt.get().getUtil().secondsToTimeString(HeadstartType.TWO_MINUTES.getSeconds(), "string")))
        );

        inventory.setItem(13, Itemizer.createItem(Manhunt.get().getCfg().headstartMenuSaveMaterial, 1, Manhunt.get().getCfg().headstartMenuSaveDisplayname, Manhunt.get().getCfg().headstartMenuSaveLore));

        int slot = 1;
        if (setup.getHeadStart() == HeadstartType.ONE_MINUTE) slot = 3;
        else if (setup.getHeadStart() == HeadstartType.ONE_HALF_MINUTE) slot = 5;
        else if (setup.getHeadStart() == HeadstartType.TWO_MINUTES) slot = 7;

        ItemStack item = inventory.getItem(slot);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        item.setItemMeta(meta);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals(Util.c(Manhunt.get().getCfg().menuHeadstartTitle))) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player player = (Player) e.getWhoClicked();
        GameSetup setup = Manhunt.get().getManhuntGameSetupMenu().gameSetups.get(player);
        if (setup == null) return;

        if (e.getSlot() == 1 && setup.getHeadStart() != HeadstartType.HALF_MINUTE) {
            setup.setHeadstart(HeadstartType.HALF_MINUTE, true);
            Util.playSound(player, Manhunt.get().getCfg().menuHeadstartSelectSound, 1, 1);
            updateItems(player, setup);
        } else if (e.getSlot() == 3 && setup.getHeadStart() != HeadstartType.ONE_MINUTE) {
            setup.setHeadstart(HeadstartType.ONE_MINUTE, true);
            Util.playSound(player, Manhunt.get().getCfg().menuHeadstartSelectSound, 1, 1);
            updateItems(player, setup);
        } else if (e.getSlot() == 5 && setup.getHeadStart() != HeadstartType.ONE_HALF_MINUTE) {
            setup.setHeadstart(HeadstartType.ONE_HALF_MINUTE, true);
            Util.playSound(player, Manhunt.get().getCfg().menuHeadstartSelectSound, 1, 1);
            updateItems(player, setup);
        } else if (e.getSlot() == 7 && setup.getHeadStart() != HeadstartType.TWO_MINUTES) {
            setup.setHeadstart(HeadstartType.TWO_MINUTES, true);
            Util.playSound(player, Manhunt.get().getCfg().menuHeadstartSelectSound, 1, 1);
            updateItems(player, setup);
        } else if (e.getSlot() == 13) {
            // continue setup
            Util.playSound(player, Manhunt.get().getCfg().menuHeadstartSelectSound, 1, 1);
            Manhunt.get().getManhuntGameSetupMenu().openMenu(player, setup.getGame());
        }
    }

}
