package me.gaagjescraft.network.team.manhunt.menus;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.events.custom.GameSetupMenuClickEvent;
import me.gaagjescraft.network.team.manhunt.events.custom.GameSetupMenuOpenEvent;
import me.gaagjescraft.network.team.manhunt.games.*;
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
    public List<Player> daylightDelays = Lists.newArrayList();
    public List<Player> allowTwistsDelays = Lists.newArrayList();
    public List<Player> allowFriendlyFireDelays = Lists.newArrayList();

    private final Manhunt plugin;

    public ManhuntGameSetupMenu(Manhunt plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player, Game game) {
        plugin.getUtil().playSound(player, plugin.getCfg().openMenuHostGameSound, .5f, 1);
        Inventory inventory = Bukkit.createInventory(null, 54, Util.c(plugin.getCfg().menuHostTitle));
        player.openInventory(inventory);
        updateItems(player, game);
    }

    public void updateItems(Player player, Game game) {
        player.getOpenInventory();
        player.getOpenInventory().getTopInventory();

        GameSetup setup = gameSetups.getOrDefault(player, plugin.getPlatformUtils().initGameSetup(player, plugin.getCfg().defaultOptionAllowTwists, plugin.getCfg().defaultOptionMaxPlayers,
                plugin.getCfg().defaultOptionDoDaylightCycle, plugin.getCfg().defaultOptionAllowFriendlyFire, plugin.getCfg().defaultOptionHeadstart));
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
            inventory.setItem(i, plugin.getItemizer().FILL_ITEM);
        }

        if (setup.getGame() == null) inventory.setItem(45, plugin.getItemizer().GO_BACK_ITEM);
        inventory.setItem(49, plugin.getItemizer().CLOSE_ITEM);

        ItemStack twistAllows;
        if (setup.isAllowTwists()) {
            twistAllows = plugin.getItemizer().createItem(plugin.getCfg().hostMenuTwistEnabledMaterial, 1, plugin.getCfg().hostMenuTwistEnabledDisplayname, plugin.getCfg().hostMenuTwistEnabledLore);
        } else {
            twistAllows = plugin.getItemizer().createItem(plugin.getCfg().hostMenuTwistDisabledMaterial, 1, plugin.getCfg().hostMenuTwistDisabledDisplayname, plugin.getCfg().hostMenuTwistDisabledLore);
        }
        inventory.setItem(11, plugin.getCfg().disableSettingsChanging ? plugin.getItemizer().MANHUNT_SETTING_DISABLED : twistAllows);

        ItemStack daycycle;
        if (setup.isDoDaylightCycle()) {
            daycycle = plugin.getItemizer().createItem(plugin.getCfg().hostMenuDaylightCycleEnabledMaterial, 1, plugin.getCfg().hostMenuDaylightCycleEnabledDisplayname, plugin.getCfg().hostMenuDaylightCycleEnabledLore);
        } else {
            daycycle = plugin.getItemizer().createItem(plugin.getCfg().hostMenuDaylightCycleDisabledMaterial, 1, plugin.getCfg().hostMenuDaylightCycleDisabledDisplayname, plugin.getCfg().hostMenuDaylightCycleDisabledLore);
        }
        inventory.setItem(29, plugin.getCfg().disableSettingsChanging ? plugin.getItemizer().MANHUNT_SETTING_DISABLED : daycycle);

        ItemStack headstart = new ItemStack(Material.valueOf(plugin.getCfg().hostMenuHeadstartMaterial));
        ItemMeta hmeta = headstart.getItemMeta();
        String headstartTime = plugin.getUtil().secondsToTimeString(setup.getHeadStart().getSeconds(), "string");
        assert hmeta != null;
        hmeta.setDisplayName(Util.c(plugin.getCfg().hostMenuHeadstartDisplayname).replaceAll("%time%", headstartTime));
        List<String> hlore;
        if (setup.getGame() == null || setup.getGame().getStatus() == GameStatus.WAITING) {
            hlore = plugin.getCfg().hostMenuHeadstartLore;
        } else {
            hlore = plugin.getCfg().hostMenuHeadstartLockedLore;
        }
        hlore = new ArrayList<>(hlore);
        for (int i = 0; i < hlore.size(); i++) {
            hlore.set(i, Util.c(hlore.get(i)).replaceAll("%time%", headstartTime));
        }
        hmeta.setLore(hlore);
        headstart.setItemMeta(hmeta);
        inventory.setItem(31, plugin.getCfg().disableSettingsChanging ? plugin.getItemizer().MANHUNT_SETTING_DISABLED : headstart);

        int runnerAmount = setup.getGame() == null ? 0 : setup.getGame().getOnlinePlayers(PlayerType.RUNNER).size();
        ItemStack run = plugin.getItemizer().createItem(plugin.getCfg().hostMenuManageRunnersMaterial, 1, plugin.getCfg().hostMenuManageRunnersDisplayname.replaceAll("%amount%", runnerAmount + ""),
                setup.getGame() != null ? plugin.getCfg().hostMenuManageRunnersLore : plugin.getCfg().hostMenuManageRunnersLockedLore);
        inventory.setItem(33, run);

        ItemStack teamfire;
        if (setup.isAllowFriendlyFire()) {
            teamfire = plugin.getItemizer().createItem(plugin.getCfg().hostMenuFriendlyFireEnabledMaterial, 1, plugin.getCfg().hostMenuFriendlyFireEnabledDisplayname, plugin.getCfg().hostMenuFriendlyFireEnabledLore);
        } else {
            teamfire = plugin.getItemizer().createItem(plugin.getCfg().hostMenuFriendlyFireDisabledMaterial, 1, plugin.getCfg().hostMenuFriendlyFireDisabledDisplayname, plugin.getCfg().hostMenuFriendlyFireDisabledLore);
        }
        inventory.setItem(15, plugin.getCfg().disableSettingsChanging ? plugin.getItemizer().MANHUNT_SETTING_DISABLED : teamfire);

        int players = setup.getGame() == null ? setup.getMaxPlayers() : setup.getGame().getMaxPlayers();
        ItemStack playerAmount = new ItemStack(Material.valueOf(plugin.getCfg().hostMenuPlayerAmountMaterial));
        ItemMeta pameta = playerAmount.getItemMeta();
        assert pameta != null;
        pameta.setDisplayName(Util.c(plugin.getCfg().hostMenuPlayerAmountDisplayname).replaceAll("%amount%", players + ""));
        List<String> palore;
        if (setup.getGame() == null) {
            palore = plugin.getCfg().hostMenuPlayerAmountLore;
        } else {
            palore = plugin.getCfg().hostMenuPlayerAmountLockedLore;
        }
        palore = new ArrayList<>(palore);
        for (int i = 0; i < palore.size(); i++) {
            palore.set(i, Util.c(palore.get(i).replaceAll("%amount%", players + "")));
        }
        pameta.setLore(palore);
        pameta.addItemFlags(ItemFlag.values());
        playerAmount.setItemMeta(pameta);

        inventory.setItem(13, plugin.getCfg().disableSettingsChanging ? plugin.getItemizer().MANHUNT_SETTING_DISABLED : playerAmount);
        if (setup.getGame() == null) {
            inventory.setItem(53, plugin.getItemizer().NEW_GAME_FINISH_ITEM);
        } else if (setup.getGame().getStatus() == GameStatus.WAITING) {
            inventory.setItem(53, plugin.getItemizer().GAME_START_ITEM);
        }

        GameSetupMenuOpenEvent event = new GameSetupMenuOpenEvent(player, gameSetups.get(player), inventory);
        plugin.getServer().getPluginManager().callEvent(event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!e.getView().getTitle().equals(Util.c(plugin.getCfg().menuHostTitle))) return;
        Player player = (Player) e.getPlayer();
        plugin.getUtil().playSound(player, plugin.getCfg().closeMenuHostGameSound, .5f, 1);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals(Util.c(plugin.getCfg().menuHostTitle))) return;
        if (e.getSlot() < 0) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        Player player = (Player) e.getWhoClicked();

        GameSetup setup = gameSetups.getOrDefault(player, plugin.getPlatformUtils().initGameSetup(player, true, 50, true, false, HeadstartType.HALF_MINUTE));

        GameSetupMenuClickEvent event = new GameSetupMenuClickEvent(player, setup, e.getClickedInventory(), e.getSlot(), e.getClick());
        plugin.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            switch (event.getSlot()) {
                case 49 -> {
                    player.closeInventory();
                    if (setup.getGame() == null) {
                        player.sendMessage(Util.c(plugin.getCfg().gameDiscardMessage));
                    }
                    gameSetups.remove(player);
                }
                case (11) -> {
                    if (allowTwistsDelays.contains(player) || !plugin.getCfg().disableSettingsChanging) return;
                    boolean nv = !setup.isAllowTwists();
                    plugin.getUtil().playSound(player, plugin.getCfg().menuHostToggleTwistSound, 1, nv ? 2 : 1);
                    setup.setAllowTwists(nv, true);
                    updateItems(player, Game.getGame(player));
                    allowTwistsDelays.add(player);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> allowTwistsDelays.remove(player), 20L);
                }
                case 13 -> {
                    if (setup.getGame() != null || plugin.getCfg().disableSettingsChanging) return;
                    player.closeInventory();
                    plugin.getManhuntPlayerAmountSetupMenu().openMenu(player, setup);
                }
                case 15 -> {
                    if (allowFriendlyFireDelays.contains(player) || plugin.getCfg().disableSettingsChanging) return;
                    boolean nv = !setup.isAllowFriendlyFire();
                    plugin.getUtil().playSound(player, plugin.getCfg().menuHostToggleFriendlyFireSound, 1, nv ? 2 : 1);
                    setup.setAllowFriendlyFire(nv, true);
                    updateItems(player, Game.getGame(player));
                    allowFriendlyFireDelays.add(player);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> allowFriendlyFireDelays.remove(player), 20L);
                }
                case 29 -> {
                    if (daylightDelays.contains(player) || plugin.getCfg().disableSettingsChanging) return;
                    boolean nv = !setup.isDoDaylightCycle();
                    plugin.getUtil().playSound(player, plugin.getCfg().menuHostToggleDaylightSound, 1, nv ? 2 : 1);
                    setup.setDoDaylightCycle(nv, true);
                    updateItems(player, Game.getGame(player));
                    daylightDelays.add(player);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> daylightDelays.remove(player), 20L);
                }
                case 31 -> {
                    if (plugin.getCfg().disableSettingsChanging || (setup.getGame() != null && setup.getGame().getStatus() != GameStatus.WAITING && setup.getGame().getStatus() != GameStatus.STARTING)) {
                        return;
                    }
                    player.closeInventory();
                    plugin.getManhuntHeadstartSetupMenu().openMenu(player, setup);
                }
                case 45 -> {
                    if (setup.getGame() != null) return;
                    player.closeInventory();
                    plugin.getManhuntMainMenu().openMenu(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                }
                case 33 -> {
                    if (setup.getGame() == null) {
                        plugin.getUtil().playSound(player, plugin.getCfg().menuHostLockedSound, 1, 1);
                    } else {
                        player.closeInventory();
                        plugin.getManhuntRunnerManageMenu().open(player, setup.getGame());
                    }
                }
                case 53 -> {
                    // submitting the game.
                    if (setup.getGame() == null) {
                        final int pricePerGame = plugin.getCfg().pricePerGame;
                        if (player.hasPermission("manhunt.hostgame")) {
                            if (pricePerGame > 0 && plugin.getEconomy() != null)
                                player.sendMessage(Util.c(plugin.getCfg().freeGameHostedMessage));
                        } else if (pricePerGame > 0 && plugin.getEconomy() != null) {
                            if (plugin.getEconomy().hasBalance(player, pricePerGame)) {
                                plugin.getEconomy().removeBalance(player, pricePerGame);
                                player.sendMessage(Util.c(plugin.getCfg().moneyPaidHostingGameMessage)
                                        .replace("%money%", pricePerGame + "")
                                        .replace("%balance%", plugin.getEconomy().getBalance(player) + "")
                                );
                            } else {
                                plugin.getUtil().playSound(player, plugin.getCfg().cantHostGameSound, 1, 1);
                                player.sendMessage(Util.c(plugin.getCfg().notEnoughMoneyHostingGameMessage)
                                        .replace("%money%", pricePerGame + "")
                                        .replace("%balance%", plugin.getEconomy().getBalance(player) + ""));
                                return;
                            }
                        } else {
                            // no permission
                            plugin.getUtil().playSound(player, plugin.getCfg().cantHostGameSound, 1, 1);
                            player.sendMessage(Util.c(plugin.getCfg().noPermissionHostingGameMessage));
                            return;
                        }

                        if (plugin.getCfg().bungeeMode && plugin.getCfg().isLobbyServer) {
                            player.closeInventory();
                            player.sendMessage(Util.c(plugin.getCfg().gameSubmittedMessage));

                            setup.getBungeeSetup().requestNextGameCreation();
                        } else {
                            Game game = plugin.getPlatformUtils().initGame(setup, player);
                            if (game == null) {
                                plugin.getUtil().playSound(player, plugin.getCfg().menuHostLockedSound, 1, 1);
                                player.sendMessage(Util.c(plugin.getCfg().alreadyOwnGameMessage));
                                return;
                            }
                            this.gameSetups.remove(player);

                            player.closeInventory();
                            player.sendMessage(Util.c(plugin.getCfg().gameCreatedMessage));
                            game.create();
                            game.setAllowFriendlyFire(setup.isAllowFriendlyFire());
                            game.setHeadStart(setup.getHeadStart());
                        }
                    } else if (setup.getGame().getStatus() == GameStatus.WAITING) {
                        Game game = setup.getGame();
                        if (game == null) {
                            player.sendMessage(Util.c(plugin.getCfg().somethingWentWrong));
                            return;
                        }
                        game.setHeadStart(setup.getHeadStart());
                        this.gameSetups.remove(player);
                        if (game.getStatus() != GameStatus.WAITING) {
                            player.sendMessage(Util.c(plugin.getCfg().gameMustBeWaitingMessage));
                            return;
                        }
                        game.start();

                        player.sendMessage(Util.c(plugin.getCfg().startingGameMessage));
                        plugin.getUtil().playSound(player, plugin.getCfg().menuHostGameStartedSound, 1, 1);
                        player.closeInventory();
                    }
                }
            }
        }
    }

}
