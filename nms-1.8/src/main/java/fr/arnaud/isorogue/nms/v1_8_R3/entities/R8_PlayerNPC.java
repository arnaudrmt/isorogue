package fr.arnaud.isorogue.nms.v1_8_R3.entities;

import com.mojang.authlib.GameProfile;
import fr.arnaud.isorogue.entity.IPlayerNPC;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.entity.LivingEntity;

public class R8_PlayerNPC implements IPlayerNPC {

    private final EntityPlayer visualNpc;
    private final GameProfile gameProfile;
    private final LivingEntity hitbox;

    public R8_PlayerNPC(EntityPlayer visualNpc, GameProfile gameProfile, LivingEntity hitbox) {
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

    public EntityPlayer getVisualNpc() {
        return visualNpc;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }
}