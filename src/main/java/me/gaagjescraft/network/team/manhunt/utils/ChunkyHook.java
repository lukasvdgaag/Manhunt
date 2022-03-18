package me.gaagjescraft.network.team.manhunt.utils;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import org.bukkit.Bukkit;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.GenerationTask;
import org.popcraft.chunky.Selection;
import org.popcraft.chunky.platform.World;
import org.popcraft.chunky.shape.ShapeType;
import org.popcraft.chunky.util.Input;

import java.util.Optional;

public class ChunkyHook {

    public void chunkgen(double x,  double z, String worldidentifer){
        ChunkyBukkit chunk = (ChunkyBukkit) Bukkit.getPluginManager().getPlugin("Chunky");
        Selection.Builder selection = chunk.getChunky().getSelection();
        selection.centerX(x);
        selection.centerZ(z);
        Optional<World> world = Input.tryWorld(chunk.getChunky(), worldidentifer);
        selection.world(world.get());
        selection.shape(ShapeType.CIRCLE);
        selection.radius(Manhunt.get().getCfg().gensizechunks);
        GenerationTask task = new GenerationTask(chunk.getChunky(), selection.build());
        chunk.getChunky().getGenerationTasks().put(worldidentifer, task);
        chunk.getChunky().getScheduler().runTask(task);
    }
}
