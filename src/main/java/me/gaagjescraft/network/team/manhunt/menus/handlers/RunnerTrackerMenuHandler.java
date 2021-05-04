package me.gaagjescraft.network.team.manhunt.menus.handlers;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RunnerTrackerMenuHandler implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals(Util.c(Manhunt.get().getCfg().menuTrackerTitle)) && !e.getView().getTitle().equals(Util.c(Manhunt.get().getCfg().menuTeleporterTitle)))
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
                if (teleporting) {
                    player.sendMessage(Util.c(Manhunt.get().getCfg().failedTeleportingMessage));
                } else {
                    player.sendMessage(Util.c(Manhunt.get().getCfg().failedTrackingMessage));
                }
                return;
            }
            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().playerIsYouSound), 1, 1);
                player.sendMessage(Util.c(Manhunt.get().getCfg().playerIsYouMessage));
                return;
            }

            player.closeInventory();
            player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().trackingPlayerSound), 1, 1);
            if (teleporting) {
                player.teleport(target.getLocation());
                player.sendMessage(Util.c(Manhunt.get().getCfg().teleportingPlayerMessage.replace("%prefix%", targetGP.getPrefix())
                        .replace("%color%", targetGP.getColor()).replace("%player%", target.getName())));
            } else {
                gp.setTracking(game.getRunnerTeleporterMenu().getRunnersList().get(i));
                player.sendMessage(Util.c(Manhunt.get().getCfg().trackingPlayerMessage.replace("%prefix%", targetGP.getPrefix())
                        .replace("%color%", targetGP.getColor()).replace("%player%", target.getName())));
            }
        }


    }

}
