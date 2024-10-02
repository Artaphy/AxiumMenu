package me.artaphy.axiumMenu;

import me.artaphy.axiumMenu.commands.MainCommand;
import me.artaphy.axiumMenu.config.ConfigManager;
import me.artaphy.axiumMenu.events.MenuReloadEvent;
import me.artaphy.axiumMenu.exceptions.MenuLoadException;
import me.artaphy.axiumMenu.exceptions.MenuNotFoundException;
import me.artaphy.axiumMenu.listeners.MenuListener;
import me.artaphy.axiumMenu.menu.Menu;
import me.artaphy.axiumMenu.menu.MenuInventoryHolder;
import me.artaphy.axiumMenu.menu.MenuManager;
import me.artaphy.axiumMenu.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.PlaceholderAPI;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

/**
 * Main class for the AxiumMenu plugin.
 * This class handles the initialization, enabling, and disabling of the plugin.
 */
public final class AxiumMenu extends JavaPlugin {

    private ConfigManager configManager;
    private MenuManager menuManager;
    private WatchService watchService;
    private Thread watchThread;
    private static AxiumMenu instance;
    private static boolean placeholderAPIEnabled = false;

    @Override
    public void onEnable() {
        Logger.init(this);
        instance = this;
        // Initialize configuration manager
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        // Initialize menu manager
        menuManager = new MenuManager(this);
        
        // Create example menus only if the menus folder doesn't exist
        File menuFolder = new File(getDataFolder(), "menus");
        if (!menuFolder.exists()) {
            createExampleMenus();
        }
        
        // Load menus
        try {
            menuManager.loadMenus();
        } catch (MenuLoadException e) {
            Logger.error("Failed to load menus: " + e.getMessage(), e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        // Register main command
        MainCommand mainCommand = new MainCommand(this);
        Objects.requireNonNull(getCommand("axiummenu")).setExecutor(mainCommand);
        Objects.requireNonNull(getCommand("axiummenu")).setTabCompleter(mainCommand);
        
        // Register menu listener
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        
        Logger.info("AxiumMenu plugin has been enabled successfully!");
        
        // Start file watcher if serve mode is enabled
        if (configManager.isServeMode()) {
            startFileWatcher();
        }
        
        if (configManager.isDebugMode()) {
            Logger.debug("AxiumMenu plugin is starting in debug mode");
            Logger.debug("Plugin data folder: " + getDataFolder().getAbsolutePath());
        }
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
            Logger.info("PlaceholderAPI found and hooked successfully!");
        }
    }

    @Override
    public void onDisable() {
        menuManager.shutdown();
        stopFileWatcher();
        Logger.info("AxiumMenu plugin has been disabled.");
    }

    /**
     * Creates example menu files in the menus directory.
     * This method is called only during the first plugin initialization.
     */
    private void createExampleMenus() {
        File menuFolder = new File(getDataFolder(), "menus");
        if (!menuFolder.exists()) {
            boolean created = menuFolder.mkdirs();
            if (!created) {
                Logger.warn("Failed to create menus folder");
            }
        }

        saveResource("menus/example.yml", false);
        
        Logger.info("Example menu created in the menus folder");
    }

    /**
     * Retrieves the configuration manager instance.
     * @return The ConfigManager instance for this plugin.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Retrieves the menu manager instance.
     * @return The MenuManager instance for this plugin.
     */
    public MenuManager getMenuManager() {
        return menuManager;
    }

    /**
     * Starts the file watcher service if serve mode is enabled.
     * This allows for automatic reloading of menu files when changes are detected.
     */
    public void startFileWatcher() {
        if (watchThread != null && watchThread.isAlive()) {
            return;
        }
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path menuFolder = new File(getDataFolder(), "menus").toPath();
            menuFolder.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            
            watchThread = new Thread(this::watchForChanges);
            watchThread.start();
            
            Logger.info("Menu file watcher has been started");
        } catch (IOException e) {
            Logger.error("Unable to start menu file watcher: " + e.getMessage(), e);
        }
    }

    /**
     * Stops the file watcher service.
     * This method is called when the plugin is being disabled or when serve mode is turned off.
     */
    public void stopFileWatcher() {
        if (watchThread != null) {
            watchThread.interrupt();
            watchThread = null;
        }
        if (watchService != null) {
            try {
                watchService.close();
                watchService = null;
            } catch (IOException e) {
                Logger.error("Error closing file watch service: " + e.getMessage(), e);
            }
        }
        Logger.info("Menu file watcher has been stopped");
    }

    /**
     * Watches for changes in menu files and reloads menus when changes are detected.
     * This method runs in a separate thread to avoid blocking the main server thread.
     */
    private void watchForChanges() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        if (configManager.isDebugMode()) {
                            Logger.debug("File change detected: " + event.context());
                        }
                        Logger.info("Menu file change detected, reloading menus...");
                        
                        Bukkit.getScheduler().runTaskAsynchronously(this, () -> Bukkit.getPluginManager().callEvent(new MenuReloadEvent()));
                        
                        Bukkit.getScheduler().runTask(this, () -> {
                            try {
                                menuManager.loadMenus();
                                Bukkit.getOnlinePlayers().forEach(player -> {
                                    if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuInventoryHolder menuHolder) {
                                        Menu currentMenu = menuHolder.getMenu();
                                        if (currentMenu.isExpired()) {
                                            String menuName = currentMenu.getName();
                                            player.closeInventory();
                                            try {
                                                Menu updatedMenu = menuManager.getMenu(menuName);
                                                updatedMenu.open(player);
                                            } catch (MenuNotFoundException e) {
                                                Logger.warn("Menu " + menuName + " no longer exists after reload.");
                                            }
                                        }
                                    }
                                });
                            } catch (MenuLoadException e) {
                                Logger.error("Failed to reload menus: " + e.getMessage(), e);
                            }
                        });
                    }
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            // Thread interrupted, exiting normally
            Thread.currentThread().interrupt();
        }
    }

    public static AxiumMenu getInstance() {
        return instance;
    }

    @SuppressWarnings("unused")
    public static String setPlaceholders(Player player, String text) {
        if (placeholderAPIEnabled) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }
}