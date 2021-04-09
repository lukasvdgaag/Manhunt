package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.ArrayList;
import java.util.List;

public class GameWaitingEvents implements Listener {

    private List<Player> leaveDelays = new ArrayList<>();

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent e) {
        Game game = Game.getGame(e.getLocation().getWorld().getName().replace("manhunt_", ""));
        if (game == null) return;

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING || (game.getStatus() == GameStatus.PLAYING && game.getTimer() <= game.getHeadStart().getSeconds())) {
            if (e.getLocation().getBlockY() >= 150) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Game game = Game.getGame(e.getPlayer());
        if (game == null) return;
        GamePlayer gp = game.getPlayer(e.getPlayer());

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds())) {
            if (e.getItem() == null) return;
            e.setCancelled(true);
            if (e.getItem().equals(Itemizer.MANHUNT_LEAVE_ITEM) && !leaveDelays.contains(e.getPlayer())) {
                gp.leaveGameDelayed(false);
                leaveDelays.add(e.getPlayer());
                Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> leaveDelays.remove(e.getPlayer()), 20L);
            } else if (e.getItem().equals(Itemizer.MANHUNT_VOTE_ITEM) && (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTING)) {
                Manhunt.get().getManhuntTwistVoteMenu().open(e.getPlayer(),game);
            } else if (e.getItem().equals(Itemizer.MANHUNT_HOST_SETTINGS_ITEM) && gp.isHost()) {
                Manhunt.get().getManhuntGameSetupMenu().openMenu(e.getPlayer(), game);
            }
        } else if (game.getStatus() == GameStatus.PLAYING) {
            if (e.getItem() ==null) return;
            if (e.getItem().equals(Itemizer.MANHUNT_RUNNER_TRACKER)) {
                game.getRunnerTeleporterMenu().open(e.getPlayer(), gp.isDead());
            }
        }

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STOPPING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds())) {
            e.setCancelled(true);
            e.setBuild(false);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STOPPING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity().getType() != EntityType.PLAYER) return;

        Player player = (Player) e.getEntity();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STOPPING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds()+15) ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.RUNNER && game.getTimer() <= 20)) {
            e.setCancelled(true);
            e.setDamage(0);
        }
    }

    @EventHandler
    public void onBlockPlace(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

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

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STOPPING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        Player player = (Player) e.getEntity();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STOPPING || game.getStatus() == GameStatus.STARTING ||
                (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER && game.getTimer() <= game.getHeadStart().getSeconds())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        Player player = e.getPlayer();
        Game game = Game.getGame(player);
        if (game == null || game.getStatus() != GameStatus.PLAYING) return;
        GamePlayer gp = game.getPlayer(player);
        if (gp.isDead() || gp.getPlayerType() != PlayerType.RUNNER) return;

        String worldName = e.getTo().getWorld().getName();

        if (worldName.endsWith("_nether") && !gp.isReachedNether()) {
            gp.setReachedNether(true);
            e.getPlayer().sendTitle("§c§lNETHER", "§7You have entered the nether!", 20, 50, 20);
            for (GamePlayer gp2 : game.getPlayers()) {
                Player p = Bukkit.getPlayer(gp2.getUuid());
                if (p == null) continue;
                p.sendMessage(gp.getPrefix() + " " + e.getPlayer().getName() + "§a has reached §c§lTHE NETHER§a!");
                p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1, 1);
            }
        } else if (worldName.endsWith("_the_end") && !gp.isReachedEnd()) {
            gp.setReachedEnd(true);
            e.getPlayer().sendTitle("§5§lTHE END", "§7You have entered the end!", 20, 50, 20);
            for (GamePlayer gp2 : game.getPlayers()) {
                Player p = Bukkit.getPlayer(gp2.getUuid());
                if (p == null) continue;
                p.sendMessage(gp.getPrefix() + " " + e.getPlayer().getName() + "§a has reached §5§lTHE END§a!");
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
            }
        }

    }

}
