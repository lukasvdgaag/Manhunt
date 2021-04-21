package me.gaagjescraft.network.team.manhunt.games;

public enum TwistVote {

    NONE("none"),
    EXTRA_HEALTH("extra health"),
    SPEED_BOOST("speed boost"),
    BLINDNESS("random blindness"),
    ACID_RAIN("acid rain"),
    RANDOM_YEET("random yeets"),
    HARDCORE("hardcore");

    String displayName;

    TwistVote(String display) {
        this.displayName = display;
    }

    public String getDisplayName() {
        return displayName;
    }
}
