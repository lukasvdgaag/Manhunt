package me.gaagjescraft.network.team.manhunt.events;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class WorldLoadHandler implements Listener {

    private final Manhunt plugin;

    public WorldLoadHandler(Manhunt plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void LoadWorldAdvancement(PlayerAdvancementDoneEvent event){
        Game game = Game.getGame(event.getPlayer());
        if (game == null) return;
        NamespacedKey key = event.getAdvancement().getKey();
        if (key.getNamespace().equals(NamespacedKey.MINECRAFT) && (key.getKey().equals("story/follow_ender_eye")  || key.getKey().equals("story/enter_the_end"))){
            plugin.getWorldManager().worldLoad(game.getWorldIdentifier() + "_the_end");
        }
    }

}
