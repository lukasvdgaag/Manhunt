package me.gaagjescraft.network.team.manhunt;

import me.gaagjescraft.network.team.manhunt.commands.EventCmd;
import me.gaagjescraft.network.team.manhunt.commands.LeaveCmd;
import me.gaagjescraft.network.team.manhunt.commands.ManhuntCmd;
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
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Manhunt extends JavaPlugin {

    private static Manhunt instance;
    private EventMenu eventMenu;
    private ManhuntGamesMenu manhuntGamesMenu;
    private ManhuntGameSetupMenu manhuntGameSetupMenu;
    private ManhuntPlayerAmountSetupMenu manhuntPlayerAmountSetupMenu;
    private ManhuntTwistVoteMenu manhuntTwistVoteMenu;
    private ManhuntHeadstartSetupMenu manhuntHeadstartSetupMenu;
    private Util util;

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
        util = new Util();

        saveDefaultConfig();
        reloadConfig();

        loadCAE();
        loadSchedulers();
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

    public Util getUtil() {
        return util;
    }

    private void loadSchedulers() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            List<Player> prs = new ArrayList<>(getManhuntGamesMenu().getViewers());
            for (Player p : prs) {
                if (p == null) continue;
                getManhuntGamesMenu().openMenu(p);
            }

            for (Game game : Game.getGames()) {
                for (GamePlayer gp : game.getPlayers()) {
                    Player p = Bukkit.getPlayer(gp.getUuid());
                    if (p == null) continue;
                    if (p.getLocation().getBlockY() <= 150) {
                        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING || (game.getStatus() == GameStatus.PLAYING && game.getTimer() <= game.getHeadStart().getSeconds() && gp.getPlayerType() == PlayerType.HUNTER)) {
                            p.sendMessage("§cYou can't leave the waiting zone yet.");
                            p.teleport(game.getSchematic().getSpawnLocation());
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
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
        getServer().getPluginManager().registerEvents(eventMenu, this);
        getServer().getPluginManager().registerEvents(manhuntGamesMenu, this);
        getServer().getPluginManager().registerEvents(manhuntGameSetupMenu, this);
        getServer().getPluginManager().registerEvents(manhuntPlayerAmountSetupMenu, this);
        getServer().getPluginManager().registerEvents(manhuntTwistVoteMenu, this);
        getServer().getPluginManager().registerEvents(manhuntHeadstartSetupMenu, this);
        getServer().getPluginManager().registerEvents(new RunnerTrackerMenuHandler(), this);

        getServer().getPluginManager().registerEvents(new DeathEventHandler(), this);
        getServer().getPluginManager().registerEvents(new GameWaitingEvents(), this);
        getServer().getPluginManager().registerEvents(new LeaveEventHandler(), this);
        getServer().getPluginManager().registerEvents(new GameEventsHandlers(), this);
    }
}
