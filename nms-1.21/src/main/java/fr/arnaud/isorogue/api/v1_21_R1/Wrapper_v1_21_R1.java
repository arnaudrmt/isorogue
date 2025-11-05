package fr.arnaud.isorogue.api.v1_21_R1;

import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

public class Wrapper_v1_21_R1 implements BukkitWrapperAPI {

    @Override
    public boolean isXRayOccluder(Block block) {
        Material type = block.getType();
        return type == Material.OAK_LEAVES || type == Material.DARK_OAK_LEAVES ||
                type == Material.OAK_LOG || type == Material.DARK_OAK_LOG ;
    }

    @Override
    public boolean isLava(Material material) {
        return material == Material.LAVA;
    }

    @Override
    public Sound getSound(SoundType type) {
        switch (type) {
            case ZOMBIE_HURT: return Sound.ENTITY_ZOMBIE_HURT;
            case ZOMBIE_DEATH: return Sound.ENTITY_ZOMBIE_DEATH;
            case PIGMAN_HURT: return Sound.ENTITY_ZOMBIFIED_PIGLIN_HURT;
            case PIGMAN_DEATH: return Sound.ENTITY_ZOMBIFIED_PIGLIN_DEATH;
            case ENDERMAN_HURT: return Sound.ENTITY_ENDERMAN_HURT;
            case ENDERMAN_DEATH: return Sound.ENTITY_ENDERMAN_DEATH;

            case CHEST_OPEN: return Sound.BLOCK_CHEST_OPEN;
            case ITEM_PICKUP: return Sound.ENTITY_ITEM_PICKUP;
            case FIZZ: return Sound.BLOCK_FIRE_EXTINGUISH;
            case SUCCESSFUL_HIT: return Sound.ENTITY_PLAYER_ATTACK_STRONG;
        }
        return Sound.ENTITY_PLAYER_LEVELUP;
    }

    @Override
    public Material getMaterial(MaterialType type) {
        switch (type) {
            case GRASS_BLOCK: return Material.GRASS_BLOCK;
            case ENDER_STONE: return Material.END_STONE;
            case NETHER_WARTS: return Material.NETHER_WART;

            case TALL_GRASS: return Material.TALL_GRASS;
            case POPPY: return Material.POPPY;

            case OAK_LOG: return Material.OAK_LOG;
            case OAK_LEAVES: return Material.OAK_LEAVES;
            case MUSHROOM_STEM: return Material.MUSHROOM_STEM;
            case MUSHROOM_CAP: return Material.RED_MUSHROOM_BLOCK;

            case PORKCHOP: return Material.PORKCHOP;
            case WOODEN_SWORD: return Material.WOODEN_SWORD;
            case GOLDEN_SWORD: return Material.GOLDEN_SWORD;
            case GOLDEN_CHESTPLATE: return Material.GOLDEN_CHESTPLATE;
        }
        return Material.STICK;
    }
}
