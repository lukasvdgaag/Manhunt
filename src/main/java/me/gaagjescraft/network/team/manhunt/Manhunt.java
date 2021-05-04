package me.gaagjescraft.network.team.manhunt;

import me.gaagjescraft.network.team.manhunt.commands.*;
import me.gaagjescraft.network.team.manhunt.events.DeathEventHandler;
import me.gaagjescraft.network.team.manhunt.events.GameEventsHandlers;
import me.gaagjescraft.network.team.manhunt.events.GameWaitingEvents;
import me.gaagjescraft.network.team.manhunt.events.LeaveEventHandler;
import me.gaagjescraft.network.team.manhunt.events.bungee.PluginMessageHandler;
import me.gaagjescraft.network.team.manhunt.games.*;
import me.gaagjescraft.network.team.manhunt.menus.*;
import me.gaagjescraft.network.team.manhunt.menus.handlers.RunnerTrackerMenuHandler;
import me.gaagjescraft.network.team.manhunt.utils.Config;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Manhunt extends JavaPlugin {

    private static Manhunt instance;
    private EventMenu eventMenu;
    private ManhuntGamesMenu manhuntGamesMenu;
    private ManhuntGameSetupMenu manhuntGameSetupMenu;
    private ManhuntPlayerAmountSetupMenu manhuntPlayerAmountSetupMenu;
    private ManhuntTwistVoteMenu manhuntTwistVoteMenu;
    private ManhuntHeadstartSetupMenu manhuntHeadstartSetupMenu;
    private ManhuntRunnerManageMenu manhuntRunnerManageMenu;
    private PluginMessageHandler pluginMessageHandler;
    private Util util;
    private Config config;

    public static Manhunt get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        config = new Config();

        for (TwistVote vote : TwistVote.values()) {
            vote.updateDisplayName();
        }
        util = new Util();
        Itemizer.load();

        eventMenu = new EventMenu();
        manhuntGamesMenu = new ManhuntGamesMenu();
        manhuntGameSetupMenu = new ManhuntGameSetupMenu();
        manhuntPlayerAmountSetupMenu = new ManhuntPlayerAmountSetupMenu();
        manhuntTwistVoteMenu = new ManhuntTwistVoteMenu();
        manhuntHeadstartSetupMenu = new ManhuntHeadstartSetupMenu();
        manhuntRunnerManageMenu = new ManhuntRunnerManageMenu();
        pluginMessageHandler = new PluginMessageHandler();

        loadCAE();
        loadSchedulers();

        getLogger().info("----------------------------------");
        getLogger().info("Manhunt");
        getLogger().info("");
        getLogger().info("Author: GaagjesCraft Network Team (GCNT)");
        getLogger().info("Website: https://gaagjescraft.net/");
        getLogger().info("Version: " + this.getDescription().getVersion());
        getLogger().info("");
        getLogger().info("This is a premium plugin and is licensed to GCNT.");
        getLogger().info("It is not allowed to resell or redistribute the plugin.");
        if (Bukkit.getPluginManager().isPluginEnabled("MultiverseCore")) {
            getLogger().info("Found MultiverseCore! We will register the Manhunt worlds so everything will work smoothly. Please know that in order for portals to work, you need to have Multiverse Portals installed too.");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            getLogger().info("Found WorldEdit! This is used to handle the waiting lobby schematic pasting and removing.");
        }
        getLogger().info("");
        getLogger().info("This plugin was created in collaboration with ExodusMC.");
        getLogger().info("Do not claim this project as yours.");
        getLogger().info("----------------------------------");
    }

    @Override
    public void onDisable() {
        for (Game game : Game.getGames()) {
            game.delete();
        }
    }

    public EventMenu getEventMenu() {
        return eventMenu;
    }

    public ManhuntGamesMenu getManhuntGamesMenu() {
        return manhuntGamesMenu;
    }

    public ManhuntGameSetupMenu getManhuntGameSetupMenu() {
        return manhuntGameSetupMenu;
    }

    public ManhuntPlayerAmountSetupMenu getManhuntPlayerAmountSetupMenu() {
        return manhuntPlayerAmountSetupMenu;
    }

    public ManhuntTwistVoteMenu getManhuntTwistVoteMenu() {
        return manhuntTwistVoteMenu;
    }

    public ManhuntHeadstartSetupMenu getManhuntHeadstartSetupMenu() {
        return manhuntHeadstartSetupMenu;
    }

    public ManhuntRunnerManageMenu getManhuntRunnerManageMenu() {
        return manhuntRunnerManageMenu;
    }

    public PluginMessageHandler getPluginMessageHandler() {
        return pluginMessageHandler;
    }

    public Util getUtil() {
        return util;
    }

    public Config getCfg() {
        return config;
    }

    private void loadSchedulers() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (Manhunt.get().getCfg().teleportPlayersToLobbyInVoid) {
                for (Player p : getCfg().lobby.getWorld().getPlayers()) {
                    if (p.getLocation().getBlockY() < 50) {
                        p.teleport(getCfg().lobby);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        p.sendMessage(Util.c(Manhunt.get().getCfg().cannotLeaveLobbyMessage));
                    }
                }
            }

            for (Game game : Game.getGames()) {
                if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING || game.getStatus() == GameStatus.PLAYING && game.getTimer() <= game.getHeadStart().getSeconds()) {
                    for (GamePlayer gp : game.getOnlinePlayers(null)) {
                        Player p = Bukkit.getPlayer(gp.getUuid());
                        if (p == null) continue;
                        if (p.getLocation().getBlockY() <= 150) {
                            if (game.getStatus() != GameStatus.PLAYING || (game.getStatus() == GameStatus.PLAYING && game.getTimer() <= game.getHeadStart().getSeconds() && gp.getPlayerType() == PlayerType.HUNTER)) {
                                p.sendMessage(Util.c(Manhunt.get().getCfg().cannotLeaveWaitingZoneMessage));
                                p.teleport(game.getSchematic().getSpawnLocation());
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            }
                        }
                    }
                }
            }
        }, 0L, 20L);

    }

    private void loadCAE() {
        getCommand("event").setExecutor(new EventCmd());
        getCommand("manhunt").setExecutor(new ManhuntCmd());
        getCommand("leave").setExecutor(new LeaveCmd());
        getCommand("compass").setExecutor(new CompassCmd());
        getCommand("rejoin").setExecutor(new RejoinCmd());
        getServer().getPluginManager().registerEvents(eventMenu, this);
        getServer().getPluginManager().registerEvents(manhuntGamesMenu, this);
        getServer().getPluginManager().registerEvents(manhuntGameSetupMenu, this);
        getServer().getPluginManager().registerEvents(manhuntPlayerAmountSetupMenu, this);
        getServer().getPluginManager().registerEvents(manhuntTwistVoteMenu, this);
        getServer().getPluginManager().registerEvents(manhuntHeadstartSetupMenu, this);
        getServer().getPluginManager().registerEvents(manhuntRunnerManageMenu, this);
        getServer().getPluginManager().registerEvents(new RunnerTrackerMenuHandler(), this);
        //getServer().getMessenger().registerIncomingPluginChannel(this, "exodus:manhunt", pluginMessageHandler);
        //getServer().getMessenger().registerOutgoingPluginChannel(this, "exodus:manhunt");

        getServer().getPluginManager().registerEvents(new DeathEventHandler(), this);
        getServer().getPluginManager().registerEvents(new GameWaitingEvents(), this);
        getServer().getPluginManager().registerEvents(new LeaveEventHandler(), this);
        getServer().getPluginManager().registerEvents(new GameEventsHandlers(), this);
    }
}
