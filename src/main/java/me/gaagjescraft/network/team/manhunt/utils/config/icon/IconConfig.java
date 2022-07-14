package me.gaagjescraft.network.team.manhunt.utils.config.icon;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class IconConfig {
    private final String material;
    private final String displayName;
    private final List<String> lore;

    public IconConfig(String material, String displayName, List<String> lore) {
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
    }

    public String getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public static IconConfig load(String prefix, FileConfiguration fileConfiguration) {
        return new IconConfig(
                fileConfiguration.getString(prefix + "-material"),
                fileConfiguration.getString(prefix + "-displayname"),
                fileConfiguration.getStringList(prefix + "-lore"));
    }

    public void save(String prefix, FileConfiguration fileConfiguration) {
        fileConfiguration.set(prefix + "-material", material);
        fileConfiguration.set(prefix + "-displayname", displayName);
        fileConfiguration.set(prefix + "-lore", lore);
    }
}
