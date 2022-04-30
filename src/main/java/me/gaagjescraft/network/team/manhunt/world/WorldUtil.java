package me.gaagjescraft.network.team.manhunt.world;

import org.bukkit.World;

public interface WorldUtil {

    World create(long seed, String worldidentifer, World.Environment environment);

    void delete(String worldidentifer);

}
