package fr.arnaud.isorogue.game.managers;

import fr.arnaud.isorogue.entity.IPlayerNPC;
import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.game.enemy.IEnemy;
import fr.arnaud.isorogue.game.item.IWeapon;
import fr.arnaud.isorogue.api.NMSHandler;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class CombatManager {

    private final GameInstance gameInstance;
    private int attackCooldown = 0;
    private final int maxAttackCooldown = 10;
    private int leftToKill = 0;

    public CombatManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void tick() {
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }

    public void performAttack() {
        if (attackCooldown > 0) return;

        NMSHandler nmsHandler = gameInstance.getPlugin().getNMSHandler();
        IPlayerNPC playerNPC = gameInstance.getPlayerNPC();

        nmsHandler.playArmSwingAnimation(gameInstance.getPlayer(), playerNPC.getVisualEntity());
        attackCooldown = maxAttackCooldown;

        Location npcLocation = playerNPC.getVisualEntityLocation();
        Vector npcDirection = npcLocation.getDirection().setY(0).normalize();
        double attackRangeSquared = 3.0 * 3.0;

        for (IEnemy enemy : gameInstance.getLevelManager().getCurrentLevel().getEnemyManager().getEnemies()) {
            if (enemy.isDead()) continue;

            LivingEntity enemyEntity = enemy.getBukkitEntity();
            Location enemyLocation = enemyEntity.getLocation();

            boolean hit = false;

            double dx = Math.abs(npcLocation.getX() - enemyLocation.getX());
            double dz = Math.abs(npcLocation.getZ() - enemyLocation.getZ());
            if (dx < 0.75 && dz < 0.75 && Math.abs(npcLocation.getY() - enemyLocation.getY()) < 2) {
                hit = true;
            }

            if (!hit && npcLocation.distanceSquared(enemyLocation) < attackRangeSquared) {
                Vector toEnemy = enemyLocation.toVector().subtract(npcLocation.toVector()).setY(0).normalize();

                double dot = toEnemy.dot(npcDirection);

                if (dot > 0) {
                    hit = true;
                }
            }

            if (hit) {
                IWeapon currentWeapon = gameInstance.getPlayerWeapon();
                int damage = (currentWeapon != null) ? currentWeapon.getDamage() : 1;
                enemy.damage(damage);
                nmsHandler.playHurtAnimation(gameInstance.getPlayer(), enemyEntity);
                break;
            }
        }
    }

    public int getLeftToKill() { return leftToKill; }
    public void setLeftToKill(int count) { this.leftToKill = count; }
}