package me.gaagjescraft.network.team.manhunt.menus.handlers;

import io.papermc.lib.PaperLib;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.events.custom.GameTrackerMenuClickEvent;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RunnerTrackerMenuHandler implements Listener {

    private final Manhunt plugin;

    public RunnerTrackerMenuHandler(Manhunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals(Util.c(plugin.getCfg().menuTrackerTitle)) && !e.getView().getTitle().equals(Util.c(plugin.getCfg().menuTeleporterTitle)))
            return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player player = (Player) e.getWhoClicked();

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        GameTrackerMenuClickEvent event = new GameTrackerMenuClickEvent(player, game, e.getClickedInventory(), e.getRawSlot(), e.getClick());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

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
                    player.sendMessage(Util.c(plugin.getCfg().failedTeleportingMessage));
                } else {
                    player.sendMessage(Util.c(plugin.getCfg().failedTrackingMessage));
                }
                return;
            }
            if (target.getUniqueId().equals(player.getUniqueId())) {
                plugin.getUtil().playSound(player, plugin.getCfg().playerIsYouSound, 1, 1);
                player.sendMessage(Util.c(plugin.getCfg().playerIsYouMessage));
                return;
            }

            player.closeInventory();
            plugin.getUtil().playSound(player, plugin.getCfg().trackingPlayerSound, 1, 1);
            if (teleporting) {
                PaperLib.teleportAsync(player, target.getLocation());
                player.sendMessage(Util.c(plugin.getCfg().teleportingPlayerMessage.replaceAll("%prefix%", targetGP.getPrefix())
                        .replaceAll("%color%", targetGP.getColor()).replaceAll("%player%", target.getName())));
            } else {
                gp.setTracking(game.getRunnerTeleporterMenu().getRunnersList().get(i));
                player.sendMessage(Util.c(plugin.getCfg().trackingPlayerMessage.replaceAll("%prefix%", targetGP.getPrefix())
                        .replaceAll("%color%", targetGP.getColor()).replaceAll("%player%", target.getName())));
            }
        }


    }

}
