package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.TwistVote;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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

import java.util.List;

public class ManhuntTwistVoteMenu implements Listener {

    private List<Player> voteCooldowns = Lists.newArrayList();

    public void open(Player player, Game game) {
        Inventory teleportMenu = Bukkit.createInventory(null, 45, "§6§lManHunt Twist Vote");

        for (int i = 0; i < teleportMenu.getSize(); i++) {
            teleportMenu.setItem(i, Itemizer.FILL_ITEM);
        }

        player.openInventory(teleportMenu);
        updateItems(player, game);
    }

    public void updateItems(Player player, Game game) {
        if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null) return;
        Inventory teleportMenu = player.getOpenInventory().getTopInventory();

        int extraHealthVotes = game.getTwistVotes(TwistVote.EXTRA_HEALTH);
        ItemStack healthitem = Itemizer.createItem(
                Material.GOLDEN_APPLE, Math.max(extraHealthVotes, 1), "§bExtra Health",
                Lists.newArrayList("", "§7Hunters will have double", "§7the health runners have.", "", "§e" + extraHealthVotes + " votes."));
        teleportMenu.setItem(12, healthitem);

        int blindnessVotes = game.getTwistVotes(TwistVote.BLINDNESS);
        ItemStack blindnessitem = Itemizer.createItem(Material.FLINT, Math.max(blindnessVotes, 1), "§bRandom Blindness",
                Lists.newArrayList("", "§7Runners will randomly gain", "§7blindness while playing.", "", "§e" + blindnessVotes + " votes."));
        teleportMenu.setItem(14, blindnessitem);

        int noneVotes = game.getTwistVotes(TwistVote.NONE);
        ItemStack noneitem = Itemizer.createItem(Material.BARRIER, Math.max(noneVotes, 1), "§bNo Twists",
                Lists.newArrayList("", "§7The game will just be plain", "§7manhunt, without any twists.", "", "§e" + noneVotes + " votes."));
        teleportMenu.setItem(19, noneitem);

        int yeetVotes = game.getTwistVotes(TwistVote.RANDOM_YEET);
        ItemStack yeetitem = Itemizer.createItem(Material.FIREWORK_ROCKET, Math.max(yeetVotes, 1), "§bRandom Yeets",
                Lists.newArrayList("", "§7Runners will randomly get", "§7yeeted into a random", "§7direction while playing.", "",
                        "§e" + yeetVotes + " votes."));
        teleportMenu.setItem(25, yeetitem);

        int speedVotes = game.getTwistVotes(TwistVote.SPEED_BOOST);
        ItemStack speeditem = Itemizer.createItem(Material.FEATHER, Math.max(speedVotes, 1), "§bSpeed Boost",
                Lists.newArrayList("", "§7Hunters will have speedboost,", "§7while runners remain slow.", "", "§e" + speedVotes + " votes."));
        teleportMenu.setItem(30, speeditem);

        int acidRainVotes = game.getTwistVotes(TwistVote.ACID_RAIN);
        ItemStack item = new ItemStack(Material.TIPPED_ARROW);
        item.setAmount(Math.max(acidRainVotes, 1));
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setBasePotionData(new PotionData(PotionType.POISON));
        meta.setDisplayName("§bAcid Rain");
        meta.setLore(Lists.newArrayList("", "§7Rain will hurt everyone's", "§7skin when in contact.", "§7Get sheltered or you die!", "", "§e" + acidRainVotes + " votes."));
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        teleportMenu.setItem(32, item);
        applyEnchants(player);
    }

    private void applyEnchants(Player player) {
        if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null) return;
        Inventory inventory = player.getOpenInventory().getTopInventory();

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);
        if (gp.getTwistVoted() == null) return;

        int slot = 0;
        switch (gp.getTwistVoted()) {
            case NONE:
                slot = 19;
                break;
            case ACID_RAIN:
                slot = 32;
                break;
            case BLINDNESS:
                slot = 14;
                break;
            case SPEED_BOOST:
                slot = 30;
                break;
            case EXTRA_HEALTH:
                slot = 12;
                break;
            case RANDOM_YEET:
                slot = 25;
                break;
        }

        ItemStack item = inventory.getItem(slot);
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        item.setItemMeta(meta);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals("§6§lManHunt Twist Vote")) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;
        Player player = (Player) e.getWhoClicked();

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (e.getSlot() == 19 && gp.getTwistVoted() != TwistVote.NONE && !voteCooldowns.contains(player)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            gp.setTwistVoted(TwistVote.NONE);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        } else if (e.getSlot() == 14 && gp.getTwistVoted() != TwistVote.BLINDNESS && !voteCooldowns.contains(player)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            gp.setTwistVoted(TwistVote.BLINDNESS);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        } else if (e.getSlot() == 12 && gp.getTwistVoted() != TwistVote.EXTRA_HEALTH && !voteCooldowns.contains(player)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            gp.setTwistVoted(TwistVote.EXTRA_HEALTH);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        } else if (e.getSlot() == 25 && gp.getTwistVoted() != TwistVote.RANDOM_YEET && !voteCooldowns.contains(player)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            gp.setTwistVoted(TwistVote.RANDOM_YEET);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        } else if (e.getSlot() == 30 && gp.getTwistVoted() != TwistVote.SPEED_BOOST && !voteCooldowns.contains(player)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            gp.setTwistVoted(TwistVote.SPEED_BOOST);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        } else if (e.getSlot() == 32 && gp.getTwistVoted() != TwistVote.ACID_RAIN && !voteCooldowns.contains(player)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            gp.setTwistVoted(TwistVote.ACID_RAIN);
            voteCooldowns.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> voteCooldowns.remove(player), 20L);
            Manhunt.get().getManhuntTwistVoteMenu().updateItems(player, game);
        }


    }

}
