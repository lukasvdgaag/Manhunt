package me.gaagjescraft.network.team.manhunt.utils;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Itemizer {

    private final Manhunt plugin;
    public ItemStack FILL_ITEM;
    public ItemStack CLOSE_ITEM;
    public ItemStack FILL_NO_GAMES;
    public ItemStack NEW_GAME_ITEM;
    public ItemStack NEW_GAME_ITEM_UNSUPPORTED_PROTOCOL;
    public ItemStack NEW_GAME_FINISH_ITEM;
    public ItemStack GAME_START_ITEM;
    public ItemStack GO_BACK_ITEM;
    public ItemStack MANHUNT_LEAVE_ITEM;
    public ItemStack MANHUNT_RUNNER_TRACKER;
    public ItemStack MANHUNT_VOTE_ITEM;
    public ItemStack MANHUNT_HOST_SETTINGS_ITEM;
    public ItemStack MANHUNT_SETTING_DISABLED;
    public ItemStack HEAD_PLUS;
    public ItemStack HEAD_MINUS;

    public Itemizer(Manhunt plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        HEAD_PLUS = plugin.getUtil().getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjliODYxYWFiYjMxNmM0ZWQ3M2I0ZTU0MjgzMDU3ODJlNzM1NTY1YmEyYTA1MzkxMmUxZWZkODM0ZmE1YTZmIn19fQ==");
        HEAD_MINUS = plugin.getUtil().getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2M4ZTdkNDZkNjkzMzQxZjkxZDI4NjcyNmYyNTU1ZWYxNTUxNGUzNDYwYjI3NWU5NzQ3ODQyYmM5ZTUzZGYifX19");

        FILL_ITEM = createItem(Material.valueOf(plugin.getCfg().generalFillMaterial), 1, "§r", Lists.newArrayList());
        CLOSE_ITEM = createItem(plugin.getCfg().generalCloseMaterial, 1, plugin.getCfg().generalCloseDisplayname, plugin.getCfg().generalCloseLore);
        FILL_NO_GAMES = createItem(plugin.getCfg().gamesMenuNoGamesMaterial, 1, plugin.getCfg().gamesMenuNoGamesDisplayname, plugin.getCfg().gamesMenuNoGamesLore);

        GO_BACK_ITEM = createItem(plugin.getCfg().generalGoBackMaterial, 1, plugin.getCfg().generalGoBackDisplayname, plugin.getCfg().generalGoBackLore);

        NEW_GAME_ITEM = createItem(plugin.getCfg().gamesMenuHostGameMaterial, 1, plugin.getCfg().gamesMenuHostGameDisplayname, plugin.getCfg().gamesMenuHostGameLore);
        NEW_GAME_ITEM_UNSUPPORTED_PROTOCOL = createItem(plugin.getCfg().gamesMenuHostUnsupportedProtocolMaterial, 1, plugin.getCfg().gamesMenuHostUnsupportedProtocolDisplayname, plugin.getCfg().gamesMenuHostUnsupportedProtocolLore);

        NEW_GAME_FINISH_ITEM = createItem(plugin.getCfg().hostMenuFinishMaterial, 1, plugin.getCfg().hostMenuFinishDisplayname, plugin.getCfg().hostMenuFinishLore);
        GAME_START_ITEM = createItem(plugin.getCfg().hostMenuStartMaterial, 1, plugin.getCfg().hostMenuStartDisplayname, plugin.getCfg().hostMenuStartLore);
        MANHUNT_RUNNER_TRACKER = addEnchantment(createItem(plugin.getCfg().generalTrackerMaterial, 1, plugin.getCfg().generalTrackerDisplayname, plugin.getCfg().generalTrackerLore), Enchantment.DURABILITY, 1);

        MANHUNT_LEAVE_ITEM = createItem(plugin.getCfg().generalLeaveMaterial, 1, plugin.getCfg().generalLeaveDisplayname, plugin.getCfg().generalLeaveLore);
        MANHUNT_VOTE_ITEM = createItem(plugin.getCfg().generalTwistVoteMaterial, 1, plugin.getCfg().generalTwistVoteDisplayname, plugin.getCfg().generalTwistVoteLore);
        MANHUNT_HOST_SETTINGS_ITEM = createItem(plugin.getCfg().generalSettingsMaterial, 1, plugin.getCfg().generalSettingsDisplayname, plugin.getCfg().generalSettingsLore);
        MANHUNT_SETTING_DISABLED = createItem(plugin.getCfg().twistVoteMenuOptionDisabledMaterial, 1, plugin.getCfg().twistVoteMenuOptionDisabledDisplayname, plugin.getCfg().twistVoteMenuOptionDisabledLore);

    }

    public ItemStack addEnchantment(ItemStack i, Enchantment ench, int level) {
        ItemMeta m = i.getItemMeta();
        m.addEnchant(ench, level, true);
        i.setItemMeta(m);
        return i;
    }

    public ItemStack createItem(String material, int amount, String displayName, List<String> lore) {
        List<String> lines = Lists.newArrayList();
        for (String s : lore) {
            lines.add(Util.c(s));
        }
        displayName = Util.c(displayName);
        return createItem(Material.valueOf(material.toUpperCase()), amount, displayName, lines);
    }

    public ItemStack createItem(Material material, int amount, String displayName, List<String> lore) {
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
