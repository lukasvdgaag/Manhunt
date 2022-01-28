package me.gaagjescraft.network.team.manhunt.games;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class GameSchematic {

    private final Game game;
    private EditSession editSession;
    private Location spawnLocation;

    GameSchematic(Game game) {
        this.game = game;
        this.editSession = null;
        this.spawnLocation = null;
    }

    public void load() {
        org.bukkit.World world = Bukkit.getWorld(game.getWorldIdentifier());
        Location spawn = world.getSpawnLocation();
        spawn.setY(150);
        spawn.setYaw(180);
        spawn.setPitch(0);

        this.spawnLocation = spawn.clone().add(1.5, 15, 0.5);

        File file = new File(Manhunt.get().getDataFolder(), "manhunt-lobby.schem");
        BlockVector3 to = BlockVector3.at(spawn.getX(), spawn.getY(), spawn.getZ());
        ClipboardFormat format = ClipboardFormats.findByFile(file);

        if (format == null) {
            Bukkit.getLogger().severe("Manhunt failed to load the manhunt-lobby.schem file from the /plugins/Manhunt/ folder so we couldn't place the lobby schematic.");
            Bukkit.getLogger().severe("We will stop this game to prevent further issues.");
            if (!file.exists()) Bukkit.getLogger().severe("Missing file: " + file.getAbsolutePath());
            else
                Bukkit.getLogger().severe("File is not actually missing, it's just the WorldEdit ClipboardFormat being null.");
            this.game.delete();
            return;
        }

        try {
            if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
                Clipboard clipboard;

                try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                    clipboard = reader.read();
                }

                try (EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(world))) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(to)
                            .build();
                    Operations.complete(operation);
                    this.editSession = editSession;
                } catch (WorldEditException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unload() {
        World world = Bukkit.getWorld(game.getWorldIdentifier());
        Location spawn = world.getSpawnLocation();
        spawn.setY(150);

        this.editSession.undo(editSession);
        WorldEdit.getInstance().flushBlockBag(null, this.editSession);

        /*CuboidRegion region = new CuboidRegion(new BukkitWorld(world),
                BlockVector3.at(spawn.getBlockX()-28,150,spawn.getBlockZ()+27),
                BlockVector3.at(spawn.getBlockX()+29,207,spawn.getBlockZ()-32));
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        EditSession editSession = new EditSessionBuilder(new BukkitWorld(world)).build();

        editSession.setBlocks((Region)region, BlockTypes.AIR);
        Bukkit.getLogger().warning("Cut the lobby area using FAWE.");*/

       /* if (this.editSession == null) {
            Bukkit.getLogger().severe("Unable to remove the waiting spawn: worldedit session is null.");
            return;
        }*/

        //this.editSession.undo(this.editSession);
        // todo reload chunks or something cuz blocks are still there on the client side.
        //this.editSession.close();
        //Bukkit.getLogger().warning("Undone the pasting of the waiting lobby schematic using FAWE.");


    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }
}
