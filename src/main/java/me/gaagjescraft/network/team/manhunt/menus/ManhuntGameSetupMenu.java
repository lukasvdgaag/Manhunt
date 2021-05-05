package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.*;
import me.gaagjescraft.network.team.manhunt.utils.Itemizer;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ManhuntGameSetupMenu implements Listener {

    public HashMap<Player, GameSetup> gameSetups = new HashMap<>();
    private List<Player> daylightDelays = Lists.newArrayList();
    private List<Player> allowTwistsDelays = Lists.newArrayList();
    private List<Player> allowFriendlyFireDelays = Lists.newArrayList();

    public void openMenu(Player player, Game game) {
        player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().openMenuHostGameSound), 0.5f, 1);
        Inventory inventory = Bukkit.createInventory(null, 54, Util.c(Manhunt.get().getCfg().menuHostTitle));
        player.openInventory(inventory);
        updateItems(player, game);
    }

    public void updateItems(Player player, Game game) {
        if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null) return;

        GameSetup setup = gameSetups.getOrDefault(player, new GameSetup(player, true, 50, true, false, HeadstartType.HALF_MINUTE));
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

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, Itemizer.FILL_ITEM);
        }

        inventory.setItem(49, Itemizer.CLOSE_ITEM);

        ItemStack twistAllows;
        if (setup.isAllowTwists()) {
            twistAllows = Itemizer.createItem(Manhunt.get().getCfg().hostMenuTwistEnabledMaterial, 1, Manhunt.get().getCfg().hostMenuTwistEnabledDisplayname, Manhunt.get().getCfg().hostMenuTwistEnabledLore);
        } else {
            twistAllows = Itemizer.createItem(Manhunt.get().getCfg().hostMenuTwistDisabledMaterial, 1, Manhunt.get().getCfg().hostMenuTwistDisabledDisplayname, Manhunt.get().getCfg().hostMenuTwistDisabledLore);
        }
        inventory.setItem(11, twistAllows);

        ItemStack daycycle;
        if (setup.isDoDaylightCycle()) {
            daycycle = Itemizer.createItem(Manhunt.get().getCfg().hostMenuDaylightCycleEnabledMaterial, 1, Manhunt.get().getCfg().hostMenuDaylightCycleEnabledDisplayname, Manhunt.get().getCfg().hostMenuDaylightCycleEnabledLore);
        } else {
            daycycle = Itemizer.createItem(Manhunt.get().getCfg().hostMenuDaylightCycleDisabledMaterial, 1, Manhunt.get().getCfg().hostMenuDaylightCycleDisabledDisplayname, Manhunt.get().getCfg().hostMenuDaylightCycleDisabledLore);
        }
        inventory.setItem(29, daycycle);

        ItemStack headstart = new ItemStack(Material.valueOf(Manhunt.get().getCfg().hostMenuHeadstartMaterial));
        ItemMeta hmeta = headstart.getItemMeta();
        String headstartTime = Manhunt.get().getUtil().secondsToTimeString(setup.getHeadstart().getSeconds(), "string");
        hmeta.setDisplayName(Util.c(Manhunt.get().getCfg().hostMenuHeadstartDisplayname).replaceAll("%time%", headstartTime));
        List<String> hlore;
        if (setup.getGame() == null || setup.getGame().getStatus() == GameStatus.WAITING) {
            hlore = Manhunt.get().getCfg().hostMenuHeadstartLore;
        } else {
            hlore = Manhunt.get().getCfg().hostMenuHeadstartLockedLore;
        }
        hlore = new ArrayList<>(hlore);
        for (int i = 0; i < hlore.size(); i++) {
            hlore.set(i, Util.c(hlore.get(i)).replaceAll("%time%", headstartTime));
        }
        hmeta.setLore(hlore);
        headstart.setItemMeta(hmeta);
        inventory.setItem(31, headstart);

        int runnerAmount = setup.getGame() == null ? 0 : setup.getGame().getOnlinePlayers(PlayerType.RUNNER).size();
        ItemStack run = Itemizer.createItem(Manhunt.get().getCfg().hostMenuManageRunnersMaterial, 1, Manhunt.get().getCfg().hostMenuManageRunnersDisplayname.replaceAll("%amount%", runnerAmount + ""),
                setup.getGame() != null ? Manhunt.get().getCfg().hostMenuManageRunnersLore : Manhunt.get().getCfg().hostMenuManageRunnersLockedLore);
        inventory.setItem(33, run);

        ItemStack teamfire;
        if (setup.isAllowFriendlyFire()) {
            teamfire = Itemizer.createItem(Manhunt.get().getCfg().hostMenuFriendlyFireEnabledMaterial, 1, Manhunt.get().getCfg().hostMenuFriendlyFireEnabledDisplayname, Manhunt.get().getCfg().hostMenuFriendlyFireEnabledLore);
        } else {
            teamfire = Itemizer.createItem(Manhunt.get().getCfg().hostMenuFriendlyFireDisabledMaterial, 1, Manhunt.get().getCfg().hostMenuFriendlyFireDisabledDisplayname, Manhunt.get().getCfg().hostMenuFriendlyFireDisabledLore);
        }
        inventory.setItem(15, teamfire);

        int players = setup.getGame() == null ? 1 : setup.getGame().getPlayers(PlayerType.RUNNER).size();
        ItemStack playerAmount = new ItemStack(Material.valueOf(Manhunt.get().getCfg().hostMenuPlayerAmountMaterial));
        ItemMeta pameta = playerAmount.getItemMeta();
        pameta.setDisplayName(Util.c(Manhunt.get().getCfg().hostMenuPlayerAmountDisplayname).replaceAll("%amount%", players + ""));
        List<String> palore;
        if (setup.getGame() == null) {
            palore = Manhunt.get().getCfg().hostMenuPlayerAmountLore;
        } else {
            palore = Manhunt.get().getCfg().hostMenuPlayerAmountLockedLore;
        }
        palore = new ArrayList<>(palore);
        for (int i = 0; i < palore.size(); i++) {
            palore.set(i, Util.c(palore.get(i).replaceAll("%amount%", players + "")));
        }
        pameta.setLore(palore);
        pameta.addItemFlags(ItemFlag.values());
        playerAmount.setItemMeta(pameta);

        inventory.setItem(13, playerAmount);
        if (setup.getGame() == null) {
            inventory.setItem(53, Itemizer.NEW_GAME_FINISH_ITEM);
        } else if (setup.getGame().getStatus() == GameStatus.WAITING) {
            inventory.setItem(53, Itemizer.GAME_START_ITEM);
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!e.getView().getTitle().equals(Util.c(Manhunt.get().getCfg().menuHostTitle))) return;
        Player player = (Player) e.getPlayer();
        player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().closeMenuHostGameSound), 0.5f, 1);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals(Util.c(Manhunt.get().getCfg().menuHostTitle))) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player player = (Player) e.getWhoClicked();

        GameSetup setup = gameSetups.getOrDefault(player, new GameSetup(player, true, 50, true, false, HeadstartType.HALF_MINUTE));
        if (e.getSlot() == 49) {
            player.closeInventory();
            if (setup.getGame() == null) {
                player.sendMessage(Util.c(Manhunt.get().getCfg().gameDiscardMessage));
            }
            gameSetups.remove(player);
        } else if (e.getSlot() == 11 && !allowTwistsDelays.contains(player)) {
            boolean nv = !setup.isAllowTwists();
            player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().menuHostToggleTwistSound), 1, nv ? 2 : 1);
            setup.setAllowTwists(nv, true);
            updateItems(player, Game.getGame(player));
            allowTwistsDelays.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), ()->allowTwistsDelays.remove(player),20L);
        } else if (e.getSlot() == 29 && !daylightDelays.contains(player)) {
            boolean nv = !setup.isDoDaylightCycle();
            player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().menuHostToggleDaylightSound), 1, nv ? 2 : 1);
            setup.setDoDaylightCycle(nv, true);
            updateItems(player, Game.getGame(player));
            daylightDelays.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), ()->daylightDelays.remove(player),20L);
        } else if (e.getSlot() == 31 && (setup.getGame() == null || setup.getGame().getStatus() == GameStatus.WAITING || setup.getGame().getStatus() == GameStatus.STARTING)) {
            player.closeInventory();
            Manhunt.get().getManhuntHeadstartSetupMenu().openMenu(player,setup);
        }
        else if (e.getSlot() == 15 && !allowFriendlyFireDelays.contains(player)) {
            boolean nv = !setup.isAllowFriendlyFire();
            player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().menuHostToggleFriendlyFireSound), 1, nv ? 2 : 1);
            setup.setAllowFriendlyFire(nv, true);
            updateItems(player, Game.getGame(player));
            allowFriendlyFireDelays.add(player);
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), ()->allowFriendlyFireDelays.remove(player),20L);
        } else if (e.getSlot() == 13 && setup.getGame() == null) {
            player.closeInventory();
            Manhunt.get().getManhuntPlayerAmountSetupMenu().openMenu(player, setup);
        } else if (e.getSlot() == 33) {
            if (setup.getGame() == null) {
                player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().menuHostLockedSound), 1, 1);
            } else {
                player.closeInventory();
                Manhunt.get().getManhuntRunnerManageMenu().open(player, setup.getGame());
            }
        } else if (e.getSlot() == 53) {
            // submitting the game.
            if (setup.getGame() == null) {
                Game game = Game.createGame(setup.isAllowTwists(), player, setup.getMaxPlayers());
                if (game == null) {
                    player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().menuHostLockedSound), 1, 1);
                    player.sendMessage(Util.c(Manhunt.get().getCfg().alreadyOwnGameMessage));
                    return;
                }
                this.gameSetups.remove(player);

                player.closeInventory();
                player.sendMessage(Util.c(Manhunt.get().getCfg().gameCreatedMessage));
                game.create();
                game.setAllowFriendlyFire(setup.isAllowFriendlyFire());
                game.setHeadStart(setup.getHeadstart());
            } else if (setup.getGame().getStatus() == GameStatus.WAITING) {
                Game game = setup.getGame();
                if (game == null) {
                    player.sendMessage(Util.c(Manhunt.get().getCfg().somethingWentWrong));
                    return;
                }
                game.setHeadStart(setup.getHeadstart());
                this.gameSetups.remove(player);
                if (game.getStatus() != GameStatus.WAITING) {
                    player.sendMessage(Util.c(Manhunt.get().getCfg().gameMustBeWaitingMessage));
                    return;
                }
                game.start();

                player.sendMessage(Util.c(Manhunt.get().getCfg().startingGameMessage));
                player.playSound(player.getLocation(), Sound.valueOf(Manhunt.get().getCfg().menuHostGameStartedSound), 1, 1);
                player.closeInventory();
            }
        }
    }

}
