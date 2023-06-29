package me.gaagjescraft.network.team.manhunt.menus.twist;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.TwistVote;
import me.gaagjescraft.network.team.manhunt.utils.LayoutUtils;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import me.gaagjescraft.network.team.manhunt.utils.config.icon.IconConfig;
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

import java.util.*;

public class ManhuntTwistVoteMenu implements Listener {

    private final List<Player> voteCooldowns = Lists.newArrayList();

    private final List<TwistItem> twistItems;

    private final Manhunt plugin;

    private final BiMap<Integer, TwistVote> twistSlots;

    public ManhuntTwistVoteMenu(Manhunt plugin) {
        this.plugin = plugin;
        this.twistItems = new ArrayList<>();
        this.twistSlots = HashBiMap.create();

        addBuiltinTwists();
    }

    public void addTwistItem(TwistItem twistItem) {
        twistItems.add(twistItem);
    }

    public void open(Player player, Game game) {
        Inventory teleportMenu = Bukkit.createInventory(null, 45, Util.c(plugin.getCfg().menuTwistVoteTitle));

        for (int i = 0; i < teleportMenu.getSize(); i++) {
            teleportMenu.setItem(i, plugin.getItemizer().FILL_ITEM);
        }

        plugin.getUtil().playSound(player, plugin.getCfg().openTwistVoteMenuSound, 1, 1);
        player.openInventory(teleportMenu);
        updateItems(player, game);
    }

    public void updateItems(Player player, Game game) {
        player.getOpenInventory();
        player.getOpenInventory().getTopInventory();
        Inventory teleportMenu = player.getOpenInventory().getTopInventory();

        List<Integer> slots = LayoutUtils.getLayoutForItems(twistItems.size(), teleportMenu.getSize());
        int index = 0;
        for (TwistItem twistItem : twistItems) {
            int votes = game.getTwistVotes(twistItem.getVote());
            List<String> lore = Util.r(twistItem.getLore(), "%votes%", String.valueOf(votes));

            ItemStack item = plugin.getItemizer().createItem(
                    twistItem.getIcon(),
                    Math.max(votes, 1),
                    twistItem.getDisplayName(),
                    lore);

            twistItem.getCustomizer().accept(item);

            int slot = slots.get(index++);
            twistSlots.put(slot, twistItem.getVote());
            teleportMenu.setItem(slot, item);
        }

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

        int slot = twistSlots.inverse().get(gp.getTwistVoted());

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
        if (!e.getView().getTitle().equals(Util.c(plugin.getCfg().menuTwistVoteTitle))) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;
        Player player = (Player) e.getWhoClicked();

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        TwistVote twistVote = twistSlots.get(e.getSlot());
        if (twistVote == null) {
            return;
        }

        if (gp.getTwistVoted() != twistVote && !voteCooldowns.contains(player)) {
            gp.setTwistVoted(twistVote);
            voteCooldowns.add(player);
            plugin.getManhuntTwistVoteMenu().updateItems(player, game);
            plugin.getUtil().playSound(player, plugin.getCfg().twistVoteSound, 1, 2);

            Bukkit.getScheduler().runTaskLater(plugin, () -> voteCooldowns.remove(player), 20L);
        }
    }

    private void addBuiltinTwists() {
        addTwistItem(new TwistItem(TwistVote.HARDCORE, plugin.getCfg().hardcoreTwistConfig.getIcon()));
        addTwistItem(new TwistItem(TwistVote.EXTRA_HEALTH, plugin.getCfg().extraHealthTwistConfig.getIcon()));
        addTwistItem(new TwistItem(TwistVote.BLINDNESS, plugin.getCfg().blindnessTwistConfig.getIcon()));
        addTwistItem(new TwistItem(TwistVote.RANDOM_YEET, plugin.getCfg().randomYeetTwistConfig.getIcon()));
        addTwistItem(new TwistItem(TwistVote.SPEED_BOOST, plugin.getCfg().speedBoostTwistConfig.getIcon()));
        addTwistItem(new TwistItem(TwistVote.GET_HIGH, plugin.getCfg().getHighTwistConfig.getIcon()));

        IconConfig acidRainIcon = plugin.getCfg().acidRainTwistConfig.getIcon();
        addTwistItem(new TwistItem(TwistVote.ACID_RAIN, acidRainIcon, item -> {
            ItemMeta meta = item.getItemMeta();
            if (item.getType() == Material.TIPPED_ARROW
                    || item.getType() == Material.POTION
                    || item.getType() == Material.SPLASH_POTION
                    || item.getType() == Material.LINGERING_POTION) {
                assert meta != null;
                ((PotionMeta) meta).setBasePotionData(new PotionData(PotionType.POISON));
            }

            assert meta != null;
            meta.setDisplayName(Util.c(acidRainIcon.getDisplayName()));

            meta.setLore(Objects.requireNonNull(item.getItemMeta()).getLore());
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }));

        addTwistItem(new TwistItem(
                TwistVote.NONE,
                plugin.getCfg().twistVoteMenuNoneMaterial,
                plugin.getCfg().twistVoteMenuNoneDisplayname,
                plugin.getCfg().twistVoteMenuNoneLore
        ));
    }

}
