package me.gaagjescraft.network.team.manhunt.world;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.WorldManager;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import org.bukkit.*;

import static com.onarandombox.MultiverseCore.enums.AllowedPortalType.ALL;

public class Multiverse implements WorldUtil{

    private final MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
    private final MVWorldManager worldManager;

    {
        assert core != null;
        worldManager = core.getMVWorldManager();
    }

    @Override
    public World create(long seed, String worldidentifer, World.Environment environment) {
        String seedstr = String.valueOf(seed);
        worldManager.addWorld(worldidentifer, environment, seedstr, WorldType.NORMAL, true, null);
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
        return wworld.getCBWorld();
    }

    @Override
    public void delete(String worldidentifier) {
       if (Bukkit.getWorld(worldidentifier) != null){
           worldManager.unloadWorld(worldidentifier);
           worldManager.deleteWorld(worldidentifier);
       }
    }
}
