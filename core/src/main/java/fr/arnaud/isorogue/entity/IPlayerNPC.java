package fr.arnaud.isorogue.entity;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface IPlayerNPC {

    LivingEntity getHitbox();

    LivingEntity getVisualEntity();

    int getVisualEntityId();

    default Location getVisualEntityLocation() {
        LivingEntity visualEntity = getVisualEntity();
        return new Location(visualEntity.getWorld(), visualEntity.getLocation().getX(),
                visualEntity.getLocation().getY(), visualEntity.getLocation().getZ());
    }
}
