package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class RunnerTrackerMenu {

    private Game game;
    private Inventory teleporterMenu;
    private Inventory trackerMenu;
    private List<GamePlayer> runnersList;

    public RunnerTrackerMenu(Game game) {
        this.game = game;
        update();
    }

    public void update() {
        int size = 9;
        this.runnersList = game.getPlayers(PlayerType.RUNNER);
        int runners = runnersList.size();
        if (runners <= 9) size = 18;
        else if (runners <= 18) size = 27;
        else if (runners <= 27) size = 36;
        else if (runners <= 36) size = 45;
        else if (runners <= 45) size = 54;

        Inventory teleportMenu = Bukkit.createInventory(null, size, "§6§lManHunt Teleporter Menu");
        Inventory trackMenu = Bukkit.createInventory(null, size, "§6§lManHunt Tracker Menu");

        teleportMenu.setItem(size - 5, Itemizer.CLOSE_ITEM);
        trackMenu.setItem(size - 5, Itemizer.CLOSE_ITEM);

        int slot = 0;
        for (GamePlayer runner : runnersList) {
            if (slot == size - 9) break;
            if (runner == null) continue;
            Player player = Bukkit.getPlayer(runner.getUuid());
            if (player == null) continue;

            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(runner.getUuid()));
            meta.setDisplayName("§e" + player.getName());
            meta.setLore(Lists.newArrayList("", "§6Click§e to teleport."));
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
            teleportMenu.setItem(slot, item);

            ItemStack item1 = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta1 = (SkullMeta) item1.getItemMeta();
            meta1.setOwningPlayer(Bukkit.getOfflinePlayer(runner.getUuid()));
            meta1.setDisplayName("§e" + player.getName());
            meta1.setLore(Lists.newArrayList("", "§6Click§e to track."));
            meta1.addItemFlags(ItemFlag.values());
            item1.setItemMeta(meta1);
            trackMenu.setItem(slot, item1);
        }

        this.teleporterMenu = teleportMenu;
        this.trackerMenu = trackMenu;
    }

    public void open(Player player, boolean teleporting) {
        if (teleporting) player.openInventory(this.teleporterMenu);
        else player.openInventory(this.trackerMenu);
    }

    public List<GamePlayer> getRunnersList() {
        return runnersList;
    }

    public Game getGame() {
        return game;
    }
}
