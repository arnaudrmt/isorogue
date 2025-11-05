package fr.arnaud.isorogue.nms.v1_21_R1.enemies;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;
import java.util.List;

public class R21_CustomPigman extends ZombifiedPiglin {

    public R21_CustomPigman(EntityType entityType, Level level) {
        super(EntityType.ZOMBIFIED_PIGLIN, level);

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

        this.setBaby(false);
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
