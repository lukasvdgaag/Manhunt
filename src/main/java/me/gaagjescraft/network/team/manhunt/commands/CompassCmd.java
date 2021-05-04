package me.gaagjescraft.network.team.manhunt.commands;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
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
            player.sendMessage(Util.c(Manhunt.get().getCfg().notIngameMessage));
            return true;
        }

        if (game.getStatus() != GameStatus.PLAYING || game.getTimer() < game.getHeadStart().getSeconds()) {
            player.sendMessage(Util.c(Manhunt.get().getCfg().compassUnavailableMessage));
            return true;
        }

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.isSimilar(Itemizer.MANHUNT_RUNNER_TRACKER)) {
                player.sendMessage(Util.c(Manhunt.get().getCfg().compassAlreadyAddedMessage));
                return true;
            }
        }

        player.getInventory().addItem(Itemizer.MANHUNT_RUNNER_TRACKER);
        player.sendMessage(Util.c(Manhunt.get().getCfg().compassGivenMessage));
        return true;
    }
}
