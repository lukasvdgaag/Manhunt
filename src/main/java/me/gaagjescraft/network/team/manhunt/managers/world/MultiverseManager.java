package me.gaagjescraft.network.team.manhunt.managers.world;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import org.bukkit.*;

import static com.onarandombox.MultiverseCore.enums.AllowedPortalType.ALL;

public class MultiverseManager implements WorldManager {

    private final MVWorldManager worldManager;

    private final Manhunt plugin;

    public MultiverseManager(Manhunt plugin) {
        this.plugin = plugin;
        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        assert core != null;
        worldManager = core.getMVWorldManager();
    }

    @Override
    public World create(long seed, String worldName, World.Environment environment) {
        String seedstr = String.valueOf(seed);
        worldManager.addWorld(worldName, environment, seedstr, WorldType.NORMAL, true, null);
        MultiverseWorld wworld = worldManager.getMVWorld(worldName);
        wworld.setKeepSpawnInMemory(true);
        wworld.allowPortalMaking(ALL);
        wworld.getCBWorld().setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
        wworld.getCBWorld().setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        wworld.getCBWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);

        if (plugin.getCfg().enableWorldBorder) {
            WorldBorder border = wworld.getCBWorld().getWorldBorder();
            border.setSize(plugin.getCfg().worldBorderSize);
            border.setCenter(wworld.getSpawnLocation());
            border.setDamageAmount(plugin.getCfg().worldBorderDamage);
            border.setDamageBuffer(plugin.getCfg().worldBorderDamageBuffer);
            border.setWarningDistance(plugin.getCfg().worldBorderWarningDistance);
            border.setWarningTime(plugin.getCfg().worldBorderWarningTime);
        }
        return wworld.getCBWorld();
    }

    @Override
    public void delete(String worldName) {
        if (Bukkit.getWorld(worldName) != null) {
            worldManager.unloadWorld(worldName);
            worldManager.deleteWorld(worldName);
        }
    }
    @Override
    public void worldLoad(String worldName){
        if (Bukkit.getWorld(worldName) != null){
            worldManager.loadWorld(worldName);
        }
    }

    @Override
    public void worldUnload(String worldName){
        if (Bukkit.getWorld(worldName) != null){
            worldManager.unloadWorld(worldName);
        }
    }
}
