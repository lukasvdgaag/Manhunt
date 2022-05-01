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
    private final Manhunt plugin;

    public GameSetupBungee(Manhunt plugin, GameSetup setup) {
        this.plugin = plugin;
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
            plugin.getUtil().sendTitle(gameSetup.getHost(), Util.c(plugin.getCfg().searchingServerTitle), 0, 1000, 0);
            runnableTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
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
                        plugin.getUtil().playSound(gameSetup.getHost(), plugin.getCfg().searchingServerTickingSound, 1, 1);
                    }
                    if (currentTick - lastServerTicked >= 60 && !isServerMatched()) {
                        if (!isLastServer) requestNextGameCreation();
                        else {
                            Bukkit.getScheduler().cancelTask(runnableTaskId);
                            plugin.getUtil().playSound(gameSetup.getHost(), plugin.getCfg().noServersAvailableSound, 1, 1);
                            gameSetup.getHost().resetTitle();
                            plugin.getUtil().sendTitle(gameSetup.getHost(), Util.c(plugin.getCfg().noServersAvailableTitle), 10, 50, 10);
                            gameSetup.getHost().sendMessage(Util.c(plugin.getCfg().noServersAvailableMessage));

                            if (plugin.getEconomy() != null && plugin.getEconomy().getBalance(gameSetup.getHost()) != -1 && !gameSetup.getHost().hasPermission("manhunt.hostgame")) {
                                plugin.getEconomy().addBalance(gameSetup.getHost(), plugin.getCfg().pricePerGame);
                                gameSetup.getHost().sendMessage(Util.c(plugin.getCfg().moneyRefundedNoServersMessage).replace("%money%", plugin.getCfg().pricePerGame + ""));
                            }
                        }
                        return;
                    }
                    currentTick++;
                }
            }, 0, 1L);
        }
        List<String> servers = plugin.getCfg().gameServers;
        for (String server : servers) {
            if (!serversChecked.contains(server)) {
                plugin.getBungeeMessenger().createGameServer(this.gameSetup, server);
                serversChecked.add(server);
                currentServerChecking = server;
                return;
            }
        }
        this.isLastServer = true;
    }

}
