package me.gaagjescraft.network.team.manhunt.commands;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ManhuntCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to perform this command");
            return true;
        }
        Player p = (Player) sender;

        if (args.length >= 2 && args[0].equalsIgnoreCase("stop") && p.hasPermission("manhunt.admin")) {
            // have the ability to stop others' games.
            Game game = Game.getGame(args[1]);
            if (game == null) {
                p.sendMessage(Util.c(Manhunt.get().getCfg().invalidGameIdMessage));
                return true;
            }
            if (game.getStatus() == GameStatus.STOPPING) {
                p.sendMessage(Util.c(Manhunt.get().getCfg().gameAlreadyStoppingMessage));
                return true;
            }
            game.stopGame(true);
            return true;
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("start") && p.hasPermission("manhunt.admin")) {
            // have the ability to stop others' games.
            Game game = Game.getGame(args[1]);
            if (game == null) {
                p.sendMessage(Util.c(Manhunt.get().getCfg().invalidGameIdMessage));
                return true;
            }
            if (game.getStatus() != GameStatus.WAITING) {
                p.sendMessage(Util.c(Manhunt.get().getCfg().gameMustBeWaitingMessage));
                return true;
            }
            game.start();
            return true;
        }

        Game game = (Game.getGame(p));
        if (game == null || !game.getPlayer(p).isOnline()) {
            //Manhunt.get().getManhuntGamesMenu().openMenu(p);
            Manhunt.get().getManhuntMainMenu().openMenu(p);
            return true;
        }

        GamePlayer gp = game.getPlayer(p);
        if (gp == null) {
            p.sendMessage(Util.c(Manhunt.get().getCfg().somethingWentWrong));

            return true;
        }

        if (!gp.isHost()) {
            p.sendMessage(Util.c(Manhunt.get().getCfg().commandNoHostMessage));

            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§7§m---------------§r §6§lManhunt §7§m---------------");
            p.sendMessage("§b/manhunt stop §f> §7Stop your current manhunt game.");
            p.sendMessage("§b/manhunt start §f> §7Start your current manhunt game.");
            p.sendMessage("§b/manhunt addrunner <player> §f> §7Make a player a runner.");
            p.sendMessage("§b/manhunt removerunner <player> §f> §7Demote a runner to a hunter.");
            p.sendMessage("§b/manhunt runners §f> §7Get a list of runners.");
            p.sendMessage("§b/manhunt forcetwist §f> §7Force the twist to start.");
            p.sendMessage("§7§m---------------------------------------");
            return true;
        } else {
            if (args[0].equalsIgnoreCase("stop")) {
                p.sendMessage(Util.c(Manhunt.get().getCfg().stoppingGameMessage));
                game.stopGame(true);
                return true;
            } else if (args[0].equalsIgnoreCase("start")) {
                p.sendMessage(Util.c(Manhunt.get().getCfg().startingGameMessage));
                game.start();
                return true;
            } else if (args[0].equalsIgnoreCase("forcetwist")) {
                if (game.getStatus() != GameStatus.PLAYING) {
                    p.sendMessage(Util.c(Manhunt.get().getCfg().gameMustBePlayingMessage));
                    return true;
                }
                if (game.isEventActive()) {
                    p.sendMessage(Util.c(Manhunt.get().getCfg().twistAlreadyActiveMessage));
                    return true;
                }
                p.sendMessage(Util.c(Manhunt.get().getCfg().twistForcedMessage));
                game.getScheduler().doEvent();
                return true;
            } else if (args[0].equalsIgnoreCase("addrunner")) {
                if (args.length == 1)
                    p.sendMessage(ChatColor.RED + "Correct usage: /manhunt addrunner <player>");
                else {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        p.sendMessage(Util.c(Manhunt.get().getCfg().playerNotOnlineMessage));
                        return true;
                    } else if (target.equals(p)) {
                        p.sendMessage(Util.c(Manhunt.get().getCfg().playerIsYouMessage));
                        return true;
                    }

                    GamePlayer targetGP = game.getPlayer(target);
                    if (targetGP == null) {
                        p.sendMessage(Util.c(Manhunt.get().getCfg().targetPlayerNotIngameMessage));
                    } else if (targetGP.getPlayerType() == PlayerType.RUNNER) {
                        p.sendMessage(Util.c(Manhunt.get().getCfg().targetPlayerAlreadyRunnerMessage));
                    } else {
                        targetGP.setPlayerType(PlayerType.RUNNER);
                        game.getRunnerTeleporterMenu().update();
                        p.sendMessage(Util.c(Manhunt.get().getCfg().playerAddRunnerMessage).replaceAll("%player%", target.getName()));
                    }
                }
                return true;
            } else if (args[0].equalsIgnoreCase("removerunner")) {
                if (args.length == 1)
                    p.sendMessage(ChatColor.RED + "Correct usage: /manhunt removerunner <player>");
                else {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        p.sendMessage(Util.c(Manhunt.get().getCfg().playerNotOnlineMessage));
                        return true;
                    } else if (target.equals(p)) {
                        p.sendMessage(Util.c(Manhunt.get().getCfg().playerIsYouMessage));
                        return true;
                    }

                    GamePlayer targetGP = game.getPlayer(target);
                    if (targetGP == null) {
                        p.sendMessage(Util.c(Manhunt.get().getCfg().targetPlayerNotIngameMessage));
                    } else if (targetGP.getPlayerType() != PlayerType.RUNNER) {
                        p.sendMessage(Util.c(Manhunt.get().getCfg().targetPlayerNoRunner));
                    } else {
                        targetGP.setPlayerType(PlayerType.HUNTER);
                        game.getRunnerTeleporterMenu().update();
                        p.sendMessage(Util.c(Manhunt.get().getCfg().playerRemoveRunnerMessage).replaceAll("%player%", target.getName()));
                    }
                }
                return true;
            } else if (args[0].equalsIgnoreCase("runners")) {
                p.sendMessage("§7§m---------------&r §6§lManhunt §7§m---------------");
                p.sendMessage("§bRunners:");
                for (GamePlayer gps : game.getPlayers(PlayerType.RUNNER)) {
                    Player pp = Bukkit.getPlayer(gps.getUuid());
                    p.sendMessage("§7- §e" + pp.getName());
                }
                p.sendMessage("§7§m---------------------------------------");
            } else {
                p.sendMessage("§7§m---------------&r §6§lManhunt §7§m---------------");
                p.sendMessage("§b/manhunt stop §f> §7Stop your current manhunt game.");
                p.sendMessage("§b/manhunt start §f> §7Start your current manhunt game.");
                p.sendMessage("§b/manhunt addrunner <player> §f> §7Make a player a runner.");
                p.sendMessage("§b/manhunt removerunner <player> §f> §7Demote a runner to a hunter.");
                p.sendMessage("§b/manhunt runners §f> §7Get a list of runners.");
                p.sendMessage("§b/manhunt forcetwist If> §7Force the twist to start.");
                p.sendMessage("§7§m---------------------------------------");
                return true;
            }
        }


        return true;
    }
}
