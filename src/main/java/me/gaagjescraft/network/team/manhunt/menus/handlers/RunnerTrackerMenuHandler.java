package me.gaagjescraft.network.team.manhunt.menus.handlers;

import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RunnerTrackerMenuHandler implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals("§6§lManHunt Tracker Menu") && !e.getView().getTitle().equals("§6§lManHunt Teleporter Menu"))
            return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;
        Player player = (Player) e.getWhoClicked();

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (e.getCurrentItem() == null) return;
        int i = e.getSlot();
        if (game.getRunnerTeleporterMenu().getRunnersList().size() > i) {
            boolean teleporting = gp.isDead();

            GamePlayer targetGP = game.getRunnerTeleporterMenu().getRunnersList().get(i);
            Player target = Bukkit.getPlayer(targetGP.getUuid());
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Something went wrong whilst " + (teleporting ? "teleporting you to this player." : "tracking this player."));
                return;
            }

            player.closeInventory();
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
