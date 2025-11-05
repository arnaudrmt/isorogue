package fr.arnaud.isorogue.world.generator.populator;

import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import fr.arnaud.isorogue.world.LevelTheme;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class DecorationPopulator extends BlockPopulator {

    private final LevelTheme theme;
    private static final int MAP_WIDTH = 15;
    private static final int MAP_LENGTH = 40;

    public DecorationPopulator(LevelTheme theme) {
        this.theme = theme;
    }

    @Override
    public void populate(World world, Random random, Chunk source) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = source.getX() * 16 + x;
                int worldZ = source.getZ() * 16 + z;

                if (worldX >= 0 && worldX < MAP_WIDTH && worldZ >= 0 && worldZ < MAP_LENGTH) {
                    if (worldX <= 1 || worldX >= MAP_WIDTH - 2 || worldZ <= 1 || worldZ >= MAP_LENGTH - 2) {
                        continue;
                    }

                    int y = world.getHighestBlockYAt(worldX, worldZ);
                    Block block = world.getBlockAt(worldX, y, worldZ);
                    Block ground = block.getRelative(BlockFace.DOWN);

                    if (theme == LevelTheme.OVERWORLD && ground.getType() == theme.getTopBlock()) {
                        populateOverworld(block, random);
                    } else if (theme == LevelTheme.NETHER && ground.getType() == theme.getTopBlock()) {
                        populateNether(block, random);
                    } else if (theme == LevelTheme.END && ground.getType() == theme.getTopBlock()) {
                        populateEnd(block, random);
                    }
                }
            }
        }
    }

    private void populateOverworld(Block block, Random random) {
        if (random.nextInt(100) < 8) {
            block.setType(IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.TALL_GRASS));
            block.setData((byte) 1);
        } else if (random.nextInt(100) < 2) {
            block.setType(IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.POPPY));
        } else if (random.nextInt(1000) < 2) {
            block.getWorld().generateTree(block.getLocation(), TreeType.TREE);
        } else if (random.nextInt(1000) < 3) {
            spawnChest(block);
        }
    }

    private void populateNether(Block block, Random random) {
        if (random.nextInt(100) < 8) {
            block.getRelative(BlockFace.DOWN).setType(Material.SOUL_SAND);
            block.setType(IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.NETHER_WARTS));
        } else if (random.nextInt(100) < 2) {
            block.setType(Material.RED_MUSHROOM);
        } else if (random.nextInt(1000) < 5) {
            generateNetherMushroomTree(block, random);
        } else if (random.nextInt(1000) < 10) {
            block.setType(Material.LAVA);
        } else if (random.nextInt(1000) < 3) {
            spawnChest(block);
        }
    }

    private void populateEnd(Block block, Random random) {
        if (random.nextInt(1000) < 5) {
            block.getRelative(0, -1, 0).setType(Material.OBSIDIAN);
            block.setType(Material.OBSIDIAN);
            block.getRelative(0, 1, 0).setType(Material.OBSIDIAN);
        } else if (random.nextInt(1000) < 3) {
            spawnChest(block);
        }
    }

    private void spawnChest(Block block) {
        block.setType(Material.CHEST);
        Location hologramLocation = block.getLocation().add(0.5, 0.8, 0.5);
        IsoRogue.getInstance().getNMSHandler()
                .spawnHologram(hologramLocation, ChatColor.YELLOW + "" + ChatColor.BOLD + "Click Me!");
    }

    private void generateNetherMushroomTree(Block startBlock, Random random) {
        Material hugeMushroom2 = IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.MUSHROOM_CAP);

        int height = 3 + random.nextInt(3);

        for (int i = 0; i < height; i++) {
            startBlock.getRelative(0, i, 0).setType(
                    IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.MUSHROOM_STEM));
        }

        Block capCenter = startBlock.getRelative(0, height, 0);
        capCenter.setType(hugeMushroom2);

        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            capCenter.getRelative(face).setType(hugeMushroom2);
            capCenter.getRelative(face).getRelative(BlockFace.DOWN).setType(hugeMushroom2);
        }
        capCenter.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.DOWN).setType(hugeMushroom2);
        capCenter.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.DOWN).setType(hugeMushroom2);
        capCenter.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.DOWN).setType(hugeMushroom2);
        capCenter.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.DOWN).setType(hugeMushroom2);
    }
}