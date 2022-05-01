package me.gaagjescraft.network.team.manhunt.commands;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CompassCmd implements CommandExecutor {

    private final Manhunt plugin;

    public CompassCmd(Manhunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to do this!");
            return true;
        }

        Game game = Game.getGame(player);

        if (game == null) {
            player.sendMessage(Util.c(plugin.getCfg().notIngameMessage));
            return true;
        }

        if (game.getStatus() != GameStatus.PLAYING || game.getTimer() < game.getHeadStart().getSeconds()) {
            player.sendMessage(Util.c(plugin.getCfg().compassUnavailableMessage));
            return true;
        }

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType() == Material.COMPASS && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getCfg().generalTrackerDisplayname))) {
                player.sendMessage(Util.c(plugin.getCfg().compassAlreadyAddedMessage));
                return true;
            }
        }

        player.getInventory().addItem(plugin.getItemizer().MANHUNT_RUNNER_TRACKER);
        player.sendMessage(Util.c(plugin.getCfg().compassGivenMessage));
        return true;
    }
}
