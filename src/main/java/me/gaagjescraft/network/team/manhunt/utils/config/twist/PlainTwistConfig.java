package me.gaagjescraft.network.team.manhunt.utils.config.twist;

import org.bukkit.configuration.file.FileConfiguration;

public class PlainTwistConfig extends IconTwistConfig {
    private String sound;
    private String title;
    private String message;

    public PlainTwistConfig(String twistName) {
        super(twistName);
    }

    public String getSound() {
        return sound;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public void load(FileConfiguration configuration) {
        super.load(configuration);
        this.sound = configuration.getString("sounds.twists." + twistName);
        this.title = configuration.getString("titles.twists." + twistName);
        this.message = configuration.getString("messages.twists." + twistName);
    }

    public void save(FileConfiguration configuration) {
        super.save(configuration);
        configuration.set("sounds.twists." + twistName, sound);
        configuration.set("titles.twists." + twistName, title);
        configuration.set("messages.twists." + twistName, message);
    }
}
