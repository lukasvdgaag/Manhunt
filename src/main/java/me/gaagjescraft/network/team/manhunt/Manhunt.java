package me.gaagjescraft.network.team.manhunt;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import me.gaagjescraft.network.team.manhunt.commands.*;
import me.gaagjescraft.network.team.manhunt.events.DeathEventHandler;
import me.gaagjescraft.network.team.manhunt.events.GameEventsHandlers;
import me.gaagjescraft.network.team.manhunt.events.GameWaitingEvents;
import me.gaagjescraft.network.team.manhunt.events.LeaveEventHandler;
import me.gaagjescraft.network.team.manhunt.events.bungee.BungeeMessenger;
import me.gaagjescraft.network.team.manhunt.events.bungee.BungeeSocketManager;
import me.gaagjescraft.network.team.manhunt.games.*;
import me.gaagjescraft.network.team.manhunt.inst.storage.MongoStorage;
import me.gaagjescraft.network.team.manhunt.inst.storage.MySQLStorage;
import me.gaagjescraft.network.team.manhunt.inst.storage.PlayerStorage;
import me.gaagjescraft.network.team.manhunt.inst.storage.YamlStorage;
import me.gaagjescraft.network.team.manhunt.menus.*;
import me.gaagjescraft.network.team.manhunt.menus.handlers.RunnerTrackerMenuHandler;
import me.gaagjescraft.network.team.manhunt.utils.*;
import me.gaagjescraft.network.team.manhunt.utils.platform.OriginalPlatformUtils;
import me.gaagjescraft.network.team.manhunt.utils.platform.PlatformUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Manhunt extends JavaPlugin {

    private static Manhunt instance;
    private ManhuntGamesMenu manhuntGamesMenu;
    private ManhuntGameSetupMenu manhuntGameSetupMenu;
    private ManhuntPlayerAmountSetupMenu manhuntPlayerAmountSetupMenu;
    private ManhuntTwistVoteMenu manhuntTwistVoteMenu;
    private ManhuntHeadstartSetupMenu manhuntHeadstartSetupMenu;
    private ManhuntRunnerManageMenu manhuntRunnerManageMenu;
    private ManhuntMainMenu manhuntMainMenu;
    private Util util;
    private Config config;
    private PlayerStorage playerStorage;
    private PlatformUtils platformUtils;
    private TagUtils tagUtils;
    private BungeeSocketManager bungeeSocketManager;
    private VaultEcoHook ecoHook;
    private BungeeMessenger bungeeMessenger;
    private Multiversehook multicreate;
    private ChunkyHook chunkgen;

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

        manhuntGamesMenu = new ManhuntGamesMenu();
        manhuntGameSetupMenu = new ManhuntGameSetupMenu();
        manhuntPlayerAmountSetupMenu = new ManhuntPlayerAmountSetupMenu();
        manhuntTwistVoteMenu = new ManhuntTwistVoteMenu();
        manhuntHeadstartSetupMenu = new ManhuntHeadstartSetupMenu();
        manhuntRunnerManageMenu = new ManhuntRunnerManageMenu();
        manhuntMainMenu = new ManhuntMainMenu();
        setPlatformUtils(new OriginalPlatformUtils());

        File targetSchem = new File(Manhunt.get().getDataFolder(), "manhunt-lobby.schem");
        if (!targetSchem.exists()) {
            try {
                saveResource("manhunt-lobby.schem", false);
            } catch (Exception e) {
                getLogger().severe("Manhunt failed to copy the manhunt-lobby.schem from the resources. Please download it yourself from this website and upload it to the /plugins/Manhunt/ folder.");
                getLogger().severe("We will disable the plugin for the time being to prevent further complications.");
                onDisable();
                return;
            }
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
            getLogger().info("Found Multiverse-Core! We will register and create Manhunt worlds with Multiverse so everything will work smoothly.");
            multicreate = new Multiversehook();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            getLogger().info("Found WorldEdit! This is used to handle the waiting lobby schematic pasting and removing.");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Chunky")){
            getLogger().info("Chunky Pregenerator found we will pregen some blocks around spawn before player is teleported");
            chunkgen = new ChunkyHook();
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
        if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
            getLogger().info("Found ViaVersion! You can now use the protocol version checker.");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Vault") && getCfg().pricePerGame > 0) {
            ecoHook = new VaultEcoHook();
            if (!ecoHook.setupEconomy()) {
                getLogger().info("Found Vault, but couldn't setup the Economy. Is it set up?");
            } else {
                getLogger().info("Found vault! You can now charge money for hosting games.");
            }
        }
        setBungeeMessenger(new BungeeMessenger());
        if (getCfg().bungeeMode) {
            bungeeSocketManager = new BungeeSocketManager();
            if (getCfg().isLobbyServer) {
                bungeeSocketManager.enableServer();
            } else {
                bungeeSocketManager.connectClientToServer();
            }
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            getLogger().info("BungeeCord support was enabled in the config, so we started the socket. Waiting for a connection...");
        }
        getLogger().info("");

        try {
            InetAddress ad = InetAddress.getLocalHost();
            getLogger().info("Possible socket IP (often internal): " + ad.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        getLogger().info("Do not claim this project as yours.");
        getLogger().info("----------------------------------");

        Bukkit.getOnlinePlayers().forEach(player -> getPlayerStorage().loadUser(player.getUniqueId()));
        /*
        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        MVWorldManager worldManager = core.getMVWorldManager();
        worldManager.deleteWorld("world_nether");
        worldManager.deleteWorld("world_the_end");

         */
    }

    public PlatformUtils getPlatformUtils() {
        return platformUtils;
    }

    public void setPlatformUtils(PlatformUtils platformUtils) {
        this.platformUtils = platformUtils;
    }

    @Override
    public void onDisable() {
        for (Game game : Game.getGames()) {
            game.delete();
        }

        if (bungeeSocketManager != null && getCfg().isLobbyServer) getBungeeMessenger().createDisconnectClientMessage();
        if (bungeeSocketManager != null) bungeeSocketManager.close();
    }

    public void loadStorage() {
        String storageType = getCfg().storageType;
        if (storageType.equalsIgnoreCase("mongodb")) {
            this.playerStorage = new MongoStorage();
        } else if (storageType.equalsIgnoreCase("yaml") || storageType.equalsIgnoreCase("file")) {
            this.playerStorage = new YamlStorage();
        } else if (storageType.equalsIgnoreCase("mysql")) {
            this.playerStorage = new MySQLStorage();
        } else {
            Bukkit.getLogger().severe("Manhunt found an invalid storage type in the config.yml. Please choose one of the following: YAML, MySQL, MongoDB.");
            Bukkit.getLogger().severe("We will be shutting down the plugin to prevent further complications.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.playerStorage.setup();
    }

    public BungeeMessenger getBungeeMessenger() {
        return bungeeMessenger;
    }

    public void setBungeeMessenger(BungeeMessenger bungeeMessenger) {
        this.bungeeMessenger = bungeeMessenger;
    }

    public TagUtils getTagUtils() {
        return tagUtils;
    }

    public PlayerStorage getPlayerStorage() {
        return playerStorage;
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

    public BungeeSocketManager getBungeeSocketManager() {
        return bungeeSocketManager;
    }

    public ManhuntMainMenu getManhuntMainMenu() {
        return manhuntMainMenu;
    }

    public Util getUtil() {
        return util;
    }

    public Config getCfg() {
        return config;
    }

    public VaultEcoHook getEconomy() {
        return ecoHook;
    }

    public Multiversehook getMultiversehook() {return multicreate;}

    public ChunkyHook getChunkHook() {return chunkgen;}


    private void loadSchedulers() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (Manhunt.get().getCfg().teleportPlayersToLobbyInVoid && getCfg().lobby != null) {
                for (Player p : getCfg().lobby.getWorld().getPlayers()) {
                    if (p.getLocation().getBlockY() < Manhunt.get().getCfg().lobbyTeleportYCoord) {
                        p.teleport(getCfg().lobby);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        p.sendMessage(Util.c(Manhunt.get().getCfg().cannotLeaveLobbyMessage));
                    }
                }
            }

            if (!getCfg().isLobbyServer) {
                for (Game game : Game.getGames()) {
                    if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING || game.getStatus() == GameStatus.PLAYING && game.getTimer() <= game.getHeadStart().getSeconds()) {
                        for (GamePlayer gp : game.getOnlinePlayers(null)) {
                            Player p = Bukkit.getPlayer(gp.getUuid());
                            if (p == null) continue;
                            if (p.getLocation().getBlockY() <= 150 || !p.getWorld().getName().equals(game.getWorld().getName())) {
                                if (!gp.isSpectating() && (game.getStatus() != GameStatus.PLAYING || (game.getStatus() == GameStatus.PLAYING && game.getTimer() <= game.getHeadStart().getSeconds() && gp.getPlayerType() == PlayerType.HUNTER))) {
                                    p.sendMessage(Util.c(Manhunt.get().getCfg().cannotLeaveWaitingZoneMessage));
                                    p.teleport(game.getSchematic().getSpawnLocation());
                                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                }
                            }
                        }
                    }
                }
            }
        }, 0L, 20L);

        if (!getCfg().isLobbyServer) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
                for (Game game : Game.getGames()) {
                    getUtil().updateHealth(game);
                }
            }, 5, 5);
        }
    }

    private void loadCAE() {
        getCommand("manhunt").setExecutor(new ManhuntCmd());
        getCommand("leave").setExecutor(new LeaveCmd());
        getCommand("compass").setExecutor(new CompassCmd());
        getCommand("rejoin").setExecutor(new RejoinCmd());
        getCommand("manhuntstats").setExecutor(new StatsCmd());
        getServer().getPluginManager().registerEvents(manhuntGamesMenu, this);
        getServer().getPluginManager().registerEvents(manhuntGameSetupMenu, this);
        getServer().getPluginManager().registerEvents(manhuntPlayerAmountSetupMenu, this);
        getServer().getPluginManager().registerEvents(manhuntTwistVoteMenu, this);
        getServer().getPluginManager().registerEvents(manhuntHeadstartSetupMenu, this);
        getServer().getPluginManager().registerEvents(manhuntRunnerManageMenu, this);
        getServer().getPluginManager().registerEvents(manhuntMainMenu, this);
        getServer().getPluginManager().registerEvents(new RunnerTrackerMenuHandler(), this);
        //getServer().getMessenger().registerIncomingPluginChannel(this, "exodus:manhunt", pluginMessageHandler);
        //getServer().getMessenger().registerOutgoingPluginChannel(this, "exodus:manhunt");

        getServer().getPluginManager().registerEvents(new DeathEventHandler(), this);
        getServer().getPluginManager().registerEvents(new GameWaitingEvents(), this);
        getServer().getPluginManager().registerEvents(new LeaveEventHandler(), this);
        getServer().getPluginManager().registerEvents(new GameEventsHandlers(), this);
    }
}
