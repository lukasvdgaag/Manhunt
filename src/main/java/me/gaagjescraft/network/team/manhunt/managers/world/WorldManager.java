package me.gaagjescraft.network.team.manhunt.managers.world;

import org.bukkit.World;

public interface WorldManager {

    World create(long seed, String worldName, World.Environment environment);

    void delete(String worldName);

    void multiverseLoad(String worldName);

    void worldUnload(String worldName);

}
