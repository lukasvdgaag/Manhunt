package me.gaagjescraft.network.team.manhunt.world;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.utils.FileUtils;
import org.bukkit.*;

import java.io.IOException;

public class DefaultBukkit implements WorldUtil{
    @Override
    public World create(long seed, String worldidentifier, World.Environment environment) {
        //Default Bukkit World creation
        WorldCreator creator = new WorldCreator(worldidentifier);
        creator.environment(environment);
        creator.seed(seed);
        World world = creator.createWorld();

        assert world != null;
        world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);

        if (Manhunt.get().getCfg().enableWorldBorder) {
            WorldBorder border = world.getWorldBorder();
            border.setSize(Manhunt.get().getCfg().worldBorderSize);
            border.setCenter(world.getSpawnLocation());
            border.setDamageAmount(Manhunt.get().getCfg().worldBorderDamage);
            border.setDamageBuffer(Manhunt.get().getCfg().worldBorderDamageBuffer);
            border.setWarningDistance(Manhunt.get().getCfg().worldBorderWarningDistance);
            border.setWarningTime(Manhunt.get().getCfg().worldBorderWarningTime);
        }
        return world;
    }

    @Override
    public void delete(String worldidentifier) {
        World w = Bukkit.getWorld(worldidentifier);
        if (w != null) {
            Bukkit.unloadWorld(w, false);
            try {
                FileUtils.deleteDirectory(w.getWorldFolder());
            } catch (IOException ignored) {
            }
        }
    }
}
