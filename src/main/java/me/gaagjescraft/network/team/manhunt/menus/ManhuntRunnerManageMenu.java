package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class ManhuntRunnerManageMenu implements Listener {

    public List<Player> chatPlayers;

    public ManhuntRunnerManageMenu() {
        this.chatPlayers = Lists.newArrayList();
    }

    public void open(Player player, Game game) {
        Inventory menu = Bukkit.createInventory(null, 27, "Manhunt Runner Manager");
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
        player.openInventory(menu);
        updateItems(player, game);
    }

    public void updateItems(Player player, Game game) {
        if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null) return;
        Inventory menu = player.getOpenInventory().getTopInventory();

        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, Itemizer.FILL_ITEM);
        }

        int slot = 0;
        for (GamePlayer gp : game.getPlayers(PlayerType.RUNNER)) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(gp.getUuid());
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwningPlayer(op);
            meta.setDisplayName("§e" + op.getName() + (gp.isHost() ? " §6(Host)" : ""));
            meta.setLore(Lists.newArrayList("", gp.isHost() ? "§cYou can't remove the host." : "§6Shift Click§e to remove runner."));
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
            menu.setItem(slot, item);
            slot++;
        }

        menu.setItem(26, Itemizer.createItem(Material.OAK_SIGN, 1, "§bAdd new runner", Lists.newArrayList("", "§7Promote a hunter to a", "§7runner. Please know that", "§7the target player has", "§7to be in this game.", "", "§6Click§e to add a new player.")));
        menu.setItem(22, Itemizer.createItem(Material.LIME_CONCRETE, 1, "§a§lSave Settings", Lists.newArrayList("", "§7Save the settings and", "§7go back to the main", "§7settings menu.", "", "§6Click§e to go back.")));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals("Manhunt Runner Manager")) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player player = (Player) e.getWhoClicked();
        Game game = Game.getGame(player);
        if (game == null) return;

        if (e.getSlot() < 18) {
            // can click a potential head.
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                List<GamePlayer> runners = game.getPlayers(PlayerType.RUNNER);
                if (runners.size() > e.getSlot()) {
                    if (e.getClick().isShiftClick()) {
                        GamePlayer gp = runners.get(e.getSlot());
                        if (gp.isHost()) {
                            player.sendMessage("§cYou can't remove the host as a runner!");
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        } else {
                            Player target = Bukkit.getPlayer(gp.getUuid());
                            player.sendMessage("§cYou removed " + target.getName() + " as runner!");
                            gp.setPlayerType(PlayerType.HUNTER);
                            game.getRunnerTeleporterMenu().update();
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                            this.chatPlayers.remove(player);
                            open(player, game);
                        }
                        return;
                    }
                }
            }
        } else if (e.getSlot() == 22) {
            // continue setup
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
            Manhunt.get().getManhuntGameSetupMenu().openMenu(player, game);
        } else if (e.getSlot() == 26) {
            this.chatPlayers.add(player);
            player.sendMessage("§bType the name of the player in the chat to turn them into a runner.");
            player.sendMessage("§7The target player does have to be in the game.");
            player.sendMessage("§cType 'cancel' to cancel.");
            player.closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!this.chatPlayers.contains(e.getPlayer())) return;

        Game game = Game.getGame(e.getPlayer());
        if (game == null) {
            this.chatPlayers.remove(e.getPlayer());
            return;
        }

        e.setCancelled(true);

        if (e.getMessage().equalsIgnoreCase("cancel")) {
            e.getPlayer().sendMessage("§cCancelled the runner adding.");
            this.chatPlayers.remove(e.getPlayer());
            return;
        }

        Player target = Bukkit.getPlayer(e.getMessage());
        if (target == null) {
            e.getPlayer().sendMessage("§cThat player is not online.");
            e.getPlayer().sendMessage("§7Try again, or type 'cancel' to cancel.");
            return;
        }

        GamePlayer targetGP = game.getPlayer(target);
        if (targetGP == null) {
            e.getPlayer().sendMessage("§cThat player is not in your game.");
            e.getPlayer().sendMessage("§7Try again, or type 'cancel' to cancel.");
            return;
        }

        if (targetGP.getPlayerType() == PlayerType.RUNNER) {
            e.getPlayer().sendMessage("§cThat player is already a runner.");
            e.getPlayer().sendMessage("§7Try again, or type 'cancel' to cancel.");
            return;
        }

        targetGP.setPlayerType(PlayerType.RUNNER);
        e.getPlayer().sendMessage("§aYou made " + target.getName() + " a runner!");
        this.chatPlayers.remove(e.getPlayer());
    }

}
