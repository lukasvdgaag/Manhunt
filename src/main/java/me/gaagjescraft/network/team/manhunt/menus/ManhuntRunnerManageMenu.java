package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.utils.Util;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManhuntRunnerManageMenu implements Listener {

    public List<Player> chatPlayers;

    private final Manhunt plugin;

    public ManhuntRunnerManageMenu(Manhunt plugin) {
        this.plugin = plugin;
        this.chatPlayers = Lists.newArrayList();
    }

    public void open(Player player, Game game) {
        Inventory menu = Bukkit.createInventory(null, 27, Util.c(plugin.getCfg().menuRunnerManagerTitle));
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
        player.openInventory(menu);
        updateItems(player, game);
    }

    public void updateItems(Player player, Game game) {
        player.getOpenInventory();
        player.getOpenInventory().getTopInventory();
        Inventory menu = player.getOpenInventory().getTopInventory();

        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, plugin.getItemizer().FILL_ITEM);
        }

        int slot = 0;
        for (GamePlayer gp : game.getPlayers(PlayerType.RUNNER)) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(gp.getUuid());
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            assert meta != null;
            meta.setOwningPlayer(op);
            meta.setDisplayName(Util.c(
                    gp.isHost() ? plugin.getCfg().runnerManagerMenuHostRunnerDisplayname : plugin.getCfg().runnerManagerMenuGeneralRunnerDisplayname
            ).replaceAll("%player%", Objects.requireNonNull(op.getName())));
            List<String> lore = gp.isHost() ? plugin.getCfg().runnerManagerMenuHostRunnerLore : plugin.getCfg().runnerManagerMenuGeneralRunnerLore;
            lore = new ArrayList<>(lore);
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, Util.c(lore.get(i).replaceAll("%player%", op.getName())));
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
            menu.setItem(slot, item);
            slot++;
        }

        menu.setItem(22, plugin.getItemizer().createItem(
                plugin.getCfg().runnerManagerMenuSaveMaterial, 1,
                plugin.getCfg().runnerManagerMenuSaveDisplayname,
                plugin.getCfg().runnerManagerMenuSaveLore
        ));
        menu.setItem(26, plugin.getItemizer().createItem(
                plugin.getCfg().runnerManagerMenuAddRunnerMaterial, 1,
                plugin.getCfg().runnerManagerMenuAddRunnerDisplayname,
                plugin.getCfg().runnerManagerMenuAddRunnerLore
        ));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals(Util.c(plugin.getCfg().menuRunnerManagerTitle))) return;
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
                        OfflinePlayer target = Bukkit.getOfflinePlayer(gp.getUuid());

                        if (runners.size() > 1) {
                            // can remove host because there are multiple hunters.
                            player.sendMessage(Util.c(plugin.getCfg().playerRemoveRunnerMessage.replaceAll("%player%", Objects.requireNonNull(target.getName()))));
                            gp.setPlayerType(PlayerType.HUNTER);
                            game.getRunnerTeleporterMenu().update();
                            plugin.getUtil().playSound(player, plugin.getCfg().runnerRemovedSound, 1, 1);

                            this.chatPlayers.remove(player);
                            open(player, game);
                        } else {
                            // cannot remove any runners because there must be at least one in the game.
                            player.sendMessage(Util.c(plugin.getCfg().menuRunnerManagerCannotRemoveRunnerMessage.replaceAll("%player%", Objects.requireNonNull(target.getName()))));
                            plugin.getUtil().playSound(player, plugin.getCfg().menuRunnerManagerCannotRemoveRunnerSound, 1, 1);
                        }
                    }
                }
            }
        } else if (e.getSlot() == 22) {
            // continue setup
            plugin.getUtil().playSound(player, plugin.getCfg().menuHeadstartSaveSound, 1, 1);
            plugin.getManhuntGameSetupMenu().openMenu(player, game);
        } else if (e.getSlot() == 26) {
            this.chatPlayers.add(player);
            for (String s : plugin.getCfg().runnerAddInstructionsMessage) {
                player.sendMessage(Util.c(s));
            }
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
            e.getPlayer().sendMessage(Util.c(plugin.getCfg().runnerAddCancelledMessage));
            this.chatPlayers.remove(e.getPlayer());
            return;
        }

        Player target = Bukkit.getPlayer(e.getMessage());
        if (target == null) {
            e.getPlayer().sendMessage(Util.c(plugin.getCfg().playerNotOnlineMessage));
            e.getPlayer().sendMessage(Util.c(plugin.getCfg().runnerAddTryAgainMessage));
            return;
        }

        GamePlayer targetGP = game.getPlayer(target);
        if (targetGP == null) {
            e.getPlayer().sendMessage(Util.c(plugin.getCfg().targetPlayerNotIngameMessage.replaceAll("%player%", target.getName())));
            e.getPlayer().sendMessage(Util.c(plugin.getCfg().runnerAddTryAgainMessage));
            return;
        }

        if (targetGP.getPlayerType() == PlayerType.RUNNER) {
            e.getPlayer().sendMessage(Util.c(plugin.getCfg().targetPlayerAlreadyRunnerMessage.replaceAll("%player%", target.getName())));
            e.getPlayer().sendMessage(Util.c(plugin.getCfg().runnerAddTryAgainMessage));
            return;
        }

        targetGP.setPlayerType(PlayerType.RUNNER);
        e.getPlayer().sendMessage(Util.c(plugin.getCfg().playerAddRunnerMessage.replaceAll("%player%", target.getName())));
        this.chatPlayers.remove(e.getPlayer());
    }

}
