package fr.arnaud.isorogue.api.v1_8_R3;

import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

public class Wrapper_v1_8_R3 implements BukkitWrapperAPI {

    @Override
    public boolean isXRayOccluder(Block block) {
        Material type = block.getType();
        return type == Material.LEAVES || type == Material.LEAVES_2 || type == Material.LOG || type == Material.LOG_2;
    }

    @Override
    public boolean isLava(Material material) {
        return material == Material.LAVA || material == Material.STATIONARY_LAVA;
    }

    @Override
    public Sound getSound(SoundType type) {
        switch (type) {
            case ZOMBIE_HURT: return Sound.ZOMBIE_HURT;
            case ZOMBIE_DEATH: return Sound.ZOMBIE_DEATH;
            case PIGMAN_HURT: return Sound.ZOMBIE_PIG_HURT;
            case PIGMAN_DEATH: return Sound.ZOMBIE_PIG_DEATH;
            case ENDERMAN_HURT: return Sound.ENDERMAN_HIT;
            case ENDERMAN_DEATH: return Sound.ENDERMAN_DEATH;

            case CHEST_OPEN: return Sound.CHEST_OPEN;
            case ITEM_PICKUP: return Sound.ITEM_PICKUP;
            case FIZZ: return Sound.FIZZ;
            case SUCCESSFUL_HIT: return Sound.SUCCESSFUL_HIT;
        }
        return Sound.LEVEL_UP;
    }

    @Override
    public Material getMaterial(MaterialType type) {
        switch (type) {
            case GRASS_BLOCK: return Material.GRASS;
            case ENDER_STONE: return Material.ENDER_STONE;
            case NETHER_WARTS: return Material.NETHER_WARTS;

            case TALL_GRASS: return Material.LONG_GRASS;
            case POPPY: return Material.RED_ROSE;

            case OAK_LOG: return Material.LOG;
            case OAK_LEAVES: return Material.LEAVES;
            case MUSHROOM_STEM: return Material.HUGE_MUSHROOM_1;
            case MUSHROOM_CAP: return Material.HUGE_MUSHROOM_2;

            case PORKCHOP: return Material.PORK;
            case WOODEN_SWORD: return Material.WOOD_SWORD;
            case GOLDEN_SWORD: return Material.GOLD_SWORD;
            case GOLDEN_CHESTPLATE: return Material.GOLD_CHESTPLATE;
        }
        return Material.STICK;
    }
}
