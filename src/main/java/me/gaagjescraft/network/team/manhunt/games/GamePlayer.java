package me.gaagjescraft.network.team.manhunt.games;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.inst.PlayerStat;
import me.gaagjescraft.network.team.manhunt.utils.AdditionsBoard;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GamePlayer {

    private Game game;
    private UUID uuid;
    private TwistVote twistVoted;
    private PlayerType playerType;
    private int deaths;
    private boolean isHost;
    private AdditionsBoard scoreboard;
    private int kills;
    private boolean isDead;
    private GamePlayer tracking;
    private boolean reachedNether;
    private boolean reachedEnd;
    private boolean isLeavingGame;
    private BukkitTask leaveTask;
    private Location bedSpawn;
    private boolean online;
    private boolean joinedBefore;
    private String username;
    private boolean spectating;

    private Location netherPortal; // portal from overworld -> nether
    private Location overworldPortal; // portal from nether -> overworld
    private Location endPortal; // portal from overworld -> end

    public GamePlayer(Game game, UUID uuid, PlayerType playerType, boolean isHost, String username) {
        this.game = game;
        this.uuid = uuid;
        this.twistVoted = null;
        this.playerType = playerType;
        this.deaths = 0;
        this.isHost = isHost;
        this.scoreboard = null;
        this.kills = 0;
        this.isDead = false;
        this.tracking = null;
        this.reachedEnd = false;
        this.reachedNether = false;
        this.isLeavingGame = false;
        this.leaveTask = null;
        this.bedSpawn = null;
        this.online = true;
        this.username = username;
        this.joinedBefore = !Manhunt.get().getCfg().bungeeMode;
        this.spectating = false;

        this.netherPortal = null;
        this.overworldPortal = null;
        this.endPortal = null;

        if (!Manhunt.get().getCfg().isLobbyServer) updateScoreboard();
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
                    player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().delayedLeaveCancelSound), 1, 0.5f);
                    player.sendMessage(Util.c(Manhunt.get().getCfg().delayedLeaveCancelMessage));
                }
            }
            return;
        }
        setLeavingGame(true);
        player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().delayedLeaveStartSound), 1, 2f);
        player.sendMessage(Util.c(Manhunt.get().getCfg().delayedLeaveStartMessage));
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
                    player.sendMessage(Util.c(Manhunt.get().getCfg().playerLeftGameMessage));
                }
            }
        }.runTaskLater(Manhunt.get(), 60L);
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
        if (isFullyDead() && Manhunt.get().getCfg().spectatorsHaveGeneralColor)
            return Manhunt.get().getCfg().spectatorChatColor;
        return playerType == PlayerType.HUNTER ? Manhunt.get().getCfg().hunterColor : Manhunt.get().getCfg().runnerColor;
    }

    public String getPrefix() {
        return playerType == PlayerType.HUNTER ? Manhunt.get().getCfg().hunterChatPrefix : Manhunt.get().getCfg().runnerChatPrefix;
    }

    public String getPrefix(boolean includeDead) {
        if (includeDead && isFullyDead()) return Manhunt.get().getCfg().deadChatPrefix;
        return getPrefix();
    }

    public boolean isDead() {
        return isDead || isSpectating();
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public GamePlayer getTracking() {
        return tracking;
    }

    public void setTracking(GamePlayer tracking) {
        this.tracking = tracking;
        if (tracking == null) return;
        Player player = Bukkit.getPlayer(this.uuid);
        Player track = Bukkit.getPlayer(tracking.getUuid());
        if (track == null || player == null) return;
        if (!track.getWorld().getName().equals(player.getWorld().getName())) {
            if (track.getWorld().getEnvironment() == World.Environment.NETHER && player.getWorld().getEnvironment() == World.Environment.NORMAL && tracking.getNetherPortal() != null) {
                // player is in overworld, tracked player is in the nether.
                player.setCompassTarget(tracking.getNetherPortal());
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(Manhunt.get().getCfg().trackingPortalActionbar.replaceAll("%player%", track.getName()).replaceAll("%color%", tracking.getColor()))));
            } else if (player.getWorld().getEnvironment() == World.Environment.NETHER && track.getWorld().getEnvironment() == World.Environment.NORMAL && tracking.getOverworldPortal() != null) {
                // player is in nether, tracked player is in the overworld.
                player.setCompassTarget(tracking.getOverworldPortal());
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(Manhunt.get().getCfg().trackingPortalActionbar.replaceAll("%player%", track.getName()).replaceAll("%color%", tracking.getColor()))));
            } else if (track.getWorld().getEnvironment() == World.Environment.THE_END && player.getWorld().getEnvironment() == World.Environment.NORMAL && tracking.getEndPortal() != null) {
                // player is in overworld, tracked player is in the end.
                player.setCompassTarget(tracking.getEndPortal());
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(Manhunt.get().getCfg().trackingPortalActionbar.replaceAll("%player%", track.getName()).replaceAll("%color%", tracking.getColor()))));
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(Manhunt.get().getCfg().trackingOtherDimensionActionbar.replaceAll("%player%", track.getName()).replaceAll("%color%", tracking.getColor()))));
            }
            return;
        }

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType() == Material.COMPASS && item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', Manhunt.get().getCfg().generalTrackerDisplayname))) {
                CompassMeta meta = (CompassMeta) item.getItemMeta();
                if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
                    meta.setLodestone(track.getLocation());
                    meta.setLodestoneTracked(false);
                } else {
                    meta.setLodestoneTracked(true);
                    meta.setLodestone(null);
                    player.setCompassTarget(track.getLocation());
                }
                item.setItemMeta(meta);
                // player.getInventory().setItem(i, compass);
                break;
            }
        }

        int distance = (int) player.getLocation().distance(track.getLocation());
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(Manhunt.get().getCfg().trackingActionbar.replaceAll("%player%", track.getName()).replaceAll("%color%", tracking.getColor()).replaceAll("%distance%", distance + ""))));
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
        if (Manhunt.get().getCfg().announceTwistVoteToEntireGame) {
            game.sendMessage(null, Util.c(Manhunt.get().getCfg().twistVotedMessage.replaceAll("%color%", getColor()).replaceAll("%player%", Bukkit.getPlayer(uuid).getName()).replaceAll("%twist%", twistVoted.getDisplayName())));
        } else {
            Player p = Bukkit.getPlayer(uuid);
            p.sendMessage(Util.c(Manhunt.get().getCfg().playerTwistVoteMessage.replaceAll("%color%", getColor()).replaceAll("%player%", p.getName()).replaceAll("%twist%", twistVoted.getDisplayName())));
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
                p.sendMessage(Util.c(Manhunt.get().getCfg().runnerAddedMessage.replaceAll("%player%", name)));
                Util.sendTitle(p, Util.c(Manhunt.get().getCfg().runnerAddedTitle.replaceAll("%player%", name)), 20, 50, 20);
            } else {
                p.sendMessage(Util.c(Manhunt.get().getCfg().runnerRemovedMessage.replaceAll("%player%", name)));
            }
            if (Manhunt.get().getTagUtils() != null) Manhunt.get().getTagUtils().updateTag(p);
        }
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        deaths++;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        kills++;

        int exp = playerType == PlayerType.HUNTER ? 8 : 5;
        int credits = playerType == PlayerType.HUNTER ? 15 : 10;

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1.5f);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§d⚔ §e§lKILL: §b+" + credits + " credits §d⚔"));
            //player.sendMessage("§d+" + credits + " credits §6[kill]");
            player.sendMessage("§b+" + exp + " experience §6[kill]");
            // todo make this configurable.
        }
        if (Manhunt.get().getExodusCociteSupport() != null) {
            Bukkit.getScheduler().runTaskAsynchronously(Manhunt.get(), () -> {
                Manhunt.get().getExodusCociteSupport().addCredits(uuid, credits);
                Manhunt.get().getExodusCociteSupport().addExp(uuid, exp);
            });
        }

        PlayerStat stat = Manhunt.get().getPlayerStorage().getUser(uuid);
        if (playerType == PlayerType.HUNTER) stat.setHunterKills(stat.getHunterKills() + 1);
        else stat.setRunnerKills(stat.getRunnerKills() + 1);
        Manhunt.get().getPlayerStorage().saveUser(uuid);
    }

    public void addLose() {
        int exp = playerType == PlayerType.HUNTER ? 10 : 15;
        int credits = playerType == PlayerType.HUNTER ? 10 : 15;

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            player.sendMessage("§d+" + credits + " credits §6[participation reward]");
            player.sendMessage("§b+" + exp + " experience §6[participation reward]");
            // todo make this configurable.
        }
        if (Manhunt.get().getExodusCociteSupport() != null) {
            Bukkit.getScheduler().runTaskAsynchronously(Manhunt.get(), () -> {
                Manhunt.get().getExodusCociteSupport().addCredits(uuid, credits);
                Manhunt.get().getExodusCociteSupport().addExp(uuid, exp);
            });
        }
    }

    public void doTopBonus() {
        for (int i = 0; i < Math.min(3, game.getPlayers().size()); i++) {
            if (game.getPlayers().get(i).getUuid().equals(this.uuid)) {
                // player is on the top 3 of the kills.
                int exp = 5;
                int credits = 3;
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.sendMessage("§b+" + exp + " experience §d+" + credits + " credits §6[top 3 bonus]");
                    // todo make this configurable.
                }
                if (Manhunt.get().getExodusCociteSupport() != null) {
                    Bukkit.getScheduler().runTaskAsynchronously(Manhunt.get(), () -> {
                        Manhunt.get().getExodusCociteSupport().addExp(uuid, exp);
                        Manhunt.get().getExodusCociteSupport().addCredits(uuid, credits);
                    });
                }
                break;
            }
        }
    }

    public void addWin() {
        int exp = playerType == PlayerType.HUNTER ? 30 : 75;
        int credits = playerType == PlayerType.HUNTER ? 150 : 250;

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            player.sendMessage("§d+" + credits + " credits §6[win]");
            player.sendMessage("§b+" + exp + " experience §6[win]");
            // todo make this configurable.
        }
        if (Manhunt.get().getExodusCociteSupport() != null) {
            Bukkit.getScheduler().runTaskAsynchronously(Manhunt.get(), () -> {
                Manhunt.get().getExodusCociteSupport().addCredits(uuid, credits);
                Manhunt.get().getExodusCociteSupport().addExp(uuid, exp);
            });
        }

        PlayerStat stat = Manhunt.get().getPlayerStorage().getUser(uuid);
        if (playerType == PlayerType.HUNTER) stat.setHunterWins(stat.getHunterWins() + 1);
        else stat.setRunnerWins(stat.getRunnerWins() + 1);
        Manhunt.get().getPlayerStorage().saveUser(uuid);
    }

    public void addGame() {
        PlayerStat stat = Manhunt.get().getPlayerStorage().getUser(uuid);
        if (playerType == PlayerType.HUNTER) stat.setHunterGamesPlayed(stat.getHunterGamesPlayed() + 1);
        else stat.setRunnerGamesPlayed(stat.getRunnerGamesPlayed() + 1);
        Manhunt.get().getPlayerStorage().saveUser(uuid);
    }

    /*public void updateHealthTag() {
        boolean go = false;
        if (game.getStatus() == GameStatus.PLAYING) {
            if (isDead) go = false;
            else if (getPlayerType() == PlayerType.RUNNER) go = true;
            else if (getPlayerType() == PlayerType.HUNTER && game.getTimer() < game.getHeadStart().getSeconds())
                go = false;
            else go = true;
        }
    }*/

    public void prepareForSpectate() {
        Player player = Bukkit.getPlayer(uuid);
        player.setInvisible(true);
        reset(player, false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().setItem(4, Itemizer.MANHUNT_RUNNER_TRACKER);

        player.teleport(game.getWorld().getSpawnLocation().add(0, 20, 0));

        if (isFullyDead()) {
            player.getInventory().setItem(8, Itemizer.MANHUNT_LEAVE_ITEM);
        }

        for (GamePlayer gp : game.getOnlinePlayers(null)) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p != null && !p.equals(player)) {
                p.hidePlayer(Manhunt.get(), player);
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
                Util.sendTitle(player, Util.c(Manhunt.get().getCfg().respawningSoonTitle.replaceAll("%time%", i + "")), 5, 10, 5);
                World world = Bukkit.getWorld("manhunt_" + game.getIdentifier());

                if (world == null) {
                    this.cancel();
                    return;
                }

                if (i == 0) {
                    player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().playerRespawnedSound), 1, 1);
                    setDead(false);
                    prepareForGame(game.getStatus());

                    player.setInvisible(false);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    for (GamePlayer gp : game.getOnlinePlayers(null)) {
                        Player p = Bukkit.getPlayer(gp.getUuid());
                        if (p != null && !p.equals(player)) {
                            p.showPlayer(Manhunt.get(), player);
                        }
                    }

                    if (getBedSpawn() != null && getBedSpawn().getBlock() != null && getBedSpawn().getBlock().getType().name().endsWith("BED")) {
                        // player has bed spawn set.
                        player.setGameMode(GameMode.SURVIVAL);
                        player.teleport(getBedSpawn());
                        player.sendMessage(Util.c(Manhunt.get().getCfg().respawnedBedMessage));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(Manhunt.get().getCfg().respawnedBedActionbar)));
                    } else {
                        // no bed spawn
                        player.setGameMode(GameMode.SURVIVAL);
                        player.teleport(world.getSpawnLocation());
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(Manhunt.get().getCfg().respawnedWorldspawnActionbar)));
                    }
                    this.cancel();
                }

                i--;
            }
        }.runTaskTimer(Manhunt.get(), 40L, 20L);

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
            player.setScoreboard(Manhunt.get().getServer().getScoreboardManager().getNewScoreboard());
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

        if (player.getMaxHealth() > maxHealth) {
            player.setHealth(health);
            player.setMaxHealth(maxHealth);
        } else {
            player.setMaxHealth(maxHealth);
            player.setHealth(health);
        }

    }

    public void prepareForGame(GameStatus stat) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        reset(player, false);
        updateScoreboard();

        if (isSpectating() || isFullyDead()) {
            prepareForSpectate();
            return;
        }

        if (stat == GameStatus.WAITING || stat == GameStatus.LOADING || stat == GameStatus.STARTING) {
            player.setGameMode(GameMode.ADVENTURE);
            if (game.isTwistsAllowed()) player.getInventory().setItem(0, Itemizer.MANHUNT_VOTE_ITEM);
            player.getInventory().setItem(8, Itemizer.MANHUNT_LEAVE_ITEM);

            if (isHost) {
                player.getInventory().setItem(4, Itemizer.MANHUNT_HOST_SETTINGS_ITEM);
            }
        } else if (stat == GameStatus.PLAYING) {
            player.setGameMode(GameMode.SURVIVAL);
            if ((getPlayerType() == PlayerType.HUNTER && game.getTimer() > game.getHeadStart().getSeconds()) || game.getPlayers(PlayerType.RUNNER).size() > 1) {
                player.getInventory().setItem(8, Itemizer.MANHUNT_RUNNER_TRACKER);
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
        Manhunt.get().getManhuntGameSetupMenu().gameSetups.remove(player);
        reset(player, true);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setMaxHealth(20);
    }

    public void updateScoreboard() {
        AdditionsBoard board = this.scoreboard;

        List<String> lines = Lists.newArrayList();
        int timeLeft = game.getTimer();

        if (game.getStatus() == GameStatus.LOADING) {
            lines = Manhunt.get().getCfg().loadingScoreboard;
        } else if (game.getStatus() == GameStatus.WAITING) {
            if (isHost) {
                lines = Manhunt.get().getCfg().waitingHostScoreboard;
            } else {
                lines = Manhunt.get().getCfg().waitingScoreboard;
            }
        } else if (game.getStatus() == GameStatus.STARTING) {
            if (getPlayerType() == PlayerType.RUNNER) {
                lines = Manhunt.get().getCfg().startingRunnerScoreboard;
            } else {
                lines = Manhunt.get().getCfg().startingHunterScoreboard;
            }
            timeLeft = 10 - game.getTimer();
        } else if (game.getStatus() == GameStatus.PLAYING) {
            if (game.getTimer() <= game.getHeadStart().getSeconds()) {
                if (getPlayerType() == PlayerType.RUNNER) {
                    lines = Manhunt.get().getCfg().playingUnreleasedRunnerScoreboard;
                } else {
                    lines = Manhunt.get().getCfg().playingUnreleasedHunterScoreboard;
                }
                timeLeft = game.getHeadStart().getSeconds() - game.getTimer();
            } else {
                lines = Manhunt.get().getCfg().playingScoreboard;
                timeLeft = game.getTimer();
            }
        } else if (game.getStatus() == GameStatus.STOPPING) {
            PlayerType won = game.getWinningTeam();
            if (won == null) {
                lines = Manhunt.get().getCfg().stoppingDrawScoreboard;
            } else {
                if (getPlayerType() == won) {
                    lines = Manhunt.get().getCfg().stoppingWinScoreboard;
                } else {
                    lines = Manhunt.get().getCfg().stoppingLoseScoreboard;
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
            board = new AdditionsBoard(player, lines.size());
            board.setTitle(Util.c(Manhunt.get().getCfg().scoreboardTitle));
            this.scoreboard = board;
        }

        lines = new ArrayList<>(lines);

        for (int i = 0; i < lines.size(); i++) {
            String line = Util.c(lines.get(i));
            line = line.replaceAll("%host%", game.getIdentifier());
            line = line.replaceAll("%prefix%", getPrefix(true));
            line = line.replaceAll("%time%", timeLeft + "");
            line = line.replaceAll("%time_formatted%", Manhunt.get().getUtil().secondsToTimeString(timeLeft, "simplified"));
            line = line.replaceAll("%color%", getColor());
            line = line.replaceAll("%winner%", game.getWinningTeam() == null ? "null" : game.getWinningTeam().name());
            line = line.replaceAll("%lives%", Math.max(getMaxLives() - getDeaths(), 0) + "");
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
        if (getPlayerType() == PlayerType.RUNNER) return 1;
        return 3;
    }

    public Location getBedSpawn() {
        return bedSpawn;
    }

    public void setBedSpawn(Location bedSpawn) {
        this.bedSpawn = bedSpawn;
    }

    public boolean isFullyDead() {
        return ((spectating) || (playerType == PlayerType.HUNTER && deaths >= 3) || (playerType == PlayerType.RUNNER && deaths > 0));
    }

}
