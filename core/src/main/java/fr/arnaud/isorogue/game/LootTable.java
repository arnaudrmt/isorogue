package fr.arnaud.isorogue.game;

import fr.arnaud.isorogue.game.item.IItem;
import fr.arnaud.isorogue.game.item.armors.*;
import fr.arnaud.isorogue.game.item.weapons.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LootTable {

    private final Random random = new Random();
    private final List<IItem> possibleLoot = Arrays.asList(
            new StoneSword(), new IronSword(), new GoldenSword(), new DiamondSword(),
            new LeatherChestplate(), new ChainmailChestplate(), new IronChestplate(), new GoldenChestplate(), new DiamondChestplate()
    );

    public IItem getRandomLoot() {
        return possibleLoot.get(random.nextInt(possibleLoot.size()));
    }
}