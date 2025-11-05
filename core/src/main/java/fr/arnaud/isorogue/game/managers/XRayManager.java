package fr.arnaud.isorogue.game.managers;

import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import fr.arnaud.isorogue.entity.IPlayerNPC;
import fr.arnaud.isorogue.game.GameInstance;
import fr.arnaud.isorogue.api.NMSHandler;
import fr.arnaud.isorogue.utils.ParticleUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class XRayManager {

    private final GameInstance gameInstance;
    private final Set<Block> hiddenBlocks = new HashSet<>();

    private static final BlockFace[] AXES = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.UP,
            BlockFace.DOWN
    };

    public XRayManager(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void update() {
        NMSHandler nmsHandler = gameInstance.getPlugin().getNMSHandler();
        Player player = gameInstance.getPlayer();
        IPlayerNPC playerNPC = gameInstance.getPlayerNPC();
        ArmorStand cameraVehicle = gameInstance.getLevelManager().getCameraVehicle();

        if (playerNPC == null || cameraVehicle == null) return;

        Location cameraLoc = cameraVehicle.getLocation();
        Location playerLoc = playerNPC.getHitbox().getEyeLocation();

        Set<Block> blocksInLineOfSight = getBlocksInBeam(cameraLoc, playerLoc);
        Set<Block> blocksToRestore = new HashSet<>(hiddenBlocks);
        blocksToRestore.removeAll(blocksInLineOfSight);

        for (Block block : blocksToRestore) {
            nmsHandler.sendBlockChange(player, block.getLocation(), block.getType(), block.getData());
        }

        for (Block block : blocksInLineOfSight) {
            if (!hiddenBlocks.contains(block)) {
                nmsHandler.sendBlockChange(player, block.getLocation(), Material.AIR, (byte) 0);
            }
        }

        hiddenBlocks.clear();
        hiddenBlocks.addAll(blocksInLineOfSight);
        drawOutlines();
    }

    public void cleanup() {
        NMSHandler nmsHandler = gameInstance.getPlugin().getNMSHandler();
        Player player = gameInstance.getPlayer();
        for (Block block : hiddenBlocks) {
            nmsHandler.sendBlockChange(player, block.getLocation(), block.getType(), block.getData());
        }
        hiddenBlocks.clear();
    }

    private Set<Block> getBlocksInBeam(Location start, Location end) {
        BukkitWrapperAPI apiWrapper = gameInstance.getPlugin().getWrapperApi();

        Set<Block> blocks = new HashSet<>();
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double distance = start.distance(end);

        for (double d = 0; d < distance; d += 0.5) {
            Location checkCenter = start.clone().add(direction.clone().multiply(d));
            int radius = 1;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block block = checkCenter.clone().add(x, y, z).getBlock();
                        if (apiWrapper.isXRayOccluder(block)) {
                            blocks.add(block);
                        }
                    }
                }
            }
        }
        return blocks;
    }

    private void drawOutlines() {
        NMSHandler nmsHandler = gameInstance.getPlugin().getNMSHandler();
        Player player = gameInstance.getPlayer();
        for (Block block : hiddenBlocks) {
            for (BlockFace face : AXES) {
                if (!hiddenBlocks.contains(block.getRelative(face))) {
                    drawFaceEdges(nmsHandler, player, block, face);
                }
            }
        }
    }

    private void drawFaceEdges(NMSHandler nmsHandler, Player viewer, Block block, BlockFace face) {
        Location p0 = block.getLocation();
        Location p1 = p0.clone().add(1, 0, 0);
        Location p2 = p0.clone().add(0, 1, 0);
        Location p3 = p0.clone().add(0, 0, 1);
        Location p4 = p0.clone().add(1, 1, 0);
        Location p5 = p0.clone().add(1, 0, 1);
        Location p6 = p0.clone().add(0, 1, 1);
        Location p7 = p0.clone().add(1, 1, 1);

        switch (face) {
            case DOWN:
                ParticleUtils.drawLine(nmsHandler, viewer, p0, p1);
                ParticleUtils.drawLine(nmsHandler, viewer, p0, p3);
                ParticleUtils.drawLine(nmsHandler, viewer, p1, p5);
                ParticleUtils.drawLine(nmsHandler, viewer, p3, p5);
                break;
            case UP:
                ParticleUtils.drawLine(nmsHandler, viewer, p2, p4);
                ParticleUtils.drawLine(nmsHandler, viewer, p2, p6);
                ParticleUtils.drawLine(nmsHandler, viewer, p4, p7);
                ParticleUtils.drawLine(nmsHandler, viewer, p6, p7);
                break;
            case NORTH:
                ParticleUtils.drawLine(nmsHandler, viewer, p0, p2);
                ParticleUtils.drawLine(nmsHandler, viewer, p1, p4);
                ParticleUtils.drawLine(nmsHandler, viewer, p0, p1);
                ParticleUtils.drawLine(nmsHandler, viewer, p2, p4);
                break;
            case SOUTH:
                ParticleUtils.drawLine(nmsHandler, viewer, p3, p6);
                ParticleUtils.drawLine(nmsHandler, viewer, p5, p7);
                ParticleUtils.drawLine(nmsHandler, viewer, p3, p5);
                ParticleUtils.drawLine(nmsHandler, viewer, p6, p7);
                break;
            case WEST:
                ParticleUtils.drawLine(nmsHandler, viewer, p0, p3);
                ParticleUtils.drawLine(nmsHandler, viewer, p2, p6);
                ParticleUtils.drawLine(nmsHandler, viewer, p0, p2);
                ParticleUtils.drawLine(nmsHandler, viewer, p3, p6);
                break;
            case EAST:
                ParticleUtils.drawLine(nmsHandler, viewer, p1, p5);
                ParticleUtils.drawLine(nmsHandler, viewer, p4, p7);
                ParticleUtils.drawLine(nmsHandler, viewer, p1, p4);
                ParticleUtils.drawLine(nmsHandler, viewer, p5, p7);
                break;
        }
    }
}