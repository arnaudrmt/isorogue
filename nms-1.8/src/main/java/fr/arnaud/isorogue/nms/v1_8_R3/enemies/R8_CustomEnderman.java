package fr.arnaud.isorogue.nms.v1_8_R3.enemies;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;

import java.lang.reflect.Field;
import java.util.List;

public class R8_CustomEnderman extends EntityEnderman {

    public R8_CustomEnderman(World world) {
        super(world);

        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);

            ((List) bField.get(this.goalSelector)).clear();
            ((List) cField.get(this.goalSelector)).clear();
            ((List) bField.get(this.targetSelector)).clear();
            ((List) cField.get(this.targetSelector)).clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, 1.0D, false));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));

        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));

        this.persistent = true;
    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {}

    @Override
    public void teleportTo(Location exit, boolean portal) {}

    @Override
    public boolean co() {return false;}

    @Override
    public void t_() {
        super.t_();

        if (this.fireTicks > 0) {
            this.fireTicks = 0;
        }
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        return false;
    }
}
