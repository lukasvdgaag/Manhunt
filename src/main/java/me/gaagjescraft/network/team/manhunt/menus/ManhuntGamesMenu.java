package me.gaagjescraft.network.team.manhunt.menus;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
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
        Inventory inventory = Bukkit.createInventory(null, 27, Util.c(Manhunt.get().getCfg().menuGamesTitle));

        inventory.setItem(22, Itemizer.CLOSE_ITEM);

        if (player.hasPermission("manhunt.hostgame")) {
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

            String status = Util.c(Manhunt.get().getCfg().loadingStatusPrefix);
            if (g.getStatus() == GameStatus.STARTING) {
                status = Util.c(Manhunt.get().getCfg().startingStatusPrefix);
            } else if (g.getStatus() == GameStatus.PLAYING) {
                status = Util.c(Manhunt.get().getCfg().playingStatusPrefix);
            } else if (g.getStatus() == GameStatus.WAITING) {
                status = Util.c(Manhunt.get().getCfg().waitingStatusPrefix);
            } else if (g.getStatus() == GameStatus.STOPPING) {
                status = Util.c(Manhunt.get().getCfg().stoppingStatusPrefix);
            }

            int onlineHunters = g.getOnlinePlayers(PlayerType.HUNTER).size();
            int onlineRunners = g.getOnlinePlayers(PlayerType.RUNNER).size();

            meta.setDisplayName(Util.c(Manhunt.get().getCfg().gamesMenuGameHostDisplayname).replace("%host%", g.getIdentifier()));
            List<String> lore = (onlineHunters < g.getMaxPlayers() && (g.getStatus() != GameStatus.STOPPING && g.getStatus() != GameStatus.LOADING)) ?
                    Manhunt.get().getCfg().gamesMenuGameHostLore : Manhunt.get().getCfg().gamesMenuGameHostLockedLore;
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, Util.c(lore.get(i)
                        .replace("%host%", g.getIdentifier())
                        .replace("%hunters%", onlineHunters + "")
                        .replace("%runners%", onlineRunners + "")
                        .replace("%maxplayers%", g.getMaxPlayers() + "")
                        .replace("%online%", (onlineHunters + onlineRunners) + "")
                        .replace("%status%", status)));
            }
            meta.setLore(lore);
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
        if (!e.getView().getTitle().equals(Util.c(Manhunt.get().getCfg().menuGamesTitle))) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        if (e.getSlot() == 22) {
            e.getWhoClicked().closeInventory();
            return;
        } else if (e.getSlot() == 26 && e.getWhoClicked().hasPermission("manhunt.hostgame")) {
            Manhunt.get().getManhuntGameSetupMenu().openMenu((Player) e.getWhoClicked(), null);
            return;
        }

        List<Game> games = Game.getGames();
        if (e.getSlot() >= games.size()) return;

        Game g = games.get(e.getSlot());
        if (g == null) return;

        boolean result = g.addPlayer((Player) e.getWhoClicked());
        if (!result)
            e.getWhoClicked().sendMessage(Util.c(Manhunt.get().getCfg().gameUnavailableMessage.replace("%host%", g.getIdentifier())));

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
