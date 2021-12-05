package me.gaagjescraft.network.team.manhunt.inst;

import java.util.UUID;

public class PlayerStat {

    private final UUID uuid;
    private long bestSpeedRunTime;
    private int hunterGamesPlayed;
    private int runnerGamesPlayed;
    private int hunterKills; // kills made as hunter
    private int runnerKills; // kills made as runner
    private int hunterWins; // wins as hunter
    private int runnerWins; // wins as runner

    public PlayerStat(UUID uuid, long bestSpeedRunTime, int hunterKills, int runnerKills, int hunterWins, int runnerWins, int runnerGamesPlayed, int hunterGamesPlayed) {
        this.uuid = uuid;
        this.bestSpeedRunTime = bestSpeedRunTime;
        this.hunterKills = hunterKills;
        this.runnerKills = runnerKills;
        this.hunterWins = hunterWins;
        this.runnerWins = runnerWins;
        this.runnerGamesPlayed = runnerGamesPlayed;
        this.hunterGamesPlayed = hunterGamesPlayed;
    }

    public long getBestSpeedRunTime() {
        return bestSpeedRunTime;
    }

    public void setBestSpeedRunTime(long bestSpeedRunTime) {
        this.bestSpeedRunTime = bestSpeedRunTime;
    }

    public int getHunterKills() {
        return hunterKills;
    }

    public void setHunterKills(int hunterKills) {
        this.hunterKills = hunterKills;
    }

    public int getRunnerKills() {
        return runnerKills;
    }

    public void setRunnerKills(int runnerKills) {
        this.runnerKills = runnerKills;
    }

    public int getHunterWins() {
        return hunterWins;
    }

    public void setHunterWins(int hunterWins) {
        this.hunterWins = hunterWins;
    }

    public int getRunnerWins() {
        return runnerWins;
    }

    public void setRunnerWins(int runnerWins) {
        this.runnerWins = runnerWins;
    }

    public int getHunterGamesPlayed() {
        return hunterGamesPlayed;
    }

    public void setHunterGamesPlayed(int hunterGamesPlayed) {
        this.hunterGamesPlayed = hunterGamesPlayed;
    }

    public int getRunnerGamesPlayed() {
        return runnerGamesPlayed;
    }

    public void setRunnerGamesPlayed(int runnerGamesPlayed) {
        this.runnerGamesPlayed = runnerGamesPlayed;
    }

    public UUID getUuid() {
        return uuid;
    }
}
