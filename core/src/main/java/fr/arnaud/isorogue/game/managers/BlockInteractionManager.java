package fr.arnaud.isorogue.game.managers;

import fr.arnaud.isorogue.entity.IPlayerNPC;
import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.api.NMSHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.*;

public class BlockInteractionManager {

    private final GameInstance gameInstance;

    private final Set<Block> openedChests = new HashSet<>();

    public BlockInteractionManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public Block findTargetedChest(double maxDistance) {
        IPlayerNPC playerNPC = gameInstance.getPlayerNPC();
        if (playerNPC == null) return null;

        LivingEntity hitbox = playerNPC.getHitbox();
        Location eyeLocation = hitbox.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        Location feetLocation = hitbox.getLocation();

        List<Block> potentialTargets = new ArrayList<>();

        for (double d = 1.0; d < maxDistance; d += 0.5) {
            Location checkCenter = eyeLocation.clone().add(direction.clone().multiply(d));

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        Block block = checkCenter.clone().add(x, y, z).getBlock();
                        if (block.getType() == Material.CHEST) {
                            potentialTargets.add(block);
                        }
                    }
                }
            }
        }

        if (potentialTargets.isEmpty()) {
            return null;
        }

        potentialTargets.sort(Comparator.comparingDouble(block ->
                feetLocation.distanceSquared(block.getLocation().add(0.5, 0.5, 0.5))));

        return potentialTargets.stream().
                filter(chest -> !openedChests.contains(chest))
                .findFirst().orElse(null);
    }

    public void openChest(Block chestBlock) {
        if (openedChests.contains(chestBlock)) return;

        NMSHandler nmsHandler = gameInstance.getPlugin().getNMSHandler();

        nmsHandler.playChestAnimation(gameInstance.getPlayer(), chestBlock, true);

        gameInstance.getLootManager().spawnLoot(chestBlock.getLocation().add(0.5, 0.5, 0.5));

        for (Entity entity : chestBlock.getWorld().getNearbyEntities(chestBlock.getLocation(), 1, 1, 1)) {
            if (entity.getType() == EntityType.ARMOR_STAND && entity.getCustomName() != null
                    && entity.getCustomName().contains("Click Me")) {
                entity.remove();
                break;
            }
        }

        this.openedChests.add(chestBlock);
    }
}