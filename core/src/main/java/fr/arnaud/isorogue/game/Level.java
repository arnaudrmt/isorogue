package fr.arnaud.isorogue.game;

import fr.arnaud.isorogue.game.enemy.EnemyManager;
import fr.arnaud.isorogue.world.LevelTheme;
import org.bukkit.World;

public class Level {

    private final GameInstance gameInstance;
    private final World world;
    private final LevelTheme theme;
    private final EnemyManager enemyManager;

    public Level(GameInstance gameInstance, World world, LevelTheme theme) {
        this.gameInstance = gameInstance;
        this.world = world;
        this.theme = theme;
        this.enemyManager = new EnemyManager(gameInstance);
    }

    public void initialize() {

        enemyManager.spawnEnemies();

        int enemiesToKill = enemyManager.getEnemies().size();
        gameInstance.getCombatManager().setLeftToKill(enemiesToKill);
    }

    public void cleanup() {
        enemyManager.cleanup();
    }

    public World getWorld() {
        return world;
    }
    public LevelTheme getTheme() {
        return theme;
    }
    public EnemyManager getEnemyManager() {
        return enemyManager;
    }
}