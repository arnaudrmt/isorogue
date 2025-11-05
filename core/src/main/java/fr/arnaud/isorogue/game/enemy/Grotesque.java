package fr.arnaud.isorogue.game.enemy;

import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

public class Grotesque extends AbstractEnemy {

    public Grotesque(LivingEntity bukkitEntity) {
        super(bukkitEntity, 10);
    }

    @Override public int getDamage() { return 4; }
    @Override public Sound getHurtSound() { return IsoRogue.getInstance().getWrapperApi().getSound(BukkitWrapperAPI.SoundType.ZOMBIE_HURT); }
    @Override public Sound getDeathSound() { return IsoRogue.getInstance().getWrapperApi().getSound(BukkitWrapperAPI.SoundType.ZOMBIE_DEATH); }
    @Override public Material getDeathParticleMaterial() { return Material.ROTTEN_FLESH; }
    @Override public double getVisionAngle() { return 45.0; }
    @Override public double getVisionRadius() { return 3.0; }
    @Override public double getLoseAggroRangeSquared() { return 5.0 * 5.0; }
    @Override public int getParticleColor() { return 0x4DA533; }
}