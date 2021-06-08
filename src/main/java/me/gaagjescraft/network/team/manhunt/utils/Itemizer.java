package me.gaagjescraft.network.team.manhunt.utils;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Itemizer {

    public static ItemStack FILL_ITEM;
    public static ItemStack CLOSE_ITEM;
    public static ItemStack FILL_NO_GAMES;
    public static ItemStack NEW_GAME_ITEM;
    public static ItemStack NEW_GAME_ITEM_UNSUPPORTED_PROTOCOL;
    public static ItemStack NEW_GAME_FINISH_ITEM;
    public static ItemStack GAME_START_ITEM;

    public static ItemStack MANHUNT_LEAVE_ITEM;
    public static ItemStack MANHUNT_RUNNER_TRACKER;
    public static ItemStack MANHUNT_VOTE_ITEM;
    public static ItemStack MANHUNT_HOST_SETTINGS_ITEM;

    public static ItemStack HEAD_PLUS;
    public static ItemStack HEAD_MINUS;

    public static void load() {
        HEAD_PLUS = Manhunt.get().getUtil().getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjliODYxYWFiYjMxNmM0ZWQ3M2I0ZTU0MjgzMDU3ODJlNzM1NTY1YmEyYTA1MzkxMmUxZWZkODM0ZmE1YTZmIn19fQ==");
        HEAD_MINUS = Manhunt.get().getUtil().getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2M4ZTdkNDZkNjkzMzQxZjkxZDI4NjcyNmYyNTU1ZWYxNTUxNGUzNDYwYjI3NWU5NzQ3ODQyYmM5ZTUzZGYifX19");

        FILL_ITEM = createItem(Material.valueOf(Manhunt.get().getCfg().generalFillMaterial), 1, "§r", Lists.newArrayList());
        CLOSE_ITEM = createItem(Manhunt.get().getCfg().generalCloseMaterial, 1, Manhunt.get().getCfg().generalCloseDisplayname, Manhunt.get().getCfg().generalCloseLore);
        FILL_NO_GAMES = createItem(Manhunt.get().getCfg().gamesMenuNoGamesMaterial, 1, Manhunt.get().getCfg().gamesMenuNoGamesDisplayname, Manhunt.get().getCfg().gamesMenuNoGamesLore);

        NEW_GAME_ITEM = createItem(Manhunt.get().getCfg().gamesMenuHostGameMaterial, 1, Manhunt.get().getCfg().gamesMenuHostGameDisplayname, Manhunt.get().getCfg().gamesMenuHostGameLore);
        NEW_GAME_ITEM_UNSUPPORTED_PROTOCOL = createItem(Manhunt.get().getCfg().gamesMenuHostUnsupportedProtocolMaterial, 1, Manhunt.get().getCfg().gamesMenuHostUnsupportedProtocolDisplayname, Manhunt.get().getCfg().gamesMenuHostUnsupportedProtocolLore);

        NEW_GAME_FINISH_ITEM = createItem(Manhunt.get().getCfg().hostMenuFinishMaterial, 1, Manhunt.get().getCfg().hostMenuFinishDisplayname, Manhunt.get().getCfg().hostMenuFinishLore);
        GAME_START_ITEM = createItem(Manhunt.get().getCfg().hostMenuStartMaterial, 1, Manhunt.get().getCfg().hostMenuStartDisplayname, Manhunt.get().getCfg().hostMenuStartLore);
        MANHUNT_RUNNER_TRACKER = createItem(Manhunt.get().getCfg().generalTrackerMaterial, 1, Manhunt.get().getCfg().generalTrackerDisplayname, Manhunt.get().getCfg().generalTrackerLore);

        MANHUNT_LEAVE_ITEM = createItem(Manhunt.get().getCfg().generalLeaveMaterial, 1, Manhunt.get().getCfg().generalLeaveDisplayname, Manhunt.get().getCfg().generalLeaveLore);
        MANHUNT_VOTE_ITEM = createItem(Manhunt.get().getCfg().generalTwistVoteMaterial, 1, Manhunt.get().getCfg().generalTwistVoteDisplayname, Manhunt.get().getCfg().generalTwistVoteLore);
        MANHUNT_HOST_SETTINGS_ITEM = createItem(Manhunt.get().getCfg().generalSettingsMaterial, 1, Manhunt.get().getCfg().generalSettingsDisplayname, Manhunt.get().getCfg().generalSettingsLore);
    }

    public static ItemStack createItem(String material, int amount, String displayName, List<String> lore) {
        List<String> lines = Lists.newArrayList();
        for (String s : lore) {
            lines.add(Util.c(s));
        }
        displayName = Util.c(displayName);
        return createItem(Material.valueOf(material.toUpperCase()), amount, displayName, lines);
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
