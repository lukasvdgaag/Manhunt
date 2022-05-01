package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.vehicle.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameWaitingEvents implements Listener {

    private final List<Player> leaveDelays;
    private final Manhunt plugin;

    public GameWaitingEvents(Manhunt plugin) {
        this.plugin = plugin;
        this.leaveDelays = new ArrayList<>();
    }

    private void checkVehicleCancel(Cancellable e, Entity entity) {
        if (entity == null || entity.getType() != EntityType.PLAYER) return;
        Player player = (Player) entity;
        Game game = Game.getGame(player);
        if (game == null) return;
        if (game.getPlayer(player).isDead()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onboat(VehicleExitEvent e) {
        checkVehicleCancel(e, e.getExited());
    }

    @EventHandler
    public void onboat(VehicleEntityCollisionEvent e) {
        checkVehicleCancel(e, e.getEntity());
    }

    @EventHandler
    public void onboat(VehicleDamageEvent e) {
        checkVehicleCancel(e, e.getAttacker());
    }

    @EventHandler
    public void onboat(VehicleDestroyEvent e) {
        checkVehicleCancel(e, e.getAttacker());
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent e) {
        checkVehicleCancel(e, e.getEntered());
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent e) {
        if (Objects.requireNonNull(e.getLocation().getWorld()).getName().startsWith("manhunt_") && (e.getEntityType() == EntityType.BAT || e.getEntityType() == EntityType.SQUID)) {
            e.setCancelled(true);
            return;
        }
        if (plugin.getCfg().lobby == null) return;

        Game game = Game.getGame(e.getLocation().getWorld().getName().replaceAll("manhunt_", ""));
        if (game == null) return;

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING || (game.getStatus() == GameStatus.PLAYING && game.getTimer() <= game.getHeadStart().getSeconds())) {
            if (e.getLocation().getBlockY() >= 125) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity().getType() != EntityType.PLAYER) return;
        Player player = (Player) e.getEntity();

        if (plugin.getCfg().lobby != null) {
            if (cancelInLobby(e, player.getWorld())) return;
        }

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (gp.isDead()) {
            e.setCancelled(true);
            return;
        }

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds())) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Game game = Game.getGame(e.getPlayer());
        if (game == null) return;
        GamePlayer gp = game.getPlayer(e.getPlayer());

        if (gp.isDead()) {
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
            e.setCancelled(true);
        }

        if (game.getStatus() == GameStatus.PLAYING) {
            if (e.getItem() == null) return;
            if (e.getItem().getType() == Material.COMPASS && Objects.requireNonNull(e.getItem().getItemMeta()).getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getCfg().generalTrackerDisplayname))) {
                game.getRunnerTeleporterMenu().open(e.getPlayer(), gp.isDead());
                return;
            }
            if (e.getItem().equals(plugin.getItemizer().MANHUNT_LEAVE_ITEM)) {
                if (gp.isFullyDead()) {
                    gp.leaveGameDelayed(false);
                    leaveDelays.add(e.getPlayer());
                    Bukkit.getScheduler().runTaskLater(plugin, () -> leaveDelays.remove(e.getPlayer()), 20L);
                    return;
                }
            }
        }

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds())) {
            if (e.getItem() == null) return;
            e.setCancelled(true);
            e.setUseItemInHand(Event.Result.DENY);
            e.setUseInteractedBlock(Event.Result.DENY);
            if (e.getItem().equals(plugin.getItemizer().MANHUNT_LEAVE_ITEM) && !leaveDelays.contains(e.getPlayer())) {
                gp.leaveGameDelayed(false);
                leaveDelays.add(e.getPlayer());
                Bukkit.getScheduler().runTaskLater(plugin, () -> leaveDelays.remove(e.getPlayer()), 20L);
            } else if (e.getItem().equals(plugin.getItemizer().MANHUNT_VOTE_ITEM) && (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING)) {
                plugin.getManhuntTwistVoteMenu().open(e.getPlayer(), game);
            } else if (e.getItem().equals(plugin.getItemizer().MANHUNT_HOST_SETTINGS_ITEM) && gp.isHost()) {
                plugin.getManhuntGameSetupMenu().openMenu(e.getPlayer(), game);
            }
        }

    }

    public boolean cancelInLobby(Cancellable event, World world) {
        if (plugin.getCfg().lobby != null) {
            if (world.getName().equals(plugin.getCfg().lobby.getWorld().getName())) {
                event.setCancelled(true);
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if (plugin.getCfg().cancelLobbyInteractions) {
            if (cancelInLobby(e, e.getBlock().getWorld())) {
                e.setBuild(false);
                return;
            }
        }

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (gp.isDead()) {
            e.setBuild(false);
            e.setCancelled(true);
            return;
        }

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STOPPING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds())) {
            e.setCancelled(true);
            e.setBuild(false);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (plugin.getCfg().cancelLobbyInteractions) {
            if (cancelInLobby(e, e.getBlock().getWorld())) return;
        }
        cancelInGame(e, player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity().getType() != EntityType.PLAYER) return;
        Player player = (Player) e.getEntity();
        if (plugin.getCfg().cancelLobbyInteractions) {
            if (cancelInLobby(e, e.getEntity().getWorld())) return;
        }

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (gp.isDead()) {
            e.setDamage(0);
            e.setCancelled(true);
            return;
        }

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STOPPING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds() + 15) ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.RUNNER && game.getTimer() <= 20)) {
            e.setCancelled(true);
            e.setDamage(0);
        }
    }

    @EventHandler
    public void onBlockPlace(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if (plugin.getCfg().cancelLobbyInteractions) {
            if (cancelInLobby(e, e.getPlayer().getWorld())) return;
        }
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        ItemStack item = e.getItemDrop().getItemStack();

        if ((item.getType() == Material.COMPASS && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getCfg().generalTrackerDisplayname))) || gp.isDead()) {
            e.setCancelled(true);
            return;
        }

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STOPPING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds())) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onBlockPlace(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (gp.isFullyDead()) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().equals(plugin.getItemizer().MANHUNT_LEAVE_ITEM)) {
                gp.leaveGameDelayed(false);
                leaveDelays.add(player);
                Bukkit.getScheduler().runTaskLater(plugin, () -> leaveDelays.remove(player), 20L);
            } else if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.COMPASS && Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getCfg().generalTrackerDisplayname))) {
                game.getRunnerTeleporterMenu().open(player, true);
            }
            return;
        }

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STOPPING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds())) {
            e.setCancelled(true);
        }
    }

    public boolean cancelInGame(Cancellable e, Player player) {
        Game game = Game.getGame(player);
        if (game == null) return false;
        GamePlayer gp = game.getPlayer(player);

        if (gp.isDead()) {
            e.setCancelled(true);
            return true;
        }

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STOPPING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds())) {
            e.setCancelled(true);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        Player player = (Player) e.getEntity();
        if (plugin.getCfg().cancelLobbyInteractions) {
            if (cancelInLobby(e, e.getEntity().getWorld())) return;
        }
        cancelInGame(e, player);
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        Player player = e.getPlayer();
        Game game = Game.getGame(player);
        if (game == null || game.getStatus() != GameStatus.PLAYING) return;
        GamePlayer gp = game.getPlayer(player);
        if (gp.isDead() || gp.getPlayerType() != PlayerType.RUNNER) return;

        String worldName = Objects.requireNonNull(Objects.requireNonNull(e.getTo()).getWorld()).getName();

        if (gp.isDead()) {
            return;
        }

        if (worldName.endsWith("_nether")) {
            gp.setNetherPortal(e.getFrom());
        } else if (Objects.requireNonNull(e.getFrom().getWorld()).getName().endsWith("_nether")) {
            gp.setOverworldPortal(e.getFrom());
        } else if (worldName.endsWith("_the_end")) {
            gp.setEndPortal(e.getFrom());
        }

        if (worldName.endsWith("_nether") && !gp.isReachedNether()) {
            gp.setReachedNether(true);
            plugin.getUtil().sendTitle(e.getPlayer(), Util.c(plugin.getCfg().playerEnteredNetherTitle.replaceAll("%prefix%", gp.getPrefix()).replaceAll("%player%", e.getPlayer().getName())), 20, 50, 20);

            for (GamePlayer gp2 : game.getOnlinePlayers(null)) {
                Player p = Bukkit.getPlayer(gp2.getUuid());
                if (p == null) continue;
                p.sendMessage(Util.c(plugin.getCfg().runnerEnteredNetherMessage.replaceAll("%prefix%", gp.getPrefix()).replaceAll("%player%", e.getPlayer().getName())));
                plugin.getUtil().playSound(p, plugin.getCfg().runnerEnteredNetherSound, 1, 1);
            }
        } else if (worldName.endsWith("_the_end") && !gp.isReachedEnd()) {
            gp.setReachedEnd(true);
            plugin.getUtil().sendTitle(e.getPlayer(), Util.c(plugin.getCfg().playerEnteredEndTitle.replaceAll("%prefix%", gp.getPrefix()).replaceAll("%player%", e.getPlayer().getName())), 20, 50, 20);

            for (GamePlayer gp2 : game.getOnlinePlayers(null)) {
                Player p = Bukkit.getPlayer(gp2.getUuid());
                if (p == null) continue;
                p.sendMessage(Util.c(plugin.getCfg().runnerEnteredEndMessage.replaceAll("%prefix%", gp.getPrefix()).replaceAll("%player%", e.getPlayer().getName())));
                plugin.getUtil().playSound(p, plugin.getCfg().runnerEnteredEndSound, 1, 1);
            }
        }

    }

    @EventHandler
    public void onHandSwitch(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        if (plugin.getCfg().lobby != null) {
            if (cancelInLobby(e, player.getWorld())) return;
        }
        cancelInGame(e, player);
    }

}
