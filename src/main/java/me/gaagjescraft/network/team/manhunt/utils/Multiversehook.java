package me.gaagjescraft.network.team.manhunt.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import org.bukkit.*;


import static com.onarandombox.MultiverseCore.enums.AllowedPortalType.ALL;

public class Multiversehook{


    public void multicreate(String worldidentifer, long seed, Game g1){


        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        MVWorldManager worldManager = core.getMVWorldManager();
        String seedstr = String.valueOf(seed);
        worldManager.addWorld(worldidentifer, World.Environment.NORMAL, seedstr, WorldType.NORMAL, true, null);

        MultiverseWorld wworld = worldManager.getMVWorld(worldidentifer);
        wworld.setKeepSpawnInMemory(true);
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

            setgamerules(g1, seed, worldidentifer, wworld.getName());


    }

    public void setgamerules(Game g1, long seed, String worldidentifer, String worldname){
        //load nether and end
        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        MVWorldManager worldManager = core.getMVWorldManager();
        String seedstr = String.valueOf(seed);
        //MultiverseNetherPortals netherportals = (MultiverseNetherPortals) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-NetherPortals");
        worldManager.addWorld(worldidentifer + "_nether", World.Environment.NETHER, seedstr, WorldType.NORMAL, true, null);
        MultiverseWorld manhuntnether = worldManager.getMVWorld(worldidentifer + "_nether");
        worldManager.addWorld(worldidentifer + "_the_end", World.Environment.THE_END, seedstr, WorldType.NORMAL, true, null);
        MultiverseWorld manhuntend = worldManager.getMVWorld(worldidentifer + "_the_end");
        String nethername = manhuntnether.getName();
        String endname = manhuntend.getName();
        //gamerule things
        manhuntnether.getCBWorld().setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
        manhuntnether.getCBWorld().setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        manhuntnether.getCBWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        manhuntend.getCBWorld().setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
        manhuntend.getCBWorld().setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        manhuntend.getCBWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);

    }


    public void multiverseworldremoval(String Worldidentifier){

        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        MVWorldManager worldManager = core.getMVWorldManager();
        worldManager.deleteWorld(Worldidentifier + "_nether");
        worldManager.deleteWorld(Worldidentifier + "_the_end");
        worldManager.deleteWorld(Worldidentifier);
        //MultiverseNetherPortals netherportals = (MultiverseNetherPortals) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-NetherPortals");
        //netherportals.removeWorldLink(worldname, nethername, PortalType.NETHER);

    }
}
