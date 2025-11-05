package fr.arnaud.isorogue.game.managers;

import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.game.LootTable;
import fr.arnaud.isorogue.game.item.IArmor;
import fr.arnaud.isorogue.game.item.IItem;
import fr.arnaud.isorogue.game.item.IWeapon;
import fr.arnaud.isorogue.game.item.ItemRegistry;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class LootManager {

    private final GameInstance gameInstance;
    private final LootTable lootTable;

    public LootManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        this.lootTable = new LootTable();
    }

    public void spawnLoot(Location location) {
        IItem loot = lootTable.getRandomLoot();
        if (loot == null) return;

        ItemStack lootStack = loot.getItemStack();
        location.getWorld().playSound(location, gameInstance.getWrapper().getSound(BukkitWrapperAPI.SoundType.CHEST_OPEN),
                1.0f, 1.2f);
        Item droppedItem = location.getWorld().dropItem(location.add(0, 0.5, 0), lootStack);

        Entity hologram = gameInstance.getPlugin().getNMSHandler().spawnHologram(
                location.clone().add(0, 0.5, 0),
                ChatColor.AQUA + loot.getName()
        );
        droppedItem.setPassenger(hologram);

        Bukkit.getScheduler().runTaskLater(gameInstance.getPlugin(), () -> {
            if (!droppedItem.isDead()) droppedItem.remove();
            if (!hologram.isDead()) hologram.remove();
        }, 100L);

        double x = (Math.random() - 0.5) * 0.4;
        double y = 0.3 + (Math.random() * 0.2);
        double z = (Math.random() - 0.5) * 0.4;
        droppedItem.setVelocity(new Vector(x, y, z));
    }

    public void checkForPickups() {
        Location npcLocation = gameInstance.getPlayerNPC().getVisualEntityLocation();

        for (Item item : npcLocation.getWorld().getEntitiesByClass(Item.class)) {
            if (item.getLocation().distanceSquared(npcLocation) < 2.25) {

                IItem potentialItem = ItemRegistry.getFromMaterial(item.getItemStack().getType());
                if (potentialItem == null) continue;

                boolean pickedUp = false;
                if (potentialItem instanceof IWeapon) {
                    IWeapon newWeapon = (IWeapon) potentialItem;
                    if (gameInstance.getPlayerWeapon() == null || newWeapon.getTier() > gameInstance.getPlayerWeapon().getTier()) {
                        gameInstance.setPlayerWeapon(newWeapon);
                        pickedUp = true;
                    }
                } else if (potentialItem instanceof IArmor) {
                    IArmor newArmor = (IArmor) potentialItem;
                    if (gameInstance.getEquippedArmor() == null || newArmor.getTier() > gameInstance.getEquippedArmor().getTier()) {
                        gameInstance.equipArmor(newArmor);
                        pickedUp = true;
                    }
                }

                if (pickedUp) {
                    if (item.getPassenger() != null && item.getPassenger().getType() == EntityType.ARMOR_STAND) {
                        item.getPassenger().remove();
                    }
                    item.remove();
                    npcLocation.getWorld().playSound(npcLocation,
                            gameInstance.getWrapper().getSound(BukkitWrapperAPI.SoundType.ITEM_PICKUP), 1.0f, 1.0f);
                }
            }
        }
    }
}