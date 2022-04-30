package me.gaagjescraft.network.team.manhunt.games.compass;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import java.util.Objects;

public abstract class AbstractCompassTracker implements CompassTracker {

    public final GamePlayer gamePlayer;
    public GamePlayer trackingPlayer;
    public Location trackingLocation;

    public AbstractCompassTracker(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    @Override
    public void updateCompass() {
        Player player = Bukkit.getPlayer(gamePlayer.getUuid());
        if (player == null) return;

        Location location = getLocation();
        if (location == null) return;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType() == Material.COMPASS && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', Manhunt.get().getCfg().generalTrackerDisplayname))) {
                CompassMeta meta = (CompassMeta) item.getItemMeta();
                if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
                    meta.setLodestone(location);
                    meta.setLodestoneTracked(false);
                } else {
                    meta.setLodestoneTracked(true);
                    meta.setLodestone(null);
                    player.setCompassTarget(location);
                }
                item.setItemMeta(meta);
                // player.getInventory().setItem(i, compass);
                break;
            }
        }

        if (trackingPlayer != null) {
            int distance = (int) player.getLocation().distance(location);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(Manhunt.get().getCfg().trackingActionbar.replaceAll("%player%", Objects.requireNonNull(Bukkit.getPlayer(trackingPlayer.getUuid())).getName()).replaceAll("%color%", trackingPlayer.getColor()).replaceAll("%distance%", distance + ""))));
        }
    }

    @Override
    public GamePlayer getTrackingPlayer() {
        return trackingPlayer;
    }

    @Override
    public Location getTrackingLocation() {
        return trackingLocation;
    }

    @Override
    public Location getLocation() {
        Player player = Bukkit.getPlayer(gamePlayer.getUuid());
        if (player != null && trackingPlayer != null) {
            Player track = Bukkit.getPlayer(trackingPlayer.getUuid());
            if (track != null) {
                if (!track.getWorld().getName().equals(player.getWorld().getName())) {
                    if (track.getWorld().getEnvironment() == World.Environment.NETHER && player.getWorld().getEnvironment() == World.Environment.NORMAL && trackingPlayer.getNetherPortal() != null) {
                        // player is in overworld, tracked player is in the nether.
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(Manhunt.get().getCfg().trackingPortalActionbar.replaceAll("%player%", track.getName()).replaceAll("%color%", trackingPlayer.getColor()))));
                        return trackingPlayer.getNetherPortal();
                    } else if (player.getWorld().getEnvironment() == World.Environment.NETHER && track.getWorld().getEnvironment() == World.Environment.NORMAL && trackingPlayer.getOverworldPortal() != null) {
                        // player is in nether, tracked player is in the overworld.
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(Manhunt.get().getCfg().trackingPortalActionbar.replaceAll("%player%", track.getName()).replaceAll("%color%", trackingPlayer.getColor()))));
                        return trackingPlayer.getOverworldPortal();
                    } else if (track.getWorld().getEnvironment() == World.Environment.THE_END && player.getWorld().getEnvironment() == World.Environment.NORMAL && trackingPlayer.getEndPortal() != null) {
                        // player is in overworld, tracked player is in the end.
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(Manhunt.get().getCfg().trackingPortalActionbar.replaceAll("%player%", track.getName()).replaceAll("%color%", trackingPlayer.getColor()))));
                        return trackingPlayer.getEndPortal();
                    } else {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Util.c(Manhunt.get().getCfg().trackingOtherDimensionActionbar.replaceAll("%player%", track.getName()).replaceAll("%color%", trackingPlayer.getColor()))));
                        return null;
                    }
                } else {
                    return track.getLocation();
                }
            }
        }

        return trackingLocation;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    @Override
    public boolean isTracking() {
        return trackingLocation != null || trackingPlayer != null;
    }

    @Override
    public void setTracking(Location location) {
        this.trackingLocation = location;
        this.trackingPlayer = null;
    }

    @Override
    public void setTracking(GamePlayer tracking) {
        this.trackingPlayer = tracking;
        this.trackingLocation = null;
    }
}
