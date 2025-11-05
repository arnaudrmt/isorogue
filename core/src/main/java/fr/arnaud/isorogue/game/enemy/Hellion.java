package fr.arnaud.isorogue.game.enemy;

import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

public class Hellion extends AbstractEnemy {

    public Hellion(LivingEntity bukkitEntity) {
        super(bukkitEntity, 50);
    }

    @Override public int getDamage() { return 6; }
    @Override public Sound getHurtSound() { return IsoRogue.getInstance().getWrapperApi().getSound(BukkitWrapperAPI.SoundType.PIGMAN_HURT); }
    @Override public Sound getDeathSound() { return IsoRogue.getInstance().getWrapperApi().getSound(BukkitWrapperAPI.SoundType.PIGMAN_DEATH); }
    @Override public Material getDeathParticleMaterial() { return IsoRogue.getInstance().getWrapperApi().getMaterial(BukkitWrapperAPI.MaterialType.PORKCHOP); }
    @Override public double getVisionAngle() { return 60.0; }
    @Override public double getVisionRadius() { return 3.5; }
    @Override public double getLoseAggroRangeSquared() { return 10.0 * 10.0; }
    @Override public int getParticleColor() { return 0xA62530; }
}