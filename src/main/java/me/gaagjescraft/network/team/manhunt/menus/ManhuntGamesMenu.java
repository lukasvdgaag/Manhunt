package me.gaagjescraft.network.team.manhunt.menus;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
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

public class ManhuntGamesMenu implements Listener {

    private List<Player> viewers = new ArrayList<>();

    public void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, Util.c(Manhunt.get().getCfg().menuGamesTitle));

        inventory.setItem(22, Itemizer.CLOSE_ITEM);
        inventory.setItem(18, Itemizer.GO_BACK_ITEM);

        if (player.hasPermission("manhunt.hostgame")) {
            int protocol = Manhunt.get().getUtil().getProtocol(player);

            if (protocol == -1 || protocol >= Manhunt.get().getCfg().minimumClientProtocolVersion)
                inventory.setItem(26, Itemizer.NEW_GAME_ITEM);
            else inventory.setItem(26, Itemizer.NEW_GAME_ITEM_UNSUPPORTED_PROTOCOL);
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

            int onlineHunters = (Manhunt.get().getCfg().bungeeMode && Manhunt.get().getCfg().isLobbyServer) ? g.getBungeeHunterCount() : g.getOnlinePlayers(PlayerType.HUNTER).size();
            int onlineRunners = (Manhunt.get().getCfg().bungeeMode && Manhunt.get().getCfg().isLobbyServer) ? g.getBungeeRunnerCount() : g.getOnlinePlayers(PlayerType.RUNNER).size();

            meta.setDisplayName(Util.c(Manhunt.get().getCfg().gamesMenuGameHostDisplayname).replaceAll("%host%", g.getIdentifier()));
            List<String> lore = (onlineHunters < g.getMaxPlayers() && (g.getStatus() != GameStatus.STOPPING && g.getStatus() != GameStatus.LOADING)) ?
                    Manhunt.get().getCfg().gamesMenuGameHostLore : Manhunt.get().getCfg().gamesMenuGameHostLockedLore;
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

        Player p = (Player) e.getWhoClicked();

        if (e.getSlot() == 18) {
            Manhunt.get().getManhuntMainMenu().openMenu(p);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }
        if (e.getSlot() == 22) {
            e.getWhoClicked().closeInventory();
            return;
        } else if (e.getSlot() == 26 && e.getWhoClicked().hasPermission("manhunt.hostgame")) {
            int protocol = Manhunt.get().getUtil().getProtocol(p);

            if (protocol == -1 || protocol >= Manhunt.get().getCfg().minimumClientProtocolVersion)
                Manhunt.get().getManhuntGameSetupMenu().openMenu(p, null);
            else
                p.playSound(p.getLocation(), Sound.valueOf(Manhunt.get().getCfg().menuHostLockedSound), 1, 1);
            return;
        }

        List<Game> games = Game.getGames();
        if (e.getSlot() >= games.size()) return;

        Game g = games.get(e.getSlot());
        if (g == null) return;

        int playercount = Manhunt.get().getCfg().bungeeMode ? g.getBungeeHunterCount() : g.getOnlinePlayers(PlayerType.HUNTER).size();
        if (playercount >= g.getMaxPlayers() || e.getClick() == ClickType.RIGHT) {
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
            e.getWhoClicked().sendMessage(Util.c(Manhunt.get().getCfg().gameUnavailableMessage.replaceAll("%host%", g.getIdentifier())));

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
