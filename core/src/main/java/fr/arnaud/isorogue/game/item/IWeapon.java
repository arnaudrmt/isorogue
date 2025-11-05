package fr.arnaud.isorogue.game.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface IWeapon extends IItem {

    String getName();

    Material getMaterial();

    ItemStack getItemStack();

    int getDamage();

    int getTier();
}