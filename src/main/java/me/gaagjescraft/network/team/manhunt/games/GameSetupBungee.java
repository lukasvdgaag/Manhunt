package me.gaagjescraft.network.team.manhunt.games;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class GameSetupBungee {

    private final GameSetup gameSetup;
    private final List<String> serversChecked;
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

    public void setNoGameAvailable(boolean noGameAvailable) {
        this.noGameAvailable = noGameAvailable;
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

    public void requestNextGameCreation() {
        if (!isStartedSearching()) {
            this.startedSearching = true;
            Util.sendTitle(gameSetup.getHost(), Util.c(Manhunt.get().getCfg().searchingServerTitle), 0, 1000, 0);
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
                        Util.playSound(gameSetup.getHost(), Manhunt.get().getCfg().searchingServerTickingSound, 1, 1);
                    }
                    if (currentTick - lastServerTicked >= 60 && !isServerMatched()) {
                        if (!isLastServer) requestNextGameCreation();
                        else {
                            Bukkit.getScheduler().cancelTask(runnableTaskId);
                            Util.playSound(gameSetup.getHost(), Manhunt.get().getCfg().noServersAvailableSound, 1, 1);
                            gameSetup.getHost().resetTitle();
                            Util.sendTitle(gameSetup.getHost(), Util.c(Manhunt.get().getCfg().noServersAvailableTitle), 10, 50, 10);
                            gameSetup.getHost().sendMessage(Util.c(Manhunt.get().getCfg().noServersAvailableMessage));

                            if (Manhunt.get().getEconomy().getBalance(gameSetup.getHost()) != -1 && !gameSetup.getHost().hasPermission("manhunt.hostgame")) {
                                Manhunt.get().getEconomy().addBalance(gameSetup.getHost(), Manhunt.get().getCfg().pricePerGame);
                                gameSetup.getHost().sendMessage(Util.c(Manhunt.get().getCfg().moneyRefundedNoServersMessage).replace("%money%", Manhunt.get().getCfg().pricePerGame + ""));
                            }
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
                Manhunt.get().getBungeeMessenger().createGameServer(this.gameSetup, server);
                serversChecked.add(server);
                currentServerChecking = server;
                return;
            }
        }
        this.isLastServer = true;
    }

}
