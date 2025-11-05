package fr.arnaud.isorogue.world;

import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.world.generator.IsoChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import java.io.File;

public class WorldManager {

    public World createGameWorld(String worldName, LevelTheme theme) {
        WorldCreator wc = new WorldCreator(worldName);
        wc.generator(new IsoChunkGenerator(theme));
        wc.environment(theme.getEnvironment());
        wc.type(WorldType.FLAT);
        wc.generateStructures(false);
        return Bukkit.createWorld(wc);
    }

    public void deleteWorld(World world) {
        if (world == null) return;

        for (Player p : world.getPlayers()) {
            p.teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
        }

        File worldFolder = world.getWorldFolder();

        boolean unloaded = Bukkit.unloadWorld(world, false);

        if (unloaded) {
            IsoRogue.getInstance().getLogger().info("SYNC: Successfully unloaded world: " + world.getName());
            if (deleteWorldFolder(worldFolder)) {
                IsoRogue.getInstance().getLogger().info("SYNC: Successfully deleted world folder.");
            } else {
                IsoRogue.getInstance().getLogger().warning("SYNC ERROR: Could not delete world folder: " +
                        worldFolder.getPath());
            }
        } else {
            IsoRogue.getInstance().getLogger().warning("SYNC ERROR: Could not unload world: " + world.getName());
        }
    }

    private boolean deleteWorldFolder(File path) {
        if (!path.exists()) {
            return false;
        }
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!deleteWorldFolder(file)) {
                        return false;
                    }
                } else {
                    if (!file.delete()) {
                        return false;
                    }
                }
            }
        }
        return path.delete();
    }
}