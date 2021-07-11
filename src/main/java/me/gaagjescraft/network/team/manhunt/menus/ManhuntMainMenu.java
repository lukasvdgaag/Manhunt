package me.gaagjescraft.network.team.manhunt.menus;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import net.md_5.bungee.api.chat.TextComponent;
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

    public void updateSlot(int slot, Player player) {
        InventoryView view = player.getOpenInventory();
        if (view == null || view.getTopInventory() == null) return;

        Inventory gui = view.getTopInventory();
        int tokenCount = Manhunt.get().getEconomy() == null ? 0 : (int) Manhunt.get().getEconomy().getBalance(player);

        if (slot == 13) {
            ItemStack tokens;
            if (player.hasPermission("manhunt.hostgame") || (Manhunt.get().getEconomy() != null && Manhunt.get().getCfg().pricePerGame > 0 && Manhunt.get().getEconomy().hasBalance(player, 1))) {
                tokens = Itemizer.createItem(Manhunt.get().getCfg().mainMenuBalanceInfoEnoughMaterial, 1,
                        Util.c(Manhunt.get().getCfg().mainMenuBalanceInfoEnoughDisplayname),
                        Manhunt.get().getUtil().replace(Manhunt.get().getCfg().mainMenuBalanceInfoEnoughLore, "%balance%", tokenCount + ""));
            } else {
                tokens = Itemizer.createItem(Manhunt.get().getCfg().mainMenuBalanceInfoNotEnoughMaterial, 1,
                        Util.c(Manhunt.get().getCfg().mainMenuBalanceInfoNotEnoughDisplayname),
                        Manhunt.get().getUtil().replace(Manhunt.get().getCfg().mainMenuBalanceInfoNotEnoughLore, "%balance%", tokenCount + ""));
            }
            gui.setItem(13, tokens);
        } else if (slot == 29) {
            ItemStack tokens;
            ItemMeta tokensMeta;

            int protocol = Manhunt.get().getUtil().getProtocol(player);
            if (protocol == -1 || protocol >= Manhunt.get().getCfg().minimumClientProtocolVersion) {
                if (player.hasPermission("manhunt.hostgame") || (Manhunt.get().getEconomy() != null && Manhunt.get().getCfg().pricePerGame > 0 && Manhunt.get().getEconomy().hasBalance(player, 1))) {
                    tokens = Itemizer.createItem(Manhunt.get().getCfg().mainMenuHostGameCanHostMaterial, 1,
                            Util.c(Manhunt.get().getCfg().mainMenuHostGameCanHostDisplayname), Manhunt.get().getUtil().replace(Manhunt.get().getCfg().mainMenuHostGameCanHostLore, "%balance%", tokenCount + ""));
                    tokensMeta = tokens.getItemMeta();
                    tokensMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                } else {
                    tokens = Itemizer.createItem(Manhunt.get().getCfg().mainMenuHostGameCannotHostMaterial, 1,
                            Util.c(Manhunt.get().getCfg().mainMenuHostGameCannotHostDisplayname), Manhunt.get().getUtil().replace(Manhunt.get().getCfg().mainMenuHostGameCannotHostLore, "%balance%", tokenCount + ""));
                    tokensMeta = tokens.getItemMeta();
                }
            } else {
                tokens = Itemizer.createItem(Manhunt.get().getCfg().mainMenuHostGameInvalidVersionMaterial, 1,
                        Util.c(Manhunt.get().getCfg().mainMenuHostGameInvalidVersionDisplayname), Manhunt.get().getUtil().replace(Manhunt.get().getCfg().mainMenuHostGameInvalidVersionLore, "%balance%", tokenCount + ""));
                tokensMeta = tokens.getItemMeta();
            }
            tokens.setItemMeta(tokensMeta);
            gui.setItem(29, tokens);
        } else if (slot == 33) {
            ItemStack tokens;
            int protocol = Manhunt.get().getUtil().getProtocol(player);
            if (protocol == -1 || protocol >= Manhunt.get().getCfg().minimumClientProtocolVersion) {
                tokens = Itemizer.createItem(Manhunt.get().getCfg().mainMenuJoinGameMaterial, 1,
                        Util.c(Manhunt.get().getCfg().mainMenuJoinGameDisplayname), Manhunt.get().getCfg().mainMenuJoinGameLore);
            } else {
                tokens = Itemizer.createItem(Manhunt.get().getCfg().mainMenuJoinGameInvalidVersionMaterial, 1,
                        Util.c(Manhunt.get().getCfg().mainMenuJoinGameInvalidVersionDisplayname), Manhunt.get().getCfg().mainMenuJoinGameInvalidVersionLore);
            }
            gui.setItem(33, tokens);
        }
    }

    public void openMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, Util.c(Manhunt.get().getCfg().mainMenuTitle));
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);

        for (int i = 0; i < 54; i++) {
            gui.setItem(i, Itemizer.FILL_ITEM);
        }

        gui.setItem(53, Itemizer.createItem(Manhunt.get().getCfg().mainMenuStoreMaterial, 1, Util.c(Manhunt.get().getCfg().mainMenuStoreDisplayname), Manhunt.get().getCfg().mainMenuStoreLore));
        gui.setItem(49, Itemizer.CLOSE_ITEM);

        player.openInventory(gui);

        updateSlot(13, player);
        updateSlot(29, player);
        updateSlot(33, player);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals(Util.c(Manhunt.get().getCfg().mainMenuTitle))) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player player = (Player) e.getWhoClicked();

        if (e.getSlot() == 49) {
            player.closeInventory();
        } else if (e.getSlot() == 53) {
            player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().mainMenuClickStoreItemSound), 1, 1);
            player.spigot().sendMessage(TextComponent.fromLegacyText(Manhunt.get().getCfg().mainMenuClickStoreItemMessage));
            player.closeInventory();
        } else if (e.getSlot() == 29) {
            int protocol = Manhunt.get().getUtil().getProtocol(player);

            if (protocol == -1 || protocol >= Manhunt.get().getCfg().minimumClientProtocolVersion) {
                if (player.hasPermission("manhunt.hostgame") || (Manhunt.get().getEconomy() != null && Manhunt.get().getCfg().pricePerGame > 0 && Manhunt.get().getEconomy().hasBalance(player, 1))) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    Manhunt.get().getManhuntGameSetupMenu().openMenu(player, null);
                } else if (Manhunt.get().getEconomy() != null) {
                    // player does not have the permission, and not enough money.
                    player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().cantHostGameSound), 1, 1);
                    player.sendMessage(Util.c(Manhunt.get().getCfg().notEnoughMoneyHostingGameMessage));
                } else {
                    // player does not have the permission, no economy found.
                    player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().cantHostGameSound), 1, 1);
                    player.sendMessage(Util.c(Manhunt.get().getCfg().noPermissionHostingGameMessage));
                }
            } else {
                player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().cantHostGameSound), 1, 1);
                player.sendMessage(Util.c(Manhunt.get().getCfg().cannotHostGameInvalidVersionMessage));
            }
        } else if (e.getSlot() == 33) {
            int protocol = Manhunt.get().getUtil().getProtocol(player);

            if (protocol == -1 || protocol >= Manhunt.get().getCfg().minimumClientProtocolVersion) {
                Manhunt.get().getManhuntGamesMenu().openMenu(player);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                player.sendMessage(Util.c(Manhunt.get().getCfg().cannotJoinGameInvalidVersionMessage));
            }
        }

    }

}
