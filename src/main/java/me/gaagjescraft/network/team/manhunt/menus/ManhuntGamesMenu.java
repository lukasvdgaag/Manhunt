package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ManhuntGamesMenu implements Listener {

    private List<Player> viewers = new ArrayList<>();

    public void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "Manhunt Events");

        inventory.setItem(22, Itemizer.CLOSE_ITEM);

        if (player.hasPermission("exodus.hostevent")) {
            inventory.setItem(26, Itemizer.NEW_GAME_ITEM);
        }

        List<Game> games = Game.getGames();
        if (games.isEmpty()) {
            for (int i = 0; i < inventory.getSize(); i++)
                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR)
                    inventory.setItem(i, Itemizer.FILL_NO_GAMES);

            player.openInventory(inventory);
            return;
        }

        int slot = 0;
        for (Game g : games) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();

            String status = "§7Loading the game...";
            if (g.getStatus() == GameStatus.STARTING) {
                status = "§aThis game is starting.";
            } else if (g.getStatus() == GameStatus.PLAYING) {
                status = "§aThis game is running.";
            } else if (g.getStatus() == GameStatus.WAITING) {
                status = "§aWaiting for more players to join...";
            } else if (g.getStatus() == GameStatus.STOPPING) {
                status = "§cThis game is ending.";
            }

            meta.setDisplayName("§a" + g.getIdentifier() + "'s Manhunt");
            meta.setLore(Lists.newArrayList("", "§7Hunt down the speed runners",
                    "§7and kill them without dying yourself.",
                    "§7Hunters win if all runners are dead.", "",
                    "§bThere are §e" + g.getOnlinePlayers(PlayerType.HUNTER).size() + "§7/§e" + g.getMaxPlayers() + "§b hunters competing §e" + g.getOnlinePlayers(PlayerType.RUNNER).size() + "§b runners.",
                    status,
                    "",
                    g.getOnlinePlayers(PlayerType.HUNTER).size() < g.getMaxPlayers() ? "§6Click §eto join the game." : "§cThis game is full."));
            meta.addItemFlags(ItemFlag.values());
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(g.getIdentifier()));
            item.setItemMeta(meta);
            inventory.setItem(slot, item);

            slot++;
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR)
                inventory.setItem(i, Itemizer.FILL_ITEM);
        }

        player.openInventory(inventory);
        viewers.add(player);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals("Manhunt Events")) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        if (e.getSlot() == 22) {
            e.getWhoClicked().closeInventory();
            return;
        } else if (e.getSlot() == 26 && e.getWhoClicked().hasPermission("exodus.hostevent")) {
            Manhunt.get().getManhuntGameSetupMenu().openMenu((Player) e.getWhoClicked(), null);
            return;
        }

        List<Game> games = Game.getGames();
        if (e.getSlot() >= games.size()) return;

        Game g = games.get(e.getSlot());
        if (g == null) return;

        boolean result = g.addPlayer((Player) e.getWhoClicked());
        if (!result)
            e.getWhoClicked().sendMessage(ChatColor.RED + g.getIdentifier() + "'s game is full or unavailable right now!");

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
