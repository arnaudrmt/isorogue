package fr.arnaud.isorogue.nms.v1_21_R1.enemies;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class R21_CustomEnderman extends EnderMan {

    public R21_CustomEnderman(EntityType entityType, Level level) {
        super(EntityType.ENDERMAN, level);

        try {
            Field bField = GoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = GoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);

            ((List) bField.get(this.goalSelector)).clear();
            ((List) cField.get(this.goalSelector)).clear();
            ((List) bField.get(this.targetSelector)).clear();
            ((List) cField.get(this.targetSelector)).clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public boolean teleportTo(ServerLevel worldserver, double d0, double d1, double d2, Set<RelativeMovement> set, float f, float f1, PlayerTeleportEvent.TeleportCause cause) {
        return false;
    }

    @Override
    public boolean teleportTo(ServerLevel worldserver, double d0, double d1, double d2, Set<RelativeMovement> set, float f, float f1) {
        return false;
    }

    @Override
    public boolean randomTeleport(double d0, double d1, double d2, boolean flag) {
        return false;
    }

    @Override
    public boolean teleport() {
        return false;
    }

    @Override
    public Optional<Boolean> randomTeleport(double d0, double d1, double d2, boolean flag, PlayerTeleportEvent.TeleportCause cause) {
        return Optional.empty();
    }

    @Override
    public void teleportTo(double d0, double d1, double d2) {
        return;
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getRemainingFireTicks() > 0) {
            this.setRemainingFireTicks(0);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
