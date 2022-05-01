package me.gaagjescraft.network.team.manhunt.utils;

public class ChunkyHook {

    public void chunkgen(double x, double z, String worldidentifer) {
        /*Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            ChunkyBukkit chunk = (ChunkyBukkit) Bukkit.getPluginManager().getPlugin("Chunky");
            Selection.Builder selection = chunk.getChunky().getSelection();
            selection.centerX(x);
            selection.centerZ(z);
            Optional<World> world = Input.tryWorld(chunk.getChunky(), worldidentifer);
            selection.world(world.get());
            selection.shape(ShapeType.CIRCLE);
            selection.radius(plugin.getCfg().gensizechunks);
            GenerationTask task = new GenerationTask(chunk.getChunky(), selection.build());
            chunk.getChunky().getGenerationTasks().put(worldidentifer, task);
            chunk.getChunky().getScheduler().runTask(task);
        }, 600L);*/
    }
}
