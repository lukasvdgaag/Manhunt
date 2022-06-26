package me.gaagjescraft.network.team.manhunt.managers.world;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.utils.FileUtils;
import org.bukkit.*;

import java.io.IOException;

import static org.bukkit.Bukkit.getServer;

public class BukkitWorldManager implements WorldManager {

    private final Manhunt plugin;

    public BukkitWorldManager(Manhunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public World create(long seed, String worldName, World.Environment environment) {
        //Default Bukkit World creation
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(environment);
        creator.seed(seed);
        World world = creator.createWorld();

        assert world != null;
        world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);

        if (plugin.getCfg().enableWorldBorder) {
            WorldBorder border = world.getWorldBorder();
            border.setSize(plugin.getCfg().worldBorderSize);
            border.setCenter(world.getSpawnLocation());
            border.setDamageAmount(plugin.getCfg().worldBorderDamage);
            border.setDamageBuffer(plugin.getCfg().worldBorderDamageBuffer);
            border.setWarningDistance(plugin.getCfg().worldBorderWarningDistance);
            border.setWarningTime(plugin.getCfg().worldBorderWarningTime);
        }
        return world;
    }

    @Override
    public void delete(String worldName) {
        World w = Bukkit.getWorld(worldName);
        if (w != null) {
            Bukkit.unloadWorld(w, false);
            try {
                FileUtils.deleteDirectory(w.getWorldFolder());
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void worldLoad(String worldName) {
        getServer().createWorld(new WorldCreator(worldName));
    }

    @Override
    public void worldUnload(String worldName){
        getServer().unloadWorld(worldName, true);
    }
}
