package me.gaagjescraft.network.team.manhunt.utils.config.twist;

import me.gaagjescraft.network.team.manhunt.utils.config.icon.IconConfig;
import org.bukkit.configuration.file.FileConfiguration;

public class IconTwistConfig {
    protected final String twistName;
    private IconConfig icon;

    public IconTwistConfig(String twistName) {
        this.twistName = twistName;
    }

    public IconConfig getIcon() {
        return icon;
    }

    public void load(FileConfiguration configuration) {
        this.icon = IconConfig.load("items.twist-vote-menu." + twistName, configuration);
    }

    public void save(FileConfiguration configuration) {
        icon.save("items.twist-vote-menu." + twistName, configuration);
    }
}
