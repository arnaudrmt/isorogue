package fr.arnaud.isorogue.api;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

public interface BukkitWrapperAPI {

    boolean isXRayOccluder(Block block);

    boolean isLava(Material material);

    enum SoundType {
        // Enemy Sounds
        ZOMBIE_HURT,
        ZOMBIE_DEATH,
        PIGMAN_HURT,
        PIGMAN_DEATH,
        ENDERMAN_HURT,
        ENDERMAN_DEATH,
        ENDERMAN_TELEPORT,

        // World/Interaction Sounds
        CHEST_OPEN,
        ITEM_PICKUP,
        FIZZ,
        SUCCESSFUL_HIT
    }

    Sound getSound(SoundType type);

    enum MaterialType {
        // Thematic Blocks
        GRASS_BLOCK,
        ENDER_STONE,
        NETHER_WARTS,

        // Decorations
        TALL_GRASS,
        POPPY,

        // Structures
        OAK_LOG,
        OAK_LEAVES,
        MUSHROOM_STEM,
        MUSHROOM_CAP,

        // Items
        PORKCHOP,
        WOODEN_SWORD,
        GOLDEN_SWORD,
        GOLDEN_CHESTPLATE
    }

    Material getMaterial(MaterialType type);
}
