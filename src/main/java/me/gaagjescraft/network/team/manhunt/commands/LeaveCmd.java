package me.gaagjescraft.network.team.manhunt.commands;

import me.gaagjescraft.network.team.manhunt.games.Game;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player in order to perform this command.");
            return true;
        }

        Player player = (Player) sender;
        Game game = Game.getGame(player);
        if (game == null) {
            player.sendMessage("§cYou're not in a game!");
            return true;
        }

        game.removePlayer(player);
        player.sendMessage("§cYou left your game.");
        return true;
    }
}
