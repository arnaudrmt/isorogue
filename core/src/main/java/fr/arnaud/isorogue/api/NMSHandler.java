package fr.arnaud.isorogue.api;

import fr.arnaud.isorogue.entity.IPlayerNPC;
import fr.arnaud.isorogue.game.enemy.MobType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

public interface NMSHandler {

    // Player Packets

    void injectPacketListener(Player player);

    void uninjectPacketListener(Player player);

    void forcePlayerRotation(Player player, float yaw, float pitch);

    // Player NPC Packets

    IPlayerNPC spawnPlayerNPC(Player player, Location location);

    void despawnPlayerNPC(Player viewer, IPlayerNPC npc);

    void setNPCEquipment(Player viewer, int entityId, int slot, ItemStack item);

    void moveEntity(IPlayerNPC npc, double x, double y, double z);

    void updateNPCHeadRotation(Player viewer, IPlayerNPC npc, float yaw, float pitch);

    void updateNPCSneak(Player viewer, IPlayerNPC npc, boolean isSneaking);

    void setNPCOnFire(Player viewer, IPlayerNPC npc, boolean onFire);

    // Custom Mobs Packets

    void registerCustomEntities();

    LivingEntity spawnCustomMob(Location location, MobType type);

    void setEntityTarget(LivingEntity entity, LivingEntity target, EntityTargetEvent.TargetReason reason);

    float getEntityHeadYaw(LivingEntity entity);

    void navigateEntityToLocation(LivingEntity entity, Location location, double speed);

    // Entity Packets

    void teleportEntity(Player viewer, Entity entity, Location location);

    // Entity Animations Packets

    void playArmSwingAnimation(Player viewer, LivingEntity entity);

    void playHurtAnimation(Player viewer, LivingEntity entity);

    // Blocks Packets

    void playChestAnimation(Player viewer, Block chestBlock, boolean isOpen);

    void sendBlockChange(Player player, Location location, Material material, byte data);

    // NMS Utils Packets

    void spawnParticle(Player player, String particleName, Location location,
                       float offsetX, float offsetY, float offsetZ, float speed, int count, int... data);

    Entity spawnHologram(Location location, String text);

    void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);
}