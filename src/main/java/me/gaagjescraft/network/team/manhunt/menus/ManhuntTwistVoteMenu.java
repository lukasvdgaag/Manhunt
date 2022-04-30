package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.TwistVote;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class ManhuntTwistVoteMenu implements Listener {

    private final List<Player> voteCooldowns = Lists.newArrayList();

    public void open(Player player, Game game) {
        Inventory teleportMenu = Bukkit.createInventory(null, 45, Util.c(Manhunt.get().getCfg().menuTwistVoteTitle));

        for (int i = 0; i < teleportMenu.getSize(); i++) {
            teleportMenu.setItem(i, Itemizer.FILL_ITEM);
        }

        Util.playSound(player, Manhunt.get().getCfg().openTwistVoteMenuSound, 1, 1);
        player.openInventory(teleportMenu);
        updateItems(player, game);
    }

    public void updateItems(Player player, Game game) {
        player.getOpenInventory();
        player.getOpenInventory().getTopInventory();
        Inventory teleportMenu = player.getOpenInventory().getTopInventory();

        int hardcoreVotes = game.getTwistVotes(TwistVote.HARDCORE);
        ItemStack hardcoreItem = Itemizer.createItem((Manhunt.get().getCfg().twistVoteMenuHardcoreMaterial), Math.max(hardcoreVotes, 1),
                Manhunt.get().getCfg().twistVoteMenuHardcoreDisplayname,
                Util.r(Manhunt.get().getCfg().twistVoteMenuHardcoreLore, "%votes%", hardcoreVotes + ""));
        teleportMenu.setItem(10, hardcoreItem);

        int extraHealthVotes = game.getTwistVotes(TwistVote.EXTRA_HEALTH);
        ItemStack healthitem = Itemizer.createItem(
                (Manhunt.get().getCfg().twistVoteMenuExtraHealthMaterial), Math.max(extraHealthVotes, 1), Manhunt.get().getCfg().twistVoteMenuExtraHealthDisplayname,
                Util.r(Manhunt.get().getCfg().twistVoteMenuExtraHealthLore, "%votes%", extraHealthVotes + ""));
        teleportMenu.setItem(12, healthitem);

        int blindnessVotes = game.getTwistVotes(TwistVote.BLINDNESS);
        ItemStack blindnessitem = Itemizer.createItem((Manhunt.get().getCfg().twistVoteMenuBlindnessMaterial), Math.max(blindnessVotes, 1), Manhunt.get().getCfg().twistVoteMenuBlindnessDisplayname,
                Util.r(Manhunt.get().getCfg().twistVoteMenuBlindnessLore, "%votes%", blindnessVotes + ""));
        teleportMenu.setItem(14, blindnessitem);

        int yeetVotes = game.getTwistVotes(TwistVote.RANDOM_YEET);
        ItemStack yeetitem = Itemizer.createItem((Manhunt.get().getCfg().twistVoteMenuRandomYeetMaterial), Math.max(yeetVotes, 1), Manhunt.get().getCfg().twistVoteMenuRandomYeetDisplayname,
                Util.r(Manhunt.get().getCfg().twistVoteMenuRandomYeetLore, "%votes%", yeetVotes + ""));
        teleportMenu.setItem(16, yeetitem);

        int speedVotes = game.getTwistVotes(TwistVote.SPEED_BOOST);
        ItemStack speeditem = Itemizer.createItem((Manhunt.get().getCfg().twistVoteMenuSpeedBoostMaterial), Math.max(speedVotes, 1), Manhunt.get().getCfg().twistVoteMenuSpeedBoostDisplayname,
                Util.r(Manhunt.get().getCfg().twistVoteMenuSpeedBoostLore, "%votes%", speedVotes + ""));
        teleportMenu.setItem(29, speeditem);

        int noneVotes = game.getTwistVotes(TwistVote.NONE);
        ItemStack noneitem = Itemizer.createItem((Manhunt.get().getCfg().twistVoteMenuNoneMaterial), Math.max(noneVotes, 1), Manhunt.get().getCfg().twistVoteMenuNoneDisplayname,
                Util.r(Manhunt.get().getCfg().twistVoteMenuNoneLore, "%votes%", noneVotes + ""));
        teleportMenu.setItem(31, noneitem);

        int acidRainVotes = game.getTwistVotes(TwistVote.ACID_RAIN);
        ItemStack item = new ItemStack(Material.valueOf(Manhunt.get().getCfg().twistVoteMenuAcidRainMaterial));
        item.setAmount(Math.max(acidRainVotes, 1));
        ItemMeta meta = item.getItemMeta();
        if (item.getType() == Material.TIPPED_ARROW || item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) {
            assert meta != null;
            ((PotionMeta) meta).setBasePotionData(new PotionData(PotionType.POISON));
        }
        assert meta != null;
        meta.setDisplayName(Util.c(Manhunt.get().getCfg().twistVoteMenuAcidRainDisplayname));
        List<String> lore = Manhunt.get().getCfg().twistvoteMenuAcidRainLore;
        lore = new ArrayList<>(lore);
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, Util.c(lore.get(i).replaceAll("%votes%", acidRainVotes + "")));
        }
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        teleportMenu.setItem(33, item);
        applyEnchants(player);
    }

    private void applyEnchants(Player player) {
        player.getOpenInventory();
        player.getOpenInventory().getTopInventory();
        Inventory inventory = player.getOpenInventory().getTopInventory();

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);
        if (gp.getTwistVoted() == null) return;

        int slot = switch (gp.getTwistVoted()) {
            case HARDCORE -> 10;
            case EXTRA_HEALTH -> 12;
            case BLINDNESS -> 14;
            case RANDOM_YEET -> 16;
            case SPEED_BOOST -> 29;
            case NONE -> 31;
            case ACID_RAIN -> 33;
        };

        ItemStack item = inventory.getItem(slot);
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        item.setItemMeta(meta);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals(Util.c(Manhunt.get().getCfg().menuTwistVoteTitle))) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;
        Player player = (Player) e.getWhoClicked();

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (e.getSlot() == 10 && gp.getTwistVoted() != TwistVote.HARDCORE && !voteCooldowns.contains(player)) {
            Util.playSound(player, Manhunt.get().getCfg().twistVoteSound, 1, 2);
            gp.setTwistVoted(TwistVote.HARDCORE);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        } else if (e.getSlot() == 12 && gp.getTwistVoted() != TwistVote.EXTRA_HEALTH && !voteCooldowns.contains(player)) {
            Util.playSound(player, Manhunt.get().getCfg().twistVoteSound, 1, 2);
            gp.setTwistVoted(TwistVote.EXTRA_HEALTH);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        } else if (e.getSlot() == 14 && gp.getTwistVoted() != TwistVote.BLINDNESS && !voteCooldowns.contains(player)) {
            Util.playSound(player, Manhunt.get().getCfg().twistVoteSound, 1, 2);
            gp.setTwistVoted(TwistVote.BLINDNESS);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        } else if (e.getSlot() == 16 && gp.getTwistVoted() != TwistVote.RANDOM_YEET && !voteCooldowns.contains(player)) {
            Util.playSound(player, Manhunt.get().getCfg().twistVoteSound, 1, 2);
            gp.setTwistVoted(TwistVote.RANDOM_YEET);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        } else if (e.getSlot() == 29 && gp.getTwistVoted() != TwistVote.SPEED_BOOST && !voteCooldowns.contains(player)) {
            Util.playSound(player, Manhunt.get().getCfg().twistVoteSound, 1, 2);
            gp.setTwistVoted(TwistVote.SPEED_BOOST);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        } else if (e.getSlot() == 31 && gp.getTwistVoted() != TwistVote.NONE && !voteCooldowns.contains(player)) {
            Util.playSound(player, Manhunt.get().getCfg().twistVoteSound, 1, 2);
            gp.setTwistVoted(TwistVote.NONE);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        } else if (e.getSlot() == 33 && gp.getTwistVoted() != TwistVote.ACID_RAIN && !voteCooldowns.contains(player)) {
            Util.playSound(player, Manhunt.get().getCfg().twistVoteSound, 1, 2);
            gp.setTwistVoted(TwistVote.ACID_RAIN);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        }


    }

}
