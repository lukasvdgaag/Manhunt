package me.gaagjescraft.network.team.manhunt.commands;

import me.gaagjescraft.network.team.manhunt.games.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RejoinCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou must be a player to perform this command.");
            return true;
        }

        Player player = (Player) sender;
        Game game = Game.getGame(player);
        if (game == null) {
            sender.sendMessage("§cYou have no game to join back in.");
            return true;
        } else if (game.getPlayer(player).isOnline()) {
            sender.sendMessage("§cYou are already in this game.");
            return true;
        }

        if (!game.addPlayer(player)) {
            sender.sendMessage("§cWe failed to put you back into your previous game!");
            return true;
        } else {
            sender.sendMessage("§5You have been put into your old game.");
            return true;
        }
    }
}
