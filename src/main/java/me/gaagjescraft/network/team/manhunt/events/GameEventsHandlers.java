package me.gaagjescraft.network.team.manhunt.events;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPI;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;

import java.util.List;

public class GameEventsHandlers implements Listener {

    private final Manhunt plugin;

    public GameEventsHandlers(Manhunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBedSpawn(PlayerBedEnterEvent e) {
        Player player = e.getPlayer();
        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        if (gp.isDead()) {
            e.setCancelled(true);
            return;
        }
        gp.setBedSpawn(e.getBed().getLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled() || plugin.getManhuntRunnerManageMenu().chatPlayers.contains(e.getPlayer())) return;
        Player player = e.getPlayer();

        Game game = Game.getGame(player);
        if (game == null) {
            if (!plugin.getCfg().enableLobbyChat) return;
            e.setCancelled(true);

            String prefix = plugin.getCfg().lobbyChatPrefix;
            String format = plugin.getCfg().lobbyChatFormat;
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                format = PlaceholderAPI.setPlaceholders(player, format);
            }
            String message = Util.c(format
                    .replaceAll("%prefix%", prefix)
                    .replaceAll("%player%", player.getName())
                    .replaceAll("%message%", e.getMessage()));

            List<Player> send = Lists.newArrayList();

            if (Game.getGames().size() == 0) {
                send.addAll(Bukkit.getOnlinePlayers());
            } else {
                for (Game g : Game.getGames()) {
                    for (GamePlayer gp : g.getPlayers(null)) {
                        Player target = Bukkit.getPlayer(gp.getUuid());
                        if (gp.isOnline() && target != null) {
                            send.add(target);
                        }
                    }
                }
            }

            for (Player p : send) {
                p.sendMessage(message);
            }

            return;
        }
        GamePlayer gp = game.getPlayer(player);

        e.setCancelled(true);
        String prefix = game.getStatus() != GameStatus.PLAYING ? plugin.getCfg().globalChatPrefix : gp.getPrefix(true);
        String format = plugin.getCfg().chatFormat;
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(player, format);
        }
        String message = Util.c(format
                .replaceAll("%prefix%", prefix)
                .replaceAll("%color%", gp.getColor())
                .replaceAll("%player%", player.getName())
                .replaceAll("%message%", e.getMessage()));

        if (plugin.getCfg().chatPerTeam) {
            for (GamePlayer gps : game.getOnlinePlayers(null)) {
                Player p = Bukkit.getPlayer(gps.getUuid());
                if (p == null) continue;
                if (game.getStatus() != GameStatus.PLAYING) {
                    p.sendMessage(message);
                    continue;
                }
                if (plugin.getCfg().separateDeadChat) {
                    if (gp.isFullyDead()) {
                        if (gps.isFullyDead()) {
                            p.sendMessage(message);
                        }
                        continue;
                    } else if (!gp.isFullyDead() && gp.isFullyDead()) continue;
                }
                if (gp.getPlayerType() == gps.getPlayerType()) {
                    // same team
                    p.sendMessage(message);
                }
            }
        } else {
            for (GamePlayer gps : game.getOnlinePlayers(null)) {
                Player p = Bukkit.getPlayer(gps.getUuid());
                if (p == null) continue;
                if (game.getStatus() != GameStatus.PLAYING) {
                    p.sendMessage(message);
                    continue;
                }
                if (plugin.getCfg().separateDeadChat) {
                    if (gp.isFullyDead()) {
                        if (gps.isFullyDead()) {
                            p.sendMessage(message);
                        }
                    } else {
                        // if (!gps.isFullyDead()) {
                        p.sendMessage(message);
                        // }
                    }
                } else {
                    p.sendMessage(message);
                }
            }
        }
    }

}
