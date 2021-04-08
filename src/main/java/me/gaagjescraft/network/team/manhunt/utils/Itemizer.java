package me.gaagjescraft.network.team.manhunt.utils;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Itemizer {

    public static ItemStack FILL_ITEM;
    public static ItemStack CLOSE_ITEM;
    public static ItemStack FILL_NO_GAMES;
    public static ItemStack NEW_GAME_ITEM;
    public static ItemStack NEW_GAME_FINISH_ITEM;
    public static ItemStack GAME_START_ITEM;

    public static ItemStack MANHUNT_LEAVE_ITEM;
    public static ItemStack MANHUNT_RUNNER_TRACKER;
    public static ItemStack MANHUNT_VOTE_ITEM;
    public static ItemStack MANHUNT_HOST_SETTINGS_ITEM;

    public static ItemStack EVENT_KB_STICK;

    public static ItemStack HEAD_PLUS;
    public static ItemStack HEAD_MINUS;

    static {
        HEAD_PLUS = Manhunt.get().getUtil().getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjliODYxYWFiYjMxNmM0ZWQ3M2I0ZTU0MjgzMDU3ODJlNzM1NTY1YmEyYTA1MzkxMmUxZWZkODM0ZmE1YTZmIn19fQ==");
        HEAD_MINUS = Manhunt.get().getUtil().getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2M4ZTdkNDZkNjkzMzQxZjkxZDI4NjcyNmYyNTU1ZWYxNTUxNGUzNDYwYjI3NWU5NzQ3ODQyYmM5ZTUzZGYifX19");

        FILL_ITEM = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillMeta = FILL_ITEM.getItemMeta();
        fillMeta.setDisplayName("§r");
        fillMeta.setLore(new ArrayList<>());
        fillMeta.addItemFlags(ItemFlag.values());
        FILL_ITEM.setItemMeta(fillMeta);

        CLOSE_ITEM = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = CLOSE_ITEM.getItemMeta();
        closeMeta.setDisplayName("§cClose");
        closeMeta.setLore(Lists.newArrayList("§7Close the menu."));
        closeMeta.addItemFlags(ItemFlag.values());
        CLOSE_ITEM.setItemMeta(closeMeta);

        FILL_NO_GAMES = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta fillNG = FILL_NO_GAMES.getItemMeta();
        fillNG.setDisplayName("§cNo games found!");
        fillNG.setLore(Lists.newArrayList("§7There are no free games right now.", "§7Try again later!"));
        fillNG.addItemFlags(ItemFlag.values());
        FILL_NO_GAMES.setItemMeta(fillNG);

        NEW_GAME_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta ngMeta = NEW_GAME_ITEM.getItemMeta();
        ngMeta.setDisplayName("§aHost a new game");
        ngMeta.setLore(Lists.newArrayList("", "§bThanks for choosing ExodusMC!", "§7As a sign of appreciation,",
                "§7we allow you to host new events.", "§7Simply click this item to get started.", "", "§6Click§e to host a new game."));
        ngMeta.addItemFlags(ItemFlag.values());
        NEW_GAME_ITEM.setItemMeta(ngMeta);

        NEW_GAME_FINISH_ITEM = new ItemStack(Material.LIME_WOOL);
        ItemMeta fmeta = NEW_GAME_FINISH_ITEM.getItemMeta();
        fmeta.setDisplayName("§aFinish setup");
        fmeta.setLore(Lists.newArrayList("", "§7This will finish the current setup", "§7and make it public to other players.", "", "§6Click§e to finish the setup."));
        fmeta.addItemFlags(ItemFlag.values());
        NEW_GAME_FINISH_ITEM.setItemMeta(fmeta);

        GAME_START_ITEM = new ItemStack(Material.LIME_WOOL);
        ItemMeta gmeta = GAME_START_ITEM.getItemMeta();
        gmeta.setDisplayName("§aStart the game");
        gmeta.setLore(Lists.newArrayList("", "§7This will start your current game", "§7and starts the countdown.", "", "§6Click§e to start the game."));
        gmeta.addItemFlags(ItemFlag.values());
        GAME_START_ITEM.setItemMeta(gmeta);

        MANHUNT_RUNNER_TRACKER = new ItemStack(Material.COMPASS);
        ItemMeta trackerMeta = MANHUNT_RUNNER_TRACKER.getItemMeta();
        trackerMeta.setDisplayName("§bRunner Tracker");
        trackerMeta.setLore(Lists.newArrayList("", "§7Right-click to open the", "§7runner tracker menu.", ""));
        trackerMeta.addItemFlags(ItemFlag.values());
        trackerMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        MANHUNT_RUNNER_TRACKER.setItemMeta(trackerMeta);

        MANHUNT_LEAVE_ITEM = new ItemStack(Material.RED_BED);
        ItemMeta leaveMeta = MANHUNT_LEAVE_ITEM.getItemMeta();
        leaveMeta.setDisplayName("§cLeave game");
        leaveMeta.setLore(Lists.newArrayList("", "§7Leave this game.", ""));
        leaveMeta.addItemFlags(ItemFlag.values());
        MANHUNT_LEAVE_ITEM.setItemMeta(leaveMeta);

        MANHUNT_VOTE_ITEM = new ItemStack(Material.PAPER);
        ItemMeta voteMeta = MANHUNT_VOTE_ITEM.getItemMeta();
        voteMeta.setDisplayName("§bTwist Voting");
        voteMeta.setLore(Lists.newArrayList("", "§7Vote for a twist of your choice.", ""));
        voteMeta.addItemFlags(ItemFlag.values());
        MANHUNT_VOTE_ITEM.setItemMeta(voteMeta);

        MANHUNT_HOST_SETTINGS_ITEM = new ItemStack(Material.CHEST);
        ItemMeta settingsMeta = MANHUNT_HOST_SETTINGS_ITEM.getItemMeta();
        settingsMeta.setDisplayName("§eGame Settings");
        settingsMeta.setLore(Lists.newArrayList("", "§7Change the game settings.", ""));
        settingsMeta.addItemFlags(ItemFlag.values());
        MANHUNT_HOST_SETTINGS_ITEM.setItemMeta(settingsMeta);
    }

    public static ItemStack createItem(Material material, int amount, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        item.setAmount(amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }

}
