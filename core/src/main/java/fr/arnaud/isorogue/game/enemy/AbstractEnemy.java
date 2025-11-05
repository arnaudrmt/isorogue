package fr.arnaud.isorogue.game.enemy;

import fr.arnaud.isorogue.entity.IPlayerNPC;
import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.api.NMSHandler;
import fr.arnaud.isorogue.utils.ParticleUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.Material;

public abstract class AbstractEnemy implements IEnemy {

    protected final LivingEntity bukkitEntity;
    protected int health;
    protected AIState currentState = AIState.IDLE;

    public AbstractEnemy(LivingEntity bukkitEntity, int startHealth) {
        this.bukkitEntity = bukkitEntity;
        this.health = startHealth;
    }

    public abstract Sound getHurtSound();
    public abstract Sound getDeathSound();
    public abstract Material getDeathParticleMaterial();

    public abstract double getVisionAngle();
    public abstract double getVisionRadius();
    public abstract double getLoseAggroRangeSquared();
    public abstract int getParticleColor();

    @Override
    public void updateAI(GameInstance gameInstance) {
        if (isDead()) return;

        NMSHandler nmsHandler = gameInstance.getPlugin().getNMSHandler();
        IPlayerNPC playerNPC = gameInstance.getPlayerNPC();
        if (playerNPC == null) return;

        LivingEntity playerHitbox = playerNPC.getHitbox();
        Location playerLocation = playerHitbox.getLocation();
        Location enemyLocation = bukkitEntity.getLocation();
        double distanceSquared = enemyLocation.distanceSquared(playerLocation);

        switch (currentState) {
            case IDLE:
                ParticleUtils.drawVisionCone(nmsHandler, gameInstance.getPlayer(), this,
                        getVisionAngle(), getVisionRadius(), 10, 15, getParticleColor());

                if (distanceSquared < 3.0 * 3.0 && !gameInstance.getPlayerInput().isSneaking()) {
                    aggro(gameInstance, playerHitbox);
                    return;
                }

                Vector toPlayer = playerLocation.toVector().subtract(enemyLocation.toVector());
                if (toPlayer.lengthSquared() < getVisionRadius() * getVisionRadius()) {
                    float headYaw = nmsHandler.getEntityHeadYaw(bukkitEntity);
                    double yawRad = Math.toRadians(headYaw);
                    Vector enemyDirection = new Vector(-Math.sin(yawRad), 0, Math.cos(yawRad));
                    if (Math.toDegrees(enemyDirection.angle(toPlayer)) < getVisionAngle() / 2.0) {
                        aggro(gameInstance, playerHitbox);
                    }
                }
                break;

            case CHASING:
                nmsHandler.navigateEntityToLocation(bukkitEntity, playerLocation, 1.0D);
                if (distanceSquared < 2.0 * 2.0) {
                    currentState = AIState.ATTACKING;
                } else if (distanceSquared > getLoseAggroRangeSquared()) {
                    loseAggro(gameInstance);
                }
                break;

            case ATTACKING:
                nmsHandler.navigateEntityToLocation(bukkitEntity, enemyLocation, 0);
                if (distanceSquared > 2.0 * 2.0) {
                    currentState = AIState.CHASING;
                }
                break;
        }
    }

    protected void aggro(GameInstance gameInstance, LivingEntity targetHitbox) {
        currentState = AIState.CHASING;
        gameInstance.getPlugin().getNMSHandler().setEntityTarget(bukkitEntity, targetHitbox,
                EntityTargetEvent.TargetReason.CUSTOM);
    }

    protected void loseAggro(GameInstance gameInstance) {
        currentState = AIState.IDLE;
        gameInstance.getPlugin().getNMSHandler().setEntityTarget(bukkitEntity, null,
                EntityTargetEvent.TargetReason.CUSTOM);
    }

    @Override
    public void damage(int amount) {
        health -= amount;
        bukkitEntity.getWorld().playSound(bukkitEntity.getLocation(), getHurtSound(), 1.0f, 1.0f);
        if (isDead()) die();
    }

    @Override
    public void die() {
        bukkitEntity.getWorld().playSound(bukkitEntity.getLocation(), getDeathSound(), 1.0f, 1.0f);
        for (int i = 0; i < 10; i++) {
            Item item = bukkitEntity.getWorld().dropItem(bukkitEntity.getEyeLocation(), new ItemStack(getDeathParticleMaterial()));
            double x = (Math.random() - 0.5) * 0.5;
            double y = 0.2 + (Math.random() * 0.3);
            double z = (Math.random() - 0.5) * 0.5;
            item.setVelocity(new Vector(x, y, z));
            item.setPickupDelay(Integer.MAX_VALUE);
        }
        cleanup();
    }

    @Override public LivingEntity getBukkitEntity() { return this.bukkitEntity; }
    @Override public boolean isDead() { return this.health <= 0; }
    @Override public void cleanup() { this.bukkitEntity.remove(); }
}