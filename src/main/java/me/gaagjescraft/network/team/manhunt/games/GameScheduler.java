package me.gaagjescraft.network.team.manhunt.games;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

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
                for (GamePlayer gp : game.getPlayers()) {
                    gp.updateScoreboard();
                    if (gp.getTracking() != null) gp.setTracking(gp.getTracking());
                }

                // todo add automatic start for minimum amount of players.
                if (game.getStatus() == GameStatus.STARTING) {
                    doStartingCountdown();
                    if (game.getTimer() == 10) {
                        game.setStatus(GameStatus.PLAYING);
                        game.setTimer(0);
                        game.getRunnerTeleporterMenu().update();
                        return;
                    }
                    game.setTimer(game.getTimer() + 1);
                } else if (game.getStatus() == GameStatus.PLAYING) {
                    doHuntersReleaseCountdown();
                    if (game.getTimer() == game.getNextEventTime()) {
                        doEvent();
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

        new BukkitRunnable() {
            @Override
            public void run() {
                for (GamePlayer gp : game.getPlayers()) {
                    gp.updateScoreboard();
                }

                Location loc = Manhunt.get().getConfig().getLocation("lobby");
                for (GamePlayer gp : game.getPlayers()) {
                    Player player = Bukkit.getPlayer(gp.getUuid());
                    if (player == null) continue;
                    gp.updateScoreboard();
                    player.setLevel(game.getTimer());
                    player.setExp((game.getTimer()) / 10f);

                    if (game.getTimer() == 0) {
                        player.sendMessage("§bThanks for playing Manhunt on ExodusMC!");
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

    private void doHuntersReleaseCountdown() {
        int timer = game.getTimer();
        String mainTitle = "§a";
        if (timer == game.getHeadStart().getSeconds()-2) {
            mainTitle = "§e";
        } else if (timer == game.getHeadStart().getSeconds()-1) {
            mainTitle = "§c";
        }

        // announce time at 60s, 30s, 10s, <5s
        int headstart = game.getHeadStart().getSeconds();
        if ((headstart >= 120 && timer == headstart-120) || (headstart >= 90 && timer == headstart-90) ||(headstart >= 60 && timer == headstart-60) || (headstart >= 30 && timer == headstart-30) || (headstart >= 10 && timer == headstart-10) || (timer >= headstart-5 && timer < headstart)) {
            for (GamePlayer gp : game.getPlayers()) {
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p == null) continue;
                if (timer < headstart-6) {
                    p.sendMessage("§7Hunters will be released in §e" + Manhunt.get().getUtil().secondsToTimeString(headstart-timer, "string") + "§7!");
                }
                if (gp.getPlayerType() == PlayerType.HUNTER) {
                    if (timer > headstart-6) p.sendTitle(mainTitle + "§l" + (game.getHeadStart().getSeconds() - timer) + "", "§7You will be released shortly!", 5, 10, 5);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                }
            }
        }
        if (timer == game.getHeadStart().getSeconds()) {
            game.getSchematic().unload();

            for (GamePlayer gp : game.getPlayers()) {
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p == null) continue;
                p.sendMessage("§8§m--------------------------");
                p.sendMessage("§cThe hunters have been released from sky!");
                p.sendMessage("§8§m--------------------------");
                if (gp.getPlayerType() == PlayerType.HUNTER) {
                    p.sendTitle("§e§lRELEASED!", "§aYou have been released. Go get 'em!", 10, 50, 10);
                } else {
                    p.sendTitle("§c§lWATCH OUT!", "§eThe hunters have been released!", 10, 50, 10);
                }

                if (gp.getPlayerType() == PlayerType.HUNTER) {
                    gp.prepareForGame(GameStatus.PLAYING);
                }
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
            }
        }
    }

    private void doStartingCountdown() {
        int timer = game.getTimer();
        String mainTitle = "§a";
        if (timer == 8) {
            mainTitle = "§e";
        } else if (timer == 9) {
            mainTitle = "§c";
        }

        if (timer == 10) {
            game.selectTwist();
        }

        for (GamePlayer gp : game.getPlayers()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p == null) continue;

            if (timer == 0 || (timer >= 5 && timer < 10)) {
                p.sendMessage("§eGame is starting in " + mainTitle + (10 - timer) + " seconds!");
                p.sendTitle(mainTitle + "§l" + (10 - timer) + "", "§7Starting soon!", 5, 10, 5);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            } else if (timer == 10) {
                p.sendMessage("§8§m--------------------------");
                p.sendMessage("§aThe game has started!");
                p.sendMessage("§bThe speedrunners will have a §c" + Manhunt.get().getUtil().secondsToTimeString(game.getHeadStart().getSeconds(), "string") + "§b headstart.");
                p.sendMessage(gp.getPlayerType() == PlayerType.HUNTER ? "§bYou will be released in §c" + Manhunt.get().getUtil().secondsToTimeString(game.getHeadStart().getSeconds(), "string") + "§b!" : "§bYou have §c" + Manhunt.get().getUtil().secondsToTimeString(game.getHeadStart().getSeconds(), "string") + "§b to prepare for the hunters!");

                if (game.isTwistsAllowed()) {
                    p.sendMessage("§r");
                    p.sendMessage(null, "§eThe twist §a§l" + game.getSelectedTwist().getDisplayName() + "§e won with §6" + game.getTwistVotes(game.getSelectedTwist()) + " votes§e!");
                }
                p.sendMessage("§8§m--------------------------");

                if (gp.getPlayerType() == PlayerType.HUNTER) {
                    p.sendTitle("§a§lGAME HAS STARTED!", "§bThe speedrunners have a §e"+ Manhunt.get().getUtil().secondsToTimeString(game.getHeadStart().getSeconds(), "string") +"§b headstart.", 10, 50, 10);
                } else {
                    p.sendTitle("§a§lGAME HAS STARTED!", "§bHunters will be released in §e"+Manhunt.get().getUtil().secondsToTimeString(game.getHeadStart().getSeconds(), "string")+"§b!", 10, 50, 10);
                    gp.prepareForGame(GameStatus.PLAYING);
                    p.teleport(game.getWorld().getSpawnLocation());
                }
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            }
        }
    }

    private void doEvent() {
        if (game.getSelectedTwist() == TwistVote.RANDOM_YEET) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (GamePlayer gp : game.getPlayers(PlayerType.HUNTER)) {
                Player player = Bukkit.getPlayer(gp.getUuid());
                if (player==null) continue;
                player.sendMessage("§aIt's time for a random yeet!");
                if (!gp.isDead())  {
                    player.setVelocity(new Vector(random.nextDouble(-5, 5.1), random.nextDouble(1,2.3), random.nextDouble(-5,5.1)));
                    player.sendTitle("§9§lYEET!", "§cYou got yeeted into the air!", 20, 50, 20);
                    player.playSound(player.getLocation(),Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,1,1);
                    Bukkit.getScheduler().runTaskLater(Manhunt.get(), ()->player.stopSound(Sound.ITEM_ELYTRA_FLYING),25L);
                }
            }
            game.determineNextEventTime();
        }
        else if (game.getSelectedTwist() == TwistVote.SPEED_BOOST) {
            game.setEventActive(true);
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int rand = random.nextInt(1,4);

            StringBuilder a = new StringBuilder();
            for (int i=0;i<rand;i++){
                a.append("I");
            }

            for (GamePlayer gp : game.getPlayers()) {
                Player player = Bukkit.getPlayer(gp.getUuid());
                if (player==null) continue;

                player.playSound(player.getLocation(),Sound.ENTITY_FIREWORK_ROCKET_SHOOT,1,1);
                player.sendTitle("§b§lSPEED!", "§aThe runners received speed for §e20 seconds§a!", 20, 50 ,20);
                player.sendMessage("§bSpeed " + a.toString() + " has been applied to the runners for §e20 seconds§b!");
                if (gp.getPlayerType()==PlayerType.RUNNER) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, rand));
                }
            }
            Bukkit.getScheduler().runTaskLater(Manhunt.get(),()->{
                game.setEventActive(false);
                game.determineNextEventTime();
            }, 400);
        }
        else if (game.getSelectedTwist() == TwistVote.BLINDNESS) {
            game.setEventActive(true);
            for (GamePlayer gp : game.getPlayers()) {
                Player player = Bukkit.getPlayer(gp.getUuid());
                if (player==null) continue;

                player.playSound(player.getLocation(),Sound.BLOCK_PORTAL_TRAVEL,0.1f,1f);
                player.sendTitle("§5§lBLINDNESS!", "§cHunters are blinded for §e10 seconds§c!", 20, 50 ,20);
                player.sendMessage("§5All hunters have been blinded for §e10 seconds§5!");
                if (gp.getPlayerType()==PlayerType.HUNTER) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1));
                }
            }
            Bukkit.getScheduler().runTaskLater(Manhunt.get(),()->{
                game.setEventActive(false);
                game.determineNextEventTime();
            }, 200);
        }
    }

}
