package fr.arnaud.isorogue.game.managers;

import fr.arnaud.isorogue.game.GameInstance;
import org.bukkit.block.Block;

public class PlayerInteractionManager {

    private final GameInstance gameInstance;

    public PlayerInteractionManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void handleLeftClick() {
        Block targetBlock = gameInstance.getBlockInteractionManager().findTargetedChest(3.0);

        if (targetBlock != null) {
            gameInstance.getBlockInteractionManager().openChest(targetBlock);
        } else {
            gameInstance.getCombatManager().performAttack();
        }
    }
}