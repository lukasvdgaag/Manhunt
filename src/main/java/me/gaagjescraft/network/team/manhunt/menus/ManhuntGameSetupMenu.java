package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GameSetup;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

public class ManhuntGameSetupMenu implements Listener {

    public HashMap<Player, GameSetup> gameSetups = new HashMap<>();
    private List<Player> daylightDelays = Lists.newArrayList();
    private List<Player> allowTwistsDelays = Lists.newArrayList();
    private List<Player> allowFriendlyFireDelays = Lists.newArrayList();


    public void openMenu(Player player, Game game) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1);
        Inventory inventory = Bukkit.createInventory(null, 36, "§6§lHost ManHunt Event");
        player.openInventory(inventory);
        updateItems(player, game);
    }

    public void updateItems(Player player, Game game) {
        if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null) return;

        GameSetup setup = gameSetups.getOrDefault(player, new GameSetup(player, true, 50, true, false));
        if (!gameSetups.containsKey(player)) {
            gameSetups.put(player, setup);
        }
        if (game != null) {
            setup.setGame(game);
            setup.setDoDaylightCycle(game.isDoDaylightCycle(), false);
            setup.setAllowTwists(game.isTwistsAllowed(), false);
            setup.setAllowFriendlyFire(game.isAllowFriendlyFire(), false);
            setup.setMaxPlayers(game.getMaxPlayers());
            setup.setHeadstart(game.getHeadStart(), false);
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();
        inventory.setItem(31, Itemizer.CLOSE_ITEM);

        ItemStack twistAllows = setup.isAllowTwists() ? new ItemStack(Material.LIME_DYE) : new ItemStack(Material.GRAY_DYE);
        ItemMeta tameta = twistAllows.getItemMeta();
        tameta.setDisplayName("§bAllow Twists §7(" + (setup.isAllowTwists() ? "§aEnabled" : "§cDisabled") + "§7)");
        tameta.setLore(Lists.newArrayList("", "§7We offer certain twists to", "§7make the game even more fun.",
                "§7Enabling this will give players", "§7the ability to vote for a twist", "§7while waiting for the game to start.", "",
                setup.isAllowTwists() ? "§aTwists are enabled." : "§cTwists are disabled.", "",
                "§6Click§e to toggle twists."));
        tameta.addItemFlags(ItemFlag.values());
        twistAllows.setItemMeta(tameta);

        inventory.setItem(10, twistAllows);

        ItemStack daycycle = new ItemStack(Material.CLOCK);
        ItemMeta daymeta = daycycle.getItemMeta();
        daymeta.setDisplayName("§bDo Daylight Cycle §7(" + (setup.isDoDaylightCycle() ? "§aEnabled" : "§cDisabled") + "§7)");
        daymeta.setLore(Lists.newArrayList("", "§7We offer hosts the ability to", "§7toggle the daylight cycle.", "§7Disabling it will make it permanently day.",
                "§7Enabling it will switch between day/night.", "",
                setup.isDoDaylightCycle() ? "§aDaylight cycle is enabled." : "§cDaylight cycle is disabled.", "",
                "§6Click§e to toggle daylight cycle."));
        daymeta.addItemFlags(ItemFlag.values());
        daycycle.setItemMeta(daymeta);

        inventory.setItem(12, daycycle);

        ItemStack headstart = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta hmeta = headstart.getItemMeta();
        hmeta.setDisplayName("§bRunners Headstart §7(§a" + setup.getHeadstart().getSeconds() + "§7)");
        List<String> hlore = Lists.newArrayList("", "§7Runners will get a headstart", "§7of §e" + setup.getHeadstart().getSeconds() + "§7 seconds to", "§7prepare for the hunters.", "");
        if (setup.getGame() == null || setup.getGame().getStatus() == GameStatus.WAITING) {
            hlore.add("§6Click§e to edit.");
        }
        else {
            hlore.add("§cYou can't change this once");
            hlore.add("§cthe game has started.");
        }
        hmeta.setLore(hlore);
        headstart.setItemMeta(hmeta);

        inventory.setItem(13, headstart);

        ItemStack teamfire = new ItemStack(setup.isAllowFriendlyFire() ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD);
        ItemMeta tfmeta = teamfire.getItemMeta();
        tfmeta.setDisplayName("§bAllow Friendly Fire §7(" + (setup.isAllowFriendlyFire() ? "§aEnabled" : "§cDisabled") + "§7)");
        tfmeta.setLore(Lists.newArrayList("", "§7Whether hunters can hit other hunters", "§7while playing the game.",
                "§8Runners can't hit each other either way.", "",
                setup.isAllowFriendlyFire() ? "§aFriendly fire is enabled." : "§cFriendly fire is disabled.", "",
                "§6Click§e to toggle friendly fire."));
        tfmeta.addItemFlags(ItemFlag.values());
        teamfire.setItemMeta(tfmeta);

        inventory.setItem(14, teamfire);

        ItemStack playerAmount = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta pameta = playerAmount.getItemMeta();
        pameta.setDisplayName("§bMax Players §7(§e" + setup.getMaxPlayers() + "§7)");
        List<String> palore = Lists.newArrayList("", "§7The maximum amount of hunters", "§7that can join this game.",
                "§c§oThis number must be between 10-100.", "", "§bCurrent maximum: §e" + setup.getMaxPlayers(), "");
        if (setup.getGame() == null) {
            palore.add("§6Click§e to edit.");
        } else {
            palore.add("§cYou can't change this once");
            palore.add("§cthe game was has been created.");
        }
        pameta.setLore(palore);
        pameta.addItemFlags(ItemFlag.values());
        playerAmount.setItemMeta(pameta);

        inventory.setItem(16, playerAmount);
        if (setup.getGame() == null) {
            inventory.setItem(35, Itemizer.NEW_GAME_FINISH_ITEM);
        } else if (setup.getGame().getStatus() == GameStatus.WAITING) {
            inventory.setItem(35, Itemizer.GAME_START_ITEM);
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!e.getView().getTitle().equals("§6§lHost ManHunt Event")) return;
        Player player = (Player) e.getPlayer();
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.5f, 1);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals("§6§lHost ManHunt Event")) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player player = (Player) e.getWhoClicked();

        GameSetup setup = gameSetups.getOrDefault(player, new GameSetup(player, true, 50, true, false));
        if (e.getSlot() == 31) {
            player.closeInventory();
            if (setup.getGame() == null) {
                player.sendMessage(ChatColor.RED + "You discarded your new manhunt event host.");
            }
            gameSetups.remove(player);
        } else if (e.getSlot() == 10 && !allowTwistsDelays.contains(player)) {
            boolean nv = !setup.isAllowTwists();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, nv ? 2 : 1);
            setup.setAllowTwists(nv, true);
            updateItems(player, Game.getGame(player));
            allowTwistsDelays.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), ()->allowTwistsDelays.remove(player),20L);
        } else if (e.getSlot() == 12 && !daylightDelays.contains(player)) {
            boolean nv = !setup.isDoDaylightCycle();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, nv ? 2 : 1);
            setup.setDoDaylightCycle(nv, true);
            updateItems(player, Game.getGame(player));
            daylightDelays.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), ()->daylightDelays.remove(player),20L);
        } else if (e.getSlot() == 13 && (setup.getGame() == null || setup.getGame().getStatus() == GameStatus.WAITING)) {
            Manhunt.get().getManhuntHeadstartSetupMenu().openMenu(player,setup);
        }
        else if (e.getSlot() == 14 && !allowFriendlyFireDelays.contains(player)) {
            boolean nv = !setup.isAllowFriendlyFire();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, nv ? 2 : 1);
            setup.setAllowFriendlyFire(nv, true);
            updateItems(player, Game.getGame(player));
            allowFriendlyFireDelays.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), ()->allowFriendlyFireDelays.remove(player),20L);
        } else if (e.getSlot() == 16 && setup.getGame() == null) {
            Manhunt.get().getManhuntPlayerAmountSetupMenu().openMenu(player, setup);
        } else if (e.getSlot() == 35) {
            // submitting the game.
            if (setup.getGame() == null) {
                Game game = Game.createGame(setup.isAllowTwists(), player, setup.getMaxPlayers());
                if (game == null) {
                    player.sendMessage(ChatColor.RED + "It seems like you already have a game running. Let's wait for that one to finish first. If you wish to force stop your current game, please type §7/manhunt stop§c.");
                    return;
                }
                this.gameSetups.remove(player);

                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Successfully saved your game settings. Now generating the world...");
                game.create();
                game.setAllowFriendlyFire(setup.isAllowFriendlyFire());
            } else if (setup.getGame().getStatus() == GameStatus.WAITING) {
                Game game = setup.getGame();
                if (game == null) {
                    player.sendMessage(ChatColor.RED + "Something went wrong whilst starting your game. Try the command instead.");
                    return;
                }
                this.gameSetups.remove(player);
                if (game.getStatus() != GameStatus.WAITING) {
                    player.sendMessage(ChatColor.RED + "The game must be in waiting mode in order to start it.");
                    return;
                }
                game.start();
                player.sendMessage(ChatColor.GREEN + "Your game has been started.");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                player.closeInventory();
            }
        }
    }

}
