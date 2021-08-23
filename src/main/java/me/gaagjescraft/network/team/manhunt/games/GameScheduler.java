package me.gaagjescraft.network.team.manhunt.games;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameScheduler {

    private Game game;

    GameScheduler(Game game) {
        this.game = game;
    }

    public void start() {
        game.setTimer(0);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (GamePlayer gp : game.getOnlinePlayers(null)) {
                    if ((Bukkit.getPlayer(gp.getUuid()) != null)) {
                        gp.updateScoreboard();
                        if (gp.getTracking() != null) gp.setTracking(gp.getTracking());
                    }
                }

                if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.LOADING || game.getStatus() == GameStatus.STARTING) {
                    World w = game.getWorld();
                    if (w != null) w.setTime(6000);
                }

                // todo add automatic start for minimum amount of players.
                if (game.getStatus() == GameStatus.STARTING) {
                    if (Manhunt.get().getCfg().debug) Bukkit.getLogger().severe("Starting the game now. (1)");
                    doStartingCountdown();
                    if (game.getTimer() == 10) {
                        if (Manhunt.get().getCfg().debug)
                            Bukkit.getLogger().severe("Finished countdown, dropping runners now. (1)");
                        game.setStatus(GameStatus.PLAYING);
                        game.setTimer(0);
                        game.getRunnerTeleporterMenu().update();
                        return;
                    }
                    game.setTimer(game.getTimer() + 1);
                } else if (game.getStatus() == GameStatus.PLAYING) {
                    doHuntersReleaseCountdown();
                    if (game.getTimer() == game.getNextEventTime() && game.isTwistsAllowed()) {
                        doEvent();
                    }

                    if (game.isEventActive() && game.getSelectedTwist() == TwistVote.ACID_RAIN && game.isTwistsAllowed()) {
                        doAcidRainEvent();
                    }

                    game.setTimer(game.getTimer() + 1);
                } else if (game.getStatus() == GameStatus.STOPPING) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Manhunt.get(), 0L, 20L);
    }

    public void end() {
        this.game.setTimer(10);

        if (Manhunt.get().getCfg().bungeeMode) {
            Manhunt.get().getUtil().createEndGameMessage(this.game, false);
        }

        if (!Manhunt.get().getCfg().isLobbyServer) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    List<GamePlayer> online = game.getOnlinePlayers(null);

                    Location loc = Manhunt.get().getCfg().lobby;
                    for (GamePlayer gp : online) {
                        Player player = Bukkit.getPlayer(gp.getUuid());
                        if (player == null) continue;
                        gp.updateScoreboard();
                        player.setLevel(game.getTimer());
                        player.setExp(game.getTimer() > 10 ? 1.0f : (game.getTimer() / 10f));

                        if (game.getTimer() == 0) {
                            player.sendMessage(Util.c(Manhunt.get().getCfg().thanksForPlaying));
                            player.teleport(loc);
                            gp.restoreForLobby();
                        }
                    }

                    if (game.getTimer() == 0) {
                        this.cancel();
                        game.delete();
                        return;
                    }

                    game.setTimer(game.getTimer() - 1);
                }
            }.runTaskTimer(Manhunt.get(), 0, 20L);
        }
    }

    public void doHostTransferCountdown() {
        new BukkitRunnable() {
            int current = 0;

            @Override
            public void run() {
                Player host = Bukkit.getPlayer(game.getHostUUID());
                GamePlayer hostGP = game.getPlayer(game.getHostUUID());
                if (host != null && host.isOnline() && hostGP != null && hostGP.isOnline()) {
                    this.cancel();
                    return;
                }

                if (game.getOnlinePlayers(null).size() == 0) {
                    game.delete();
                    this.cancel();
                    return;
                }

                if (current == 30) { // transferring host after 30 seconds.
                    this.cancel();

                    if (hostGP != null) {
                        hostGP.setHost(false);
                        hostGP.setPlayerType(PlayerType.HUNTER);
                    }

                    GamePlayer newHost;

                    List<GamePlayer> runners = game.getOnlinePlayers(PlayerType.RUNNER);
                    runners.removeIf(GamePlayer::isFullyDead);
                    if (!runners.isEmpty()) { // first checking if there are any runners to make one of them the host
                        ThreadLocalRandom random = ThreadLocalRandom.current();
                        int chosen = random.nextInt(runners.size());

                        newHost = runners.get(chosen);
                    } else { // there are no runners, moving on to the hunters.
                        List<GamePlayer> hunters = game.getOnlinePlayers(PlayerType.HUNTER);
                        ThreadLocalRandom random = ThreadLocalRandom.current();
                        int chosen = random.nextInt(hunters.size());

                        newHost = hunters.get(chosen);
                        newHost.setPlayerType(PlayerType.RUNNER);
                    }

                    newHost.setHost(true);
                    newHost.prepareForGame(game.getStatus());
                    // todo make these messages configurable.

                    Player target = Bukkit.getPlayer(newHost.getUuid());
                    String name = target != null ? target.getName() : "N/A";

                    for (GamePlayer gp : game.getOnlinePlayers(null)) {
                        Player p = Bukkit.getPlayer(gp.getUuid());
                        if (p == null) continue;
                        p.sendMessage(Util.c(Manhunt.get().getCfg().newHostAssignedMessage).replace("%player%", name));
                        Util.sendTitle(p, Util.c(Manhunt.get().getCfg().newHostAssignedTitle).replace("%player%", name), 20, 60, 20);

                        p.playSound(p.getLocation(), Sound.valueOf(Manhunt.get().getCfg().newHostAssignedSound), 1, 1);
                    }

                }

                current++;
            }
        }.runTaskTimer(Manhunt.get(), 0, 20L);

    }

    private void doHuntersReleaseCountdown() {
        int timer = game.getTimer();
        List<GamePlayer> online = game.getOnlinePlayers(null);

        // announce time at 60s, 30s, 10s, <5s
        int headstart = game.getHeadStart().getSeconds();
        if ((headstart >= 120 && timer == headstart - 120) || (headstart >= 90 && timer == headstart - 90) || (headstart >= 60 && timer == headstart - 60) || (headstart >= 30 && timer == headstart - 30) || (headstart >= 10 && timer == headstart - 10) || (timer >= headstart - 5 && timer < headstart)) {
            String time = Manhunt.get().getUtil().secondsToTimeString(headstart - timer, "string");
            for (GamePlayer gp : online) {
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p == null) continue;
                p.sendMessage(Util.c(Manhunt.get().getCfg().hunterReleaseCountdownMessage.replaceAll("%time%", time)));
                if (gp.getPlayerType() == PlayerType.HUNTER) {
                    if (timer > headstart - 6)
                        Util.sendTitle(p, Util.c(Manhunt.get().getCfg().hunterReleaseCountdownTitle.replaceAll("%time%", (game.getHeadStart().getSeconds() - timer) + "")), 5, 10, 5);
                    p.playSound(p.getLocation(), Sound.valueOf(Manhunt.get().getCfg().countdownSound), 1, 1);
                }
            }
        }
        if (timer == game.getHeadStart().getSeconds()) {
            game.getSchematic().unload();

            List<GamePlayer> runners = game.getOnlinePlayers(PlayerType.RUNNER);

            for (GamePlayer gp : online) {
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p == null) continue;
                for (String s : Manhunt.get().getCfg().huntersReleasedMessage) {
                    p.sendMessage(Util.c(s));
                }
                if (gp.getPlayerType() == PlayerType.HUNTER) {
                    Util.sendTitle(p, Util.c(Manhunt.get().getCfg().huntersReleasedHunterTitle), 10, 50, 10);
                } else {
                    Util.sendTitle(p, Util.c(Manhunt.get().getCfg().huntersReleasedRunnerTitle), 10, 50, 10);
                }

                if (gp.getPlayerType() == PlayerType.HUNTER) {
                    gp.prepareForGame(GameStatus.PLAYING);
                }
                p.playSound(p.getLocation(), Sound.valueOf(Manhunt.get().getCfg().huntersReleasedSound), 1, 1);

                if (gp.getPlayerType() == PlayerType.HUNTER) {
                    int random = ThreadLocalRandom.current().nextInt(runners.size());
                    gp.setTracking(runners.get(random));
                }
            }
        }
    }

    private void doStartingCountdown() {
        int timer = game.getTimer();
        if (timer == 10) {
            if (Manhunt.get().getCfg().debug) Bukkit.getLogger().severe("Finished countdown, selecting twist. (1)");
            game.selectTwist();
        }

        for (GamePlayer gp : game.getOnlinePlayers(null)) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p == null) continue;

            if (timer == 0 || (timer >= 5 && timer < 10)) {
                p.sendMessage(Util.c(Manhunt.get().getCfg().startingCountdownMessage.replaceAll("%time%", (10 - timer) + "")));
                Util.sendTitle(p, Util.c(Manhunt.get().getCfg().startingCountdownTitle.replaceAll("%time%", (10 - timer) + "")), 5, 10, 5);
                p.playSound(p.getLocation(), Sound.valueOf(Manhunt.get().getCfg().countdownSound), 1, 1);
            } else if (timer == 10) {
                List<String> msgs = gp.getPlayerType() == PlayerType.HUNTER ? Manhunt.get().getCfg().gameStartHunterMessage : Manhunt.get().getCfg().gameStartRunnerMessage;
                for (String s : msgs) {
                    p.sendMessage(Util.c(s.replaceAll("%time%", Manhunt.get().getUtil().secondsToTimeString(game.getHeadStart().getSeconds(), "string"))
                            .replaceAll("%host%", game.getIdentifier())));
                }

                if (game.isTwistsAllowed()) {
                    for (String s : Manhunt.get().getCfg().twistSelectMessage) {
                        p.sendMessage(Util.c(s.replaceAll("%twist%", game.getSelectedTwist().getDisplayName()).replaceAll("%votes%", game.getTwistVotes(game.getSelectedTwist()) + "")));
                    }
                }

                if (gp.getPlayerType() == PlayerType.HUNTER) {
                    Util.sendTitle(p, Util.c(Manhunt.get().getCfg().gameStartHunterTitle.replaceAll("%time%", Manhunt.get().getUtil().secondsToTimeString(game.getHeadStart().getSeconds(), "string"))), 10, 50, 10);
                    p.getInventory().setItem(0, null); // remove twist vote item
                } else {
                    Util.sendTitle(p, Util.c(Manhunt.get().getCfg().gameStartRunnerTitle.replaceAll("%time%", Manhunt.get().getUtil().secondsToTimeString(game.getHeadStart().getSeconds(), "string"))), 10, 50, 10);
                    gp.prepareForGame(GameStatus.PLAYING);
                    p.teleport(game.getWorld().getSpawnLocation());
                }
                p.playSound(p.getLocation(), Sound.valueOf(Manhunt.get().getCfg().countdownSound), 1, 2);
            }
        }
    }

    public void doEvent() {
        if (game.getSelectedTwist() == TwistVote.RANDOM_YEET) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (GamePlayer gp : game.getOnlinePlayers(null)) {
                Player player = Bukkit.getPlayer(gp.getUuid());
                if (player == null) continue;
                player.sendMessage(Util.c(Manhunt.get().getCfg().twistRandomYeetMessage));
                if (!gp.isDead()) {
                    player.setVelocity(new Vector(random.nextDouble(-5, 5.1), random.nextDouble(1, 2.3), random.nextDouble(-5, 5.1)));
                    Util.sendTitle(player, Util.c(Manhunt.get().getCfg().twistRandomYeetTitle), 20, 50, 20);
                    player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().twistRandomYeetSound), 1, 1);
                }
            }
            game.determineNextEventTime();
        } else if (game.getSelectedTwist() == TwistVote.SPEED_BOOST) {
            game.setEventActive(true);
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int rand = random.nextInt(1,4);

            StringBuilder a = new StringBuilder();
            for (int i = 0; i<rand; i++){
                a.append("I");
            }

            for (GamePlayer gp : game.getOnlinePlayers(null)) {
                Player player = Bukkit.getPlayer(gp.getUuid());
                if (player == null) continue;

                player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().twistSpeedBoostSound), 1, 1);
                Util.sendTitle(player, Util.c(Manhunt.get().getCfg().twistSpeedBoostTitle), 20, 50, 20);
                player.sendMessage(Util.c(Manhunt.get().getCfg().twistSpeedBoostMessage.replaceAll("%strength%", a.toString())));
                if (gp.getPlayerType() == PlayerType.RUNNER && !gp.isFullyDead()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, rand));
                }
            }
            Bukkit.getScheduler().runTaskLater(Manhunt.get(),()->{
                game.setEventActive(false);
                game.determineNextEventTime();
            }, 400);
        } else if (game.getSelectedTwist() == TwistVote.BLINDNESS) {
            game.setEventActive(true);
            for (GamePlayer gp : game.getOnlinePlayers(null)) {
                Player player = Bukkit.getPlayer(gp.getUuid());
                if (player == null) continue;

                player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().twistBlindnessSound), 1, 1);
                Util.sendTitle(player, Util.c(Manhunt.get().getCfg().twistBlindnessTitle), 20, 50, 20);
                player.sendMessage(Util.c(Manhunt.get().getCfg().twistBlindnessMessage));
                if (gp.getPlayerType() == PlayerType.HUNTER && !gp.isFullyDead()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1));
                }
            }
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> {
                game.setEventActive(false);
                game.determineNextEventTime();
            }, 200);
        } else if (game.getSelectedTwist() == TwistVote.ACID_RAIN) {
            game.setEventActive(true);
            game.getWorld().setStorm(true);
            game.getWorld().setThundering(true);
            for (GamePlayer gp : game.getOnlinePlayers(null)) {
                Player player = Bukkit.getPlayer(gp.getUuid());
                if (player == null) continue;
                player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().twistAcidRainSound), 1, 1);
                player.sendMessage(Util.c(Manhunt.get().getCfg().twistAcidRainMessage));
                Util.sendTitle(player, Util.c(Manhunt.get().getCfg().twistAcidRainTitle), 20, 50, 20);
            }

            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> {
                game.sendMessage(null, Util.c(Manhunt.get().getCfg().twistAcidRainEndedMessage));
                game.setEventActive(false);
                game.determineNextEventTime();
                if (game.getWorld() != null) {
                    game.getWorld().setStorm(false);
                    game.getWorld().setThundering(false);
                }
            }, 600);
        } else if (game.getSelectedTwist() == TwistVote.HARDCORE) {
            game.setEventActive(true);
            for (GamePlayer gp : game.getOnlinePlayers(null)) {
                Player player = Bukkit.getPlayer(gp.getUuid());
                if (player == null) continue;
                if (!gp.isFullyDead()) player.setHealth(6);
                player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().twistHardcoreSound), 1, 1);
                player.sendMessage(Util.c(Manhunt.get().getCfg().twistHardcoreMessage));
                Util.sendTitle(player, Util.c(Manhunt.get().getCfg().twistHardcoreTitle), 20, 50, 20);
            }

            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> {
                game.setEventActive(false);
                game.determineNextEventTime();
                for (GamePlayer gp : game.getOnlinePlayers(null)) {
                    Player player = Bukkit.getPlayer(gp.getUuid());
                    if (player == null) continue;
                    player.setHealth(player.getMaxHealth());
                    player.sendMessage(Util.c(Manhunt.get().getCfg().twistHardcoreEndedMessage));
                }
            }, 600);
        }
    }

    public void doAcidRainEvent() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<String> addedLocations = Lists.newArrayList();

        Bukkit.getScheduler().runTaskAsynchronously(Manhunt.get(), () -> {
            for (GamePlayer gp : game.getOnlinePlayers(null)) {
                if (gp.isDead()) continue;
                Player player = Bukkit.getPlayer(gp.getUuid());
                if (player.getWorld().getEnvironment() != World.Environment.NORMAL) continue;

                if (!gp.isFullyDead() && player.getLocation().getY() + 1 > player.getWorld().getHighestBlockYAt(player.getLocation())) {
                    double damage = player.getInventory().getHelmet() != null ? 0.5 : 1.0;
                    Bukkit.getScheduler().runTask(Manhunt.get(), () -> {
                        player.damage(damage);
                        player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, damage));
                    });
                    Manhunt.get().getUtil().spawnAcidParticles(player.getLocation().add(0, 1, 0), true);
                }

                for (int i = 0; i < 30; i++) {
                    int x = random.nextInt(-20, 20);
                    int z = random.nextInt(-20, 20);

                    Location loc = player.getWorld().getHighestBlockAt(player.getLocation().add(x, 0, z)).getLocation();
                    if (!addedLocations.contains(loc.getBlockX() + ":" + loc.getBlockZ())) {
                        addedLocations.add(loc.getBlockX() + ":" + loc.getBlockZ());

                        Manhunt.get().getUtil().spawnAcidParticles(loc, false);
                    }
                }
            }
        });

    }

}
