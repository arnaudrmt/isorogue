package fr.arnaud.isorogue.game.item.weapons;

import fr.arnaud.isorogue.game.item.IWeapon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class StoneSword implements IWeapon {

    @Override public String getName() { return "Stone Sword"; }
    @Override public Material getMaterial() { return Material.STONE_SWORD; }
    @Override public ItemStack getItemStack() { return new ItemStack(getMaterial()); }
    @Override public int getDamage() { return 4; }
    @Override public int getTier() { return 2; }
}