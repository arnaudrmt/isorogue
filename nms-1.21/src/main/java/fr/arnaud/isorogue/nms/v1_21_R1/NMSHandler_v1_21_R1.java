package fr.arnaud.isorogue.nms.v1_21_R1;

import com.mojang.authlib.GameProfile;
import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.entity.IPlayerNPC;
import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.game.GameManager;
import fr.arnaud.isorogue.game.enemy.MobType;
import fr.arnaud.isorogue.api.NMSHandler;
import fr.arnaud.isorogue.nms.v1_21_R1.entities.R21_PlayerHitbox;
import fr.arnaud.isorogue.nms.v1_21_R1.enemies.R21_CustomEnderman;
import fr.arnaud.isorogue.nms.v1_21_R1.enemies.R21_CustomPigman;
import fr.arnaud.isorogue.nms.v1_21_R1.enemies.R21_CustomZombie;
import fr.arnaud.isorogue.nms.v1_21_R1.entities.R21_PlayerNPC;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R1.util.CraftMagicNumbers;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

public class NMSHandler_v1_21_R1 implements NMSHandler {

    private final IsoRogue plugin;
    private Field sneakField;

    private EntityDataAccessor<Byte> sharedFlagsDataAccessor;

    private EntityType<Entity> customZombieType;
    private EntityType<Entity> customPigmanType;
    private EntityType<Entity> customEndermanType;

    public NMSHandler_v1_21_R1(IsoRogue plugin) {
        this.plugin = plugin;
        try {
            this.sneakField = ServerboundPlayerInputPacket.class.getDeclaredField("shiftKeyDown");
            this.sneakField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "Reflection failed! Could not find sneak field in PacketPlayInSteerVehicle.", e);
        }
    }

    // Player Packets

    @Override
    public void injectPacketListener(Player player) {

        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

        Connection networkConnection;
        try {
            Field connectionField = nmsPlayer.connection.getClass().getDeclaredField("connection");
            connectionField.setAccessible(true);
            networkConnection = (Connection) connectionField.get(nmsPlayer.connection);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        ChannelPipeline pipeline = networkConnection.channel.pipeline();

        if (pipeline.get("isorogue_packet_handler") != null) pipeline.remove("isorogue_packet_handler");

        ChannelDuplexHandler handler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                GameManager gameManager = plugin.getGameManager();
                GameInstance gameInstance = gameManager.getGameByPlayer(player);
                if (gameInstance == null) return;

                if (msg instanceof ServerboundPlayerInputPacket packet) {

                    float forward = packet.getZza();
                    float sideways = packet.getXxa();
                    boolean jump = packet.isJumping();
                    boolean sneak = packet.isShiftKeyDown();
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
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        Connection networkConnection;
        try {
            Field connectionField = serverPlayer.connection.getClass().getDeclaredField("connection");
            connectionField.setAccessible(true);
            networkConnection = (Connection) connectionField.get(serverPlayer.connection);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (serverPlayer.connection != null && networkConnection.channel.isOpen()) {
            if (networkConnection.channel.pipeline().get("isorogue_packet_handler") != null) {
                networkConnection.channel.pipeline().remove("isorogue_packet_handler");
            }
        }
    }

    @Override
    public void forcePlayerRotation(Player player, float yaw, float pitch) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

        nmsPlayer.connection.teleport(nmsPlayer.getX(), nmsPlayer.getY(), nmsPlayer.getZ(), yaw, pitch);
    }

    // Player NPC Packets

    @Override
    public IPlayerNPC spawnPlayerNPC(Player viewer, Location location) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();

        LivingEntity hitbox = spawnPlayerHitbox(location, nmsWorld);

        GameProfile npcProfile = new GameProfile(UUID.randomUUID(), viewer.getName());
        GameProfile playerProfile = ((CraftPlayer) viewer).getHandle().getGameProfile();
        playerProfile.getProperties().get("textures").forEach(prop ->
                npcProfile.getProperties().put(prop.name(), prop));

        ClientInformation info = new ClientInformation(
                viewer.getLocale(),
                viewer.getClientViewDistance(),
                ((CraftPlayer) viewer).getHandle().getChatVisibility(),
                ((CraftPlayer) viewer).getHandle().canChatInColor(),
                0,
                HumanoidArm.RIGHT,
                true, true
        );
        ServerPlayer npc = new ServerPlayer(nmsServer, nmsWorld, npcProfile, info);

        npc.setPos(location.getX(), location.getY(), location.getZ());
        npc.setYRot(location.getYaw());
        npc.setXRot(location.getPitch());

        ServerEntity serverEntity = new ServerEntity(nmsWorld, npc, 0, false, packet -> { }, Set.of());

        ServerGamePacketListenerImpl listener = ((CraftPlayer) viewer).getHandle().connection;

        listener.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc));

        listener.send(npc.getAddEntityPacket(serverEntity));

        listener.send(new ClientboundRotateHeadPacket(npc, (byte) (location.getYaw() * 256.0F / 360.0F)));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            listener.send(new ClientboundPlayerInfoRemovePacket(List.of(npc.getUUID())));
        }, 5L);

        return new R21_PlayerNPC(npc, npcProfile, hitbox);
    }

    public LivingEntity spawnPlayerHitbox(Location location, ServerLevel nmsLevel) {
        R21_PlayerHitbox hitbox = new R21_PlayerHitbox(nmsLevel);

        hitbox.setPos(location.getX(), location.getY(), location.getZ());
        nmsLevel.addFreshEntity(hitbox, CreatureSpawnEvent.SpawnReason.CUSTOM);

        hitbox.getBukkitEntity().setMetadata("isorogue.player.hitbox",
                new FixedMetadataValue(plugin, true));

        return (LivingEntity) hitbox.getBukkitEntity();
    }

    @Override
    public void despawnPlayerNPC(Player viewer, IPlayerNPC npc) {
        if (viewer == null || npc == null) return;
        ServerGamePacketListenerImpl connection = ((CraftPlayer) viewer).getHandle().connection;
        connection.send(new ClientboundRemoveEntitiesPacket(npc.getVisualEntityId()));
    }

    @Override
    public void setNPCEquipment(Player viewer, int entityId, int slot, ItemStack item) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) viewer).getHandle().connection;
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        EquipmentSlot nmsSlot;
        switch (slot) {
            case 1: nmsSlot = EquipmentSlot.FEET; break;
            case 2: nmsSlot = EquipmentSlot.LEGS; break;
            case 3: nmsSlot = EquipmentSlot.CHEST; break;
            case 4: nmsSlot = EquipmentSlot.HEAD; break;
            default: nmsSlot = EquipmentSlot.MAINHAND; break;
        }

        List<com.mojang.datafixers.util.Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> equipmentList = List.of(
                com.mojang.datafixers.util.Pair.of(nmsSlot, nmsItem)
        );
        connection.send(new ClientboundSetEquipmentPacket(entityId, equipmentList));
    }

    @Override
    public void moveEntity(IPlayerNPC npc, double x, double y, double z) {
        R21_PlayerNPC r3Npc = (R21_PlayerNPC) npc;
        ServerPlayer nmsNpc = ((CraftPlayer) r3Npc.getVisualNpc()).getHandle();

        double dx = x - nmsNpc.getX();
        double dy = y - nmsNpc.getY();
        double dz = z - nmsNpc.getZ();

        nmsNpc.move(MoverType.SELF, new Vec3(dx, dy, dz));
    }

    @Override
    public void updateNPCHeadRotation(Player viewer, IPlayerNPC npc, float yaw, float pitch) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) viewer).getHandle().connection;

        R21_PlayerNPC r3Npc = (R21_PlayerNPC) npc;
        ServerPlayer nmsNpc = ((CraftPlayer) r3Npc.getVisualNpc()).getHandle();

        byte yawByte = (byte) ((yaw * 256.0F) / 360.0F);
        byte pitchByte = (byte) ((pitch * 256.0F) / 360.0F);

        ClientboundMoveEntityPacket.Rot lookPacket =
                new ClientboundMoveEntityPacket.Rot(
                        nmsNpc.getId(),
                        yawByte,
                        pitchByte,
                        nmsNpc.onGround
                );

        ClientboundRotateHeadPacket headPacket = new ClientboundRotateHeadPacket(nmsNpc, yawByte);

        connection.sendPacket(lookPacket);
        connection.sendPacket(headPacket);
    }

    @Override
    public void updateNPCSneak(Player viewer, IPlayerNPC npc, boolean isSneaking) {
        R21_PlayerNPC r3Npc = (R21_PlayerNPC) npc;
        updateEntityMetaData(viewer, r3Npc, 0x02, isSneaking);
    }

    @Override
    public void setNPCOnFire(Player viewer, IPlayerNPC npc, boolean onFire) {
        R21_PlayerNPC r3Npc = (R21_PlayerNPC) npc;
        updateEntityMetaData(viewer, r3Npc, 0x01, onFire);
    }

    private void updateEntityMetaData(Player viewer, R21_PlayerNPC npc, int flag, boolean state) {
        if (this.sharedFlagsDataAccessor == null) {
            plugin.getLogger().severe("Cannot update entity flags: DataAccessor was not loaded correctly.");
            return;
        }

        ServerPlayer visualNpc = ((CraftPlayer) npc.getVisualNpc()).getHandle();
        SynchedEntityData entityData = visualNpc.getEntityData();

        byte currentFlags = entityData.get(this.sharedFlagsDataAccessor);
        byte newFlags;

        if (state) newFlags = (byte) (currentFlags | flag);
        else newFlags = (byte) (currentFlags & ~flag);

        entityData.set(this.sharedFlagsDataAccessor, newFlags);

        ClientboundSetEntityDataPacket metadataPacket = new ClientboundSetEntityDataPacket(npc.getVisualEntityId(),
                entityData.getNonDefaultValues());
        ((CraftPlayer) viewer).getHandle().connection.send(metadataPacket);
    }

    // Custom Mobs Packets

    @Override
    public void registerCustomEntities() {

        customZombieType = EntityType.Builder
                .of(R21_CustomZombie::new, MobCategory.MONSTER)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(8)
                .build("custom_zombie");
        Registry.register(BuiltInRegistries.ENTITY_TYPE,
                ResourceLocation.tryParse("yourplugin:custom_zombie"), customZombieType);

        customPigmanType = EntityType.Builder
                .of(R21_CustomPigman::new, MobCategory.MONSTER)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(8)
                .build("custom_pigman");
        Registry.register(BuiltInRegistries.ENTITY_TYPE,
                ResourceLocation.tryParse("yourplugin:custom_pigman"), customPigmanType);

        customEndermanType = EntityType.Builder
                .of(R21_CustomEnderman::new, MobCategory.MONSTER)
                .sized(0.6f, 2.9f)
                .clientTrackingRange(8)
                .build("custom_enderman");
        Registry.register(BuiltInRegistries.ENTITY_TYPE,
                ResourceLocation.tryParse("yourplugin:custom_enderman"), customEndermanType);
    }

    @Override
    public LivingEntity spawnCustomMob(Location location, MobType type) {
        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();

        Entity customMob;
        switch (type) {
            case HELLION:
                customMob = customPigmanType.create(nmsWorld);
                break;
            case STALKER:
                customMob = customEndermanType.create(nmsWorld);
                break;
            case GROTESQUE:
            default:
                customMob = customZombieType.create(nmsWorld);
                break;
        }

        if (customMob != null) {
            customMob.setPos(location.getX(), location.getY(), location.getZ());
            customMob.setYRot(location.getYaw());
            customMob.setXRot(location.getPitch());

            nmsWorld.addFreshEntity(customMob, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM);

            customMob.getBukkitEntity().setMetadata("isorogue.enemy",
                    new FixedMetadataValue(plugin, true));

            return (LivingEntity) customMob.getBukkitEntity();
        }

        return null;
    }

    @Override
    public void setEntityTarget(LivingEntity entity, LivingEntity target, EntityTargetEvent.TargetReason reason) {
        Mob nmsEntity = (Mob) ((CraftLivingEntity) entity).getHandle();

        if (nmsEntity == null) return;

        if (target == null || !target.isValid() || target.isDead() ||
                entity.equals(target) || !entity.getWorld().equals(target.getWorld())) {
            nmsEntity.setTarget(null);
            return;
        }

        net.minecraft.world.entity.LivingEntity nmsTarget = ((CraftLivingEntity) target).getHandle();
        nmsEntity.setTarget(nmsTarget, reason, true);
    }

    @Override
    public float getEntityHeadYaw(LivingEntity entity) {
        net.minecraft.world.entity.LivingEntity nmsEntity = ((CraftLivingEntity) entity).getHandle();
        return nmsEntity.getYHeadRot();
    }

    @Override
    public void navigateEntityToLocation(LivingEntity entity, Location location, double speed) {
        Mob nmsEntity = (Mob) ((CraftLivingEntity) entity).getHandle();
        PathNavigation navigator = nmsEntity.getNavigation();

        navigator.moveTo(location.getX(), location.getY(), location.getZ(), speed);
    }

    // Entity Packets

    @Override
    public void teleportEntity(Player viewer, org.bukkit.entity.Entity entity, Location location) {
        if (entity == null) return;

        ServerGamePacketListenerImpl connection = ((CraftPlayer) viewer).getHandle().connection;
        Entity nmsEntity = ((CraftEntity) entity).getHandle();

        nmsEntity.setPos(location.getX(), location.getY(), location.getZ());
        nmsEntity.setYRot(location.getYaw());
        nmsEntity.setXRot(location.getPitch());

        ClientboundTeleportEntityPacket teleportPacket = new ClientboundTeleportEntityPacket(nmsEntity);
        connection.send(teleportPacket);
    }

    @Override
    public void playArmSwingAnimation(Player viewer, LivingEntity entity) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) viewer).getHandle().connection;
        Entity nmsEntity = ((CraftEntity) entity).getHandle();

        ClientboundAnimatePacket packet = new ClientboundAnimatePacket(nmsEntity, 0);
        connection.send(packet);
    }

    @Override
    public void playHurtAnimation(Player viewer, LivingEntity entity) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) viewer).getHandle().connection;
        Entity nmsEntity = ((CraftEntity) entity).getHandle();

        ClientboundEntityEventPacket packet = new ClientboundEntityEventPacket(nmsEntity, (byte) 2);
        connection.send(packet);
    }

    // Blocks Packets

    @Override
    public void playChestAnimation(Player viewer, org.bukkit.block.Block chestBlock, boolean isOpen) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) viewer).getHandle().connection;
        CraftWorld craftWorld = (CraftWorld) chestBlock.getWorld();

        BlockPos blockPos = new BlockPos(chestBlock.getX(), chestBlock.getY(), chestBlock.getZ());

        ChestBlockEntity chestTile = (ChestBlockEntity) craftWorld.getHandle().getBlockEntity(blockPos);

        if (chestTile != null) {
            ClientboundBlockEventPacket packet = new ClientboundBlockEventPacket(
                    blockPos, chestTile.getBlockState().getBlock(), 1, isOpen ? 1 : 0 );
            connection.send(packet);
        }
    }

    @Override
    public void sendBlockChange(Player player, Location location, Material material, byte data) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;

        net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos(
                location.getBlockX(), location.getBlockY(), location.getBlockZ()
        );

        Block nmsBlock = CraftMagicNumbers.getBlock(material);
        BlockState blockState = nmsBlock.defaultBlockState();

        ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(
                ((CraftWorld) location.getWorld()).getHandle(), blockPos);

        connection.send(packet);
    }

    // NMS Utils Packets

    @Override
    public void spawnParticle(Player player, String particleName, Location location,
                              float offsetX, float offsetY, float offsetZ, float speed, int count, int... data) {
        ParticleOptions dust;
        try {
            dust = getParticleOptionsByName("REDSTONE", offsetX, offsetY, offsetZ, 1.0f);
            if (dust == null) {
                plugin.getLogger().warning("Could not find particle with name: " + particleName);
                return;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Could not parse particle: " + particleName, e);
            return;
        }

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;

        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(dust, true,
                location.getX(), location.getY(), location.getZ(),
                offsetX, offsetY, offsetZ, speed, count);

        connection.send(packet);
    }

    private ParticleOptions getParticleOptionsByName(String name, float r, float g, float b, float scale) {
        name = name.toUpperCase(Locale.ROOT);

        switch (name) {
            case "CLOUD":
                return ParticleTypes.CLOUD;
            case "REDSTONE":
                return new DustParticleOptions(new Vector3f(r, g, b), scale);
            case "ENCHANTMENT_TABLE":
                return ParticleTypes.ENCHANT;
            default:
                return ParticleTypes.ENCHANTED_HIT;
        }
    }

    @Override
    public org.bukkit.entity.Entity spawnHologram(Location location, String text) {
        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        ArmorStand nmsHologram = new ArmorStand(EntityType.ARMOR_STAND, nmsWorld);

        nmsHologram.setPos(location.getX(), location.getY(), location.getZ());
        nmsHologram.setYRot(0);
        nmsHologram.setXRot(0);

        nmsHologram.setCustomName(Component.literal(text));
        nmsHologram.setCustomNameVisible(true);
        nmsHologram.setInvisible(true);
        nmsHologram.setNoGravity(true);
        nmsHologram.setSmall(true);

        nmsWorld.addFreshEntity(nmsHologram, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM);

        nmsHologram.getBukkitEntity().setMetadata("isorogue-hologram",
                new FixedMetadataValue(plugin, true));

        return nmsHologram.getBukkitEntity();
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;

        Component titleComponent = Component.literal(title);
        Component subtitleComponent = subtitle != null ? Component.literal(subtitle) : null;

        ClientboundSetTitlesAnimationPacket timingPacket = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
        connection.send(timingPacket);

        ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(titleComponent);
        connection.send(titlePacket);

        if (subtitleComponent != null) {
            ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(subtitleComponent);
            connection.send(subtitlePacket);
        }
    }
}