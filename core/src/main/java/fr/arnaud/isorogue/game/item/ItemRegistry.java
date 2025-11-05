package fr.arnaud.isorogue.game.item;

import fr.arnaud.isorogue.game.item.armors.*;
import fr.arnaud.isorogue.game.item.weapons.*;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {

    private static final Map<Material, IItem> REGISTRY = new HashMap<>();

    static {
        register(new StoneSword());
        register(new IronSword());
        register(new GoldenSword());
        register(new DiamondSword());
        register(new LeatherChestplate());
        register(new ChainmailChestplate());
        register(new IronChestplate());
        register(new GoldenChestplate());
        register(new DiamondChestplate());
    }

    private static void register(IItem item) {
        REGISTRY.put(item.getMaterial(), item);
    }

    public static IItem getFromMaterial(Material material) {
        return REGISTRY.get(material);
    }
}