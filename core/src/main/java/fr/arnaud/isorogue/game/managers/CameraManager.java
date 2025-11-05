package fr.arnaud.isorogue.game.managers;

import fr.arnaud.isorogue.entity.IPlayerNPC;
import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.api.NMSHandler;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CameraManager {

    private final GameInstance gameInstance;

    private static final Vector CAMERA_OFFSET = new Vector(-3, 5, -3);
    private static final double FOLLOW_SPEED = 0.1;

    public CameraManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void update() {
        Player player = gameInstance.getPlayer();
        IPlayerNPC playerNPC = gameInstance.getPlayerNPC();
        NMSHandler nmsHandler = gameInstance.getPlugin().getNMSHandler();

        ArmorStand cameraVehicle = gameInstance.getLevelManager().getCameraVehicle();

        if (playerNPC == null || cameraVehicle == null) return;

        Location npcLocation = playerNPC.getVisualEntityLocation();
        Location cameraTarget = npcLocation.clone().add(CAMERA_OFFSET);
        Location currentCameraPos = cameraVehicle.getLocation();

        Vector direction = cameraTarget.toVector().subtract(currentCameraPos.toVector());

        if (direction.lengthSquared() > 0.01) {
            Location newCameraPos = currentCameraPos.add(direction.multiply(FOLLOW_SPEED));
            nmsHandler.teleportEntity(player, cameraVehicle, newCameraPos);
        }

        nmsHandler.forcePlayerRotation(player, -45, 55);
    }
}