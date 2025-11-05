package fr.arnaud.isorogue.game.managers;

import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import fr.arnaud.isorogue.entity.IPlayerNPC;
import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.api.NMSHandler;
import fr.arnaud.isorogue.player.PlayerInput;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PlayerMovementManager {

    private final GameInstance gameInstance;

    private double yVelocity = 0;
    private boolean wasOnFireLastTick = false;
    private int lavaDamageCooldown = 0;
    private float lastKnownYaw = 0.0f;
    private long lastMessageTimestamp;
    private boolean lastSneakState = false;

    private static final double GRAVITY = -0.08;
    private static final double JUMP_FORCE = 0.42;
    private static final double WALK_SPEED = 0.22;
    private static final double SNEAK_SPEED = 0.08;

    public PlayerMovementManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void update() {
        Player player = gameInstance.getPlayer();
        IPlayerNPC playerNPC = gameInstance.getPlayerNPC();
        NMSHandler nmsHandler = gameInstance.getPlugin().getNMSHandler();

        if (playerNPC == null) return;

        PlayerInput input = gameInstance.getPlayerInput();
        LivingEntity hitbox = playerNPC.getHitbox();

        handleEnvironmentalEffects(hitbox, player, playerNPC, nmsHandler);

        boolean isSneaking = input.isSneaking();
        boolean onSoulSand = hitbox.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SOUL_SAND;
        double speed = isSneaking ? SNEAK_SPEED : WALK_SPEED;
        if (onSoulSand) speed *= 0.5;

        Vector moveVector = new Vector();
        moveVector.setX(input.getSideways());
        moveVector.setZ(-input.getForward());

        if (moveVector.lengthSquared() > 0) {
            moveVector.normalize().multiply(speed);
        }

        if (playerNPC.getVisualEntity().isOnGround()) {
            yVelocity = 0;
            if (input.isJumping() && !isSneaking) {
                yVelocity = JUMP_FORCE;
            }
        } else {
            yVelocity += GRAVITY;
        }

        nmsHandler.moveEntity(playerNPC, moveVector.getX(), yVelocity, moveVector.getZ());
        nmsHandler.teleportEntity(player, playerNPC.getVisualEntity(), playerNPC.getVisualEntity().getLocation());
        hitbox.teleport(playerNPC.getVisualEntity().getLocation());

        if (moveVector.lengthSquared() > 0) {
            lastKnownYaw = (float) Math.toDegrees(Math.atan2(-moveVector.getX(), moveVector.getZ()));
        }
        nmsHandler.updateNPCHeadRotation(player, playerNPC, lastKnownYaw, 0);

        if (isSneaking != lastSneakState) {
            nmsHandler.updateNPCSneak(player, playerNPC, isSneaking);
            lastSneakState = isSneaking;
        }

        checkLevelEnd(hitbox, player);
    }

    private void handleEnvironmentalEffects(LivingEntity hitbox, Player player, IPlayerNPC npc, NMSHandler nmsHandler) {
        Block blockIn = hitbox.getLocation().getBlock();
        if (lavaDamageCooldown > 0) lavaDamageCooldown--;

        boolean inLava = IsoRogue.getInstance().getWrapperApi().isLava(blockIn.getType());

        if (inLava) {
            if (!wasOnFireLastTick) {
                nmsHandler.setNPCOnFire(player, npc, true);
                wasOnFireLastTick = true;
            }
            if (lavaDamageCooldown <= 0) {
                gameInstance.damagePlayer(1.0);
                hitbox.getWorld().playSound(hitbox.getLocation(), gameInstance.getWrapper().getSound(BukkitWrapperAPI.SoundType.FIZZ),
                        0.5f, 1.0f);
                lavaDamageCooldown = 20;
            }
        } else {
            if (wasOnFireLastTick) {
                nmsHandler.setNPCOnFire(player, npc, false);
                wasOnFireLastTick = false;
            }
        }
    }

    private void checkLevelEnd(LivingEntity hitbox, Player player) {
        if (gameInstance.getLevelManager().isTransitioning()) return;

        if (hitbox.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.EMERALD_BLOCK) {
            int leftToKill = gameInstance.getLevelManager().getCurrentLevel().getEnemyManager().getEnemies().size();
            if (leftToKill > 0) {
                if ((System.currentTimeMillis() - lastMessageTimestamp) >= 5000) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "NOT YET! " + ChatColor.YELLOW +
                            "You must defeat " + leftToKill + " more enemies!");
                    lastMessageTimestamp = System.currentTimeMillis();
                }
            } else {
                gameInstance.getLevelManager().advanceToNextLevel();
            }
        }
    }

    public float getLastKnownYaw() {
        return lastKnownYaw;
    }
}