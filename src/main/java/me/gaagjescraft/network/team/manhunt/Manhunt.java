package me.gaagjescraft.network.team.manhunt;

import me.gaagjescraft.network.team.manhunt.commands.*;
import me.gaagjescraft.network.team.manhunt.events.DeathEventHandler;
import me.gaagjescraft.network.team.manhunt.events.GameEventsHandlers;
import me.gaagjescraft.network.team.manhunt.events.GameWaitingEvents;
import me.gaagjescraft.network.team.manhunt.events.LeaveEventHandler;
import me.gaagjescraft.network.team.manhunt.events.bungee.BungeeSocketManager;
import me.gaagjescraft.network.team.manhunt.events.bungee.PluginMessageHandler;
import me.gaagjescraft.network.team.manhunt.games.*;
import me.gaagjescraft.network.team.manhunt.inst.storage.MongoStorage;
import me.gaagjescraft.network.team.manhunt.inst.storage.PlayerStorage;
import me.gaagjescraft.network.team.manhunt.menus.*;
import me.gaagjescraft.network.team.manhunt.menus.handlers.RunnerTrackerMenuHandler;
import me.gaagjescraft.network.team.manhunt.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
    private PlayerStorage playerStorage;
    private TagUtils tagUtils;
    private BungeeSocketManager bungeeSocketManager;

    public static Manhunt get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        reloadConfig();
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

        if (!new File(Manhunt.get().getDataFolder(), "manhunt-lobby.schem").exists()) {
            Manhunt.get().saveResource("manhunt-lobby.schem", false);
        }

        loadStorage();

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
        if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")) {
            getLogger().info("Found Multiverse-Core! We will register the Manhunt worlds so everything will work smoothly. Please know that in order for portals to work, you need to have Multiverse Portals installed too.");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            getLogger().info("Found WorldEdit! This is used to handle the waiting lobby schematic pasting and removing.");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PAPIHook().register();
            getLogger().info("Found PlaceholderAPI! You can now use the player placeholders as described on the resource page.");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("NametagEdit")) {
            this.tagUtils = new TagUtils();
            Bukkit.getPluginManager().registerEvents(this.tagUtils, this);
            getLogger().info("Found NametagEdit! We will now change the nametags of the players during the games.");
        }
        if (getCfg().bungeeMode) {
            bungeeSocketManager = new BungeeSocketManager();
            if (getCfg().isLobbyServer) {
                bungeeSocketManager.connectServer();
            } else {
                bungeeSocketManager.connectClient();
            }
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            getLogger().info("BungeeCord support was enabled in the config, so we started the socket. Waiting for a connection...");
        }
        getLogger().info("");

        try {
            InetAddress ad = InetAddress.getLocalHost();
            getLogger().info("Info stuffies: " + ad);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        getLogger().info("This plugin was created in collaboration with ExodusMC.");
        getLogger().info("Do not claim this project as yours.");
        getLogger().info("----------------------------------");
    }

    @Override
    public void onDisable() {
        for (Game game : Game.getGames()) {
            game.delete();
        }

        if (bungeeSocketManager != null) bungeeSocketManager.close();
        Bukkit.getScheduler().cancelTasks(this);
    }

    public void loadStorage() {
        String storageType = getCfg().storageType;
        if (storageType.equalsIgnoreCase("mongodb")) {
            this.playerStorage = new MongoStorage();
        }
        // todo add default file/sqlite storage.

        this.playerStorage.setup();
    }

    public TagUtils getTagUtils() {
        return tagUtils;
    }

    public PlayerStorage getPlayerStorage() {
        return playerStorage;
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

    public BungeeSocketManager getBungeeSocketManager() {
        return bungeeSocketManager;
    }

    public Util getUtil() {
        return util;
    }

    public Config getCfg() {
        return config;
    }

    private void loadSchedulers() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (Manhunt.get().getCfg().teleportPlayersToLobbyInVoid && getCfg().lobby != null) {
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
        getCommand("manhuntstats").setExecutor(new StatsCmd());
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
