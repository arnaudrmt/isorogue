package fr.arnaud.isorogue.game;

import fr.arnaud.isorogue.IsoRogue;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private final IsoRogue plugin;
    private final Map<UUID, GameInstance> activeGames = new HashMap<>();

    public GameManager(IsoRogue plugin) {
        this.plugin = plugin;
    }

    public void startNewGame(Player player) {
        if (activeGames.containsKey(player.getUniqueId())) return;

        GameInstance gameInstance = new GameInstance(plugin, player);
        activeGames.put(player.getUniqueId(), gameInstance);
        gameInstance.start();
    }

    public void stopGame(Player player) {
        GameInstance gameInstance = activeGames.remove(player.getUniqueId());
        if (gameInstance != null) {
            gameInstance.stop();
        }
    }

    public void cleanupAllGames() {
        for (GameInstance instance : new ArrayList<>(activeGames.values())) {
            instance.stop();
        }
        activeGames.clear();
    }

    public GameInstance getGameByPlayer(Player player) {
        return activeGames.get(player.getUniqueId());
    }

    public GameInstance getGameByEntity(Entity entity) {
        for (GameInstance instance : activeGames.values()) {
            if (instance.getPlayerNPC() != null &&
                    instance.getPlayerNPC().getHitbox().getEntityId() == entity.getEntityId()) {
                return instance;
            }
        }
        return null;
    }

    public Map<UUID, GameInstance> getActiveGames() {
        return activeGames;
    }
}