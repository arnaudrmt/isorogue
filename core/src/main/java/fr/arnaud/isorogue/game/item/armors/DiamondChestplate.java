package fr.arnaud.isorogue.game.item.armors;

import fr.arnaud.isorogue.game.item.IArmor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DiamondChestplate implements IArmor {

    @Override public String getName() { return "Diamond Chestplate"; }
    @Override public Material getMaterial() { return Material.DIAMOND_CHESTPLATE; }
    @Override public ItemStack getItemStack() { return new ItemStack(getMaterial()); }
    @Override public double getDamageReduction() { return 0.75; }
    @Override public int getTier() { return 5; }
}