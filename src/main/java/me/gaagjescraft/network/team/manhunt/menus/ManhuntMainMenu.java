package me.gaagjescraft.network.team.manhunt.menus;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ManhuntMainMenu implements Listener {

    private final Manhunt plugin;

    public ManhuntMainMenu(Manhunt plugin) {
        this.plugin = plugin;
    }

    public void updateSlot(int slot, Player player) {
        InventoryView view = player.getOpenInventory();
        view.getTopInventory();

        Inventory gui = view.getTopInventory();
        int tokenCount = plugin.getEconomy() == null ? 0 : (int) plugin.getEconomy().getBalance(player);

        if (slot == 13) {
            ItemStack tokens;
            if (player.hasPermission("manhunt.hostgame") || (plugin.getEconomy() != null && plugin.getCfg().pricePerGame > 0 && plugin.getEconomy().hasBalance(player, 1))) {
                tokens = plugin.getItemizer().createItem(plugin.getCfg().mainMenuBalanceInfoEnoughMaterial, 1,
                        Util.c(plugin.getCfg().mainMenuBalanceInfoEnoughDisplayname),
                        plugin.getUtil().replace(plugin.getCfg().mainMenuBalanceInfoEnoughLore, "%balance%", tokenCount + ""));
            } else {
                tokens = plugin.getItemizer().createItem(plugin.getCfg().mainMenuBalanceInfoNotEnoughMaterial, 1,
                        Util.c(plugin.getCfg().mainMenuBalanceInfoNotEnoughDisplayname),
                        plugin.getUtil().replace(plugin.getCfg().mainMenuBalanceInfoNotEnoughLore, "%balance%", tokenCount + ""));
            }
            gui.setItem(13, tokens);
        } else if (slot == 29) {
            ItemStack tokens;
            ItemMeta tokensMeta;

            int protocol = plugin.getUtil().getProtocol(player);
            if (protocol == -1 || protocol >= plugin.getCfg().minimumClientProtocolVersion) {
                if (player.hasPermission("manhunt.hostgame") || (plugin.getEconomy() != null && plugin.getCfg().pricePerGame > 0 && plugin.getEconomy().hasBalance(player, 1))) {
                    tokens = plugin.getItemizer().createItem(plugin.getCfg().mainMenuHostGameCanHostMaterial, 1,
                            Util.c(plugin.getCfg().mainMenuHostGameCanHostDisplayname), plugin.getUtil().replace(plugin.getUtil().replace(plugin.getCfg().mainMenuHostGameCanHostLore, "%balance%", tokenCount + ""), "%price%", plugin.getCfg().pricePerGame + ""));
                    tokensMeta = tokens.getItemMeta();
                    assert tokensMeta != null;
                    tokensMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                } else {
                    tokens = plugin.getItemizer().createItem(plugin.getCfg().mainMenuHostGameCannotHostMaterial, 1,
                            Util.c(plugin.getCfg().mainMenuHostGameCannotHostDisplayname), plugin.getUtil().replace(plugin.getUtil().replace(plugin.getCfg().mainMenuHostGameCannotHostLore, "%balance%", tokenCount + ""), "%price%", plugin.getCfg().pricePerGame + ""));
                    tokensMeta = tokens.getItemMeta();
                }
            } else {
                tokens = plugin.getItemizer().createItem(plugin.getCfg().mainMenuHostGameInvalidVersionMaterial, 1,
                        Util.c(plugin.getCfg().mainMenuHostGameInvalidVersionDisplayname), plugin.getUtil().replace(plugin.getUtil().replace(plugin.getCfg().mainMenuHostGameInvalidVersionLore, "%balance%", tokenCount + ""), "%price%", plugin.getCfg().pricePerGame + ""));
                tokensMeta = tokens.getItemMeta();
            }
            tokens.setItemMeta(tokensMeta);
            gui.setItem(29, tokens);
        } else if (slot == 33) {
            ItemStack tokens;
            int protocol = plugin.getUtil().getProtocol(player);
            if (protocol == -1 || protocol >= plugin.getCfg().minimumClientProtocolVersion) {
                tokens = plugin.getItemizer().createItem(plugin.getCfg().mainMenuJoinGameMaterial, 1,
                        Util.c(plugin.getCfg().mainMenuJoinGameDisplayname), plugin.getCfg().mainMenuJoinGameLore);
            } else {
                tokens = plugin.getItemizer().createItem(plugin.getCfg().mainMenuJoinGameInvalidVersionMaterial, 1,
                        Util.c(plugin.getCfg().mainMenuJoinGameInvalidVersionDisplayname), plugin.getCfg().mainMenuJoinGameInvalidVersionLore);
            }
            gui.setItem(33, tokens);
        }
    }

    public void openMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, Util.c(plugin.getCfg().mainMenuTitle));
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);

        for (int i = 0; i < 54; i++) {
            gui.setItem(i, plugin.getItemizer().FILL_ITEM);
        }

        gui.setItem(53, plugin.getItemizer().createItem(plugin.getCfg().mainMenuStoreMaterial, 1, Util.c(plugin.getCfg().mainMenuStoreDisplayname), plugin.getCfg().mainMenuStoreLore));
        gui.setItem(49, plugin.getItemizer().CLOSE_ITEM);

        player.openInventory(gui);

        updateSlot(13, player);
        updateSlot(29, player);
        updateSlot(33, player);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals(Util.c(plugin.getCfg().mainMenuTitle))) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player player = (Player) e.getWhoClicked();

        if (e.getSlot() == 49) {
            player.closeInventory();
        } else if (e.getSlot() == 53) {
            plugin.getUtil().playSound(player, plugin.getCfg().mainMenuClickStoreItemSound, 1, 1);
            player.spigot().sendMessage(ChatMessageType.CHAT, ComponentSerializer.parse(plugin.getCfg().mainMenuClickStoreItemMessage));
            player.closeInventory();
        } else if (e.getSlot() == 29) {
            int protocol = plugin.getUtil().getProtocol(player);

            if (protocol == -1 || protocol >= plugin.getCfg().minimumClientProtocolVersion) {
                if (player.hasPermission("manhunt.hostgame") || (plugin.getEconomy() != null && plugin.getCfg().pricePerGame > 0 && plugin.getEconomy().hasBalance(player, 1))) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    plugin.getManhuntGameSetupMenu().openMenu(player, null);
                } else if (plugin.getEconomy() != null) {
                    // player does not have the permission, and not enough money.
                    plugin.getUtil().playSound(player, plugin.getCfg().cantHostGameSound, 1, 1);
                    player.sendMessage(Util.c(plugin.getCfg().notEnoughMoneyHostingGameMessage));
                } else {
                    // player does not have the permission, no economy found.
                    plugin.getUtil().playSound(player, plugin.getCfg().cantHostGameSound, 1, 1);
                    player.sendMessage(Util.c(plugin.getCfg().noPermissionHostingGameMessage));
                }
            } else {
                plugin.getUtil().playSound(player, plugin.getCfg().cantHostGameSound, 1, 1);
                player.sendMessage(Util.c(plugin.getCfg().cannotHostGameInvalidVersionMessage));
            }
        } else if (e.getSlot() == 33) {
            int protocol = plugin.getUtil().getProtocol(player);

            if (protocol == -1 || protocol >= plugin.getCfg().minimumClientProtocolVersion) {
                plugin.getManhuntGamesMenu().openMenu(player);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                player.sendMessage(Util.c(plugin.getCfg().cannotJoinGameInvalidVersionMessage));
            }
        }

    }

}
