package me.gaagjescraft.network.team.manhunt.games;

import me.gaagjescraft.network.team.manhunt.Manhunt;

import java.util.ArrayList;
import java.util.List;

public class GameSetupBungee {

    private GameSetup gameSetup;
    private List<String> serversChecked;
    private boolean startedSearching;
    private boolean noGameAvailable;

    public GameSetupBungee(GameSetup setup) {
        this.gameSetup = setup;
        this.startedSearching = true;
        this.noGameAvailable = false;
        this.serversChecked = new ArrayList<>();
    }

    public boolean isNoGameAvailable() {
        return noGameAvailable;
    }

    public boolean isStartedSearching() {
        return startedSearching;
    }

    public void requestNextGameCreation() {
        this.startedSearching = true;
        List<String> servers = Manhunt.get().getConfig().getStringList("game_servers");
        for (String server : servers) {
            if (!serversChecked.contains(server)) {
                Manhunt.get().getUtil().createGameServer(this.gameSetup, server);
                return;
            }
        }
        this.noGameAvailable = true;
    }

}
