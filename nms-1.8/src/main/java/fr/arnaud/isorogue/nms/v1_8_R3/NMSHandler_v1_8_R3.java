package fr.arnaud.isorogue.nms.v1_8_R3;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.entity.IPlayerNPC;
import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.game.GameManager;
import fr.arnaud.isorogue.game.enemy.MobType;
import fr.arnaud.isorogue.api.NMSHandler;
import fr.arnaud.isorogue.nms.v1_8_R3.entities.R8_PlayerHitbox;
import fr.arnaud.isorogue.nms.v1_8_R3.enemies.R8_CustomEnderman;
import fr.arnaud.isorogue.nms.v1_8_R3.enemies.R8_CustomPigman;
import fr.arnaud.isorogue.nms.v1_8_R3.enemies.R8_CustomZombie;
import fr.arnaud.isorogue.nms.v1_8_R3.entities.R8_PlayerNPC;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

public class NMSHandler_v1_8_R3 implements NMSHandler {

    private final IsoRogue plugin;
    private Field sneakField;

    public NMSHandler_v1_8_R3(IsoRogue plugin) {
        this.plugin = plugin;
        try {
            this.sneakField = PacketPlayInSteerVehicle.class.getDeclaredField("d");
            this.sneakField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "Reflection failed! Could not find sneak field in PacketPlayInSteerVehicle.", e);
        }
    }

    // Player Packets

    @Override
    public void injectPacketListener(Player player) {

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        if (pipeline.get("isorogue_packet_handler") != null) pipeline.remove("isorogue_packet_handler");

        ChannelDuplexHandler handler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                GameManager gameManager = plugin.getGameManager();
                GameInstance gameInstance = gameManager.getGameByPlayer(player);
                if (gameInstance == null) return;

                if (msg instanceof PacketPlayInSteerVehicle) {

                    PacketPlayInSteerVehicle packet = (PacketPlayInSteerVehicle) msg;
                    float forward = packet.a(); // Forward/Backward
                    float sideways = packet.b(); // Left/Right
                    boolean jump = packet.c();   // Spacebar
                    boolean sneak = packet.d();  // Shift (dismount)
                    gameInstance.getPlayerInput().update(forward, sideways, jump, sneak);

                    try {
                        if (sneakField != null) sneakField.setBoolean(packet, false);
                    } catch (IllegalAccessException e) {
                        plugin.getLogger().log(Level.WARNING, "Could not read steer packet.", e);
                    }
                }
                super.channelRead(ctx, msg);
            }
        };

        pipeline.addBefore("packet_handler", "isorogue_packet_handler", handler);
    }

    @Override
    public void uninjectPacketListener(Player player) {
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        if (connection != null && !connection.isDisconnected()) {
            if (connection.networkManager.channel.pipeline().get("isorogue_packet_handler") != null) {
                connection.networkManager.channel.pipeline().remove("isorogue_packet_handler");
            }
        }
    }

    @Override
    public void forcePlayerRotation(Player player, float yaw, float pitch) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> flags = EnumSet.of(
                PacketPlayOutPosition.EnumPlayerTeleportFlags.X,
                PacketPlayOutPosition.EnumPlayerTeleportFlags.Y,
                PacketPlayOutPosition.EnumPlayerTeleportFlags.Z
        );

        PacketPlayOutPosition rotationPacket = new PacketPlayOutPosition(0.0, 0.0, 0.0, yaw, pitch, flags);
        connection.sendPacket(rotationPacket);
    }

    // Player NPC Packets

    @Override
    public IPlayerNPC spawnPlayerNPC(Player player, Location location) {

        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();

        LivingEntity hitbox = spawnPlayerHitbox(location, nmsWorld);

        GameProfile npcProfile = new GameProfile(UUID.randomUUID(), player.getName());

        GameProfile playerProfile = ((CraftPlayer) player).getHandle().getProfile();
        Property skin = playerProfile.getProperties().get("textures").iterator().next();

        DataWatcher playerDataWatcher = ((CraftPlayer) player).getHandle().getDataWatcher();
        byte skinFlags = playerDataWatcher.getByte(10);

        npcProfile.getProperties().put("textures", skin);

        EntityPlayer playerNPC = new EntityPlayer(nmsServer, nmsWorld, npcProfile, new PlayerInteractManager(nmsWorld));

        DataWatcher npcDataWatcher = playerNPC.getDataWatcher();
        npcDataWatcher.watch(10, skinFlags);

        playerNPC.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                playerNPC));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(playerNPC));
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(playerNPC,
                (byte) ((location.getYaw() * 256.0F) / 360.0F)));

        Bukkit.getScheduler().runTaskLater(plugin, () ->
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                    playerNPC))
        , 5L);

        return new R8_PlayerNPC(playerNPC, npcProfile, hitbox);
    }

    public LivingEntity spawnPlayerHitbox(Location location, WorldServer nmsWorld) {

        R8_PlayerHitbox hitbox = new R8_PlayerHitbox(nmsWorld);

        hitbox.setSmall(false);
        hitbox.setInvisible(true);

        hitbox.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);
        nmsWorld.addEntity(hitbox, CreatureSpawnEvent.SpawnReason.CUSTOM);

        hitbox.getBukkitEntity().setMetadata("isorogue.player.hitbox",
                new FixedMetadataValue(plugin, true));
        return (LivingEntity) hitbox.getBukkitEntity();
    }

    @Override
    public void despawnPlayerNPC(Player viewer, IPlayerNPC npc) {
        if (viewer == null || npc == null) return;
        PlayerConnection connection = ((CraftPlayer) viewer).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getVisualEntityId()));
    }

    @Override
    public void setNPCEquipment(Player viewer, int entityId, int slot, org.bukkit.inventory.ItemStack item) {
        PlayerConnection connection = ((CraftPlayer) viewer).getHandle().playerConnection;

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityId, slot, nmsItem);
        connection.sendPacket(packet);
    }

    @Override
    public void moveEntity(IPlayerNPC npc, double x, double y, double z) {
        R8_PlayerNPC r3Npc = (R8_PlayerNPC) npc;
        EntityPlayer nmsNpc = r3Npc.getVisualNpc();
        nmsNpc.move(x, y, z);
    }

    @Override
    public void updateNPCHeadRotation(Player viewer, IPlayerNPC npc, float yaw, float pitch) {
        PlayerConnection connection = ((CraftPlayer) viewer).getHandle().playerConnection;
        R8_PlayerNPC r3Npc = (R8_PlayerNPC) npc;
        EntityPlayer nmsNpc = r3Npc.getVisualNpc();

        byte yawByte = (byte) ((yaw * 256.0F) / 360.0F);
        byte pitchByte = (byte) ((pitch * 256.0F) / 360.0F);

        PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket =
                new PacketPlayOutEntity.PacketPlayOutEntityLook(r3Npc.getVisualEntityId(), yawByte, pitchByte, nmsNpc.onGround);
        PacketPlayOutEntityHeadRotation headPacket = new PacketPlayOutEntityHeadRotation(nmsNpc, yawByte);

        connection.sendPacket(lookPacket);
        connection.sendPacket(headPacket);
    }

    @Override
    public void updateNPCSneak(Player viewer, IPlayerNPC npc, boolean isSneaking) {
        R8_PlayerNPC r3Npc = (R8_PlayerNPC) npc;
        updateEntityMetaData(viewer, r3Npc, 0x02, isSneaking);
    }

    @Override
    public void setNPCOnFire(Player viewer, IPlayerNPC npc, boolean onFire) {
        R8_PlayerNPC r3Npc = (R8_PlayerNPC) npc;
        updateEntityMetaData(viewer, r3Npc, 0x01, onFire);
    }

    private void updateEntityMetaData(Player viewer, R8_PlayerNPC npc, int data, boolean state) {
        EntityPlayer visualNpc = npc.getVisualNpc();
        DataWatcher dataWatcher = visualNpc.getDataWatcher();

        byte currentFlags = dataWatcher.getByte(0);


        if (state) dataWatcher.watch(0, (byte) (currentFlags | data));
        else dataWatcher.watch(0, (byte) (currentFlags & ~data));

        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(npc.getVisualEntityId(), dataWatcher,
                true);
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(metadataPacket);
    }

    // Custom Mobs Packets

    @Override
    public void registerCustomEntities() {
        registerEntity("Zombie", 54, R8_CustomZombie.class);
        registerEntity("PigZombie", 57, R8_CustomPigman.class);
        registerEntity("Enderman", 58, R8_CustomEnderman.class);
    }

    @Override
    public LivingEntity spawnCustomMob(Location location, MobType type) {
        World nmsWorld = ((CraftWorld) location.getWorld()).getHandle();

        Entity customMob;
        switch (type) {
            case HELLION:
                customMob = new R8_CustomPigman(nmsWorld);
                break;
            case STALKER:
                customMob = new R8_CustomEnderman(nmsWorld);
                break;
            case GROTESQUE:
            default:
                customMob = new R8_CustomZombie(nmsWorld);
                break;
        }

        if (customMob != null) {
            customMob.setLocation(location.getX(), location.getY(), location.getZ(),
                    location.getYaw(), location.getPitch());
            nmsWorld.addEntity(customMob, CreatureSpawnEvent.SpawnReason.CUSTOM);
            customMob.getBukkitEntity().setMetadata("isorogue.enemy",
                    new FixedMetadataValue(plugin, true));
            return (LivingEntity) customMob.getBukkitEntity();
        }

        return null;
    }

    @Override
    public void setEntityTarget(LivingEntity entity, LivingEntity target, EntityTargetEvent.TargetReason reason) {
        EntityCreature nmsEntity = (EntityCreature) ((CraftLivingEntity) entity).getHandle();

        if (nmsEntity == null) return;

        if (target == null || !target.isValid() || target.isDead() ||
                entity.equals(target) || !entity.getWorld().equals(target.getWorld())) {
            nmsEntity.setGoalTarget(null);
            return;
        }

        EntityLiving nmsTarget = ((CraftLivingEntity) target).getHandle();
        nmsEntity.setGoalTarget(nmsTarget, reason, true);
    }

    @Override
    public float getEntityHeadYaw(LivingEntity entity) {
        EntityLiving nmsEntity = ((CraftLivingEntity) entity).getHandle();
        return nmsEntity.aI;
    }

    @Override
    public void navigateEntityToLocation(LivingEntity entity, Location location, double speed) {
        EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
        NavigationAbstract navigator = nmsEntity.getNavigation();

        navigator.a(location.getX(), location.getY(), location.getZ(), speed);
    }

    // Entity Packets

    @Override
    public void teleportEntity(Player viewer, org.bukkit.entity.Entity entity, Location location) {
        if (entity == null) return;

        PlayerConnection connection = ((CraftPlayer) viewer).getHandle().playerConnection;

        Entity nmsEntity = ((CraftEntity) entity).getHandle();

        nmsEntity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(nmsEntity);
        connection.sendPacket(teleportPacket);
    }

    @Override
    public void playArmSwingAnimation(Player viewer, LivingEntity entity) {
        PlayerConnection connection = ((CraftPlayer) viewer).getHandle().playerConnection;
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation(((CraftLivingEntity) entity).getHandle(), 0);
        connection.sendPacket(packet);
    }

    @Override
    public void playHurtAnimation(Player viewer, LivingEntity entity) {
        PlayerConnection connection = ((CraftPlayer) viewer).getHandle().playerConnection;
        PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus(((CraftEntity) entity).getHandle(), (byte) 2);
        connection.sendPacket(packet);
    }

    // Blocks Packets

    @Override
    public void playChestAnimation(Player viewer, org.bukkit.block.Block chestBlock, boolean isOpen) {
        PlayerConnection connection = ((CraftPlayer) viewer).getHandle().playerConnection;
        CraftWorld craftWorld = (CraftWorld) chestBlock.getWorld();

        BlockPosition blockPos = new BlockPosition(chestBlock.getX(), chestBlock.getY(), chestBlock.getZ());

        TileEntityChest tileEntityChest = (TileEntityChest) craftWorld.getHandle().getTileEntity(blockPos);

        if (tileEntityChest != null) {
            PacketPlayOutBlockAction packet = new PacketPlayOutBlockAction(
                    blockPos,
                    tileEntityChest.w(), // Gets the NMS block type
                    1,                   // Action ID for chest animation
                    isOpen ? 1 : 0       // Action parameter: 1 for open, 0 for closed
            );
            connection.sendPacket(packet);
        }
    }

    @Override
    public void sendBlockChange(Player player, Location location, Material material, byte data) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        BlockPosition blockPos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        IBlockData blockData = CraftMagicNumbers.getBlock(material).fromLegacyData(data);

        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(((CraftWorld) location.getWorld()).getHandle(),
                blockPos);
        packet.block = blockData;

        connection.sendPacket(packet);
    }

    // NMS Utils Packets

    @Override
    public void spawnParticle(Player player, String particleName, Location location,
                              float offsetX, float offsetY, float offsetZ, float speed, int count, int... data) {
        EnumParticle particleType;
        try {
            particleType = EnumParticle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Could not find particle with name: " + particleName, e);
            return;
        }

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles((EnumParticle) particleType, true,
                (float) location.getX(), (float) location.getY(), (float) location.getZ(),
                offsetX, offsetY, offsetZ, speed, count, data);

        connection.sendPacket(packet);
    }

    @Override
    public org.bukkit.entity.Entity spawnHologram(Location location, String text) {
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        EntityArmorStand nmsHologram = new EntityArmorStand(craftWorld.getHandle());

        nmsHologram.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);
        nmsHologram.setCustomName(text);
        nmsHologram.setCustomNameVisible(true);
        nmsHologram.setInvisible(true);
        nmsHologram.setGravity(false);
        nmsHologram.setSmall(true);

        craftWorld.getHandle().addEntity(nmsHologram, CreatureSpawnEvent.SpawnReason.CUSTOM);
        nmsHologram.getBukkitEntity().setMetadata("isorogue-hologram",
                new FixedMetadataValue(plugin, true));
        return nmsHologram.getBukkitEntity();
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {

        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;

        IChatBaseComponent iChatBaseComponentTitle = IChatBaseComponent.
                ChatSerializer.a("{\"text\": \"" + title + "\"}");

        IChatBaseComponent iChatBaseComponentSubTitle = IChatBaseComponent.
                ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");

        PacketPlayOutTitle packetPlayOutTiming;
        if (stay != 0) {
            packetPlayOutTiming = new PacketPlayOutTitle(
                    PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut);
        } else {
            packetPlayOutTiming = new PacketPlayOutTitle(
                    PacketPlayOutTitle.EnumTitleAction.TIMES, null, 10, 100, 10);
        }
        playerConnection.sendPacket(packetPlayOutTiming);

        PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(
                PacketPlayOutTitle.EnumTitleAction.TITLE, iChatBaseComponentTitle);
        playerConnection.sendPacket(packetPlayOutTitle);

        if(subtitle != null) {
            PacketPlayOutTitle packetPlayOutSubTitle = new PacketPlayOutTitle(
                    PacketPlayOutTitle.EnumTitleAction.SUBTITLE, iChatBaseComponentSubTitle);
            playerConnection.sendPacket(packetPlayOutSubTitle);
        }
    }

    private void registerEntity(String name, int id, Class<? extends Entity> customClass) {
        try {

            Field fieldC = EntityTypes.class.getDeclaredField("c");
            fieldC.setAccessible(true);
            Map<String, Class<? extends Entity>> mapC = (Map<String, Class<? extends Entity>>) fieldC.get(null);

            Field fieldD = EntityTypes.class.getDeclaredField("d");
            fieldD.setAccessible(true);
            Map<Class<? extends Entity>, String> mapD = (Map<Class<? extends Entity>, String>) fieldD.get(null);

            Field fieldF = EntityTypes.class.getDeclaredField("f");
            fieldF.setAccessible(true);
            Map<Class<? extends Entity>, Integer> mapF = (Map<Class<? extends Entity>, Integer>) fieldF.get(null);

            Field fieldG = EntityTypes.class.getDeclaredField("g");
            fieldG.setAccessible(true);
            Map<String, Integer> mapG = (Map<String, Integer>) fieldG.get(null);

            mapC.put(name, customClass);
            mapD.put(customClass, name);
            mapF.put(customClass, id);
            mapG.put(name, id);

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Could not register entity with ID:" + id, e);
        }
    }
}