package me.gaagjescraft.network.team.manhunt;

import me.gaagjescraft.network.team.manhunt.commands.*;
import me.gaagjescraft.network.team.manhunt.events.DeathEventHandler;
import me.gaagjescraft.network.team.manhunt.events.GameEventsHandlers;
import me.gaagjescraft.network.team.manhunt.events.GameWaitingEvents;
import me.gaagjescraft.network.team.manhunt.events.LeaveEventHandler;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.menus.*;
import me.gaagjescraft.network.team.manhunt.menus.handlers.RunnerTrackerMenuHandler;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Manhunt extends JavaPlugin {

    private static Manhunt instance;
    private EventMenu eventMenu;
    private ManhuntGamesMenu manhuntGamesMenu;
    private ManhuntGameSetupMenu manhuntGameSetupMenu;
    private ManhuntPlayerAmountSetupMenu manhuntPlayerAmountSetupMenu;
    private ManhuntTwistVoteMenu manhuntTwistVoteMenu;
    private ManhuntHeadstartSetupMenu manhuntHeadstartSetupMenu;
    private ManhuntRunnerManageMenu manhuntRunnerManageMenu;
    private Util util;
    private List<Long> worldSeeds;
    private Location lobby;

    public static Manhunt get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.eventMenu = new EventMenu();
        manhuntGamesMenu = new ManhuntGamesMenu();
        manhuntGameSetupMenu = new ManhuntGameSetupMenu();
        manhuntPlayerAmountSetupMenu = new ManhuntPlayerAmountSetupMenu();
        manhuntTwistVoteMenu = new ManhuntTwistVoteMenu();
        manhuntHeadstartSetupMenu = new ManhuntHeadstartSetupMenu();
        manhuntRunnerManageMenu = new ManhuntRunnerManageMenu();
        util = new Util();

        saveDefaultConfig();
        reloadConfig();

        this.worldSeeds = getConfig().getLongList("seeds");
        this.lobby = getConfig().getLocation("lobby");

        loadCAE();
        loadSchedulers();
    }

    public Location getLobby() {
        return lobby;
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

    public Util getUtil() {
        return util;
    }

    public List<Long> getWorldSeeds() {
        return worldSeeds;
    }

    private void loadSchedulers() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player p : getLobby().getWorld().getPlayers()) {
                if (p.getLocation().getBlockY() < 50) {
                    p.teleport(getLobby());
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    p.sendMessage("§cYou can't leave the lobby!");
                }
            }

            for (Game game : Game.getGames()) {
                if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING || game.getStatus() == GameStatus.PLAYING && game.getTimer() <= game.getHeadStart().getSeconds()) {
                    for (GamePlayer gp : game.getOnlinePlayers(null)) {
                        Player p = Bukkit.getPlayer(gp.getUuid());
                        if (p == null) continue;
                        if (p.getLocation().getBlockY() <= 150) {
                            if (game.getStatus() != GameStatus.PLAYING || (game.getStatus() == GameStatus.PLAYING && game.getTimer() <= game.getHeadStart().getSeconds() && gp.getPlayerType() == PlayerType.HUNTER)) {
                                p.sendMessage("§cYou can't leave the waiting zone yet.");
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

        getServer().getPluginManager().registerEvents(new DeathEventHandler(), this);
        getServer().getPluginManager().registerEvents(new GameWaitingEvents(), this);
        getServer().getPluginManager().registerEvents(new LeaveEventHandler(), this);
        getServer().getPluginManager().registerEvents(new GameEventsHandlers(), this);
    }
}
