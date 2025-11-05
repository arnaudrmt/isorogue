package fr.arnaud.isorogue.nms.v1_21_R1.entities;

import com.mojang.authlib.GameProfile;
import fr.arnaud.isorogue.entity.IPlayerNPC;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.LivingEntity;

public class R21_PlayerNPC implements IPlayerNPC {

    private final ServerPlayer visualNpc;
    private final GameProfile gameProfile;
    private final LivingEntity hitbox;

    public R21_PlayerNPC(ServerPlayer visualNpc, GameProfile gameProfile, LivingEntity hitbox) {
        this.visualNpc = visualNpc;
        this.gameProfile = gameProfile;
        this.hitbox = hitbox;
    }

    @Override
    public LivingEntity getHitbox() {
        return hitbox;
    }

    @Override
    public LivingEntity getVisualEntity() {
        return visualNpc.getBukkitEntity();
    }

    @Override
    public int getVisualEntityId() {
        return visualNpc.getId();
    }

    public LivingEntity getVisualNpc() {
        return visualNpc.getBukkitEntity();
    }
}