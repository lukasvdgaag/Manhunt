package me.gaagjescraft.network.team.manhunt.menus.handlers;

import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RunnerTrackerMenuHandler implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals("Manhunt Tracker Menu") && !e.getView().getTitle().equals("Manhunt Teleporter Menu"))
            return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;
        Player player = (Player) e.getWhoClicked();

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (e.getCurrentItem() == null) return;
        int i = e.getRawSlot();

        if (i == e.getClickedInventory().getSize() - 5) {
            player.closeInventory();
            return;
        }

        if (i < game.getRunnerTeleporterMenu().getRunnersList().size()) {
            boolean teleporting = gp.isDead();

            GamePlayer targetGP = game.getRunnerTeleporterMenu().getRunnersList().get(i);
            Player target = Bukkit.getPlayer(targetGP.getUuid());
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Something went wrong whilst " + (teleporting ? "teleporting you to this player." : "tracking this player."));
                return;
            }
            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                if (teleporting) player.sendMessage(ChatColor.RED + "You can't teleport to yourself!");
                else player.sendMessage(ChatColor.RED + "You can't track yourself!");
                return;
            }

            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            if (teleporting) {
                player.teleport(target.getLocation());
                player.sendMessage("§aYou have been teleported to " + targetGP.getPrefix() + " " + target.getName());
            } else {
                gp.setTracking(game.getRunnerTeleporterMenu().getRunnersList().get(i));
                player.sendMessage("§aYou are now tracking " + targetGP.getPrefix() + " " + target.getName());
            }
        }


    }

}
