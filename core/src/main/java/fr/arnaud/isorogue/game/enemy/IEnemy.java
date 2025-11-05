package fr.arnaud.isorogue.game.enemy;

import fr.arnaud.isorogue.game.GameInstance;
import org.bukkit.entity.LivingEntity;

public interface IEnemy {

    void updateAI(GameInstance gameInstance);

    LivingEntity getBukkitEntity();

    void damage(int amount);

    int getDamage();

    boolean isDead();

    void die();

    void cleanup();
}