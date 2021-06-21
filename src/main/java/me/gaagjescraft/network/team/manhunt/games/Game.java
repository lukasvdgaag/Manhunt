package me.gaagjescraft.network.team.manhunt.games;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.menus.RunnerTrackerMenu;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Game {

    public static List<Game> games;

    static {
        games = new ArrayList<>();
    }

    private final String identifier;
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
    private boolean dragonDefeated;

    private int bungeeHunterCount;
    private int bungeeRunnerCount;
    private String bungeeServer;
    private UUID hostUUID;
    private boolean ready;

    private Game(String id, boolean twistsAllowed, UUID host, int maxPlayers) {
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
        this.dragonDefeated = false;
        this.bungeeServer = null;
        this.hostUUID = host;
        this.ready = false;

        this.bungeeHunterCount = 0;
        this.bungeeRunnerCount = 0;
        determineNextEventTime();

        GamePlayer gameHost = new GamePlayer(this, host, PlayerType.RUNNER, true);
        if (Manhunt.get().getCfg().bungeeMode) {
            gameHost.setOnline(false);
        }
        this.players.add(gameHost);
        games.add(this);

        if (Manhunt.get().getCfg().bungeeMode && !Manhunt.get().getCfg().isLobbyServer) this.scheduler.start();
    }

    public static Game createGame(boolean twistsAllowed, String host, UUID hostUUID, int maxPlayers) {
        if (getGame(host) != null) return null;
        return new Game(host, twistsAllowed, hostUUID, maxPlayers);
    }

    public static Game createGame(boolean twistsAllowed, Player host, int maxPlayers) {
        if (getGame(host.getName()) != null || getGame(host) != null) return null;
        return new Game(host.getName(), twistsAllowed, host.getUniqueId(), maxPlayers);
    }

    public static Game getGame(Player player) {
        if (player == null) return null;
        List<Game> gms = Lists.newArrayList();
        for (Game g : games) {
            for (GamePlayer gp : g.getPlayers())
                if (gp.getUuid().equals(player.getUniqueId()))
                    gms.add(g);
        }

        for (Game g : gms) {
            if (g.getPlayer(player).isOnline()) return g;
        }
        if (!gms.isEmpty()) return gms.get(ThreadLocalRandom.current().nextInt(gms.size()));
        return null;
    }

    public static List<Game> getGames() {
        return games;
    }

    public static Game getGame(String id) {
        for (Game g : games) if (g.getIdentifier().equalsIgnoreCase(id)) return g;
        return null;
    }

    public int getBungeeHunterCount() {
        return bungeeHunterCount;
    }

    public void setBungeeHunterCount(int bungeeHunterCount) {
        this.bungeeHunterCount = bungeeHunterCount;
    }

    public int getBungeeRunnerCount() {
        return bungeeRunnerCount;
    }

    public void setBungeeRunnerCount(int bungeeRunnerCount) {
        this.bungeeRunnerCount = bungeeRunnerCount;
    }

    public String getBungeeServer() {
        return bungeeServer;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void sendUpdate() {
        Manhunt.get().getUtil().createUpdateGameMessage(this);
    }

    public void setBungeeServer(String bungeeServer) {
        this.bungeeServer = bungeeServer;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isDragonDefeated() {
        return dragonDefeated;
    }

    public void setDragonDefeated(boolean dragonDefeated) {
        this.dragonDefeated = dragonDefeated;
    }

    public void start() {
        this.status = GameStatus.STARTING;
        this.timer = 0;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public UUID getHostUUID() {
        return hostUUID;
    }

    public void setHostUUID(UUID hostUUID) {
        this.hostUUID = hostUUID;
    }

    public boolean addPlayer(Player player) {
        if (!ready) return false;
        GamePlayer gplayer = getPlayer(player);
        int hunterCount = (Manhunt.get().getCfg().bungeeMode && Manhunt.get().getCfg().isLobbyServer) ? bungeeHunterCount : getOnlinePlayers(PlayerType.HUNTER).size();
        if (status == GameStatus.LOADING || status == GameStatus.STOPPING || (hunterCount >= maxPlayers && (gplayer == null || gplayer.isOnline())))
            return false;
        GamePlayer gamePlayer = gplayer;
        if (gamePlayer == null) {
            gamePlayer = new GamePlayer(this, player.getUniqueId(), PlayerType.HUNTER, false);
            players.add(gamePlayer);
        }

        gamePlayer.setOnline(true);

        if (Manhunt.get().getCfg().bungeeMode && Manhunt.get().getCfg().isLobbyServer) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(getBungeeServer());
            player.sendPluginMessage(Manhunt.get(), "BungeeCord", out.toByteArray());
            return true;
        }

        gamePlayer.setScoreboard(null);
        gamePlayer.prepareForGame(getStatus());

        if (!gamePlayer.isFullyDead()) {
            List<GamePlayer> online = getOnlinePlayers(null);
            for (GamePlayer gp : online) {
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p == null) continue;
                p.sendMessage(Util.c(Manhunt.get().getCfg().gameJoinMessage
                        .replaceAll("%prefix%", gamePlayer.getPrefix())
                        .replaceAll("%color%", gamePlayer.getColor())
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%players%", getOnlinePlayers(null).size() + "")
                        .replaceAll("%maxplayers%", (maxPlayers + getOnlinePlayers(PlayerType.RUNNER).size()) + "")));
            }
        } else {
            gamePlayer.prepareForSpectate();
        }

        if (getStatus() == GameStatus.WAITING || getStatus() == GameStatus.STARTING ||
                (getStatus() == GameStatus.PLAYING && gamePlayer.getPlayerType() == PlayerType.HUNTER && getTimer() <= getHeadStart().getSeconds()))
            player.teleport(this.schematic.getSpawnLocation());
        else player.teleport(Bukkit.getWorld("manhunt_" + identifier).getSpawnLocation());

        this.getRunnerTeleporterMenu().update();
        sendUpdate();
        if (Manhunt.get().getTagUtils() != null) Manhunt.get().getTagUtils().updateTag(player);
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
        gamePlayer.setOnline(false);

        if (getStatus() == GameStatus.WAITING || getStatus() == GameStatus.STARTING || getStatus() == GameStatus.STOPPING) {
            player.sendMessage(Util.c(Manhunt.get().getCfg().playerLeftWaitingMessage));
        } else {
            gamePlayer.addDeath();
            player.sendMessage(Util.c(Manhunt.get().getCfg().playerLeftPlayingMessage));
        }

        if (Manhunt.get().getCfg().bungeeMode && !Manhunt.get().getCfg().isLobbyServer) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(Manhunt.get().getCfg().lobbyServerName);
            player.sendPluginMessage(Manhunt.get(), "BungeeCord", out.toByteArray());
        }

        if (!Manhunt.get().getCfg().isLobbyServer) {
            gamePlayer.restoreForLobby();
            gamePlayer.leaveGameDelayed(true);
            if (!Manhunt.get().getCfg().bungeeMode) player.teleport(Manhunt.get().getCfg().lobby);

            if (Manhunt.get().getTagUtils() != null) Manhunt.get().getTagUtils().updateTag(player);

            //this.players.remove(gamePlayer);


            if (getStatus() != GameStatus.STOPPING) {
                if (getStatus() == GameStatus.WAITING || getStatus() == GameStatus.STARTING) {
                    if (gamePlayer.isHost()) {
                        setStatus(GameStatus.WAITING);
                    /*List<GamePlayer> runners = getPlayers(PlayerType.RUNNER);
                    List<GamePlayer> hunters = getPlayers(PlayerType.HUNTER);
                    if (!runners.isEmpty()) {
                        Random random = ThreadLocalRandom.current();
                        int randomInt = random.nextInt(runners.size());
                        newHost = runners.get(randomInt);
                        newHost.setHost(true);
                        newHost.prepareForGame(getStatus());
                    } else if (!hunters.isEmpty()) {
                        Random random = ThreadLocalRandom.current();
                        int randomInt = random.nextInt(hunters.size());
                        newHost = hunters.get(randomInt);
                        newHost.setHost(true);
                        newHost.setPlayerType(PlayerType.RUNNER);
                        newHost.prepareForGame(getStatus());
                        changedHostPlayerType = true;
                    } else {
                        stopGame(false);
                    }*/
                    }
                    if (getOnlinePlayers(null).isEmpty()) {
                        stopGame(false);
                        return;
                    }
                }

                String hostLeaveMessage = Util.c(Manhunt.get().getCfg().gameHostLeftMessage
                        .replaceAll("%prefix%", gamePlayer.getPrefix())
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%color%", gamePlayer.getColor())
                        .replaceAll("%players%", getOnlinePlayers(null).size() + "")
                        .replaceAll("%maxplayers%", (maxPlayers + getOnlinePlayers(PlayerType.RUNNER).size()) + ""));
                String leaveMessage = Util.c(Manhunt.get().getCfg().gameLeftMessage
                        .replaceAll("%prefix%", gamePlayer.getPrefix())
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%color%", gamePlayer.getColor())
                        .replaceAll("%players%", getOnlinePlayers(null).size() + "")
                        .replaceAll("%maxplayers%", (maxPlayers + getOnlinePlayers(PlayerType.RUNNER).size() + "")));

                if (gamePlayer.isHost()) {
                    getScheduler().doHostTransferCountdown();
                }

                for (GamePlayer gp : getOnlinePlayers(null)) {
                    Player p = Bukkit.getPlayer(gp.getUuid());
                    if (p == null) continue;
                    if (getStatus() == GameStatus.WAITING) {
                        if (gamePlayer.isHost()) {
                            p.sendMessage(hostLeaveMessage);
                        /*if (newHost != null)
                            p.sendMessage("§d" + Bukkit.getPlayer(newHost.getUuid()).getName() + " has been assigned as new host!");
                        if (changedHostPlayerType)
                            p.sendMessage("§e" + Bukkit.getPlayer(newHost.getUuid()).getName() + " is the new speed runner!");*/
                        }
                        //return;
                    } else {
                        p.sendMessage(leaveMessage);
                    }
                }
            }
            this.getRunnerTeleporterMenu().update();
            checkForWin(false);
        }
        for (GamePlayer gp : players) {
            gp.updateScoreboard();
        }
        sendUpdate();
    }

    public void checkForWin(boolean forceWin) {
        boolean draw = false;
        if (getStatus() != GameStatus.PLAYING) {
            draw = true;
        }

        List<GamePlayer> runners = getOnlinePlayers(PlayerType.RUNNER);
        List<GamePlayer> hunters = getOnlinePlayers(PlayerType.HUNTER);
        int aliveRunners = 0;
        int aliveHunters = 0;
        for (GamePlayer run : runners) {
            if (run.getDeaths() == 0) aliveRunners++;
        }
        for (GamePlayer hun : hunters) {
            if (hun.getDeaths() < 3) aliveHunters++;
        }

        if (draw) winningTeam = null;
        else if (isDragonDefeated()) winningTeam = PlayerType.RUNNER;
        else if (aliveRunners == 0) winningTeam = PlayerType.HUNTER;
        else if (aliveHunters == 0 || (forceWin && aliveRunners == aliveHunters)) winningTeam = PlayerType.RUNNER;

        if (winningTeam != null || forceWin) win();
    }

    public void win() {
        for (GamePlayer gp : getOnlinePlayers(null)) {
            Player player = Bukkit.getPlayer(gp.getUuid());
            if (player == null) continue;

            gp.updateScoreboard();
            player.closeInventory();

            gp.addGame();

            if (winningTeam == null) {
                for (String s : Manhunt.get().getCfg().gameEndDrawMessage) {
                    player.sendMessage(Util.c(s));
                }
                player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().gameEndDrawSound), 1, 1);
                Util.sendTitle(player, Manhunt.get().getCfg().gameEndDrawTitle, 20, 80, 20);
            } else {
                List<String> msgs = gp.getPlayerType() == winningTeam ? Manhunt.get().getCfg().gameEndWinMessage : Manhunt.get().getCfg().gameEndLoseMessage;
                for (String s : msgs) {
                    player.sendMessage(Util.c(s.replaceAll("%winner%", winningTeam.name())
                            .replaceAll("%player%", player.getName())
                            .replaceAll("%kills%", gp.getKills() + "")));
                }

                if (gp.getPlayerType() == winningTeam) {
                    player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().gameEndWinSound), 1, 1);
                    gp.addWin();
                    if (isDragonDefeated()) {
                        Util.sendTitle(player, Manhunt.get().getCfg().gameEndWinRunnerDragonTitle, 20, 80, 20);
                    } else {
                        if (winningTeam == PlayerType.HUNTER) {
                            Util.sendTitle(player, Manhunt.get().getCfg().gameEndWinHunterTitle, 20, 80, 20);
                        } else {
                            Util.sendTitle(player, Manhunt.get().getCfg().gameEndWinRunnerTitle, 20, 80, 20);
                        }
                    }
                } else {
                    player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().gameEndLoseSound), 1, 1);
                    if (gp.getPlayerType() == PlayerType.HUNTER) {
                        Util.sendTitle(player, Manhunt.get().getCfg().gameEndLoseHunterTitle, 20, 80, 20);
                    } else {
                        Util.sendTitle(player, Manhunt.get().getCfg().gameEndLoseRunnerTitle, 20, 80, 20);
                    }
                }
            }
        }
        sendUpdate();
        if (this.getStatus() != GameStatus.STOPPING) stopGame(false);
    }

    public void stopGame(boolean checkForWin) {
        this.status = GameStatus.STOPPING;
        if (checkForWin && !Manhunt.get().getCfg().isLobbyServer) checkForWin(true);

        if (this.players.size() == 0) {
            delete();
        } else {
            this.scheduler.end();
        }
    }

    public void delete() {
        Location loc = Manhunt.get().getCfg().lobby;

        if (!Manhunt.get().getCfg().isLobbyServer) {
            if (Manhunt.get().getCfg().bungeeMode) Manhunt.get().getUtil().createGameEndedMessage(this);
            for (GamePlayer gp : players) {
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p != null) {
                    if (!Manhunt.get().getCfg().bungeeMode) p.teleport(loc);
                    p.setInvisible(false);
                    p.setFlying(false);
                    p.setAllowFlight(false);
                    for (GamePlayer gp1 : players) {
                        Player p1 = Bukkit.getPlayer(gp1.getUuid());
                        if (p1 == null) continue;
                        p.showPlayer(Manhunt.get(), p1);
                        p1.showPlayer(Manhunt.get(), p);
                    }
                    if (Manhunt.get().getCfg().bungeeMode) {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(Manhunt.get().getCfg().lobbyServerName);
                        p.sendPluginMessage(Manhunt.get(), "BungeeCord", out.toByteArray());
                    }
                    if (Manhunt.get().getTagUtils() != null) Manhunt.get().getTagUtils().updateTag(p);
                }
            }
        } else if (Manhunt.get().getCfg().bungeeMode) {
            // bungeemode is enabled, and server is lobby server.
            Manhunt.get().getUtil().createEndGameMessage(this, true);
            this.players.clear();
            games.remove(this);
            return;
        }

        try {
            World w = Bukkit.getWorld("manhunt_" + identifier);
            World w1 = Bukkit.getWorld("manhunt_" + identifier + "_nether");
            World w2 = Bukkit.getWorld("manhunt_" + identifier + "_the_end");
            if (w != null) {
                Bukkit.unloadWorld(w, false);
                FileUtils.deleteDirectory(w.getWorldFolder());
                if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core"))
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv remove manhunt_" + identifier);
            }
            if (w1 != null) {
                Bukkit.unloadWorld(w1, false);
                FileUtils.deleteDirectory(w1.getWorldFolder());
                if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core"))
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv remove manhunt_" + identifier + "_nether");
            }
            if (w2 != null) {
                Bukkit.unloadWorld(w2, false);
                FileUtils.deleteDirectory(w2.getWorldFolder());
                if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core"))
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv remove manhunt_" + identifier + "_the_end");
            }
        } catch (IOException ignored) {
        }

        this.players.clear();
        games.remove(this);
    }

    public void create() {
        int random = ThreadLocalRandom.current().nextInt(Manhunt.get().getCfg().seeds.size());
        long seed = Manhunt.get().getCfg().seeds.get(random);

        WorldCreator creator = new WorldCreator("manhunt_" + identifier);
        creator.environment(World.Environment.NORMAL);
        creator.seed(seed);
        World wworld = creator.createWorld();

        wworld.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
        wworld.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        wworld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);

        if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core"))
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import manhunt_" + identifier + " NORMAL");

        WorldCreator creatorNether = new WorldCreator("manhunt_" + identifier + "_nether");
        creatorNether.environment(World.Environment.NETHER);
        creatorNether.seed(seed);

        WorldCreator creatorEnd = new WorldCreator("manhunt_" + identifier + "_the_end");
        creatorEnd.environment(World.Environment.THE_END);
        creatorEnd.seed(seed);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Manhunt.get(), () -> {
            World w = creatorNether.createWorld();
            w.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
            w.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
            w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core"))
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import manhunt_" + identifier + "_nether NETHER");
        }, 60L);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Manhunt.get(), () -> {
            World w = creatorEnd.createWorld();
            w.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
            w.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
            w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core"))
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import manhunt_" + identifier + "_the_end THE_END");
        }, 120L);

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
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, isDoDaylightCycle());

            this.schematic.load();

            ready = true;
            setStatus(GameStatus.WAITING);
            Manhunt.get().getUtil().createGameReadyMessage(this);

            for (GamePlayer gp : getOnlinePlayers(null)) {
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p == null) continue;
                p.teleport(this.schematic.getSpawnLocation());
                gp.prepareForGame(getStatus());
                gp.updateScoreboard();

                if (Manhunt.get().getTagUtils() != null) Manhunt.get().getTagUtils().updateTag(p);
            }
            if (Manhunt.get().getCfg().autoJoinOnlinePlayersWhenGameCreated) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (Game.getGame(player) == null) {
                        addPlayer(player);
                    }
                }
            }
        }, 60L);
    }


    public List<GamePlayer> getPlayers() {
        return players;
    }

    public List<GamePlayer> getOnlinePlayers(@Nullable PlayerType type) {
        List<GamePlayer> prs = new ArrayList<>();
        for (GamePlayer gp : players) {
            if ((type == null || gp.getPlayerType() == type) && gp.isOnline()) prs.add(gp);
        }
        return prs;
    }

    public List<GamePlayer> getPlayers(PlayerType type) {
        List<GamePlayer> prs = new ArrayList<>();
        for (GamePlayer gp : players) {
            if (gp.getPlayerType() == type) prs.add(gp);
        }
        return prs;
    }

    public GamePlayer getPlayer(UUID uuid) {
        for (GamePlayer gp : players) {
            if (gp.getUuid().equals(uuid)) return gp;
        }
        return null;
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
        sendUpdate();
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
        World w = getWorld();
        if (w == null) return;
        w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, doDaylightCycle);
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
        if (type != null) receivers = getOnlinePlayers(type);

        for (GamePlayer gp : receivers) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p == null) continue;
            p.sendMessage(message);
        }
    }

    public void sendTitle(@Nullable PlayerType type, @Nonnull String title, @Nonnull String subtitle, int fadeIn, int stay, int fadeOut) {
        List<GamePlayer> receivers = this.players;
        if (type != null) receivers = getOnlinePlayers(type);

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

    public boolean isEventActive() {
        return eventActive;
    }

    public void setEventActive(boolean eventActive) {
        this.eventActive = eventActive;
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
            int random = (int) Math.floor(Math.random() * (max - low + 1) + low); // random minute between 8-15.
            this.nextEventTime = this.nextEventTime + (random * 60);
        } else if (this.status == GameStatus.WAITING || this.status == GameStatus.STARTING ||
                this.status == GameStatus.LOADING) {
            this.nextEventTime = this.headStart.getSeconds() + (60 * 2); // 60*5
        }
    }

    public int getNextEventTime() {
        return nextEventTime;
    }

    public void sendGameAnnouncement() {
        // todo make this configurable.
        // todo add option fr disabling this announcement message.
        if (Manhunt.get().getCfg().isLobbyServer) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 3, 1);
                p.sendMessage("§7§m----------------------------");
                p.spigot().sendMessage(new ComponentBuilder("A new")
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/manhunt join " + getIdentifier()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aClick here to join " + getIdentifier() + "'s game!")))
                        .color(ChatColor.YELLOW)

                        .append(" Custom Manhunt ").color(ChatColor.GOLD)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/manhunt join " + getIdentifier()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aClick here to join " + getIdentifier() + "'s game!")))

                        .append("game is being").color(ChatColor.YELLOW)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/manhunt join " + getIdentifier()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aClick here to join " + getIdentifier() + "'s game!")))

                        .append(" hosted ").color(ChatColor.GREEN)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/manhunt join " + getIdentifier()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aClick here to join " + getIdentifier() + "'s game!")))

                        .append("by ").color(ChatColor.YELLOW)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/manhunt join " + getIdentifier()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aClick here to join " + getIdentifier() + "'s game!")))

                        .append(getIdentifier()).color(ChatColor.AQUA)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/manhunt join " + getIdentifier()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aClick here to join " + getIdentifier() + "'s game!")))

                        .append(". ").color(ChatColor.YELLOW)

                        .append("Click here ").color(ChatColor.LIGHT_PURPLE).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/manhunt join " + getIdentifier()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aClick here to join " + getIdentifier() + "'s game!")))

                        .append("to join!").color(ChatColor.YELLOW).bold(false)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/manhunt join " + getIdentifier()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aClick here to join " + getIdentifier() + "'s game!")))

                        .create());


                p.sendMessage("§7§m----------------------------");
            }
        }
    }

}
