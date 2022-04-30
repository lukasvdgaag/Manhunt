package me.gaagjescraft.network.team.manhunt.world;

import com.grinderwolf.swm.api.SlimePlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class Slime implements WorldUtil{

    private final SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

    @Override
    public World create(long seed, String worldidentifer, World.Environment environment) {
        return null;
    }

    @Override
    public void delete(String worldidentifer) {

    }
}
