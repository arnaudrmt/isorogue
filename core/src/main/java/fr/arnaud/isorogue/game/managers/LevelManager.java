package fr.arnaud.isorogue.game.managers;

import fr.arnaud.isorogue.entity.IPlayerNPC;
import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.game.Level;
import fr.arnaud.isorogue.world.LevelTheme;
import fr.arnaud.isorogue.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LevelManager {

    private final GameInstance gameInstance;
    private final WorldManager worldManager;
    private Level currentLevel;
    private int levelNumber = 0;
    private boolean isTransitioning = false;
    private ArmorStand cameraVehicle;

    public LevelManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
        this.worldManager = new WorldManager();
    }

    public void advanceToNextLevel() {
        if (isTransitioning) return;
        isTransitioning = true;
        levelNumber++;

        Player player = gameInstance.getPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 10,
                false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0,
                false, false));

        gameInstance.getXRayManager().cleanup();

        if (currentLevel != null) currentLevel.cleanup();
        if (cameraVehicle != null) cameraVehicle.remove();
        worldManager.deleteWorld(currentLevel != null ? currentLevel.getWorld() : null);

        LevelTheme theme = (levelNumber == 2) ? LevelTheme.NETHER : (levelNumber == 3) ? LevelTheme.END : LevelTheme.OVERWORLD;

        String worldName = "isorogue-" + player.getName() + "-level-" + levelNumber + "-" + System.currentTimeMillis();
        World newWorld = worldManager.createGameWorld(worldName, theme);
        this.currentLevel = new Level(gameInstance, newWorld, theme);

        Location safeSpawnPoint = new Location(newWorld, 1.5, newWorld.getHighestBlockYAt(1, 1), 1.5);
        Location initialCameraPos = safeSpawnPoint.clone().add(-3, 5, -3);
        initialCameraPos.setYaw(-45);
        initialCameraPos.setPitch(55);

        player.teleport(initialCameraPos.add(-10, 4, -10));

        Bukkit.getScheduler().runTaskLater(gameInstance.getPlugin(), () -> {
            player.removePotionEffect(PotionEffectType.BLINDNESS);

            cameraVehicle = newWorld.spawn(initialCameraPos, ArmorStand.class);
            cameraVehicle.setVisible(false);
            cameraVehicle.setGravity(false);
            cameraVehicle.setMarker(true);
            cameraVehicle.setPassenger(player);

            IPlayerNPC npc = gameInstance.getPlugin().getNMSHandler().spawnPlayerNPC(player, safeSpawnPoint);
            gameInstance.setPlayerNPC(npc);

            gameInstance.getPlugin().getNMSHandler().setNPCEquipment(player, npc.getVisualEntityId(), 0,
                    gameInstance.getPlayerWeapon().getItemStack());

            if (gameInstance.getEquippedArmor() != null) {
                gameInstance.getPlugin().getNMSHandler().setNPCEquipment(player, npc.getVisualEntityId(), 3,
                        gameInstance.getEquippedArmor().getItemStack());
            }

            currentLevel.initialize();

            isTransitioning = false;
        }, 40L);
    }

    public void updateCurrentLevel() {
        if (currentLevel != null && !isTransitioning) {
            currentLevel.getEnemyManager().updateAllEnemies();
        }
    }

    public void cleanup() {
        if (currentLevel != null) {
            currentLevel.cleanup();
            worldManager.deleteWorld(currentLevel.getWorld());
        }
        if (cameraVehicle != null) cameraVehicle.remove();
    }

    public Level getCurrentLevel() { return currentLevel; }
    public boolean isTransitioning() { return isTransitioning; }
    public ArmorStand getCameraVehicle() { return cameraVehicle; }
    public int getLevelNumber() { return levelNumber; }
}