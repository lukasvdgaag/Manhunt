package me.gaagjescraft.network.team.manhunt.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

import static com.onarandombox.MultiverseCore.enums.AllowedPortalType.ALL;

public class Multiversehook implements Listener {

    @EventHandler
    public void stopspawn(WorldInitEvent event){
        event.getWorld().setKeepSpawnInMemory(false);
    }

    public void multicreate(String worldidentifer, long seed, Game g1){
            MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
            MVWorldManager worldManager = core.getMVWorldManager();
            String seedstr = String.valueOf(seed);
            worldManager.addWorld(worldidentifer, World.Environment.NORMAL, seedstr, WorldType.NORMAL, true, null);
            MultiverseWorld wworld = worldManager.getMVWorld(worldidentifer);
            wworld.allowPortalMaking(ALL);
            wworld.getCBWorld().setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
            wworld.getCBWorld().setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
            wworld.getCBWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);

            if (Manhunt.get().getCfg().enableWorldBorder) {
                WorldBorder border = wworld.getCBWorld().getWorldBorder();
                border.setSize(Manhunt.get().getCfg().worldBorderSize);
                border.setCenter(wworld.getSpawnLocation());
                border.setDamageAmount(Manhunt.get().getCfg().worldBorderDamage);
                border.setDamageBuffer(Manhunt.get().getCfg().worldBorderDamageBuffer);
                border.setWarningDistance(Manhunt.get().getCfg().worldBorderWarningDistance);
                border.setWarningTime(Manhunt.get().getCfg().worldBorderWarningTime);
            }
            wworld.getCBWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, g1.isDoDaylightCycle());
            wworld.getCBWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, !Manhunt.get().getCfg().disableAdvancementAnnouncing);
            wworld.getCBWorld().setTime(6000);

            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> setgamerules(g1, seed, worldidentifer), 1200L);
    }

    public void setgamerules(Game g1, long seed, String worldidentifer){
        //load nether and end
            MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
            MVWorldManager worldManager = core.getMVWorldManager();
            String seedstr = String.valueOf(seed);

            Bukkit.getScheduler().runTaskLaterAsynchronously(Manhunt.get(), () -> {
                worldManager.addWorld(worldidentifer + "_nether", World.Environment.NETHER, seedstr, WorldType.NORMAL, true, null);
                MultiverseWorld manhuntnether = worldManager.getMVWorld(worldidentifer + "_nether");
                manhuntnether.getCBWorld().setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
                manhuntnether.getCBWorld().setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
                manhuntnether.getCBWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            }, 80L);

            //Create End Later
            Bukkit.getScheduler().runTaskLater(Manhunt.get(), () -> {
                worldManager.addWorld(worldidentifer + "_the_end", World.Environment.THE_END, seedstr, WorldType.NORMAL, true, null);
                MultiverseWorld manhuntend = worldManager.getMVWorld(worldidentifer + "_the_end");
                    manhuntend.getCBWorld().setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
                    manhuntend.getCBWorld().setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
                    manhuntend.getCBWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            }, 140L);
    }


    public void multiverseworldremoval(String Worldidentifier){
        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        MVWorldManager worldManager = core.getMVWorldManager();
        if (Bukkit.getWorld(Worldidentifier)  != null){
            worldManager.deleteWorld(Worldidentifier + "_nether");
            worldManager.deleteWorld(Worldidentifier + "_the_end");
            worldManager.deleteWorld(Worldidentifier);
        }
    }
}
