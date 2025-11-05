package fr.arnaud.isorogue.game;

import fr.arnaud.isorogue.game.enemy.IEnemy;
import fr.arnaud.isorogue.game.managers.DashManager;
import fr.arnaud.isorogue.api.NMSHandler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DashMovementTask extends BukkitRunnable {

    private final GameInstance gameInstance;
    private final Vector direction;
    private final Set<IEnemy> hitEnemies = new HashSet<>();
    private int ticksLived = 0;

    private final DashManager dashManager;

    public DashMovementTask(GameInstance gameInstance, Vector direction, DashManager dashManager) {
        this.gameInstance = gameInstance;
        this.direction = direction;
        this.dashManager = dashManager;
    }

    @Override
    public void run() {

        NMSHandler nmsHandler = gameInstance.getPlugin().getNMSHandler();
        LivingEntity hitbox = gameInstance.getPlayerNPC().getHitbox();

        Vector moveThisTick = direction.clone().multiply(DashManager.DASH_SPEED);

        Location oldLocation = hitbox.getLocation();

        nmsHandler.moveEntity(gameInstance.getPlayerNPC(), moveThisTick.getX(), -0.1, moveThisTick.getZ());

        Location newLocation = hitbox.getLocation();

        if (ticksLived > 0 && oldLocation.distanceSquared(newLocation) < 0.1) {
            stopDash();
            return;
        }

        nmsHandler.spawnParticle(gameInstance.getPlayer(), "CLOUD", newLocation,
                0.1f, 0.1f, 0.1f, 1, 5);

        Collection<Entity> nearbyEntities = hitbox.getWorld().getNearbyEntities(hitbox.getLocation(), 3, 3, 3);

        for (Entity entity : nearbyEntities) {
            IEnemy targetEnemy = gameInstance.getLevelManager().getCurrentLevel()
                    .getEnemyManager().getEnemyByEntity(entity);

            if (targetEnemy != null && !targetEnemy.isDead() && !hitEnemies.contains(targetEnemy)) {
                hitEnemies.add(targetEnemy);

                int damage = 100;
                targetEnemy.damage(damage);
                nmsHandler.playHurtAnimation(gameInstance.getPlayer(), targetEnemy.getBukkitEntity());
            }
        }

        ticksLived++;
    }

    private void stopDash() {
        this.cancel();
        this.dashManager.onDashComplete();
    }
}