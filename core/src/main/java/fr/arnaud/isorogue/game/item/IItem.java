package fr.arnaud.isorogue.game.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface IItem {
    String getName();
    Material getMaterial();
    int getTier();

    default ItemStack getItemStack() {
        return new ItemStack(getMaterial());
    }
}