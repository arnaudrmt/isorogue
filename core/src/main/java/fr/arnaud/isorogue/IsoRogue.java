package fr.arnaud.isorogue;

import fr.arnaud.isorogue.api.BukkitWrapperAPI;
import fr.arnaud.isorogue.command.IsoRogueCommand;
import fr.arnaud.isorogue.game.GameManager;
import fr.arnaud.isorogue.game.listeners.GameListener;
import fr.arnaud.isorogue.game.listeners.PlayerConnectionListener;
import fr.arnaud.isorogue.api.NMSHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class IsoRogue extends JavaPlugin {

    private static IsoRogue instance;

    private NMSHandler nmsHandler;
    private BukkitWrapperAPI wrapperAPI;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupImplementations()) {
            getLogger().severe("----------------------------------------------------");
            getLogger().severe("IsoRogue could not find a compatible implementation for this server version.");
            getLogger().severe("This version of IsoRogue is not compatible with your server.");
            getLogger().severe("Disabling plugin.");
            getLogger().severe("----------------------------------------------------");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        gameManager = new GameManager(this);
        nmsHandler.registerCustomEntities();

        getCommand("rogue").setExecutor(new IsoRogueCommand(gameManager));

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new GameListener(gameManager), this);

        getLogger().info("IsoRogue has been enabled with NMS support!");
    }

    public void onDisable() {
        getLogger().info("IsoRogue is disabling. Cleaning up active games...");

        if (gameManager != null) {
            gameManager.cleanupAllGames();
        }

        getLogger().info("Cleanup complete. IsoRogue has been disabled.");
    }

    private boolean setupImplementations() {
        String version;
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            getLogger().severe("Could not determine server version.");
            return false;
        }

        getLogger().info("Detected server version: " + version);

        try {
            String nmsHandlerClassName = "fr.arnaud.isorogue.nms." + version + ".NMSHandler_" + version;
            String apiWrapperClassName = "fr.arnaud.isorogue.api." + version + ".Wrapper_" + version;

            Class<?> nmsHandlerClass = Class.forName(nmsHandlerClassName);
            Class<?> apiWrapperClass = Class.forName(apiWrapperClassName);

            this.nmsHandler = (NMSHandler) nmsHandlerClass.getConstructor(IsoRogue.class).newInstance(this);
            this.wrapperAPI = (BukkitWrapperAPI) apiWrapperClass.getConstructor().newInstance();

            getLogger().info("Successfully loaded implementations for " + version);
            return true;

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not find implementations for " + version, e);
            return false;
        }
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public NMSHandler getNMSHandler() {
        return nmsHandler;
    }

    public BukkitWrapperAPI getWrapperApi() {
        return wrapperAPI;
    }

    public static IsoRogue getInstance() {
        return instance;
    }
}
