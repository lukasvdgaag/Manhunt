package me.gaagjescraft.network.team.manhunt.games;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

        if (timer == (game.getHeadStart().getSeconds()-10) || (timer >= (game.getHeadStart().getSeconds()-5) && timer < game.getHeadStart().getSeconds())) {
            for (GamePlayer gp : game.getPlayers()) {
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p == null) continue;
                //p.sendMessage("§7Hunters will be released in " + mainTitle + (30 - timer) + " seconds!");
                if (gp.getPlayerType() == PlayerType.HUNTER) {
                    p.sendTitle(mainTitle + "§l" + (game.getHeadStart().getSeconds() - timer) + "", "§7You will be released shortly!", 5, 10, 5);
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
                p.sendMessage("§bThe speedrunners will have a §e" + game.getHeadStart().getSeconds() + " sec§b headstart.");
                p.sendMessage(gp.getPlayerType() == PlayerType.HUNTER ? "§bYou will be released in §e" + game.getHeadStart().getSeconds() + " seconds§b!" : "§bYou have §e" + game.getHeadStart().getSeconds() + " seconds§b to prepare for the hunters!");

                if (game.isTwistsAllowed()) {
                    p.sendMessage("§r");
                    p.sendMessage(null, "§eThe twist §a§l" + game.getSelectedTwist().getDisplayName() + "§e won with §6" + game.getTwistVotes(game.getSelectedTwist()) + " votes§e!");
                }
                p.sendMessage("§8§m--------------------------");

                if (gp.getPlayerType() == PlayerType.HUNTER) {
                    p.sendTitle("§a§lGAME HAS STARTED!", "§bThe speedrunners have a §e"+ game.getHeadStart().getSeconds() +" sec§b headstart.", 10, 50, 10);
                } else {
                    p.sendTitle("§a§lGAME HAS STARTED!", "§bHunters will be released in §e"+game.getHeadStart().getSeconds()+" sec§b!", 10, 50, 10);
                    gp.prepareForGame(GameStatus.PLAYING);
                    p.teleport(game.getWorld().getSpawnLocation());
                }
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            }
        }
    }

}
