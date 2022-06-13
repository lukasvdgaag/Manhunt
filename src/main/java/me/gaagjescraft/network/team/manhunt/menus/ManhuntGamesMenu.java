package me.gaagjescraft.network.team.manhunt.menus;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ManhuntGamesMenu implements Listener {

    private final List<Player> viewers = new ArrayList<>();

    private final Manhunt plugin;

    public ManhuntGamesMenu(Manhunt plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, Util.c(plugin.getCfg().menuGamesTitle));

        inventory.setItem(22, plugin.getItemizer().CLOSE_ITEM);
        inventory.setItem(18, plugin.getItemizer().GO_BACK_ITEM);

        List<Game> games = Game.getGames();
        if (games.isEmpty()) {
            for (int i = 0; i < inventory.getSize(); i++)
                if (inventory.getItem(i) == null || Objects.requireNonNull(inventory.getItem(i)).getType() == Material.AIR)
                    inventory.setItem(i, plugin.getItemizer().FILL_NO_GAMES);

            player.openInventory(inventory);
            return;
        }

        int slot = 0;
        for (Game g : games) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();

            String status = Util.c(plugin.getCfg().loadingStatusPrefix);
            if (g.getStatus() == GameStatus.STARTING) {
                status = Util.c(plugin.getCfg().startingStatusPrefix);
            } else if (g.getStatus() == GameStatus.PLAYING) {
                status = Util.c(plugin.getCfg().playingStatusPrefix);
            } else if (g.getStatus() == GameStatus.WAITING) {
                status = Util.c(plugin.getCfg().waitingStatusPrefix);
            } else if (g.getStatus() == GameStatus.STOPPING) {
                status = Util.c(plugin.getCfg().stoppingStatusPrefix);
            }

            int onlineHunters = (plugin.getCfg().bungeeMode && plugin.getCfg().isLobbyServer) ? g.getBungeeHunterCount() : g.getOnlinePlayers(PlayerType.HUNTER).size();
            int onlineRunners = (plugin.getCfg().bungeeMode && plugin.getCfg().isLobbyServer) ? g.getBungeeRunnerCount() : g.getOnlinePlayers(PlayerType.RUNNER).size();

            assert meta != null;
            meta.setDisplayName(Util.c(plugin.getCfg().gamesMenuGameHostDisplayname).replaceAll("%host%", g.getIdentifier()));
            List<String> lore = (onlineHunters < g.getMaxPlayers() && (g.getStatus() != GameStatus.STOPPING && g.getStatus() != GameStatus.LOADING)) ?
                    plugin.getCfg().gamesMenuGameHostLore : plugin.getCfg().gamesMenuGameHostLockedLore;
            lore = new ArrayList<>(lore);
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, Util.c(lore.get(i)
                        .replaceAll("%host%", g.getIdentifier())
                        .replaceAll("%hunters%", onlineHunters + "")
                        .replaceAll("%runners%", onlineRunners + "")
                        .replaceAll("%maxplayers%", g.getMaxPlayers() + "")
                        .replaceAll("%online%", (onlineHunters + onlineRunners) + "")
                        .replaceAll("%status%", status)));
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.values());
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(g.getIdentifier()));
            item.setItemMeta(meta);
            inventory.setItem(slot, item);

            slot++;
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || Objects.requireNonNull(inventory.getItem(i)).getType() == Material.AIR)
                inventory.setItem(i, plugin.getItemizer().FILL_ITEM);
        }

        player.openInventory(inventory);
        viewers.add(player);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals(Util.c(plugin.getCfg().menuGamesTitle))) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);
        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player p = (Player) e.getWhoClicked();

        if (e.getSlot() == 18) {
            plugin.getManhuntMainMenu().openMenu(p);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        } else if (e.getSlot() == 22) {
            p.closeInventory();
            return;
        }

        List<Game> games = Game.getGames();
        if (e.getSlot() >= games.size()) return;

        Game g = games.get(e.getSlot());
        if (g == null) return;

        int playerCount = plugin.getCfg().bungeeMode ? g.getBungeeHunterCount() : g.getOnlinePlayers(PlayerType.HUNTER).size();
        if (playerCount >= g.getMaxPlayers() || e.getClick() == ClickType.RIGHT) {
            if (!g.getSpectators().contains(e.getWhoClicked().getUniqueId()))
                g.addSpectator(e.getWhoClicked().getUniqueId());

            if (e.getClick() == ClickType.RIGHT) {
                e.getWhoClicked().sendMessage("§eThis game currently not accepting any more players, so will try to add you as a spectator");
            } else {
                e.getWhoClicked().sendMessage("§eAttempting to add you to this game as a spectator...");
            }
        }

        boolean result = g.addPlayer((Player) e.getWhoClicked());
        if (!result)
            e.getWhoClicked().sendMessage(Util.c(plugin.getCfg().gameUnavailableMessage.replaceAll("%host%", g.getIdentifier())));

        e.getWhoClicked().closeInventory();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        viewers.remove((Player) e.getPlayer());
    }

    public List<Player> getViewers() {
        return viewers;
    }
}
