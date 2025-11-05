package fr.arnaud.isorogue.player;

import fr.arnaud.isorogue.game.GameInstance;

public class PlayerController {

    private final GameInstance gameInstance;
    private final PlayerInput playerInput;

    public PlayerController(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        this.playerInput = new PlayerInput();
    }

    public PlayerInput getPlayerInput() {
        return playerInput;
    }
}