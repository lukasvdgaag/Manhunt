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

import java.util.List;

public class ManhuntCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to perform this command");
            return true;
        }
        Player p = (Player) sender;

        if (args.length >= 1 && args[0].equalsIgnoreCase("setspawn") && p.hasPermission("manhunt.setspawn")) {
            Manhunt.get().getCfg().lobby = p.getLocation();
            Manhunt.get().getCfg().save();
            p.sendMessage(Util.c(Manhunt.get().getCfg().lobbySetMessage));
            return true;
        }

        if (Manhunt.get().getCfg().lobby == null) {
            p.sendMessage(ChatColor.RED + "Please set the lobby spawn before you can host/join Manhunt games.");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("join") && (!Manhunt.get().getCfg().bungeeMode || Manhunt.get().getCfg().isLobbyServer)) {
            if (args.length == 1)
                p.sendMessage(ChatColor.RED + "Correct usage: /manhunt join <game-id>");
            else {
                Game target = Game.getGame(args[1]);
                if (target == null)
                    p.sendMessage(Util.c(Manhunt.get().getCfg().invalidGameIdMessage));
                else {
                    if (!target.addPlayer(p)) {
                        p.sendMessage(Util.c(Manhunt.get().getCfg().joinFailMessage.replace("%game%", target.getIdentifier())));
                    }
                }
            }
            return true;
        }

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
            game.stopGame(true, true);
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
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("revive") && p.hasPermission("manhunt.admin")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Util.c(Manhunt.get().getCfg().playerNotOnlineMessage));
                return true;
            }

            Game game = Game.getGame(target);
            if (game == null) {
                sender.sendMessage(Util.c(Manhunt.get().getCfg().targetPlayerNotIngameMessage.replace("%player%", target.getName())));
                return true;
            }
            GamePlayer gp = game.getPlayer(target);
            gp.setDeaths(Math.min(0, gp.getDeaths() - 1));
            gp.prepareForRespawn();
            sender.sendMessage(Util.c(Manhunt.get().getCfg().revivedPlayerMessage.replace("%player%", target.getName())));
            return true;
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("addlife") && p.hasPermission("manhunt.admin")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Util.c(Manhunt.get().getCfg().playerNotOnlineMessage));
                return true;
            }

            Game game = Game.getGame(target);
            if (game == null || !game.getPlayer(target).isOnline()) {
                sender.sendMessage(Util.c(Manhunt.get().getCfg().targetPlayerNotIngameMessage.replace("%player%", target.getName())));
                return true;
            }
            GamePlayer gp = game.getPlayer(target);

            int amount = 1;
            if (args.length >= 3 && Manhunt.get().getUtil().isInt(args[2])) {
                amount = Integer.parseInt(args[2]);
            }

            gp.setExtraLives(gp.getExtraLives() + amount);
            if (gp.isFullyDead()) {
                gp.prepareForRespawn();
            }
            sender.sendMessage(Util.c(Manhunt.get().getCfg().addLifeMessage
                    .replace("%player%", target.getName())
                    .replace("%added%", amount + "")));
            return true;

        } else if (args.length >= 2 && args[0].equalsIgnoreCase("removelife") && p.hasPermission("manhunt.admin")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Util.c(Manhunt.get().getCfg().playerNotOnlineMessage));
                return true;
            }

            Game game = Game.getGame(target);
            if (game == null || !game.getPlayer(target).isOnline()) {
                sender.sendMessage(Util.c(Manhunt.get().getCfg().targetPlayerNotIngameMessage.replace("%player%", target.getName())));
                return true;
            }
            GamePlayer gp = game.getPlayer(target);

            int amount = 1;
            if (args.length >= 3 && Manhunt.get().getUtil().isInt(args[2])) {
                amount = Integer.parseInt(args[2]);
            }

            int extraLives = gp.getExtraLives();
            if (extraLives - amount > 0) {
                gp.setExtraLives(extraLives);
            } else {
                gp.setDeaths(gp.getDeaths() - amount + 1);
                target.setHealth(0);
            }
            sender.sendMessage(Util.c(Manhunt.get().getCfg().removeLifeMessage
                    .replace("%player%", target.getName())
                    .replace("%removed%", amount + "")));
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
            sendHelpMessage(p);
            return true;
        } else {
            if (args[0].equalsIgnoreCase("stop")) {
                p.sendMessage(Util.c(Manhunt.get().getCfg().stoppingGameMessage));
                game.stopGame(true, true);
                return true;
            } else if (args[0].equalsIgnoreCase("start")) {
                p.sendMessage(Util.c(Manhunt.get().getCfg().startingGameMessage));
                game.start();
                return true;
            } else if (args[0].equalsIgnoreCase("forcetwist") && p.hasPermission("manhunt.forcetwist")) {
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
                        List<GamePlayer> runners = game.getPlayers(PlayerType.RUNNER);
                        if (runners.size() > 1) {
                            // can remove host because there are multiple hunters.
                            p.sendMessage(Util.c(Manhunt.get().getCfg().playerRemoveRunnerMessage.replaceAll("%player%", target.getName())));
                            gp.setPlayerType(PlayerType.HUNTER);
                            game.getRunnerTeleporterMenu().update();
                            Util.playSound(p, Manhunt.get().getCfg().runnerRemovedSound, 1, 1);
                        } else {
                            // cannot remove any runners because there must be at least one in the game.
                            p.sendMessage(Util.c(Manhunt.get().getCfg().menuRunnerManagerCannotRemoveRunnerMessage.replaceAll("%player%", target.getName())));
                            Util.playSound(p, Manhunt.get().getCfg().menuRunnerManagerCannotRemoveRunnerSound, 1, 1);
                        }
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
                sendHelpMessage(p);
                return true;
            }
        }


        return true;
    }

    private void sendHelpMessage(Player p) {
        p.sendMessage("§7§m------§r §6§lManhunt Host Commands §7§m------");

        if (p.hasPermission("manhunt.admin")) p.sendMessage("§b/manhunt stop [player] §f> §7Stop a manhunt game.");
        else p.sendMessage("§b/manhunt stop §f> §7Stop your current manhunt game.");

        if (p.hasPermission("manhunt.admin")) p.sendMessage("§b/manhunt start [player] §f> §7Start a manhunt game.");
        else p.sendMessage("§b/manhunt stop §f> §7Start your current manhunt game.");

        p.sendMessage("§b/manhunt addrunner <player> §f> §7Make a player a runner.");
        p.sendMessage("§b/manhunt removerunner <player> §f> §7Demote a runner to a hunter.");
        p.sendMessage("§b/manhunt runners §f> §7Get a list of runners.");

        if (p.hasPermission("manhunt.forcetwist"))
            p.sendMessage("§b/manhunt forcetwist §f> §7Force the twist to start.");
        if (p.hasPermission("manhunt.revive"))
            p.sendMessage("§b/manhunt revive <player> §f> §7Revive a player.");
        if (p.hasPermission("manhunt.addlife"))
            p.sendMessage("§b/manhunt addlife <player> [amount] §f> §7Add lives to a player.");
        if (p.hasPermission("mahhunt.removelife"))
            p.sendMessage("§b/manhunt removelife <player> [amount] §f> §7Remove lives from a player.");
        p.sendMessage("§7§m-------------------------------------");
    }
}
