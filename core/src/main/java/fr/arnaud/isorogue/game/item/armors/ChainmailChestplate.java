package fr.arnaud.isorogue.game.item.armors;

import fr.arnaud.isorogue.game.item.IArmor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ChainmailChestplate implements IArmor {

    @Override public String getName() { return "Chainmail Chestplate"; }
    @Override public Material getMaterial() { return Material.CHAINMAIL_CHESTPLATE; }
    @Override public ItemStack getItemStack() { return new ItemStack(getMaterial()); }
    @Override public double getDamageReduction() { return 0.10; }
    @Override public int getTier() { return 2; }
}