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
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.Objects;
import me.clip.placeholderapi.PlaceholderAPI;

/**
 * Main class for the AxiumMenu plugin.
 * This plugin provides a flexible and powerful menu system for Bukkit/Spigot servers.
 * Features include:
 * - YAML-based menu configuration
 * - Dynamic menu loading and reloading
 * - Support for conditional actions
 * - PlaceholderAPI integration
 * - Multi-language support
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
        registerMainCommand();
        
        // Register menu listener
        MenuListener menuListener = new MenuListener(this);
        getServer().getPluginManager().registerEvents(menuListener, this);
        
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

    @SuppressWarnings("all")
    public static String setPlaceholders(Player player, String text) {
        if (placeholderAPIEnabled && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                return PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
                Logger.error("Error setting placeholders: " + e.getMessage(), e);
                return text;
            }
        }
        return text;
    }

    /**
     * Dynamically registers a command for a menu.
     * @param command The command to register.
     * @param menu The menu to open when the command is executed.
     */
    public void registerMenuCommand(String command, Menu menu) {
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            commandMap.register(command, new Command(command) {
                @Override
                public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
                    if (sender instanceof Player) {
                        menu.open((Player) sender);
                        return true;
                    }
                    return false;
                }
            });

            Logger.info("Registered command '" + command + "' for menu '" + menu.getName() + "'");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Logger.error("Failed to register command '" + command + "' for menu '" + menu.getName() + "'", e);
        }
    }

    /**
     * Registers the main command for the plugin.
     */
    private void registerMainCommand() {
        MainCommand mainCommand = new MainCommand(this);
        Objects.requireNonNull(getCommand("axiummenu")).setExecutor(mainCommand);
        Objects.requireNonNull(getCommand("axiummenu")).setTabCompleter(mainCommand);
    }
}
