package fr.arnaud.isorogue.nms.v1_21_R1.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;

public class R21_PlayerHitbox extends ArmorStand {

    public R21_PlayerHitbox(Level world) {
        super(EntityType.ARMOR_STAND, world);

        this.setInvisible(true);
        this.setSmall(false);
        this.setNoGravity(true);
    }

    @Override
    public void setRemainingFireTicks(int i) {
        super.setRemainingFireTicks(0);
    }
}
