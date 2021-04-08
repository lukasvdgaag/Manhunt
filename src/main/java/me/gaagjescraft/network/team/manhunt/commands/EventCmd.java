package me.gaagjescraft.network.team.manhunt.commands;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
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
            Manhunt.get().getConfig().set("lobby", p.getLocation());
            Manhunt.get().saveConfig();
            Manhunt.get().reloadConfig();
            p.sendMessage("§aSuccessfully set the events spawnpoint!");
            return true;
        }

        if (Game.getGame(p) == null) Manhunt.get().getEventMenu().openMenu(p);
        else p.sendMessage(ChatColor.RED + "You can't do this while you're in a game.");
        return true;
    }
}
