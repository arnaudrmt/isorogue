package fr.arnaud.isorogue.game.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface IArmor extends IItem {

    String getName();
    Material getMaterial();
    ItemStack getItemStack();
    double getDamageReduction();
    int getTier();
}