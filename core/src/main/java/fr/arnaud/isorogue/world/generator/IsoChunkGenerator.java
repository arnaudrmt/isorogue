package fr.arnaud.isorogue.world.generator;

import fr.arnaud.isorogue.world.LevelTheme;
import fr.arnaud.isorogue.world.generator.populator.DecorationPopulator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class IsoChunkGenerator extends ChunkGenerator {

    private static final int MAP_WIDTH = 15;
    private static final int MAP_LENGTH = 40;
    private static final int BASE_HEIGHT = 64;

    private final LevelTheme theme;

    public IsoChunkGenerator(LevelTheme theme) {
        this.theme = theme;
    }

    @Override
    public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomes) {
        byte[][] result = new byte[world.getMaxHeight() / 16][];
        SimplexOctaveGenerator noise = new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
        noise.setScale(0.03);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                if (worldX >= 0 && worldX < MAP_WIDTH && worldZ >= 0 && worldZ < MAP_LENGTH) {
                    double noiseValue = noise.noise(worldX, worldZ, 0.5, 0.5, true);
                    int elevation = (int) (noiseValue * theme.getReliefAmplitude());
                    int finalHeight = BASE_HEIGHT + elevation;

                    setBlock(result, x, finalHeight, z, (byte) theme.getTopBlock().getId());
                    setBlock(result, x, finalHeight - 1, z, (byte) theme.getUnderBlock().getId());
                    setBlock(result, x, BASE_HEIGHT - 2, z, (byte) Material.BEDROCK.getId());

                    if (worldX == 0 || worldX == MAP_WIDTH - 1 || worldZ == 0 || worldZ == MAP_LENGTH - 1) {
                        for (int y = finalHeight + 1; y <= finalHeight + 3; y++) {
                            setBlock(result, x, y, z, (byte) Material.BARRIER.getId());
                        }
                    }

                    if (worldX == MAP_WIDTH - 2 && worldZ == MAP_LENGTH - 2) {
                        setBlock(result, x, finalHeight, z, (byte) Material.EMERALD_BLOCK.getId());
                    }
                }
            }
        }
        return result;
    }

    private void setBlock(byte[][] result, int x, int y, int z, byte blockId) {
        if (y < 0 || y >= 256) return;
        if (result[y >> 4] == null) {
            result[y >> 4] = new byte[4096];
        }
        result[y >> 4][((y & 15) << 8) | (z << 4) | x] = blockId;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Collections.singletonList(new DecorationPopulator(theme));
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 1.5, BASE_HEIGHT + 2, 1.5);
    }
}