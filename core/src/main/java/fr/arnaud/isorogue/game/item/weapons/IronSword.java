package fr.arnaud.isorogue.game.item.weapons;

import fr.arnaud.isorogue.game.item.IWeapon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class IronSword implements IWeapon {

    @Override public String getName() { return "Iron Sword"; }
    @Override public Material getMaterial() { return Material.IRON_SWORD; }
    @Override public ItemStack getItemStack() { return new ItemStack(getMaterial()); }
    @Override public int getDamage() { return 6; }
    @Override public int getTier() { return 3; }
}