package fr.arnaud.isorogue.game.item.weapons;

import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import fr.arnaud.isorogue.game.item.IWeapon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GoldenSword implements IWeapon {

    @Override public String getName() { return "Golden Sword"; }
    @Override public Material getMaterial() { return IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.GOLDEN_SWORD); }
    @Override public ItemStack getItemStack() { return new ItemStack(getMaterial()); }
    @Override public int getDamage() { return 8; }
    @Override public int getTier() { return 4; }
}