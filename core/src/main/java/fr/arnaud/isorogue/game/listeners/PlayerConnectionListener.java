package fr.arnaud.isorogue.game.listeners;

import fr.arnaud.isorogue.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final GameManager gameManager;

    public PlayerConnectionListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (gameManager.getGameByPlayer(player.getPlayer()) != null) {
            gameManager.stopGame(player);
        }
    }
}
