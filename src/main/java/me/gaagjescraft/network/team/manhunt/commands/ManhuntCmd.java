package me.gaagjescraft.network.team.manhunt.commands;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
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

        if (args.length >= 2 && args[0].equalsIgnoreCase("stop") && p.hasPermission("exodus.event.admin")) {
            // have the ability to stop others' games.
            Game game = Game.getGame(args[1]);
            if (game == null) {
                p.sendMessage(ChatColor.RED + "There's no game with that id.");
                return true;
            }
            if (game.getStatus() == GameStatus.STOPPING) {
                p.sendMessage(ChatColor.RED + "That game is already stopping.");
                return true;
            }
            game.stopGame(true);
            return true;
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("start") && p.hasPermission("exodus.event.admin")) {
            // have the ability to stop others' games.
            Game game = Game.getGame(args[1]);
            if (game == null) {
                p.sendMessage(ChatColor.RED + "There's no game with that id.");
                return true;
            }
            if (game.getStatus() != GameStatus.WAITING) {
                p.sendMessage(ChatColor.RED + "You can only start a loaded game that is waiting for players to join.");
                return true;
            }
            game.start();
            return true;
        }

        Game game = Game.getGame(p);
        if (game == null) {
            Manhunt.get().getManhuntGamesMenu().openMenu(p);
            return true;
        }

        GamePlayer gp = game.getPlayer(p);
        if (gp == null) {
            p.sendMessage(ChatColor.RED + "Something went wrong :/");
            return true;
        }

        if (!gp.isHost()) {
            p.sendMessage(ChatColor.RED + "Nothing for you to see here... just play your game!");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§7§m---------------&r §6§lManHunt §7§m---------------");
            p.sendMessage("§b/manhunt stop §f> §7Stop your current manhunt game.");
            p.sendMessage("§b/manhunt start §f> §7Start your current manhunt game.");
            p.sendMessage("§b/manhunt addrunner <player> §f> §7Make a player a runner.");
            p.sendMessage("§b/manhunt removerunner <player> §f> §7Demote a runner to a hunter.");
            p.sendMessage("§b/manhunt runners §f> §7Get a list of runners.");
            p.sendMessage("§7§m---------------------------------------");
            return true;
        } else {
            if (args[0].equalsIgnoreCase("stop")) {
                p.sendMessage(ChatColor.GREEN + "Stopping your current game...");
                game.stopGame(true);
                return true;
            } else if (args[0].equalsIgnoreCase("start")) {
                p.sendMessage(ChatColor.GREEN + "Starting your current game...");
                game.start();
                return true;
            } else if (args[0].equalsIgnoreCase("addrunner")) {
                if (args.length == 1)
                    p.sendMessage(ChatColor.RED + "Correct usage: /manhunt addrunner <player>");
                else {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        p.sendMessage(ChatColor.RED + "That player is not online.");
                        return true;
                    } else if (target.equals(p)) {
                        p.sendMessage(ChatColor.RED + "That's you, silly!");
                        return true;
                    }

                    GamePlayer targetGP = game.getPlayer(target);
                    if (targetGP == null) {
                        p.sendMessage(ChatColor.RED + "That player is not in your game.");
                    } else if (targetGP.getPlayerType() == PlayerType.RUNNER) {
                        p.sendMessage(ChatColor.RED + "That player is already a runner.");
                    } else {
                        targetGP.setPlayerType(PlayerType.RUNNER);
                        p.sendMessage(ChatColor.GREEN + "You promoted " + target.getName() + " to a runner.");
                    }
                }
                return true;
            } else if (args[0].equalsIgnoreCase("removerunner")) {
                if (args.length == 1)
                    p.sendMessage(ChatColor.RED + "Correct usage: /manhunt removerunner <player>");
                else {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        p.sendMessage(ChatColor.RED + "That player is not online.");
                        return true;
                    } else if (target.equals(p)) {
                        p.sendMessage(ChatColor.RED + "That's you, silly!");
                        return true;
                    }

                    GamePlayer targetGP = game.getPlayer(target);
                    if (targetGP == null) {
                        p.sendMessage(ChatColor.RED + "That player is not in your game.");
                    } else if (targetGP.getPlayerType() != PlayerType.RUNNER) {
                        p.sendMessage(ChatColor.RED + "That player is not a runner.");
                    } else {
                        targetGP.setPlayerType(PlayerType.HUNTER);
                        p.sendMessage(ChatColor.GREEN + "You demoted " + target.getName() + " to a hunter.");
                    }
                }
                return true;
            } else if (args[0].equalsIgnoreCase("runners")) {
                p.sendMessage("§7§m---------------&r §6§lManHunt §7§m---------------");
                p.sendMessage("§bRunners:");
                for (GamePlayer gps : game.getPlayers(PlayerType.RUNNER)) {
                    Player pp = Bukkit.getPlayer(gps.getUuid());
                    p.sendMessage("§7- §e" + pp.getName());
                }
                p.sendMessage("§7§m---------------------------------------");
            } else {
                p.sendMessage("§7§m---------------&r §6§lManHunt §7§m---------------");
                p.sendMessage("§b/manhunt stop §f> §7Stop your current manhunt game.");
                p.sendMessage("§b/manhunt start §f> §7Start your current manhunt game.");
                p.sendMessage("§b/manhunt addrunner <player> §f> §7Make a player a runner.");
                p.sendMessage("§b/manhunt removerunner <player> §f> §7Demote a runner to a hunter.");
                p.sendMessage("§b/manhunt runners §f> §7Get a list of runners.");
                p.sendMessage("§7§m---------------------------------------");
                return true;
            }
        }


        return true;
    }
}
