package fr.arnaud.isorogue.game.listeners;

import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.game.GameManager;
import fr.arnaud.isorogue.game.enemy.IEnemy;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;

public class GameListener implements Listener {

    private final GameManager gameManager;

    public GameListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity damager = event.getDamager();

        if (!victim.hasMetadata("isorogue.player.hitbox") || !damager.hasMetadata("isorogue.enemy")) return;

        GameInstance gameInstance = gameManager.getGameByEntity(victim);
        if (gameInstance == null) return;

        for (IEnemy enemy : gameInstance.getLevelManager().getCurrentLevel().getEnemyManager().getEnemies()) {
            if(enemy.getBukkitEntity().getEntityId() == event.getDamager().getEntityId()) {
                event.setCancelled(true);
                gameInstance.damagePlayer(enemy.getDamage());
                gameInstance.getPlugin().getNMSHandler().playArmSwingAnimation(gameInstance.getPlayer(), enemy.getBukkitEntity());
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerClick(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }

        Player player = event.getPlayer();
        GameInstance gameInstance = gameManager.getGameByPlayer(player);
        if (gameInstance != null) {
            gameInstance.getPlayerInteractionManager().handleLeftClick();
        }
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getAction().toString().contains("RIGHT")) {
            return;
        }

        Player player = event.getPlayer();
        GameInstance gameInstance = gameManager.getGameByPlayer(player);
        if (gameInstance != null) {
            if (gameInstance.getPlayerInput().isSneaking()) {
                gameInstance.getDashManager().performDash();
            }
        }
    }

    @EventHandler
    public void onPlayerRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        GameInstance gameInstance = gameManager.getGameByPlayer(player);
        if (gameInstance != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getLocation().getWorld().getName().startsWith("isorogue-")) {
            if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockFlow(BlockFromToEvent event) {
        if (!event.getBlock().getWorld().getName().startsWith("isorogue-")) return;
        event.setCancelled(true);
    }
}
