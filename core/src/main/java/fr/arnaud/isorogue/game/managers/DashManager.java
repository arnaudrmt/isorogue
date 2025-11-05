package fr.arnaud.isorogue.game.managers;

import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import fr.arnaud.isorogue.game.DashMovementTask;
import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.game.enemy.IEnemy;
import fr.arnaud.isorogue.api.NMSHandler;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Comparator;

public class DashManager {

    private final GameInstance gameInstance;
    private BukkitTask activeDashTask = null;

    private int currentCooldown = 0;
    private static final int MAX_COOLDOWN = 100;

    public static final double DASH_RADIUS = 7.0;
    public static final double DASH_SPEED = 1.2;

    public DashManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void tick() {
        if (currentCooldown > 0) {
            currentCooldown--;
            Player player = gameInstance.getPlayer();
            float progress = (float) (MAX_COOLDOWN - currentCooldown) / MAX_COOLDOWN;
            player.setExp(progress);
            player.setLevel(currentCooldown / 20);
        }
    }

    public boolean isReady() {
        return currentCooldown <= 0;
    }

    public void performDash() {
        if (!isReady() || activeDashTask != null) return;

        NMSHandler nmsHandler = gameInstance.getPlugin().getNMSHandler();
        LivingEntity hitbox = gameInstance.getPlayerNPC().getHitbox();
        Location start = hitbox.getLocation();

        Vector direction;

        IEnemy closestEnemy = gameInstance.getLevelManager().getCurrentLevel().getEnemyManager().getEnemies().stream()
                .filter(enemy -> !enemy.isDead())
                .filter(enemy -> enemy.getBukkitEntity().getLocation().distanceSquared(start) < DASH_RADIUS * DASH_RADIUS)
                .min(Comparator.comparingDouble(enemy -> enemy.getBukkitEntity().getLocation().distanceSquared(start)))
                .orElse(null);

        if (closestEnemy != null) {
            direction = closestEnemy.getBukkitEntity().getLocation().toVector().subtract(start.toVector()).normalize();
        } else {
            Player player = gameInstance.getPlayer();
            float playerCameraYaw = player.getLocation().getYaw();
            double yawRad = Math.toRadians(playerCameraYaw);
            direction = new Vector(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
        }

        this.currentCooldown = MAX_COOLDOWN;
        hitbox.getWorld().playSound(start, gameInstance.getWrapper().getSound(BukkitWrapperAPI.SoundType.ENDERMAN_TELEPORT),
                1.0f, 1.5f);

        DashMovementTask task = new DashMovementTask(gameInstance, direction, this);
        this.activeDashTask = task.runTaskTimer(gameInstance.getPlugin(), 0L, 1L);
    }

    public void onDashComplete() {
        this.activeDashTask = null;
    }

    public void cleanup() {
        if (activeDashTask != null) {
            activeDashTask.cancel();
        }
        this.activeDashTask = null;
    }
}