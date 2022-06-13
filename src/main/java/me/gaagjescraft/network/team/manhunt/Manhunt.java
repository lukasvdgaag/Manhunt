package me.gaagjescraft.network.team.manhunt;

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
import me.gaagjescraft.network.team.manhunt.managers.party.*;
import me.gaagjescraft.network.team.manhunt.managers.world.BukkitWorldManager;
import me.gaagjescraft.network.team.manhunt.managers.world.MultiverseManager;
import me.gaagjescraft.network.team.manhunt.managers.world.WorldManager;
import me.gaagjescraft.network.team.manhunt.menus.*;
import me.gaagjescraft.network.team.manhunt.menus.handlers.RunnerTrackerMenuHandler;
import me.gaagjescraft.network.team.manhunt.menus.twist.ManhuntTwistVoteMenu;
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
import java.util.Objects;

public class Manhunt extends JavaPlugin {

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
    private ChunkyHook chunkgen;
    private Itemizer itemizer;
    private WorldManager worldManager;
    private PartyManager partyManager;

    @Override
    public void onEnable() {
        reloadConfig();
        config = new Config(this);

        for (TwistVote vote : TwistVote.values()) {
            vote.updateDisplayName(this);
        }
        util = new Util(this);
        itemizer = new Itemizer(this);

        manhuntGamesMenu = new ManhuntGamesMenu(this);
        manhuntGameSetupMenu = new ManhuntGameSetupMenu(this);
        manhuntPlayerAmountSetupMenu = new ManhuntPlayerAmountSetupMenu(this);
        manhuntTwistVoteMenu = new ManhuntTwistVoteMenu(this);
        manhuntHeadstartSetupMenu = new ManhuntHeadstartSetupMenu(this);
        manhuntRunnerManageMenu = new ManhuntRunnerManageMenu(this);
        manhuntMainMenu = new ManhuntMainMenu(this);
        setPlatformUtils(new OriginalPlatformUtils(this));

        File targetSchem = new File(getDataFolder(), "manhunt-lobby.schem");
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
        getLogger().info("Author: GCNT (GaagjesCraft Network Team)");
        getLogger().info("(and this one annoying dude called JT <3)");
        getLogger().info("");
        getLogger().info("Website: https://www.gcnt.net/");
        getLogger().info("Version: " + this.getDescription().getVersion());
        getLogger().info("");
        getLogger().info("This is a premium plugin and is licensed to GCNT.");
        getLogger().info("You are not allowed to resell or redistribute the plugin.");

        if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")) {
            getLogger().info("Found Multiverse-Core! We will register and create Manhunt worlds with Multiverse so everything will work smoothly.");
            worldManager = new MultiverseManager(this);
        } else {
            worldManager = new BukkitWorldManager(this);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            getLogger().info("Found WorldEdit! This is used to handle the waiting lobby schematic pasting and removing.");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Chunky")) {
            getLogger().info("Chunky Pregenerator found we will pregen some blocks around spawn before player is teleported");
            chunkgen = new ChunkyHook();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PAPIHook(this).register();
            getLogger().info("Found PlaceholderAPI! You can now use the player placeholders as described on the resource page.");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("NametagEdit")) {
            this.tagUtils = new TagUtils(this);
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

        if (getServer().getPluginManager().isPluginEnabled("Spigot-Party-API-PAF")) {
            getLogger().info("Hook into Spigot Party API for Party and Friends Extended (by Simonsator) support!");
            partyManager = new PAFBungeeManager();
        } else if (getServer().getPluginManager().isPluginEnabled("PartyAndFriends")) {
            getLogger().info("Hook into Party and Friends for Spigot (by Simonsator) support!");
            partyManager = new PAFSpigotManager();
        } else if (getServer().getPluginManager().isPluginEnabled("Parties")) {
            getLogger().info("Hook into Parties (by AlessioDP) support!");
            partyManager = new AlessioDPPartiesManager();
        } else {
            partyManager = new NoPartyManager();
        }

        setBungeeMessenger(new BungeeMessenger(this));
        if (getCfg().bungeeMode) {
            bungeeSocketManager = new BungeeSocketManager(this);
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
    }

    public PlatformUtils getPlatformUtils() {
        return platformUtils;
    }

    public void setPlatformUtils(PlatformUtils platformUtils) {
        this.platformUtils = platformUtils;
    }

    @Override
    public void onDisable() {
        if (!Game.getGames().isEmpty())
            for (Game game : Game.getGames()) {
                game.delete();
            }

        if (bungeeSocketManager != null && getCfg().isLobbyServer) getBungeeMessenger().createDisconnectClientMessage();
        if (bungeeSocketManager != null) bungeeSocketManager.close();
    }

    public void loadStorage() {
        String storageType = getCfg().storageType;
        if (storageType.equalsIgnoreCase("mongodb")) {
            this.playerStorage = new MongoStorage(this);
        } else if (storageType.equalsIgnoreCase("yaml") || storageType.equalsIgnoreCase("file")) {
            this.playerStorage = new YamlStorage(this);
        } else if (storageType.equalsIgnoreCase("mysql")) {
            this.playerStorage = new MySQLStorage(this);
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

    public ChunkyHook getChunkHook() {
        return chunkgen;
    }


    private void loadSchedulers() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (getCfg().teleportPlayersToLobbyInVoid && getCfg().lobby != null) {
                for (Player p : Objects.requireNonNull(getCfg().lobby.getWorld()).getPlayers()) {
                    if (p.getLocation().getBlockY() < getCfg().lobbyTeleportYCoord) {
                        p.teleport(getCfg().lobby);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        p.sendMessage(Util.c(getCfg().cannotLeaveLobbyMessage));
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
                                    p.sendMessage(Util.c(getCfg().cannotLeaveWaitingZoneMessage));
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

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    private void loadCAE() {
        Objects.requireNonNull(getCommand("manhunt")).setExecutor(new ManhuntCmd(this));
        Objects.requireNonNull(getCommand("leave")).setExecutor(new LeaveCmd(this));
        Objects.requireNonNull(getCommand("compass")).setExecutor(new CompassCmd(this));
        Objects.requireNonNull(getCommand("rejoin")).setExecutor(new RejoinCmd(this));
        Objects.requireNonNull(getCommand("manhuntstats")).setExecutor(new StatsCmd(this));
        getServer().getPluginManager().registerEvents(manhuntGamesMenu, this);
        getServer().getPluginManager().registerEvents(manhuntGameSetupMenu, this);
        getServer().getPluginManager().registerEvents(manhuntPlayerAmountSetupMenu, this);
        getServer().getPluginManager().registerEvents(manhuntTwistVoteMenu, this);
        getServer().getPluginManager().registerEvents(manhuntHeadstartSetupMenu, this);
        getServer().getPluginManager().registerEvents(manhuntRunnerManageMenu, this);
        getServer().getPluginManager().registerEvents(manhuntMainMenu, this);
        getServer().getPluginManager().registerEvents(new RunnerTrackerMenuHandler(this), this);
        getServer().getPluginManager().registerEvents(new DeathEventHandler(this), this);
        getServer().getPluginManager().registerEvents(new GameWaitingEvents(this), this);
        getServer().getPluginManager().registerEvents(new LeaveEventHandler(this), this);
        getServer().getPluginManager().registerEvents(new GameEventsHandlers(this), this);
    }

    public Itemizer getItemizer() {
        return itemizer;
    }
}
