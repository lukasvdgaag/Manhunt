package me.gaagjescraft.network.team.manhunt.commands;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to perform this command");
            return true;
        }
        Player p = (Player) sender;

        if (args.length >= 1 && args[0].equalsIgnoreCase("setspawn") && p.hasPermission("exodus.events.setspawn")) {
            Manhunt.get().getCfg().lobby = p.getLocation();
            Manhunt.get().getCfg().save();
            p.sendMessage(Util.c(Manhunt.get().getCfg().lobbySetMessage));
            return true;
        }

        Game game = (Game.getGame(p));
        if (game == null || !game.getPlayer(p).isOnline()) Manhunt.get().getEventMenu().openMenu(p);
        else p.sendMessage(Util.c(Manhunt.get().getCfg().notWhilePlayingMessage));
        return true;
    }
}
