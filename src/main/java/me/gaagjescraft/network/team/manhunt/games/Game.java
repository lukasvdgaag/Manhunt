package me.gaagjescraft.network.team.manhunt.games;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.menus.RunnerTrackerMenu;
import org.bukkit.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Game {

    public static List<Game> games;

    static {
        games = new ArrayList<>();
    }

    private String identifier;
    private List<GamePlayer> players;
    private boolean twistsAllowed;
    private boolean doDaylightCycle;
    private TwistVote selectedTwist;
    private int maxPlayers;
    private PlayerType winningTeam;
    private GameStatus status;
    private int timer;
    private GameSchematic schematic;
    private GameScheduler scheduler;
    private boolean allowFriendlyFire;
    private RunnerTrackerMenu runnerTrackerMenu;
    private int nextEventTime;
    private boolean eventActive;
    private HeadstartType headStart;

    private Game(String id, boolean twistsAllowed, Player host, int maxPlayers) {
        this.identifier = id;
        this.twistsAllowed = twistsAllowed;
        this.players = new ArrayList<>();
        this.selectedTwist = TwistVote.NONE;
        this.maxPlayers = maxPlayers;
        this.winningTeam = null;
        this.status = GameStatus.LOADING;
        this.timer = 0;
        this.schematic = new GameSchematic(this);
        this.doDaylightCycle = true;
        this.scheduler = new GameScheduler(this);
        this.allowFriendlyFire = false;
        this.runnerTrackerMenu = new RunnerTrackerMenu(this);
        this.eventActive = false;
        this.headStart = HeadstartType.HALF_MINUTE;
        determineNextEventTime();

        this.players.add(new GamePlayer(
                this, host.getUniqueId(), PlayerType.RUNNER, true
        ));
        games.add(this);
        this.scheduler.start();
    }

    public static Game createGame(boolean twistsAllowed, Player host, int maxPlayers) {
        if (getGame(host.getName()) != null || getGame(host) != null) return null;
        return new Game(host.getName(), twistsAllowed, host, maxPlayers);
    }

    public static List<Game> getGames() {
        return games;
    }

    public static Game getGame(String id) {
        for (Game g : games) if (g.getIdentifier().equalsIgnoreCase(id)) return g;
        return null;
    }

    public static Game getGame(Player player) {
        for (Game g : games) {
            for (GamePlayer gp : g.getPlayers())
                if (gp.getUuid().equals(player.getUniqueId()))
                    return g;
        }
        return null;
    }

    public void start() {
        this.status = GameStatus.STARTING;
        this.timer = 0;
    }

    public boolean addPlayer(Player player) {
        if (status == GameStatus.LOADING || status == GameStatus.STOPPING || getPlayers(PlayerType.HUNTER).size() >= maxPlayers)
            return false;
        GamePlayer gamePlayer = new GamePlayer(this, player.getUniqueId(), PlayerType.HUNTER, false);
        players.add(gamePlayer);

        for (GamePlayer gp : getPlayers()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p == null) continue;
            p.sendMessage(gamePlayer.getPrefix() + " " + player.getName() + "§a joined the game! §b[" + getPlayers().size() + "/" + (maxPlayers + getPlayers(PlayerType.RUNNER).size()) + "]");
        }

        gamePlayer.prepareForGame(getStatus());
        if (getStatus() == GameStatus.WAITING) player.teleport(this.schematic.getSpawnLocation());
        else player.teleport(Bukkit.getWorld("manhunt_" + identifier).getSpawnLocation());

        this.getRunnerTeleporterMenu().update();

        return true;
    }

    public void removePlayer(Player player) {
        GamePlayer gamePlayer = null;
        for (GamePlayer gp : this.players) {
            if (gp.getUuid().equals(player.getUniqueId())) {
                gamePlayer = gp;
                break;
            }
        }
        if (gamePlayer == null) return;
        Manhunt.get().getManhuntGameSetupMenu().gameSetups.remove(player);

        gamePlayer.restoreForLobby();
        gamePlayer.leaveGameDelayed(true);
        player.teleport(Objects.requireNonNull(Manhunt.get().getConfig().getLocation("lobby")));
        this.players.remove(gamePlayer);

        GamePlayer newHost = null;
        boolean changedHostPlayerType = false;

        if (getStatus() != GameStatus.STOPPING) {
            if (getStatus() == GameStatus.WAITING || getStatus() == GameStatus.STARTING) {
                if (gamePlayer.isHost()) {
                    List<GamePlayer> runners = getPlayers(PlayerType.RUNNER);
                    List<GamePlayer> hunters = getPlayers(PlayerType.HUNTER);
                    if (!runners.isEmpty()) {
                        Random random = ThreadLocalRandom.current();
                        int randomInt = random.nextInt(runners.size());
                        newHost = runners.get(randomInt);
                        newHost.setHost(true);
                    } else if (!hunters.isEmpty()) {
                        Random random = ThreadLocalRandom.current();
                        int randomInt = random.nextInt(hunters.size());
                        newHost = hunters.get(randomInt);
                        newHost.setHost(true);
                        newHost.setPlayerType(PlayerType.RUNNER);
                        changedHostPlayerType = true;
                    } else {
                        stopGame(false);
                    }
                }
            }

            for (GamePlayer gp : getPlayers()) {
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p == null) continue;
                if (getStatus() == GameStatus.WAITING) {
                    if (gamePlayer.isHost()) {
                        p.sendMessage("§dGame host " + player.getName() + "§c left the game! §b[" + getPlayers(PlayerType.HUNTER).size() + "/" + maxPlayers + "]");
                        if (newHost != null)
                            p.sendMessage("§d" + Bukkit.getPlayer(newHost.getUuid()).getName() + " has been assigned as new host!");
                        if (changedHostPlayerType)
                            p.sendMessage("§e" + Bukkit.getPlayer(newHost.getUuid()).getName() + " is the new speed runner!");
                    }
                    return;
                } else {
                    if (gamePlayer.getPlayerType() == PlayerType.HUNTER)
                        p.sendMessage(gamePlayer.getPrefix() + " " + player.getName() + "§c left the game! §b[" + getPlayers(PlayerType.HUNTER).size() + "/" + maxPlayers + "]");
                    else {
                        p.sendMessage(gamePlayer.getPrefix() + " " + player.getName() + "§c left the game! §b[" + getPlayers(PlayerType.RUNNER).size() + "/" + maxPlayers + "]");
                    }
                }
            }
        }
        this.getRunnerTeleporterMenu().update();
        for (GamePlayer gp : players) {
            gp.updateScoreboard();
        }
        checkForWin(false);
    }

    public void checkForWin(boolean forceWin) {
        List<GamePlayer> runners = getPlayers(PlayerType.RUNNER);
        List<GamePlayer> hunters = getPlayers(PlayerType.HUNTER);
        int aliveRunners = 0;
        int aliveHunters = 0;
        for (GamePlayer run : runners) {
            if (run.getDeaths() == 0) aliveRunners++;
        }
        for (GamePlayer hun : hunters) {
            if (hun.getDeaths() < 3) aliveHunters++;
        }

        if (aliveRunners == 0) winningTeam = PlayerType.HUNTER;
        else if (aliveHunters <= 5 || (forceWin && aliveRunners == hunters.size())) winningTeam = PlayerType.RUNNER;

        if (winningTeam != null) win();

    }

    public void win() {
        for (GamePlayer gp : getPlayers()) {
            Player player = Bukkit.getPlayer(gp.getUuid());
            if (player == null) continue;

            gp.updateScoreboard();
            player.closeInventory();

            player.sendMessage("§8§m--------------------------");
            player.sendMessage("§d§l" + winningTeam + "S HAVE WON THIS MANHUNT!");
            player.sendMessage("");
            player.sendMessage(gp.getPlayerType() == winningTeam ? "§aCongrats you won!" : "§cYou lost!");
            player.sendMessage("§7You will get teleported to the lobby shortly.");
            player.sendMessage("§8§m--------------------------");
            if (gp.getPlayerType() == winningTeam) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1,1);
                player.sendTitle("§a§lYOU WIN!", winningTeam == PlayerType.HUNTER ? "§7You managed to eliminate the runners!" : "§7You managed to survive the hunters!", 20, 80, 20);
            } else {
                player.sendTitle("§c§lYOU LOST!", gp.getPlayerType() == PlayerType.HUNTER ? "§7You were beaten by the runners!" : "§7You were eliminated by the hunters!", 20, 80, 20);
            }
        }

        if (this.getStatus() != GameStatus.STOPPING) stopGame(false);
    }

    public void stopGame(boolean checkForWin) {
        this.status = GameStatus.STOPPING;
        if (checkForWin) checkForWin(true);

        if (this.players.size() == 0) {
            delete();
        } else {
            this.scheduler.end();
        }
    }

    public void delete() {
        Location loc = Manhunt.get().getConfig().getLocation("lobby");
        for (Player p : Objects.requireNonNull(Bukkit.getWorld("manhunt_" + identifier)).getPlayers()) {
            p.teleport(loc);
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv delete manhunt_" + identifier);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv confirm");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv delete manhunt_" + identifier + "_nether");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv confirm");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv delete manhunt_" + identifier + "_the_end");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv confirm");

        this.players.clear();
        games.remove(this);
    }

    public void create() {
        long min = -9223372036854775800L;
        long max = 9223372036854775800L;
        long random = min + (long) (Math.random() * (max - min));

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv create manhunt_" + identifier + " NORMAL -s " + random);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Manhunt.get(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv create manhunt_" + identifier + "_nether NETHER"), 40L);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Manhunt.get(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv create manhunt_" + identifier + "_the_end END"), 80L);

        Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> {
            World world = Bukkit.getWorld("manhunt_" + identifier);
            if (world == null) {
                Bukkit.getLogger().severe("Something went wrong whilst creating the manhunt world with id " + identifier);
                return;
            }
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
            world.setGameRule(GameRule.SPAWN_RADIUS, 0);
            world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, isDoDaylightCycle());

            this.schematic.load();

            status = GameStatus.WAITING;

            for (GamePlayer gp : players) {
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p == null) continue;
                p.teleport(this.schematic.getSpawnLocation());
                gp.prepareForGame(getStatus());
                gp.updateScoreboard();
            }
        }, 60L);
    }


    public List<GamePlayer> getPlayers() {
        return players;
    }

    public List<GamePlayer> getPlayers(PlayerType type) {
        List<GamePlayer> prs = new ArrayList<>();
        for (GamePlayer gp : players) {
            if (gp.getPlayerType() == type) prs.add(gp);
        }
        return prs;
    }

    public GamePlayer getPlayer(Player p) {
        for (GamePlayer gp : players) {
            if (gp.getUuid().equals(p.getUniqueId())) return gp;
        }
        return null;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isTwistsAllowed() {
        return twistsAllowed;
    }

    public void setTwistsAllowed(boolean twistsAllowed) {
        this.twistsAllowed = twistsAllowed;
    }

    public TwistVote getSelectedTwist() {
        return selectedTwist;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public GameSchematic getSchematic() {
        return schematic;
    }

    public boolean isDoDaylightCycle() {
        return doDaylightCycle;
    }

    public void setDoDaylightCycle(boolean doDaylightCycle) {
        this.doDaylightCycle = doDaylightCycle;
    }

    public PlayerType getWinningTeam() {
        return winningTeam;
    }

    public World getWorld() {
        return Bukkit.getWorld("manhunt_" + identifier);
    }

    public GameScheduler getScheduler() {
        return scheduler;
    }

    public boolean isAllowFriendlyFire() {
        return allowFriendlyFire;
    }

    public void setAllowFriendlyFire(boolean allowFriendlyFire) {
        this.allowFriendlyFire = allowFriendlyFire;
    }

    public RunnerTrackerMenu getRunnerTeleporterMenu() {
        return runnerTrackerMenu;
    }

    public int getTwistVotes(@Nonnull TwistVote type) {
        int i = 0;
        for (GamePlayer gp : players) {
            if (gp.getTwistVoted() == type) i++;
        }
        return i;
    }

    public void sendMessage(@Nullable PlayerType type, @Nonnull String message) {
        List<GamePlayer> receivers = this.players;
        if (type != null) receivers = getPlayers(type);

        for (GamePlayer gp : receivers) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p == null) continue;
            p.sendMessage(message);
        }
    }

    public void sendTitle(@Nullable PlayerType type, @Nonnull String title, @Nonnull String subtitle, int fadeIn, int stay, int fadeOut) {
        List<GamePlayer> receivers = this.players;
        if (type != null) receivers = getPlayers(type);

        for (GamePlayer gp : receivers) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p == null) continue;
            p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public void selectTwist() {
        int amount = 0;
        List<TwistVote> twists = Lists.newArrayList();
        for (TwistVote vote : TwistVote.values()) {
            int curam = getTwistVotes(vote);
            if (curam > amount) {
                amount = curam;
                twists.clear();
                twists.add(vote);
            } else if (curam == amount) {
                twists.add(vote);
            }
        }

        Random random = ThreadLocalRandom.current();
        int r = random.nextInt(twists.size());
        this.selectedTwist = twists.get(r);
    }

    public void setEventActive(boolean eventActive) {
        this.eventActive = eventActive;
    }

    public boolean isEventActive() {
        return eventActive;
    }

    public HeadstartType getHeadStart() {
        return headStart;
    }

    public void setHeadStart(HeadstartType headStart) {
        this.headStart = headStart;
    }

    public void determineNextEventTime() {
        if (this.status == GameStatus.PLAYING) {
            int low = 2; // 8
            int max = 4; // 15
            int random = (int) Math.floor(Math.random()*(max-low+1)+low); // random minute between 8-15.
            this.nextEventTime = this.nextEventTime + (random * 60);
            sendMessage(null,"§aNext twist will occur in §b" + random + " minutes!");
        }
        else if (this.status == GameStatus.WAITING || this.status == GameStatus.STARTING ||
        this.status == GameStatus.LOADING) {
            this.nextEventTime = this.headStart.getSeconds() + (60*2); // 60*5
        }
    }

    public int getNextEventTime() {
        return nextEventTime;
    }
}
