package fr.arnaud.isorogue.game.enemy;

import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

public class Stalker extends AbstractEnemy {

    public Stalker(LivingEntity bukkitEntity) {
        super(bukkitEntity, 100);
    }

    @Override public int getDamage() { return 8; }
    @Override public Sound getHurtSound() { return IsoRogue.getInstance().getWrapperApi().getSound(BukkitWrapperAPI.SoundType.ENDERMAN_HURT); }
    @Override public Sound getDeathSound() { return IsoRogue.getInstance().getWrapperApi().getSound(BukkitWrapperAPI.SoundType.ENDERMAN_DEATH); }
    @Override public Material getDeathParticleMaterial() { return Material.ENDER_PEARL; }
    @Override public double getVisionAngle() { return 90.0; }
    @Override public double getVisionRadius() { return 4.0; }
    @Override public double getLoseAggroRangeSquared() { return 15.0 * 15.0; }
    @Override public int getParticleColor() { return 0x1a0130; }
}