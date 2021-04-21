package me.gaagjescraft.network.team.manhunt.games;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.utils.AdditionsBoard;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;

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

    public GamePlayer(Game game, UUID uuid, PlayerType playerType, boolean isHost) {
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

        updateScoreboard();
    }

    public void leaveGameDelayed(boolean forceStopScheduler) {
        Player player = Bukkit.getPlayer(uuid);
        if (isLeavingGame() || forceStopScheduler) {
            setLeavingGame(false);
            if (leaveTask != null) {
                leaveTask.cancel();
                if (!forceStopScheduler) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 0.5f);
                    player.sendMessage("§cYou cancelled your §c§lLEAVE§c.");
                }
            }
            return;
        }
        setLeavingGame(true);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2f);
        player.sendMessage("§cYou will §c§lLEAVE§c your game in §e3 seconds§c! Click again to cancel.");
        leaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(uuid);
                setLeavingGame(false);
                if (game == null || player == null) {
                    this.cancel();
                    return;
                }
                game.removePlayer(player);
                player.sendMessage("§cYou left your game.");
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

    public String getPrefix() {
        return playerType == PlayerType.HUNTER ? "§7§lHUNTER§7" : "§e§lRUNNER§e";
    }

    public String getPrefix(boolean includeDead) {
        if (includeDead) {
            String prefix = "";
            if (isFullyDead()) {
                prefix = "§c§lDEAD";
            } else {
                prefix = playerType == PlayerType.HUNTER ? "§7§lHUNTER" : "§e§lRUNNER";
            }
            return prefix + (playerType == PlayerType.HUNTER ? "§7" : "§e");
        }
        return getPrefix();
    }

    public boolean isDead() {
        return isDead;
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
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a§lTRACKING: §e" + track.getName() + " §f- §cDifferent dimension!"));
            return;
        }
        player.setCompassTarget(track.getLocation());
        int distance = (int) player.getLocation().distance(track.getLocation());
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a§lTRACKING: §e" + track.getName() + " §f- §c" + distance + "m away!"));
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
        Bukkit.getPlayer(uuid).sendMessage("§eYou voted for §a§l" + twistVoted.getDisplayName() + "§e!");
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
        Player pp = Bukkit.getPlayer(getUuid());
        for (GamePlayer gp : game.getOnlinePlayers(null)) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p == null) continue;
            if (playerType == PlayerType.RUNNER) {
                p.sendMessage("§e" + pp.getName() + "§a is now a speed runner!");
                p.sendTitle("§e§lNEW RUNNER!", "§b" + pp.getName() + "§a is now a speed runner!", 20, 50, 20);
            } else {
                p.sendMessage("§e" + pp.getName() + "§a is no longer a speed runner!");
            }
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
    }

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

    public void updateHealthTag() {
        boolean go = false;
        if (game.getStatus() == GameStatus.PLAYING) {
            if (isDead) go = false;
            else if (getPlayerType() == PlayerType.RUNNER) go = true;
            else if (getPlayerType() == PlayerType.HUNTER && game.getTimer() < game.getHeadStart().getSeconds())
                go = false;
            else go = true;
        }
        showHealth(go);
    }

    public void showHealth(boolean show) {
        Player player = Bukkit.getPlayer(uuid);
        int health = (int) player.getHealth();

        Scoreboard scoreboard = player.getScoreboard();
        Objective object = scoreboard.getObjective("mh-health");
        if (show) {
            if (object == null) {
                object = scoreboard.registerNewObjective("mh-health", "dummy", "§8(§f" + health + "§8) §6❤");
            }
            object.getScore(player.getName()).setScore(health);
            object.setRenderType(RenderType.HEARTS);
            object.setDisplaySlot(DisplaySlot.BELOW_NAME);
            object.setDisplayName("§c❤");
        } else {
            if (object != null) object.unregister();
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
                player.sendTitle("§e§l" + i, "§aRespawning you soon...", 5, 10, 5);
                World world = Bukkit.getWorld("manhunt_" + game.getIdentifier());

                if (world == null) {
                    this.cancel();
                    return;
                }

                if (i == 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
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
                        player.sendMessage("§aYou respawned at your bed location because you set your spawnpoint!");
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§aYou respawned at your bed location."));
                    } else {
                        // no bed spawn
                        player.setGameMode(GameMode.SURVIVAL);
                        player.teleport(world.getSpawnLocation());
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§aYou respawned at the spawn because you had no bed spawn."));
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
        if (game.isTwistsAllowed() && game.getSelectedTwist() == TwistVote.EXTRA_HEALTH) {
            if (getPlayerType() == PlayerType.HUNTER) {
                player.setMaxHealth(24);
                player.setHealth(24);
            }
            else {
                player.setMaxHealth(40);
                player.setHealth(40);
            }
        }
        else {
            player.setMaxHealth(20);
            player.setHealth(20);
        }
    }

    public void prepareForGame(GameStatus stat) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        reset(player, false);
        updateScoreboard();

        if (stat == GameStatus.WAITING || stat == GameStatus.LOADING || stat == GameStatus.STARTING) {
            player.setGameMode(GameMode.ADVENTURE);
            if (game.isTwistsAllowed()) player.getInventory().setItem(0, Itemizer.MANHUNT_VOTE_ITEM);
            player.getInventory().setItem(8, Itemizer.MANHUNT_LEAVE_ITEM);

            if (isHost) {
                player.getInventory().setItem(4, Itemizer.MANHUNT_HOST_SETTINGS_ITEM);
            }
        } else if (stat == GameStatus.PLAYING) {
            player.setGameMode(GameMode.SURVIVAL);
            if (getPlayerType() == PlayerType.HUNTER) {
                player.getInventory().setItem(8, Itemizer.MANHUNT_RUNNER_TRACKER);
            }
        }
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public void restoreForLobby() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        Manhunt.get().getManhuntGameSetupMenu().gameSetups.remove(player);
        reset(player, true);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);
    }

    public void updateScoreboard() {
        AdditionsBoard board = this.scoreboard;
        int count = 12;
        if (game.getStatus() == GameStatus.LOADING) {
            count = 9;
        } else if (game.getStatus() == GameStatus.WAITING) {
            count = 11;
        } else if (game.getStatus() == GameStatus.STARTING) {
            count = 10;
        } else if (game.getStatus() == GameStatus.PLAYING) {
            if (game.getTimer() <= game.getHeadStart().getSeconds()) {
                count = 11;
            }
        } else if (game.getStatus() == GameStatus.STOPPING) {
            count = 8;
        }

        if (board == null || board.getLinecount() != count) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            board = new AdditionsBoard(player, count);
            board.setTitle("§6§lMANHUNT");
            this.scoreboard = board;
        }

        if (game.getStatus() == GameStatus.LOADING) {
            board.setLine(0, "§fHost: §a" + game.getIdentifier());
            board.setLine(1, "");
            board.setLine(2, "§fYour team: &r" + getPrefix());
            board.setLine(3, "");
            board.setLine(4, "§7Waiting for the game to");
            board.setLine(5, "§7finish loading...");
            board.setLine(6, "§bYou will get teleported soon!");
            board.setLine(7, "");
            board.setLine(8, "§6exodusmc.world");
        } else if (game.getStatus() == GameStatus.WAITING) {
            board.setLine(0, "§fHost: §a" + game.getIdentifier());
            board.setLine(1, "");
            board.setLine(2, "§fSpeed runners: §a" + game.getOnlinePlayers(PlayerType.RUNNER).size());
            board.setLine(3, "§fHunters: §a" + game.getOnlinePlayers(PlayerType.HUNTER).size() + "/" + game.getMaxPlayers());
            board.setLine(4, "");
            board.setLine(5, "§fYour team: &r" + getPrefix());
            board.setLine(6, "");
            board.setLine(7, isHost() ? "§7Waiting for you to" : "§7Waiting for the host to");
            board.setLine(8, "§7start the game.");
            board.setLine(9, "");
            board.setLine(10, "§6exodusmc.world");
        } else if (game.getStatus() == GameStatus.STARTING) {
            board.setLine(0, "§fHost: §a" + game.getIdentifier());
            board.setLine(1, "");
            board.setLine(2, "§fSpeed runners: §a" + game.getOnlinePlayers(PlayerType.RUNNER).size());
            board.setLine(3, "§fHunters: §a" + game.getOnlinePlayers(PlayerType.HUNTER).size() + "/" + game.getMaxPlayers());
            board.setLine(4, "");
            board.setLine(5, "§fYour team: &r" + getPrefix());
            board.setLine(6, "");
            board.setLine(7, (getPlayerType() == PlayerType.RUNNER ? "§7You will drop in §b" : "§7Speed runners will drop in §b") + (10 - game.getTimer()) + "§7 seconds!");
            board.setLine(8, "");
            board.setLine(9, "§6exodusmc.world");
        } else if (game.getStatus() == GameStatus.PLAYING) {
            if (game.getTimer() <= game.getHeadStart().getSeconds()) {
                board.setLine(0, "§fHost: §a" + game.getIdentifier());
                board.setLine(1, "");
                board.setLine(2, "§fSpeed runners: §a" + game.getOnlinePlayers(PlayerType.RUNNER).size());
                board.setLine(3, "§fHunters: §a" + game.getOnlinePlayers(PlayerType.HUNTER).size() + "/" + game.getMaxPlayers());
                board.setLine(4, "");
                board.setLine(5, "§fYour team: &r" + getPrefix());
                board.setLine(6, "");
                board.setLine(7, getPlayerType() == PlayerType.HUNTER ? "§7Speed runners have dropped!" : "§7You have been dropped!");
                board.setLine(8, (getPlayerType() == PlayerType.HUNTER ? "§7You will drop in §b" : "§7Hunters will drop in §b") + Manhunt.get().getUtil().secondsToTimeString(game.getHeadStart().getSeconds() - game.getTimer(), "simplified") + "§7!");
                board.setLine(9, "");
                board.setLine(10, "§6exodusmc.world");
            } else {
                List<GamePlayer> gps = game.getOnlinePlayers(PlayerType.HUNTER);
                List<GamePlayer> rrs = game.getOnlinePlayers(PlayerType.RUNNER);
                gps.removeIf(p -> p.isDead);
                rrs.removeIf(p -> p.isDead);

                board.setLine(0, "§fHost: §a" + game.getIdentifier());
                board.setLine(1, "");
                board.setLine(2, "§fYour team: &r" + getPrefix());
                board.setLine(3, "");
                board.setLine(4, "§fSpeed runners left: §a" + rrs.size());
                board.setLine(5, "§fHunters left: §a" + gps.size() + "/" + game.getMaxPlayers());
                board.setLine(6, "");
                if (getPlayerType() == PlayerType.RUNNER) {
                    if (getDeaths() >= 1) board.setLine(7, "§fLives left: §c§lDEAD!");
                    else board.setLine(7, "§fLives left: §c❤ §f" + (1 - getDeaths()));
                } else {
                    if (getDeaths() >= 3) board.setLine(7, "§fLives left: §c§lDEAD!");
                    else board.setLine(7, "§fLives left: §c❤ §f" + (3 - getDeaths()));
                }
                board.setLine(8, "");
                board.setLine(9, "§fGame time: §a" + Manhunt.get().getUtil().secondsToTimeString(game.getTimer(), "simplified-zeros"));
                board.setLine(10, "");
                board.setLine(11, "§6exodusmc.world");
            }
        } else if (game.getStatus() == GameStatus.STOPPING) {
            PlayerType won = game.getWinningTeam();
            board.setLine(0, won == null ? "§e§lTHIS GAME ENDED IN A DRAW!" : "§d§l" + won.name() + "S WON THE GAME!");
            board.setLine(1, "");
            board.setLine(2, "§fHost: §a" + game.getIdentifier());
            board.setLine(3, won == null ? "§cNo one won this game!" : getPlayerType() == won ? "§aYou won!" : "§cYou lost!");
            board.setLine(4, "");
            board.setLine(5, "§7Teleporting to lobby in §b" + game.getTimer() + "§7 seconds!");
            board.setLine(6, "");
            board.setLine(7, "§6exodusmc.world");
        }

    }

    public Location getBedSpawn() {
        return bedSpawn;
    }

    public void setBedSpawn(Location bedSpawn) {
        this.bedSpawn = bedSpawn;
    }

    public boolean isFullyDead() {
        return ((playerType == PlayerType.HUNTER && deaths >= 3) || (playerType == PlayerType.RUNNER && deaths > 0));
    }

}
