package me.gaagjescraft.network.team.manhunt.games;

import me.gaagjescraft.network.team.manhunt.Manhunt;

public enum TwistVote {

    NONE("none"),
    EXTRA_HEALTH("extra health"),
    SPEED_BOOST("speed boost"),
    BLINDNESS("random blindness"),
    ACID_RAIN("acid rain"),
    RANDOM_YEET("random yeets"),
    HARDCORE("hardcore");

    private String displayName;

    TwistVote(String display) {
        this.displayName = display;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void updateDisplayName(Manhunt plugin) {
        this.displayName = plugin.getCfg().getFile().getString("prefixes.twists." + this.name().toLowerCase().replaceAll("_", "-"), getDisplayName());
    }
}
