package fr.arnaud.isorogue.game.item.weapons;

import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import fr.arnaud.isorogue.game.item.IWeapon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class WoodenSword implements IWeapon {

    @Override public String getName() { return "Wooden Sword"; }
    @Override public Material getMaterial() { return IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.WOODEN_SWORD); }
    @Override public ItemStack getItemStack() { return new ItemStack(getMaterial()); }
    @Override public int getDamage() { return 2; }
    @Override public int getTier() { return 1; }
}