package fr.arnaud.isorogue.utils;

import fr.arnaud.isorogue.game.enemy.IEnemy;
import fr.arnaud.isorogue.api.NMSHandler;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ParticleUtils {

    public static void drawVisionCone(NMSHandler nmsHandler, Player viewer, IEnemy enemy,
                                      double visionAngle, double visionRadius, int lineParticleCount, int arcParticleCount,
                                      int hexColor) {
        LivingEntity bukkitEntity = enemy.getBukkitEntity();
        Location feetLocation = bukkitEntity.getLocation();

        float headYaw = nmsHandler.getEntityHeadYaw(bukkitEntity);

        double yawRad = Math.toRadians(headYaw);
        Vector direction = new Vector(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();

        double angleRad = Math.toRadians(visionAngle / 2.0);
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);

        double negSin = Math.sin(-angleRad);
        double negCos = Math.cos(-angleRad);

        Vector rightEdge = new Vector(direction.getX() * cos - direction.getZ() * sin, 0,
                direction.getX() * sin + direction.getZ() * cos).normalize();
        Vector leftEdge = new Vector(direction.getX() * negCos - direction.getZ() * negSin, 0,
                direction.getX() * negSin + direction.getZ() * negCos).normalize();

        String particleName = "REDSTONE";
        float r = ((hexColor >> 16) & 0xFF) / 255f;
        float g = ((hexColor >> 8) & 0xFF) / 255f;
        float b = (hexColor & 0xFF) / 255f;

        for (int i = 1; i <= lineParticleCount; i++) {
            double distance = ((double) i / lineParticleCount) * visionRadius;

            Location leftPoint = feetLocation.clone().add(leftEdge.clone().multiply(distance));
            Location rightPoint = feetLocation.clone().add(rightEdge.clone().multiply(distance));

            nmsHandler.spawnParticle(viewer, particleName, leftPoint, r, g, b, 1, 0);
            nmsHandler.spawnParticle(viewer, particleName, rightPoint, r, g, b, 1, 0);
        }

        for (int i = 0; i <= arcParticleCount; i++) {
            double t = (double) i / arcParticleCount;

            double dot = leftEdge.dot(rightEdge);
            if (dot > 1.0) dot = 1.0;
            if (dot < -1.0) dot = -1.0;

            double theta = Math.acos(dot) * t;

            Vector relativeVec = rightEdge.clone().subtract(leftEdge.clone().multiply(dot)).normalize();
            Vector interpolatedVec = leftEdge.clone().multiply(Math.cos(theta)).add(relativeVec.multiply(Math.sin(theta)));

            Location arcPoint = feetLocation.clone().add(interpolatedVec.multiply(visionRadius));

            nmsHandler.spawnParticle(viewer, particleName, arcPoint, r, g, b, 1, 0);
        }
    }

    public static void drawLine(NMSHandler nmsHandler, Player viewer, Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        if (length < 0.01) return;

        double step = 0.5;
        for (double d = 0; d < length; d += step) {
            Location point = start.clone().add(direction.clone().multiply(d));

            nmsHandler.spawnParticle(viewer, "ENCHANTMENT_TABLE", point,
                    0, 0, 0, 0, 1);
        }
    }
}