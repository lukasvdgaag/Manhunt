package me.gaagjescraft.network.team.manhunt.games;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;

public class GameSetupBungee {

    private GameSetup gameSetup;
    private List<String> serversChecked;
    private boolean startedSearching;
    private boolean noGameAvailable;
    private int runnableTaskId;
    private String currentServerChecking;
    private boolean serverMatched;
    private boolean isLastServer;

    public GameSetupBungee(GameSetup setup) {
        this.gameSetup = setup;
        this.startedSearching = false;
        this.noGameAvailable = false;
        this.currentServerChecking = null;
        this.serverMatched = false;
        this.isLastServer = false;
        this.serversChecked = new ArrayList<>();
    }

    public boolean isNoGameAvailable() {
        return noGameAvailable;
    }

    public boolean isStartedSearching() {
        return startedSearching;
    }

    public boolean isServerMatched() {
        return serverMatched;
    }

    public void setServerMatched(boolean serverMatched) {
        this.serverMatched = serverMatched;
    }

    public int getRunnableTaskId() {
        return runnableTaskId;
    }

    public boolean isLastServer() {
        return isLastServer;
    }

    public void setNoGameAvailable(boolean noGameAvailable) {
        this.noGameAvailable = noGameAvailable;
    }

    public void requestNextGameCreation() {
        if (!isStartedSearching()) {
            this.startedSearching = true;
            gameSetup.getHost().sendTitle("§eSearching Servers...", "§7Hold on while we find you a host server!", 0, 1000, 0);
            runnableTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Manhunt.get(), new Runnable() {
                int currentTick = 0;
                int lastServerTicked = 0;
                String lastServerTickedName = null;

                @Override
                public void run() {
                    if (isNoGameAvailable() || isServerMatched()) {
                        return;
                    }

                    if (currentServerChecking != null && !currentServerChecking.equals(lastServerTickedName)) {
                        lastServerTickedName = currentServerChecking;
                        lastServerTicked = currentTick;
                    }

                    if (currentTick % 10 == 0) {
                        gameSetup.getHost().playSound(gameSetup.getHost().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    }
                    if (currentTick - lastServerTicked >= 40 && !isServerMatched()) {
                        if (!isLastServer) requestNextGameCreation();
                        else {
                            Bukkit.getScheduler().cancelTask(runnableTaskId);
                            gameSetup.getHost().playSound(gameSetup.getHost().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            gameSetup.getHost().resetTitle();
                            gameSetup.getHost().sendTitle("§cNo Servers Available", "§7There are currently no free servers available!", 10, 50, 10);
                            gameSetup.getHost().sendMessage("§cWe couldn't find a free server to host your Manhunt game in. Please try again later.");
                        }
                        return;
                    }
                    currentTick++;
                }
            }, 0, 1L);
        }
        List<String> servers = Manhunt.get().getCfg().gameServers;
        for (String server : servers) {
            if (!serversChecked.contains(server)) {
                Manhunt.get().getUtil().createGameServer(this.gameSetup, server);
                serversChecked.add(server);
                currentServerChecking = server;
                return;
            }
        }
        this.isLastServer = true;
    }

}
