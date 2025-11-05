package fr.arnaud.isorogue.game.item.armors;

import fr.arnaud.isorogue.game.item.IArmor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LeatherChestplate implements IArmor {

    @Override public String getName() { return "Leather Tunic"; }
    @Override public Material getMaterial() { return Material.LEATHER_CHESTPLATE; }
    @Override public ItemStack getItemStack() { return new ItemStack(getMaterial()); }
    @Override public double getDamageReduction() { return 0.05; }
    @Override public int getTier() { return 1; }
}