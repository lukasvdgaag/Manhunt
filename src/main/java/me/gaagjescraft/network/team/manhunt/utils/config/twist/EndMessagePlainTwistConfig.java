package me.gaagjescraft.network.team.manhunt.utils.config.twist;

import org.bukkit.configuration.file.FileConfiguration;

public class EndMessagePlainTwistConfig extends PlainTwistConfig {
    private String endedMessage;

    public EndMessagePlainTwistConfig(String twistName) {
        super(twistName);
    }

    public String getEndedMessage() {
        return endedMessage;
    }

    @Override
    public void load(FileConfiguration configuration) {
        super.load(configuration);
        this.endedMessage = configuration.getString("messages.twists." + twistName + "-ended");
    }

    @Override
    public void save(FileConfiguration configuration) {
        super.save(configuration);
        configuration.set("messages.twists." + twistName + "-ended", endedMessage);
    }
}
