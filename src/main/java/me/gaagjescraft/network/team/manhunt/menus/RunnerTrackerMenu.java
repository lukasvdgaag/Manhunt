package me.gaagjescraft.network.team.manhunt.menus;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.events.custom.GameTrackerMenuOpenEvent;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class RunnerTrackerMenu {

    private final Game game;
    private Inventory teleporterMenu;
    private Inventory trackerMenu;
    private List<GamePlayer> runnersList;

    private final Manhunt plugin;

    public RunnerTrackerMenu(Manhunt plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
        update();
    }

    public void update() {
        int size = 9;
        this.runnersList = game.getOnlinePlayers(PlayerType.RUNNER);
        this.runnersList.removeIf(GamePlayer::isFullyDead);
        int runners = runnersList.size();
        if (runners <= 9) size = 18;
        else if (runners <= 18) size = 27;
        else if (runners <= 27) size = 36;
        else if (runners <= 36) size = 45;
        else if (runners <= 45) size = 54;

        Inventory teleportMenu = Bukkit.createInventory(null, size, Util.c(plugin.getCfg().menuTeleporterTitle));
        Inventory trackMenu = Bukkit.createInventory(null, size, Util.c(plugin.getCfg().menuTrackerTitle));

        teleportMenu.setItem(size - 5, plugin.getItemizer().CLOSE_ITEM);
        trackMenu.setItem(size - 5, plugin.getItemizer().CLOSE_ITEM);

        int slot = 0;
        for (GamePlayer runner : runnersList) {
            if (slot == size - 9) break;
            Player player = Bukkit.getPlayer(runner.getUuid());

            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            assert meta != null;
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(runner.getUuid()));
            assert player != null;
            meta.setDisplayName(Util.c(plugin.getCfg().teleporterMenuPlayerDisplayname.replaceAll("%player%", player.getName())));
            List<String> lore = plugin.getCfg().teleporterMenuPlayerLore;
            lore = new ArrayList<>(lore);
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, Util.c(lore.get(i).replaceAll("%player%", player.getName())));
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
            teleportMenu.setItem(slot, item);

            ItemStack item1 = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta1 = (SkullMeta) item1.getItemMeta();
            assert meta1 != null;
            meta1.setOwningPlayer(Bukkit.getOfflinePlayer(runner.getUuid()));
            meta1.setDisplayName(Util.c(plugin.getCfg().trackerMenuPlayerDisplayname.replaceAll("%player%", player.getName())));
            List<String> lore1 = plugin.getCfg().trackerMenuPlayerLore;
            lore1 = new ArrayList<>(lore1);
            for (int i = 0; i < lore1.size(); i++) {
                lore1.set(i, Util.c(lore1.get(i).replaceAll("%player%", player.getName())));
            }
            meta1.setLore(lore1);
            meta1.addItemFlags(ItemFlag.values());
            item1.setItemMeta(meta1);
            trackMenu.setItem(slot, item1);
            slot++;
        }

        this.teleporterMenu = teleportMenu;
        this.trackerMenu = trackMenu;
    }

    public void open(Player player, boolean teleporting) {
        Inventory inv = teleporting ? this.teleporterMenu : this.trackerMenu;
        GameTrackerMenuOpenEvent event = new GameTrackerMenuOpenEvent(player, game, inv);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        player.openInventory(inv);
    }

    public List<GamePlayer> getRunnersList() {
        return runnersList;
    }

    public Game getGame() {
        return game;
    }
}
