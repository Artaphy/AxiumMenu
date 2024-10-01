package me.artaphy.axiumMenu;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import me.artaphy.axiumMenu.commands.MainCommand;
import me.artaphy.axiumMenu.config.ConfigManager;
import me.artaphy.axiumMenu.events.MenuReloadEvent;
import me.artaphy.axiumMenu.menu.MenuManager;
import me.artaphy.axiumMenu.listeners.MenuListener;
import me.artaphy.axiumMenu.menu.Menu;  // 添加这行导入语句

import java.util.Objects;
import java.io.File;
import java.nio.file.*;
import java.io.IOException;

/**
 * Main class for the AxiumMenu plugin.
 * This class handles the initialization, enabling, and disabling of the plugin.
 */
public final class AxiumMenu extends JavaPlugin {

    private ConfigManager configManager;
    private MenuManager menuManager;
    private WatchService watchService;
    private Thread watchThread;

    @Override
    public void onEnable() {
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
        
        menuManager.loadMenus();
        
        // Register main command
        Objects.requireNonNull(getCommand("axiummenu")).setExecutor(new MainCommand(this));
        
        // Register menu listener
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        
        getLogger().info("AxiumMenu plugin has been enabled!");
        
        // Start file watcher if serve mode is enabled
        startFileWatcher();
    }

    @Override
    public void onDisable() {
        stopFileWatcher();
        getLogger().info("AxiumMenu plugin has been disabled!");
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
                getLogger().warning("Failed to create menus folder");
            }
        }

        saveResource("menus/example.yml", false);
        saveResource("menus/example2.conf", false);
        saveResource("menus/example3.hocon", false);
        
        getLogger().info("Example menus created in the menus folder");
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
     * Creates a new menu dynamically.
     * @param name The name of the menu.
     * @param title The title of the menu.
     * @param rows The number of rows in the menu.
     * @return The newly created Menu instance.
     */
    @SuppressWarnings("unused")
    public Menu createMenu(String name, String title, int rows) {
        Menu menu = new Menu(name, title, rows);
        menuManager.registerMenu(menu);
        return menu;
    }

    /**
     * Registers a custom menu with the menu manager.
     * @param menu The Menu instance to register.
     */
    @SuppressWarnings("unused")
    public void registerMenu(Menu menu) {
        menuManager.registerMenu(menu);
    }

    /**
     * Starts the file watcher service if serve mode is enabled.
     * This allows for automatic reloading of menu files when changes are detected.
     */
    private void startFileWatcher() {
        if (configManager.isServeMode()) {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                Path menuFolder = new File(getDataFolder(), "menus").toPath();
                menuFolder.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                
                watchThread = new Thread(this::watchForChanges);
                watchThread.start();
                
                getLogger().info("Menu file watcher has been started");
            } catch (IOException e) {
                getLogger().severe("Unable to start menu file watcher: " + e.getMessage());
            }
        }
    }

    /**
     * Stops the file watcher service.
     * This method is called when the plugin is being disabled.
     */
    private void stopFileWatcher() {
        if (watchThread != null) {
            watchThread.interrupt();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                getLogger().severe("Error closing file watch service: " + e.getMessage());
            }
        }
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
                        getLogger().info("Menu file change detected, reloading menus...");
                        Bukkit.getPluginManager().callEvent(new MenuReloadEvent());
                    }
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            // Thread interrupted, exiting normally
        }
    }
}
