package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class GameEventsHandlers implements Listener {

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
        if (e.isCancelled() || Manhunt.get().getManhuntRunnerManageMenu().chatPlayers.contains(e.getPlayer())) return;
        Player player = e.getPlayer();

        Game game = Game.getGame(player);
        if (game == null) return;
        GamePlayer gp = game.getPlayer(player);

        e.setCancelled(true);
        String msg;

        if (game.getStatus() == GameStatus.PLAYING)
            msg = "§7[" + gp.getPrefix(true) + "S§7] " + ChatColor.getLastColors(gp.getPrefix()) + player.getName() + "§f: §f" + e.getMessage();
        else
            msg = "§7[§8§lGLOBAL§7] " + ChatColor.getLastColors(gp.getPrefix()) + player.getName() + "§f: §f" + e.getMessage();

        for (GamePlayer gps : game.getOnlinePlayers(null)) {
            Player p = Bukkit.getPlayer(gps.getUuid());
            if (p == null) continue;
            if (game.getStatus() == GameStatus.PLAYING) {
                if (gp.isFullyDead()) {
                    if (gps.isFullyDead()) p.sendMessage(msg);
                } else {
                    if (gps.getPlayerType() == gp.getPlayerType()) p.sendMessage(msg);
                }
            } else {
                p.sendMessage(msg);
            }
        }
    }

}
