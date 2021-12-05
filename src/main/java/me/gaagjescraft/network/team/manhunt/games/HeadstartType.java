package me.gaagjescraft.network.team.manhunt.games;

public enum HeadstartType {

    HALF_MINUTE(30),
    ONE_MINUTE(60),
    ONE_HALF_MINUTE(90),
    TWO_MINUTES(120);

    private final int seconds;

    HeadstartType(int sec) {
        this.seconds = sec;
    }

    public int getSeconds() {
        return seconds;
    }
}
