package fr.arnaud.isorogue.game.enemy;

import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.game.Level;
import fr.arnaud.isorogue.api.NMSHandler;
import fr.arnaud.isorogue.world.LevelTheme;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyManager {

    private final GameInstance gameInstance;
    private final List<IEnemy> enemies = new ArrayList<>();
    private final Random random = new Random();

    public EnemyManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void spawnEnemies() {
        int levelNumber = gameInstance.getLevelManager().getLevelNumber();
        int enemyCount = (int) Math.pow(2, levelNumber);
        LevelTheme theme = gameInstance.getLevelManager().getCurrentLevel().getTheme();

        for (int i = 0; i < enemyCount; i++) {
            Location spawnLocation = findValidSpawnLocation(theme);
            if (spawnLocation == null) continue;

            NMSHandler nmsHandler = gameInstance.getPlugin().getNMSHandler();
            LivingEntity spawnedEntity;
            IEnemy newEnemy;

            switch (theme) {
                case NETHER:
                    spawnedEntity = nmsHandler.spawnCustomMob(spawnLocation, MobType.HELLION);
                    newEnemy = new Hellion(spawnedEntity);
                    break;
                case END:
                    spawnedEntity = nmsHandler.spawnCustomMob(spawnLocation, MobType.STALKER);
                    newEnemy = new Stalker(spawnedEntity);
                    break;
                case OVERWORLD:
                default:
                    spawnedEntity = nmsHandler.spawnCustomMob(spawnLocation, MobType.GROTESQUE);
                    newEnemy = new Grotesque(spawnedEntity);
                    break;
            }
            if (newEnemy != null) {
                this.enemies.add(newEnemy);
            }
        }
    }

    private Location findValidSpawnLocation(LevelTheme theme) {
        Level currentLevel = gameInstance.getLevelManager().getCurrentLevel();
        if (currentLevel == null) return null;
        World world = currentLevel.getWorld();

        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(13) + 1;
            int z = random.nextInt(38) + 1;

            int y = world.getHighestBlockYAt(x, z);

            Block groundBlock = world.getBlockAt(x, y - 1, z);

            if (groundBlock.getType() == theme.getTopBlock()) {
                return new Location(world, x + 0.5, y, z + 0.5);
            }
        }

        System.err.println("Could not find a valid spawn location for an enemy after 50 attempts.");
        return null;
    }

    public void updateAllEnemies() {
        enemies.removeIf(IEnemy::isDead);
        for (IEnemy enemy : enemies) {
            enemy.updateAI(gameInstance);
        }
    }

    public List<IEnemy> getEnemies() {
        return enemies;
    }

    public IEnemy getEnemyByEntity(Entity entity) {
        for (IEnemy enemy : enemies) {
            if (enemy.getBukkitEntity().getEntityId() == entity.getEntityId()) {
                return enemy;
            }
        }
        return null;
    }

    public void cleanup() {
        for (IEnemy enemy : enemies) {
            enemy.cleanup();
        }
        enemies.clear();
    }
}
