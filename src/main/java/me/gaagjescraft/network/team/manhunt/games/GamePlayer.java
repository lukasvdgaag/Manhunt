package me.gaagjescraft.network.team.manhunt.games;

import com.google.common.collect.Lists;
import io.papermc.lib.PaperLib;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.compass.CompassTracker;
import me.gaagjescraft.network.team.manhunt.inst.PlayerStat;
import me.gaagjescraft.network.team.manhunt.utils.AdditionsBoard;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GamePlayer{

    private final CompassTracker compassTracker;
    private final Game game;
    private final UUID uuid;
    private TwistVote twistVoted;
    private PlayerType playerType;
    private int deaths;
    private boolean isHost;
    private AdditionsBoard scoreboard;
    private int kills;
    private boolean isDead;
    private boolean reachedNether;
    private boolean reachedEnd;
    private boolean isLeavingGame;
    private BukkitTask leaveTask;
    private Location bedSpawn;
    private boolean online;
    private boolean joinedBefore;
    private String username;
    private boolean spectating;
    private int extraLives;

    private Location netherPortal; // portal from overworld -> nether
    private Location overworldPortal; // portal from nether -> overworld
    private Location endPortal; // portal from overworld -> end

    private final Manhunt plugin;

    public GamePlayer(Game game, UUID uuid, PlayerType playerType, boolean isHost, String username) {
        this.plugin = game.getPlugin();
        this.compassTracker = plugin.getPlatformUtils().getCompassTracker(this);
        this.game = game;
        this.uuid = uuid;
        this.twistVoted = null;
        this.playerType = playerType;
        this.deaths = 0;
        this.isHost = isHost;
        this.scoreboard = null;
        this.kills = 0;
        this.isDead = false;
        this.reachedEnd = false;
        this.reachedNether = false;
        this.isLeavingGame = false;
        this.leaveTask = null;
        this.bedSpawn = null;
        this.online = true;
        this.username = username;
        this.joinedBefore = !plugin.getCfg().bungeeMode;
        this.spectating = false;
        this.extraLives = 0;

        this.netherPortal = null;
        this.overworldPortal = null;
        this.endPortal = null;

        if (!plugin.getCfg().isLobbyServer) updateScoreboard();
    }

    public int getExtraLives() {
        return extraLives;
    }

    public void setExtraLives(int extraLives) {
        this.extraLives = extraLives;
    }

    public CompassTracker getCompassTracker() {
        return compassTracker;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isSpectating() {
        return spectating;
    }

    public void setSpectating(boolean spectating) {
        this.spectating = spectating;
    }

    public boolean isJoinedBefore() {
        return joinedBefore;
    }

    public void setJoinedBefore(boolean joinedBefore) {
        this.joinedBefore = joinedBefore;
    }

    public Location getOverworldPortal() {
        return overworldPortal;
    }

    public void setOverworldPortal(Location overworldPortal) {
        this.overworldPortal = overworldPortal;
    }

    public Location getEndPortal() {
        return endPortal;
    }

    public void setEndPortal(Location endPortal) {
        this.endPortal = endPortal;
    }

    public Location getNetherPortal() {
        return netherPortal;
    }

    public void setNetherPortal(Location netherPortal) {
        this.netherPortal = netherPortal;
    }

    public void leaveGameDelayed(boolean forceStopScheduler) {
        Player player = Bukkit.getPlayer(uuid);
        if (isLeavingGame() || forceStopScheduler) {
            setLeavingGame(false);
            if (leaveTask != null) {
                leaveTask.cancel();
                if (!forceStopScheduler) {
                    plugin.getUtil().playSound(player, plugin.getCfg().delayedLeaveCancelSound, 1, .5f);
                    assert player != null;
                    player.sendMessage(Util.c(plugin.getCfg().delayedLeaveCancelMessage));
                }
            }
            return;
        }
        setLeavingGame(true);
        plugin.getUtil().playSound(player, plugin.getCfg().delayedLeaveStartSound, 1, 2);
        assert player != null;
        player.sendMessage(Util.c(plugin.getCfg().delayedLeaveStartMessage));
        leaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!this.isCancelled()) {
                    Player player = Bukkit.getPlayer(uuid);
                    setLeavingGame(false);
                    if (game == null || player == null) {
                        this.cancel();
                        return;
                    }
                    game.removePlayer(player);
                    player.sendMessage(Util.c(plugin.getCfg().playerLeftGameMessage));
                }
            }
        }.runTaskLater(plugin, 60L);
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
        if (!online) this.scoreboard = null;
    }

    public boolean isLeavingGame() {
        return isLeavingGame;
    }

    public void setLeavingGame(boolean leavingGame) {
        isLeavingGame = leavingGame;
    }

    public String getColor() {
        if (isFullyDead() && plugin.getCfg().spectatorsHaveGeneralColor)
            return plugin.getCfg().spectatorChatColor;
        return playerType == PlayerType.HUNTER ? plugin.getCfg().hunterColor : plugin.getCfg().runnerColor;
    }

    public String getPrefix() {
        return playerType == PlayerType.HUNTER ? plugin.getCfg().hunterChatPrefix : plugin.getCfg().runnerChatPrefix;
    }

    public String getPrefix(boolean includeDead) {
        if (includeDead && isFullyDead()) return plugin.getCfg().deadChatPrefix;
        return getPrefix();
    }

    public boolean isDead() {
        return isDead || isSpectating();
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public void setTracking(GamePlayer tracking) {
        setTrackingPlayer(tracking);
        if (tracking == null) return;
        getCompassTracker().updateCompass();
    }

    public boolean isReachedEnd() {
        return reachedEnd;
    }

    public void setReachedEnd(boolean reachedEnd) {
        this.reachedEnd = reachedEnd;
    }

    public boolean isReachedNether() {
        return reachedNether;
    }

    public void setReachedNether(boolean reachedNether) {
        this.reachedNether = reachedNether;
    }

    public AdditionsBoard getScoreboard() {
        return scoreboard;
    }

    public void setScoreboard(AdditionsBoard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Game getGame() {
        return game;
    }

    public TwistVote getTwistVoted() {
        return twistVoted;
    }

    public void setTwistVoted(TwistVote twistVoted) {
        this.twistVoted = twistVoted;
        if (plugin.getCfg().announceTwistVoteToEntireGame) {
            game.sendMessage(null, Util.c(plugin.getCfg().twistVotedMessage
                    .replaceAll("%color%", getColor())
                    .replaceAll("%player%", Objects.requireNonNull(Bukkit.getPlayer(uuid)).getName())
                    .replaceAll("%twist%", twistVoted.getDisplayName())
                    .replaceAll("%votes%", game.getTwistVotes(twistVoted) + "")));
        } else {
            Player p = Bukkit.getPlayer(uuid);
            assert p != null;
            p.sendMessage(Util.c(plugin.getCfg().playerTwistVoteMessage
                    .replaceAll("%color%", getColor())
                    .replaceAll("%player%", p.getName())
                    .replaceAll("%twist%", twistVoted.getDisplayName())
                    .replaceAll("%votes%", game.getTwistVotes(twistVoted) + "")));
        }
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
        OfflinePlayer pp = Bukkit.getPlayer(getUuid());
        if (pp == null) return;
        String name = pp.getName() == null ? "N/A" : pp.getName();
        for (GamePlayer gp : game.getOnlinePlayers(null)) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p == null) continue;
            if (playerType == PlayerType.RUNNER) {
                p.sendMessage(Util.c(plugin.getCfg().runnerAddedMessage.replaceAll("%player%", name)));
                plugin.getUtil().sendTitle(p, Util.c(plugin.getCfg().runnerAddedTitle.replaceAll("%player%", name)), 20, 50, 20);
            } else {
                p.sendMessage(Util.c(plugin.getCfg().runnerRemovedMessage.replaceAll("%player%", name)));
            }
            if (plugin.getTagUtils() != null) plugin.getTagUtils().updateTag(p);
        }
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void addDeath() {
        deaths++;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        kills++;

        Player player = Bukkit.getPlayer(uuid);
        // player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1.5f);
        // player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§d⚔ §e§lKILL: §b+" + credits + " credits §d⚔"));

        plugin.getUtil().performRewardActions(player, game, plugin.getUtil().replace(plugin.getCfg().killRewards, "%kills%", kills + ""));

        PlayerStat stat = plugin.getPlayerStorage().getUser(uuid);
        if (playerType == PlayerType.HUNTER) stat.setHunterKills(stat.getHunterKills() + 1);
        else stat.setRunnerKills(stat.getRunnerKills() + 1);
        plugin.getPlayerStorage().saveUser(uuid);
    }

    public void addLose() {
        Player player = Bukkit.getPlayer(uuid);
        plugin.getUtil().performRewardActions(player, game, (plugin.getCfg().loseRewards));
    }

    public void doTopBonus() {
        for (int i = 0; i < Math.min(3, game.getPlayers().size()); i++) {
            if (game.getPlayers().get(i).getUuid().equals(this.uuid)) {
                // player is on the top 3 of the kills.
                Player player = Bukkit.getPlayer(uuid);
                plugin.getUtil().performRewardActions(player, game, plugin.getUtil().replace(plugin.getUtil().replace(plugin.getCfg().topThreeRewards, "%kills%", kills + ""), "%place%", (i + 1) + ""));
                break;
            }
        }
    }

    public void addWin() {
        int exp = playerType == PlayerType.HUNTER ? 30 : 75;
        int credits = playerType == PlayerType.HUNTER ? 150 : 250;

        Player player = Bukkit.getPlayer(uuid);
        plugin.getUtil().performRewardActions(player, game, plugin.getCfg().winRewards);

        PlayerStat stat = plugin.getPlayerStorage().getUser(uuid);
        if (playerType == PlayerType.HUNTER) stat.setHunterWins(stat.getHunterWins() + 1);
        else stat.setRunnerWins(stat.getRunnerWins() + 1);
        plugin.getPlayerStorage().saveUser(uuid);
    }

    public void addGame() {
        PlayerStat stat = plugin.getPlayerStorage().getUser(uuid);
        if (playerType == PlayerType.HUNTER) stat.setHunterGamesPlayed(stat.getHunterGamesPlayed() + 1);
        else stat.setRunnerGamesPlayed(stat.getRunnerGamesPlayed() + 1);
        plugin.getPlayerStorage().saveUser(uuid);
    }

    public void prepareForSpectate() {
        if (plugin.getCfg().bungeeMode && plugin.getCfg().isLobbyServer) return;
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        player.setInvisible(true);
        reset(player, false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().setItem(4, plugin.getItemizer().MANHUNT_RUNNER_TRACKER);

        PaperLib.teleportAsync(player, game.getWorld().getSpawnLocation().add(0, 20, 0));

        if (isFullyDead()) {
            player.getInventory().setItem(8, plugin.getItemizer().MANHUNT_LEAVE_ITEM);
        }

        for (GamePlayer gp : game.getOnlinePlayers(null)) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p != null && !p.equals(player)) {
                p.hidePlayer(plugin, player);
            }
        }
    }

    public void prepareForRespawn() {
        prepareForSpectate();

        if (isFullyDead()) return;

        new BukkitRunnable() {
            int i = 3;

            @Override
            public void run() {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) return;
                plugin.getUtil().sendTitle(player, Util.c(plugin.getCfg().respawningSoonTitle.replaceAll("%time%", i + "")), 5, 10, 5);
                World world = Bukkit.getWorld(game.getWorldIdentifier());

                if (world == null) {
                    this.cancel();
                    return;
                }

                if (i == 0) {
                    plugin.getUtil().playSound(player, plugin.getCfg().playerRespawnedSound, 1, 1);
                    setDead(false);
                    prepareForGame(game.getStatus());

                    player.setInvisible(false);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    for (GamePlayer gp : game.getOnlinePlayers(null)) {
                        Player p = Bukkit.getPlayer(gp.getUuid());
                        if (p != null && !p.equals(player)) {
                            p.showPlayer(plugin, player);
                        }
                    }

                    if (getBedSpawn() != null && getBedSpawn().getBlock().getType().name().endsWith("BED")) {
                        // player has bed spawn set.
                        player.setGameMode(GameMode.SURVIVAL);
                        PaperLib.teleportAsync(player, getBedSpawn());
                        player.sendMessage(Util.c(plugin.getCfg().respawnedBedMessage));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(plugin.getCfg().respawnedBedActionbar)));
                    } else {
                        // no bed spawn
                        player.setGameMode(GameMode.SURVIVAL);
                        PaperLib.teleportAsync(player, world.getSpawnLocation());
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(plugin.getCfg().respawnedWorldspawnActionbar)));
                    }
                    this.cancel();
                }

                i--;
            }
        }.runTaskTimer(plugin, 40L, 20L);

    }

    private void reset(Player player, boolean removeScoreboard) {
        player.getInventory().clear();
        updateHealth();
        player.setTotalExperience(0);
        player.setFoodLevel(20);
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        player.setBedSpawnLocation(null);
        player.setFireTicks(0);
        for (PotionEffect eft : player.getActivePotionEffects()) {
            player.removePotionEffect(eft.getType());
        }

        if (removeScoreboard) {
            this.scoreboard = null;
            player.setScoreboard(Objects.requireNonNull(plugin.getServer().getScoreboardManager()).getNewScoreboard());
        }
    }

    public void updateHealth() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        int maxHealth = 20;
        int health = 20;

        if (game.isTwistsAllowed() && game.getSelectedTwist() == TwistVote.EXTRA_HEALTH) {
            if (getPlayerType() == PlayerType.HUNTER) {
                maxHealth = 24;
                health = 24;
            } else {
                maxHealth = 40;
                health = 40;
            }
        }

        if (Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue() > maxHealth) {
            player.setHealth(health);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(maxHealth);
        } else {
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(maxHealth);
            player.setHealth(health);
        }

    }

    public void prepareForGame(GameStatus status) {
        if (plugin.getCfg().bungeeMode && plugin.getCfg().isLobbyServer) return;
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        if (status != GameStatus.PLAYING) reset(player, false);
        // Debug game status
        if (plugin.getCfg().debug) plugin.getLogger().info("Debug: Current state of the game: " + status.name());
        updateScoreboard();

        if (isSpectating() || isFullyDead()) {
            prepareForSpectate();
            return;
        }

        if (status == GameStatus.WAITING || status == GameStatus.LOADING || status == GameStatus.STARTING) {
            player.setGameMode(GameMode.ADVENTURE);
            if (game.isTwistsAllowed()) player.getInventory().setItem(0, plugin.getItemizer().MANHUNT_VOTE_ITEM);
            player.getInventory().setItem(8, plugin.getItemizer().MANHUNT_LEAVE_ITEM);

            if (isHost) {
                player.getInventory().setItem(4, plugin.getItemizer().MANHUNT_HOST_SETTINGS_ITEM);
            }
        } else if (status == GameStatus.PLAYING) {
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            if ((getPlayerType() == PlayerType.HUNTER && game.getTimer() >= game.getHeadStart().getSeconds()) ||
                    (getPlayerType() == PlayerType.RUNNER && game.getPlayers(PlayerType.RUNNER).size() > 1)) {
                HashMap<Integer, ItemStack> failedItems = player.getInventory().addItem(plugin.getItemizer().MANHUNT_RUNNER_TRACKER);
                // Successfully added the compass
                if (failedItems.isEmpty()) player.sendMessage(plugin.getCfg().compassGivenMessage);
                    // Failed to add the compass
                else player.sendMessage(plugin.getCfg().compassUnavailableMessage);
            }
        }
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
        if (host) game.setHostUUID(this.uuid);
    }

    public void restoreForLobby() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        plugin.getManhuntGameSetupMenu().gameSetups.remove(player);
        reset(player, true);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(20);
    }

    public void updateScoreboard() {
        AdditionsBoard board = this.scoreboard;

        List<String> lines = Lists.newArrayList();
        int timeLeft = game.getTimer();

        if (game.getStatus() == GameStatus.LOADING) {
            lines = plugin.getCfg().loadingScoreboard;
        } else if (game.getStatus() == GameStatus.WAITING) {
            if (isHost) {
                lines = plugin.getCfg().waitingHostScoreboard;
            } else {
                lines = plugin.getCfg().waitingScoreboard;
            }
        } else if (game.getStatus() == GameStatus.STARTING) {
            if (getPlayerType() == PlayerType.RUNNER) {
                lines = plugin.getCfg().startingRunnerScoreboard;
            } else {
                lines = plugin.getCfg().startingHunterScoreboard;
            }
            timeLeft = 10 - game.getTimer();
        } else if (game.getStatus() == GameStatus.PLAYING) {
            if (game.getTimer() <= game.getHeadStart().getSeconds()) {
                if (getPlayerType() == PlayerType.RUNNER) {
                    lines = plugin.getCfg().playingUnreleasedRunnerScoreboard;
                } else {
                    lines = plugin.getCfg().playingUnreleasedHunterScoreboard;
                }
                timeLeft = game.getHeadStart().getSeconds() - game.getTimer();
            } else {
                lines = plugin.getCfg().playingScoreboard;
                timeLeft = game.getTimer();
            }
        } else if (game.getStatus() == GameStatus.STOPPING) {
            PlayerType won = game.getWinningTeam();
            if (won == null) {
                lines = plugin.getCfg().stoppingDrawScoreboard;
            } else {
                if (getPlayerType() == won) {
                    lines = plugin.getCfg().stoppingWinScoreboard;
                } else {
                    lines = plugin.getCfg().stoppingLoseScoreboard;
                }
            }
            timeLeft = game.getTimer();
        }

        List<GamePlayer> gps = game.getOnlinePlayers(PlayerType.HUNTER);
        final int hunters = gps.size();
        List<GamePlayer> rrs = game.getOnlinePlayers(PlayerType.RUNNER);
        final int runners = rrs.size();
        gps.removeIf(GamePlayer::isFullyDead);
        final int aliveHunters = gps.size();
        rrs.removeIf(GamePlayer::isFullyDead);
        final int aliveRunners = rrs.size();

        if (board == null || board.getLinecount() != lines.size()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            board = new AdditionsBoard(plugin, player, lines.size());
            board.setTitle(Util.c(plugin.getCfg().scoreboardTitle));
            this.scoreboard = board;
        }

        lines = new ArrayList<>(lines);

        for (int i = 0; i < lines.size(); i++) {
            String line = Util.c(lines.get(i));
            line = line.replaceAll("%host%", game.getIdentifier());
            line = line.replaceAll("%prefix%", getPrefix(true));
            line = line.replaceAll("%time%", timeLeft + "");
            line = line.replaceAll("%time_formatted%", plugin.getUtil().secondsToTimeString(timeLeft, "simplified"));
            line = line.replaceAll("%color%", getColor());
            line = line.replaceAll("%winner%", game.getWinningTeam() == null ? "null" : game.getWinningTeam().name());
            line = line.replaceAll("%lives%", getMaxLives() < 1 ? "unlimited" : Math.max(getMaxLives() - getDeaths(), 0) + "");
            line = line.replaceAll("%hunters%", hunters + "");
            line = line.replaceAll("%runners%", runners + "");
            line = line.replaceAll("%alivehunters%", aliveHunters + "");
            line = line.replaceAll("%aliverunners%", aliveRunners + "");
            line = line.replaceAll("%twist%", game.getSelectedTwist() == null ? "none" : game.getSelectedTwist().name());
            line = line.replaceAll("%kills%", getKills() + "");
            line = line.replaceAll("%maxplayers%", game.getMaxPlayers() + "");
            board.setLine(i, line);
        }

    }

    public int getMaxLives() {
        if (getPlayerType() == PlayerType.RUNNER) return 1 + getExtraLives();
        return plugin.getCfg().hunterLives + getExtraLives();
    }

    public Location getBedSpawn() {
        return bedSpawn;
    }

    public void setBedSpawn(Location bedSpawn) {
        this.bedSpawn = bedSpawn;
    }

    public boolean isFullyDead() {
        //if player is spectating, return dead.
        if (spectating) return true;

        if (playerType == PlayerType.HUNTER) {
            // if player is hunter and config says unlimited lives, return false.
            if (getMaxLives() < 1) {
                return false;
            }
            // else return if they died fewer times than they have lives.
            return deaths >= getMaxLives();
        }

        // else player is runner, check if player has died more than once.
        return deaths > 0;
    }

    public void setTrackingPlayer(GamePlayer gamePlayer) {
        getCompassTracker().setTracking(gamePlayer);
    }

}
