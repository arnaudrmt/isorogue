package fr.arnaud.isorogue.game.item.armors;

import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import fr.arnaud.isorogue.game.item.IArmor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GoldenChestplate implements IArmor {

    @Override public String getName() { return "Golden Chestplate"; }
    @Override public Material getMaterial() { return IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.GOLDEN_CHESTPLATE); }
    @Override public ItemStack getItemStack() { return new ItemStack(getMaterial()); }
    @Override public double getDamageReduction() { return 0.50; }
    @Override public int getTier() { return 4; }
}