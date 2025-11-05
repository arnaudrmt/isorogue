package fr.arnaud.isorogue.game;

import org.bukkit.scheduler.BukkitRunnable;

public class GameLoop extends BukkitRunnable {

    private final GameInstance gameInstance;

    public GameLoop(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @Override
    public void run() {
        if (gameInstance.getPlayerNPC() == null) return;

        gameInstance.getCombatManager().tick();
        gameInstance.getDashManager().tick();
        gameInstance.getLootManager().checkForPickups();

        gameInstance.getLevelManager().updateCurrentLevel();
        gameInstance.getXRayManager().update();

        gameInstance.getPlayerMovementManager().update();
        gameInstance.getCameraManager().update();
    }
}
