package me.gaagjescraft.network.team.manhunt.commands;

import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CompassCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to do this!");
            return true;
        }

        Player player = (Player) sender;
        Game game = Game.getGame(player);

        if (game == null) {
            player.sendMessage(ChatColor.RED + "You're not in a game!");
            return true;
        }

        if (game.getStatus() != GameStatus.PLAYING || game.getTimer() < game.getHeadStart().getSeconds()) {
            player.sendMessage(ChatColor.RED + "You can't get a compass right now!");
            return true;
        }

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.isSimilar(Itemizer.MANHUNT_RUNNER_TRACKER)) {
                player.sendMessage(ChatColor.RED + "You already have a runner tracker in your inventory!");
                return true;
            }
        }

        player.getInventory().addItem(Itemizer.MANHUNT_RUNNER_TRACKER);
        player.sendMessage(ChatColor.GREEN + "The runner tracker has been added to your inventory!");
        return true;
    }
}
