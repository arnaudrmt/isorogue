package fr.arnaud.isorogue.world;

import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.TreeType;

public enum LevelTheme {

    OVERWORLD(
            World.Environment.NORMAL,2.0,
            IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.GRASS_BLOCK), Material.DIRT,
            IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.TALL_GRASS),
            IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.POPPY), TreeType.TREE
    ),
    NETHER(
            World.Environment.NETHER, 4.0,
            Material.NETHERRACK, Material.SOUL_SAND,
            IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.NETHER_WARTS),
            Material.RED_MUSHROOM, TreeType.BROWN_MUSHROOM
    ),
    END(
            World.Environment.THE_END, 3.0,
            IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.ENDER_STONE), Material.OBSIDIAN,
            null, null, TreeType.REDWOOD
    );

    private final World.Environment environment;
    private final double reliefAmplitude;
    private final Material topBlock;
    private final Material underBlock;
    private final Material grass;
    private final Material flower;
    private final TreeType treeType;

    LevelTheme(World.Environment environment, double reliefAmplitude, Material topBlock, Material underBlock,
               Material grass, Material flower, TreeType treeType) {
        this.environment = environment;
        this.reliefAmplitude = reliefAmplitude;
        this.topBlock = topBlock;
        this.underBlock = underBlock;
        this.grass = grass;
        this.flower = flower;
        this.treeType = treeType;
    }

    public World.Environment getEnvironment() { return environment; }
    public double getReliefAmplitude() { return reliefAmplitude; }
    public Material getTopBlock() { return topBlock; }
    public Material getUnderBlock() { return underBlock; }
    public Material getGrass() { return grass; }
    public Material getFlower() { return flower; }
    public TreeType getTreeType() { return treeType; }
}