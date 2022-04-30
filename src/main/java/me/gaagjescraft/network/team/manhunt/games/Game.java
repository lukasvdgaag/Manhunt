package me.gaagjescraft.network.team.manhunt.games;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.events.custom.GameCreationEvent;
import me.gaagjescraft.network.team.manhunt.events.custom.GameJoinEvent;
import me.gaagjescraft.network.team.manhunt.events.custom.GameLeaveEvent;
import me.gaagjescraft.network.team.manhunt.events.custom.GameRemovalEvent;
import me.gaagjescraft.network.team.manhunt.menus.RunnerTrackerMenu;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;


public class Game {

    public static List<Game> games;

    static {
        games = new ArrayList<>();
    }

    private final String identifier;
    private final String worldIdentifier;
    private final List<GamePlayer> players;
    private final List<UUID> spectators;
    private GameSchematic schematic;
    private final GameScheduler scheduler;
    private final RunnerTrackerMenu runnerTrackerMenu;
    private boolean twistsAllowed;
    private boolean doDaylightCycle;
    private TwistVote selectedTwist;
    private int maxPlayers;
    private PlayerType winningTeam;
    private GameStatus status;
    private int timer;
    private boolean allowFriendlyFire;
    private int nextEventTime;
    private boolean eventActive;
    private HeadstartType headStart;
    private boolean dragonDefeated;

    private long seed;
    private int bungeeHunterCount;
    private int bungeeRunnerCount;
    private String bungeeServer;
    private UUID hostUUID;
    private boolean ready;

    public Game(String id, boolean twistsAllowed, UUID host, int maxPlayers) {
        this.identifier = id;
        this.worldIdentifier = "manhunt_" + (Manhunt.get().getCfg().useUuidsAsWorldNames ? host.toString() : id);
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
        this.spectators = new ArrayList<>();

        this.bungeeHunterCount = 0;
        this.bungeeRunnerCount = 0;
        determineNextEventTime();

        GamePlayer gameHost = new GamePlayer(this, host, PlayerType.RUNNER, true, null);
        if (Manhunt.get().getCfg().bungeeMode) {
            gameHost.setOnline(false);
        }

        Player p = Bukkit.getPlayer(host);
        if (p != null) gameHost.setUsername(p.getName());

        this.players.add(gameHost);
        games.add(this);

        if (!Manhunt.get().getCfg().bungeeMode || !Manhunt.get().getCfg().isLobbyServer) this.scheduler.start();
    }

    public static Game getGame(UUID worldId) {
        for (Game g : games) if (g.getWorldIdentifier().equals(worldId.toString()) || (g.getHostUUID() != null && g.getHostUUID().equals(worldId))) return g;
        return null;
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
        for (Game g : games)
            if (g.getIdentifier().equalsIgnoreCase(id) || g.getWorldIdentifier().equals("manhunt_" + id)) return g;
        return null;
    }

    public void addSpectator(UUID uuid) {
        this.spectators.add(uuid);
    }

    public void removeSpectator(UUID uuid) {
        this.spectators.remove(uuid);
    }

    public List<UUID> getSpectators() {
        return spectators;
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

    public void setBungeeServer(String bungeeServer) {
        this.bungeeServer = bungeeServer;
    }

    public void sendUpdate() {
        if (Manhunt.get().getBungeeSocketManager() == null) return;
        Manhunt.get().getBungeeMessenger().createUpdateGameMessage(this);
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
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
        if (!spectators.contains(player.getUniqueId()) || (gplayer != null && !gplayer.isSpectating())) {
            if (status == GameStatus.LOADING || status == GameStatus.STOPPING || (hunterCount >= maxPlayers && (gplayer == null || gplayer.isOnline())))
                return false;
        }

        GameJoinEvent joinEvent = new GameJoinEvent(player, this);
        Bukkit.getPluginManager().callEvent(joinEvent);
        if (joinEvent.isCancelled()) return false;

        GamePlayer gamePlayer = gplayer;
        if (gamePlayer == null) {
            gamePlayer = new GamePlayer(this, player.getUniqueId(), PlayerType.HUNTER, false, player.getName());
            players.add(gamePlayer);
        }

        gamePlayer.setUsername(player.getName());
        gamePlayer.setOnline(true);
        if (spectators.contains(player.getUniqueId())) {
            gamePlayer.setSpectating(true);
            // Verify players not invisible to spec
            player.spigot().getHiddenPlayers().forEach(p -> {
                if (!getPlayer(p).isSpectating()) {
                    player.showPlayer(Manhunt.get(), p);
                }
            });
        }

        if (Manhunt.get().getCfg().bungeeMode && Manhunt.get().getCfg().isLobbyServer) {
            if (gamePlayer.isSpectating()) {
                Manhunt.get().getBungeeMessenger().createAddSpectatorMessage(this, player.getUniqueId());
            }
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
            // Save the instance for performance
            Manhunt manhuntInstance = Manhunt.get();
            for (GamePlayer gp : online) {
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p == null) continue;
                // Verify that players can see each other if not in spec
                p.showPlayer(manhuntInstance, player);
                if (!gp.isSpectating()) player.showPlayer(manhuntInstance, p);
                // Send join msg
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
        else player.teleport(Objects.requireNonNull(Bukkit.getWorld(getWorldIdentifier())).getSpawnLocation());

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
        Bukkit.getPluginManager().callEvent(new GameLeaveEvent(player, this));

        Manhunt.get().getManhuntGameSetupMenu().gameSetups.remove(player);
        gamePlayer.setOnline(false);

        if (!gamePlayer.isFullyDead()) {
            if (getStatus() == GameStatus.WAITING || getStatus() == GameStatus.STARTING || getStatus() == GameStatus.STOPPING) {
                player.sendMessage(Util.c(Manhunt.get().getCfg().playerLeftWaitingMessage));
            } else {
                gamePlayer.addDeath();
                player.sendMessage(Util.c(Manhunt.get().getCfg().playerLeftPlayingMessage));
            }
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
                        stopGame(false, true);
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
        boolean draw = getStatus() != GameStatus.PLAYING;

        List<GamePlayer> runners = getOnlinePlayers(PlayerType.RUNNER);
        List<GamePlayer> hunters = getOnlinePlayers(PlayerType.HUNTER);
        int aliveRunners = 0;
        int aliveHunters = 0;
        for (GamePlayer run : runners) {
            if (!run.isFullyDead()) aliveRunners++;
        }
        for (GamePlayer hun : hunters) {
            if (!hun.isFullyDead()) aliveHunters++;
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
                Util.playSound(player, Manhunt.get().getCfg().gameEndDrawSound, 1, 1);
                Util.sendTitle(player, Manhunt.get().getCfg().gameEndDrawTitle, 20, 80, 20);
            } else {
                List<String> msgs = gp.getPlayerType() == winningTeam ? Manhunt.get().getCfg().gameEndWinMessage : Manhunt.get().getCfg().gameEndLoseMessage;
                determineKillOrder();

                String firstKillerName = players.size() >= 1 ? players.get(0).getUsername() : null;
                String secondKillerName = players.size() >= 2 ? players.get(1).getUsername() : null;
                String thirdKillerName = players.size() >= 3 ? players.get(2).getUsername() : null;

                int firstKillerNumber = players.size() >= 1 ? players.get(0).getKills() : 0;
                int secondKillerNumber = players.size() >= 2 ? players.get(1).getKills() : 0;
                int thirdKillerNumber = players.size() >= 3 ? players.get(2).getKills() : 0;

                for (String s : msgs) {

                    if (s.contains("%first_killer%") && firstKillerName == null) continue;
                    else if (s.contains("%second_killer%") && secondKillerName == null) continue;
                    else if (s.contains("%third_killer%") && thirdKillerName == null) continue;

                    if (s.isEmpty() || s.trim().isEmpty()) player.sendMessage("");
                    else Util.sendCenteredMessage(player, Util.c(s.replaceAll("%winner%", winningTeam.name())
                            .replaceAll("%player%", player.getName())
                            .replaceAll("%first_killer%", firstKillerName == null ? "N/A" : firstKillerName)
                            .replaceAll("%second_killer%", secondKillerName == null ? "N/A" : secondKillerName)
                            .replaceAll("%third_killer%", thirdKillerName == null ? "N/A" : thirdKillerName)
                            .replaceAll("%first_killer_number%", firstKillerNumber + "")
                            .replaceAll("%second_killer_number%", secondKillerNumber + "")
                            .replaceAll("%third_killer_number%", thirdKillerNumber + "")
                            .replaceAll("%kills%", gp.getKills() + "")));
                }

                if (gp.getPlayerType() == winningTeam) {
                    Util.playSound(player, Manhunt.get().getCfg().gameEndWinSound, 1, 1);
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
                    gp.addLose(); // doesn't actually save a lose, but is for rewards.
                    Util.playSound(player, Manhunt.get().getCfg().gameEndLoseSound, 1, 1);
                    if (gp.getPlayerType() == PlayerType.HUNTER) {
                        Util.sendTitle(player, Manhunt.get().getCfg().gameEndLoseHunterTitle, 20, 80, 20);
                    } else {
                        Util.sendTitle(player, Manhunt.get().getCfg().gameEndLoseRunnerTitle, 20, 80, 20);
                    }
                }

                gp.doTopBonus();

            }
        }
        sendUpdate();
        if (this.getStatus() != GameStatus.STOPPING) stopGame(false);
    }

    public void stopGame(boolean checkForWin, boolean refundToken) {
        if (refundToken && (!Manhunt.get().getCfg().bungeeMode || Manhunt.get().getCfg().isLobbyServer)
                && (this.status == GameStatus.WAITING || this.status == GameStatus.STARTING)) {
            OfflinePlayer p = Bukkit.getPlayer(hostUUID);
            if (p != null && p.getName() != null && p.getName().equalsIgnoreCase(this.identifier)) {
                if (p.isOnline() && p.getPlayer() != null) {
                    if (Manhunt.get().getEconomy() != null && p.getPlayer().getUniqueId().equals(hostUUID)) {
                        // refunding spent money
                        Manhunt.get().getEconomy().addBalance(p.getPlayer(), Manhunt.get().getCfg().pricePerGame);
                        p.getPlayer().sendMessage(Util.c(Manhunt.get().getCfg().moneyRefundedMessage.replaceAll("%price%", Manhunt.get().getCfg().pricePerGame + "")));
                    }
                }
            }
        }
        stopGame(checkForWin);
    }

    public void stopGame(boolean checkForWin) {
        this.status = GameStatus.STOPPING;
        if (checkForWin && !Manhunt.get().getCfg().isLobbyServer) checkForWin(true);

        if (getOnlinePlayers(null).size() == 0) {
            delete();
        } else {
            this.scheduler.end();
        }
    }

    public void delete() {
        Location loc = Manhunt.get().getCfg().lobby;

        Bukkit.getPluginManager().callEvent(new GameRemovalEvent(this));

        if (!Manhunt.get().getCfg().isLobbyServer) {
            for (GamePlayer gp : players) {
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p != null) {
                    if (!Manhunt.get().getCfg().bungeeMode) p.teleport(loc);
                    else {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(Manhunt.get().getCfg().lobbyServerName);
                        p.sendPluginMessage(Manhunt.get(), "BungeeCord", out.toByteArray());
                    }
                    p.setInvisible(false);
                    p.setFlying(false);
                    p.setAllowFlight(false);

                    p.spigot().getHiddenPlayers().forEach(p1 -> p.showPlayer(Manhunt.get(), p1));

                    if (Manhunt.get().getTagUtils() != null) Manhunt.get().getTagUtils().updateTag(p);
                }
            }
            if (Manhunt.get().getCfg().bungeeMode) Manhunt.get().getBungeeMessenger().createGameEndedMessage(this);
        } else if (Manhunt.get().getCfg().bungeeMode) {
            // bungeemode is enabled, and server is lobby server.
            Manhunt.get().getBungeeMessenger().createEndGameMessage(this, true);
            this.players.clear();
            games.remove(this);
            return;
        }

        //new method
        Manhunt.getWorldUtil().delete(getWorldIdentifier());
        Manhunt.getWorldUtil().delete(getWorldIdentifier()+ "_nether");
        Manhunt.getWorldUtil().delete(getWorldIdentifier() + "_the_end");

        this.players.clear();
        games.remove(this);

        if (Manhunt.get().getCfg().stopServerAfterGame) {
            Bukkit.shutdown();
        }
    }

    public void create() {
        int random = ThreadLocalRandom.current().nextInt(Manhunt.get().getCfg().seeds.size());
        long seed = Manhunt.get().getCfg().seeds.get(random);
        this.seed = seed;

        World w = Manhunt.getWorldUtil().create(seed, getWorldIdentifier(), World.Environment.NORMAL);

            Bukkit.getScheduler().scheduleSyncDelayedTask(Manhunt.get(), () -> {
                World wnet = Manhunt.getWorldUtil().create(seed, getWorldIdentifier() + "_nether", World.Environment.NETHER);
            }, 60L);

            Bukkit.getScheduler().scheduleSyncDelayedTask(Manhunt.get(), () -> {
                World wend = Manhunt.getWorldUtil().create(seed, getWorldIdentifier() + "_the_end", World.Environment.THE_END);
            }, 120L);



        if(Bukkit.getPluginManager().isPluginEnabled("Chunky")){
            Location spawn = Objects.requireNonNull(Bukkit.getWorld(getWorldIdentifier())).getSpawnLocation();
            double x = spawn.getBlockX();
            double z = spawn.getBlockZ();
            Manhunt.get().getChunkHook().chunkgen(x, z, Objects.requireNonNull(Bukkit.getWorld(getWorldIdentifier())).getName());
        }

        //load schematic and prepare game
        Future<Object> part2 = Bukkit.getScheduler().callSyncMethod(Manhunt.get(), () -> {
            Bukkit.getServer().getLogger().info("loading up the schematicz;");
            this.schematic.load();
            Bukkit.getServer().getLogger().info("Done loading up the schematicz");

            ready = true;
            setStatus(GameStatus.WAITING);
            Manhunt.get().getBungeeMessenger().createGameReadyMessage(this);

            Bukkit.getPluginManager().callEvent(new GameCreationEvent(this));

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
            return null;
        });

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

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
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
        return Bukkit.getWorld(getWorldIdentifier());
    }

    public World getNether(){
        return Bukkit.getWorld(getWorldIdentifier() + "_nether");
    }

    public World getEnd(){
        return Bukkit.getWorld(getWorldIdentifier() + "_the_end");
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
        if (Manhunt.get().getCfg().isLobbyServer && Manhunt.get().getCfg().sendGameHostAnnouncement) {
            List<Player> players = Manhunt.get().getCfg().sendGameHostAnnouncementToLobbyOnly ? Objects.requireNonNull(Manhunt.get().getCfg().lobby.getWorld()).getPlayers() : new ArrayList<>(Bukkit.getOnlinePlayers());

            for (Player p : players) {
                Util.playSound(p, Manhunt.get().getCfg().gameHostAnnouncementSound, 3, 1);
                p.spigot().sendMessage(ChatMessageType.CHAT, ComponentSerializer.parse(Manhunt.get().getCfg().gameHostAnnouncementMessage.replaceAll("%game%", getIdentifier())));
            }
        }
    }

    private void determineKillOrder() {
        players.sort(Comparator.comparing(GamePlayer::getKills).reversed());
    }

    public String getWorldIdentifier() {
        return worldIdentifier;
    }

    public long getSeed(){return seed;}
}