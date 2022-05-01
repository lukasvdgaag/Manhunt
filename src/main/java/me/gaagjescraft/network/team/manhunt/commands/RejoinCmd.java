package me.gaagjescraft.network.team.manhunt.commands;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RejoinCmd implements CommandExecutor {

    private final Manhunt plugin;

    public RejoinCmd(Manhunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cYou must be a player to perform this command.");
            return true;
        }

        Game game = Game.getGame(player);
        if (game == null) {
            player.sendMessage(Util.c(plugin.getCfg().noGameToRejoinMessage));
            return true;
        } else if (game.getPlayer(player).isOnline()) {
            player.sendMessage(Util.c(plugin.getCfg().alreadyInGameRejoinMessage));
            return true;
        }

        if (!game.addPlayer(player)) {
            player.sendMessage(Util.c(plugin.getCfg().rejoinFailMessage));
        } else {
            player.sendMessage(Util.c(plugin.getCfg().playerRejoinedMessage));
        }
        return true;
    }
}
