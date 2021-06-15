package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ManhuntMainMenu implements Listener {

    public void updateSlot(int slot, Player player) {
        InventoryView view = player.getOpenInventory();
        if (view == null || view.getTopInventory() == null) return;

        Inventory gui = view.getTopInventory();
        int tokenCount = (int) Manhunt.get().getEconomy().getBalance(player);

        if (slot == 13) {

            ItemStack tokens;
            ItemMeta tokensMeta;
            if (Manhunt.get().getEconomy().hasBalance(player, 1) || player.hasPermission("manhunt.hostgame")) {
                tokens = new ItemStack(Material.EMERALD_BLOCK, 1);
            } else {
                tokens = new ItemStack(Material.REDSTONE_BLOCK, 1);
            }
            tokensMeta = tokens.getItemMeta();
            tokensMeta.setDisplayName("§aYour Tokens");
            tokensMeta.setLore(Lists.newArrayList(
                    "§7Use your tokens to host",
                    "§7your own Manhunt games.",
                    "",
                    "§bYou have §d" + tokenCount +
                            "§b tokens."));
            tokensMeta.addItemFlags(ItemFlag.values());
            tokens.setItemMeta(tokensMeta);

            gui.setItem(13, tokens);


        } else if (slot == 29) {

            ItemStack tokens = new ItemStack(Material.EMERALD, 1);
            ItemMeta tokensMeta = tokens.getItemMeta();
            tokensMeta.addItemFlags(ItemFlag.values());

            int protocol = Manhunt.get().getUtil().getProtocol(player);

            if (protocol == -1 || protocol >= Manhunt.get().getCfg().minimumClientProtocolVersion) {
                tokensMeta.setDisplayName("§aHost a game");
                if (Manhunt.get().getEconomy().hasBalance(player, 1) || player.hasPermission("manhunt.hostgame")) {
                    tokensMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                    tokensMeta.setLore(Lists.newArrayList("§7Host your own Manhunt game", "§7by spending 1 token.", "§dYou get to be runner!", "", "§bYou have §e" + tokenCount + " §btokens available.", "", "§e► Click to host a game. ◄"));
                } else {
                    tokensMeta.setLore(Lists.newArrayList("§7Host your own Manhunt game", "§7by spending 1 token.", "§dYou get to be runner!", "", "§bYou have §e" + tokenCount + " §btokens available.", "", "§c► You don't have enough tokens. ◄"));
                }
            } else {
                tokensMeta.setDisplayName("§aHost a game §c(Unavailable)");
                tokensMeta.setLore(Lists.newArrayList("§7Host your own Manhunt game", "§7by spending 1 token.", "§dYou get to be runner!", "", "§bYou have §e" + tokenCount + " §btoken available.", "", "§cYou must be in 1.16+ to create games!", "§cPlease switch versions if you wish to proceed."));
            }
            tokens.setItemMeta(tokensMeta);

            gui.setItem(29, tokens);

        } else if (slot == 33) {

            ItemStack tokens = new ItemStack(Material.DIAMOND_SWORD, 1);
            ItemMeta tokensMeta = tokens.getItemMeta();
            tokensMeta.addItemFlags(ItemFlag.values());
            int protocol = Manhunt.get().getUtil().getProtocol(player);

            if (protocol == -1 || protocol >= Manhunt.get().getCfg().minimumClientProtocolVersion) {
                tokensMeta.setDisplayName("§bJoin a game");
                tokensMeta.setLore(Lists.newArrayList("§7Play along with existing", "§7Manhunt games across the server.", "", "§e► Click to open the join menu. ◄"));
            } else {
                tokensMeta.setDisplayName("§bJoin a game §c(Unavailable)");
                tokensMeta.setLore(Lists.newArrayList("§7Play along with existing", "§7Manhunt games across the server.", "", "§cYou must be in 1.16+ to create games!", "§cPlease switch versions if you wish to proceed."));
            }
            tokens.setItemMeta(tokensMeta);

            gui.setItem(33, tokens);

        }
    }

    public void openMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "Manhunt");
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);

        for (int i = 0; i < 54; i++) {
            gui.setItem(i, Itemizer.FILL_ITEM);
        }

        ItemStack paper = new ItemStack(Material.BOOK, 1);
        ItemMeta pmeta = paper.getItemMeta();
        pmeta.setDisplayName("§dStore");
        pmeta.setLore(Lists.newArrayList("§7Out of tokens?", "§7Click here to get a link to", "§7our §bstore§7 to upgrade your", "§brank§7 and get free tokens.", "", "§e► Click to get our store link. ◄"));
        paper.setItemMeta(pmeta);

        gui.setItem(53, paper);
        gui.setItem(49, Itemizer.CLOSE_ITEM);

        player.openInventory(gui);

        updateSlot(13, player);
        updateSlot(29, player);
        updateSlot(33, player);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals("Manhunt")) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player player = (Player) e.getWhoClicked();

        if (e.getSlot() == 49) {
            player.closeInventory();
        } else if (e.getSlot() == 53) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            player.spigot().sendMessage(
                    new ComponentBuilder("Click here to gain ").color(ChatColor.YELLOW).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://store.exodusmc.world/")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Upgrade your rank here!")))
                            .append("tokens").color(ChatColor.LIGHT_PURPLE).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://store.exodusmc.world/")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Upgrade your rank here!")))
                            .append(" by upgrading your rank!").color(ChatColor.YELLOW).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://store.exodusmc.world/")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Upgrade your rank here!"))).create());
            player.closeInventory();
        } else if (e.getSlot() == 29) {
            int protocol = Manhunt.get().getUtil().getProtocol(player);

            if (protocol == -1 || protocol >= Manhunt.get().getCfg().minimumClientProtocolVersion) {
                if (Manhunt.get().getEconomy().hasBalance(player, 1) || player.hasPermission("manhunt.hostgame")) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    Manhunt.get().getManhuntGameSetupMenu().openMenu(player, null);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    player.sendMessage("§cYou don't have enough tokens to host a game.");
                }
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                player.sendMessage("§cYou are playing on an outdated version of Minecraft. If you wish to host games, please join with 1.16+.");
            }
        } else if (e.getSlot() == 33) {
            int protocol = Manhunt.get().getUtil().getProtocol(player);

            if (protocol == -1 || protocol >= Manhunt.get().getCfg().minimumClientProtocolVersion) {
                Manhunt.get().getManhuntGamesMenu().openMenu(player);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                player.sendMessage("§cYou are playing on an outdated version of Minecraft. If you wish to join games, please join with 1.16+.");
            }
        }

    }

}
