package fr.arnaud.isorogue.game.item.weapons;

import fr.arnaud.isorogue.game.item.IWeapon;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class DiamondSword implements IWeapon {

    @Override public String getName() { return "Diamond Sword"; }
    @Override public Material getMaterial() { return Material.DIAMOND_SWORD; }
    @Override public ItemStack getItemStack() { return new ItemStack(getMaterial()); }
    @Override public int getDamage() { return 10; }
    @Override public int getTier() { return 5; }
}