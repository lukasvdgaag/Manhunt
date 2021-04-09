package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class GameEventsHandlers implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        Game game = Game.getGame(player);
        if (game==null) return;
        GamePlayer gp = game.getPlayer(player);

        e.setCancelled(true);
        String msg = "§7[" + gp.getPrefix() + "§7] " + ChatColor.getLastColors(gp.getPrefix()) + player.getName() + "§f: §7" + e.getMessage();
        for (GamePlayer gps : game.getPlayers()) {
            Player p = Bukkit.getPlayer(gps.getUuid());
            if (p==null)continue;
            p.sendMessage(msg);
        }
    }

}
