package fr.arnaud.isorogue.game;

import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import fr.arnaud.isorogue.entity.IPlayerNPC;
import fr.arnaud.isorogue.game.item.IArmor;
import fr.arnaud.isorogue.game.item.IWeapon;
import fr.arnaud.isorogue.game.item.weapons.WoodenSword;
import fr.arnaud.isorogue.game.managers.*;
import fr.arnaud.isorogue.player.PlayerController;
import fr.arnaud.isorogue.player.PlayerInput;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class GameInstance {

    private final IsoRogue plugin;
    private final Player player;
    private BukkitTask gameLoopTask;

    private final PlayerController playerController;
    private final LevelManager levelManager;
    private final PlayerInteractionManager playerInteractionManager;
    private final BlockInteractionManager blockInteractionManager;
    private final PlayerMovementManager playerMovementManager;
    private final CameraManager cameraManager;
    private final CombatManager combatManager;
    private final DashManager dashManager;
    private final LootManager lootManager;
    private final XRayManager xRayManager;

    private double playerVirtualHealth;
    private IWeapon playerWeapon;
    private IArmor equippedArmor;
    private IPlayerNPC playerNPC;

    private GameMode originalGameMode;
    private boolean originalAllowFlight;

    private final BukkitWrapperAPI wrapper;

    public GameInstance(IsoRogue plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        this.playerController = new PlayerController(this);
        this.levelManager = new LevelManager(this);
        this.playerInteractionManager = new PlayerInteractionManager(this);
        this.blockInteractionManager = new BlockInteractionManager(this);
        this.playerMovementManager = new PlayerMovementManager(this);
        this.cameraManager = new CameraManager(this);
        this.combatManager = new CombatManager(this);
        this.dashManager = new DashManager(this);
        this.lootManager = new LootManager(this);
        this.xRayManager = new XRayManager(this);
        this.wrapper = plugin.getWrapperApi();
    }

    public void start() {
        plugin.getNMSHandler().sendTitle(player, "§6§lGOOD LUCK!", "§eThe adventure begins...",
                10, 40, 10);

        this.playerWeapon = new WoodenSword();
        this.equippedArmor = null;
        this.playerVirtualHealth = 20.0;

        saveAndPreparePlayerState();
        plugin.getNMSHandler().injectPacketListener(player);

        this.gameLoopTask = new GameLoop(this).runTaskTimer(plugin, 0L, 1L);

        levelManager.advanceToNextLevel();
    }

    public void stop() {
        if (gameLoopTask != null) gameLoopTask.cancel();

        plugin.getNMSHandler().uninjectPacketListener(player);

        dashManager.cleanup();
        levelManager.cleanup();
        xRayManager.cleanup();

        if (playerNPC != null) {
            playerNPC.getHitbox().remove();
            plugin.getNMSHandler().despawnPlayerNPC(player, playerNPC);
        }

        restorePlayerState();
    }

    public void damagePlayer(double baseDamage) {

        double totalReduction = (equippedArmor != null) ? equippedArmor.getDamageReduction() : 0.0;
        double finalDamage = baseDamage * (1.0 - totalReduction);
        playerVirtualHealth -= finalDamage;

        plugin.getNMSHandler().playHurtAnimation(player, playerNPC.getVisualEntity());

        if (playerVirtualHealth <= 0) {
            playerVirtualHealth = 0;
            plugin.getGameManager().stopGame(player);
        } else {
            player.setHealth(playerVirtualHealth);
        }
    }

    private void saveAndPreparePlayerState() {
        originalGameMode = player.getGameMode();
        originalAllowFlight = player.getAllowFlight();
        player.setHealth(player.getMaxHealth());
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    private void restorePlayerState() {
        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        player.setGameMode(originalGameMode);
        player.setAllowFlight(originalAllowFlight);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.setExp(0);
        player.setLevel(0);
    }

    public IsoRogue getPlugin() { return plugin; }
    public Player getPlayer() { return player; }
    public IWeapon getPlayerWeapon() { return playerWeapon; }
    public IArmor getEquippedArmor() { return equippedArmor; }
    public IPlayerNPC getPlayerNPC() { return playerNPC; }
    public PlayerInput getPlayerInput() { return playerController.getPlayerInput(); }

    public LevelManager getLevelManager() { return levelManager; }
    public PlayerMovementManager getPlayerMovementManager() { return playerMovementManager; }
    public BlockInteractionManager getBlockInteractionManager() { return blockInteractionManager; }
    public CameraManager getCameraManager() { return cameraManager; }
    public CombatManager getCombatManager() { return combatManager; }
    public DashManager getDashManager() { return dashManager; }
    public LootManager getLootManager() { return lootManager; }
    public PlayerInteractionManager getPlayerInteractionManager() { return playerInteractionManager; }
    public XRayManager getXRayManager() { return xRayManager; }
    public BukkitWrapperAPI getWrapper() { return wrapper; }

    public void setPlayerWeapon(IWeapon weapon) {
        this.playerWeapon = weapon;
        plugin.getNMSHandler().setNPCEquipment(player, playerNPC.getVisualEntityId(), 0, weapon.getItemStack());
    }
    public void equipArmor(IArmor armor) {
        this.equippedArmor = armor;
        plugin.getNMSHandler().setNPCEquipment(player, playerNPC.getVisualEntityId(), 3, armor.getItemStack());
    }
    public void setPlayerNPC(IPlayerNPC npc) { this.playerNPC = npc; }
}