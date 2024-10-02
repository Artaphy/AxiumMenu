package me.artaphy.axiumMenu.menu;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.exceptions.MenuLoadException;
import me.artaphy.axiumMenu.exceptions.MenuNotFoundException;
import me.artaphy.axiumMenu.utils.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * Manages the creation, loading, and retrieval of menus for the AxiumMenu plugin.
 * This class handles menu caching, asynchronous loading, and provides thread-safe access to menus.
 */
public class MenuManager {

    private final AxiumMenu plugin;
    private final Map<String, Menu> menus = new ConcurrentHashMap<>();
    private final Map<String, Menu> menuCache = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Constructs a new MenuManager instance.
     *
     * @param plugin The main plugin instance.
     */
    public MenuManager(AxiumMenu plugin) {
        this.plugin = plugin;
        // Schedule periodic cache cleaning task
        scheduler.scheduleAtFixedRate(this::cleanCache, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * Loads all menus from the menu configuration files.
     * This method clears existing menus and reloads them from the config files.
     *
     * @throws MenuLoadException if any menu fails to load
     */
    public void loadMenus() throws MenuLoadException {
        // 标记所有现有菜单为过期
        menus.values().forEach(Menu::markAsExpired);
        
        menus.clear();
        menuCache.clear(); // 添加这一行
        Path menuFolder = plugin.getDataFolder().toPath().resolve("menus");
        if (!Files.exists(menuFolder)) {
            try {
                Files.createDirectories(menuFolder);
            } catch (IOException e) {
                throw new MenuLoadException("Failed to create menus directory", e);
            }
        }

        try (Stream<Path> paths = Files.list(menuFolder)) {
            paths.filter(path -> path.toString().endsWith(".yml"))
                 .forEach(this::loadMenuFile);

            if (menus.isEmpty()) {
                throw new MenuLoadException("No menus were successfully loaded");
            }

            // 修改这一行
            Logger.info("Menus loaded successfully. Total menus: " + menus.size());
        } catch (IOException e) {
            throw new MenuLoadException("Error accessing menu files", e);
        }
    }

    /**
     * Loads a single menu from a configuration file.
     *
     * @param file The path to the menu configuration file.
     */
    private void loadMenuFile(Path file) {
        String fileName = file.getFileName().toString();
        String menuName = getMenuName(fileName);
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file.toFile());
            Menu menu = new Menu(menuName, config);
            registerMenu(menu);
            Logger.debug("Loaded menu: " + menuName + " with " + menu.getItemCount() + " items");
            Logger.debug("Menu title: " + menu.getTitle()); // 添加这行来查看标题是否更新
            Logger.info("Successfully loaded menu: " + menuName);
        } catch (MenuLoadException e) {
            Logger.error("Failed to load menu from file " + fileName + ": " + e.getMessage());
        }
    }

    private String getMenuName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    /**
     * Retrieves a menu by its name.
     *
     * @param name The name of the menu to retrieve.
     * @return The Menu instance.
     * @throws MenuNotFoundException if the menu is not found.
     */
    public Menu getMenu(String name) throws MenuNotFoundException {
        lock.readLock().lock();
        try {
            Menu menu = menuCache.get(name);
            if (menu == null) {
                menu = menus.get(name);
                if (menu == null) {
                    throw new MenuNotFoundException("Menu not found: " + name);
                }
                menuCache.put(name, menu);
            }
            return menu;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Cleans the menu cache periodically.
     * This method is called automatically by the scheduler.
     */
    private void cleanCache() {
        menuCache.clear();
        Logger.debug("Menu cache cleared");
    }

    /**
     * Retrieves all loaded menus.
     *
     * @return An unmodifiable map of all loaded menus, with menu names as keys and Menu instances as values.
     */
    public Map<String, Menu> getAllMenus() {
        return Map.copyOf(menus);
    }

    /**
     * Registers a new menu or updates an existing one.
     *
     * @param menu The Menu instance to register.
     */
    public void registerMenu(Menu menu) {
        lock.writeLock().lock();
        try {
            menus.put(menu.getName(), menu);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieves a menu asynchronously by its name.
     * This method is useful for loading menus without blocking the main thread.
     *
     * @param name The name of the menu to retrieve.
     * @return A CompletableFuture that will complete with the Menu instance, or complete exceptionally with a MenuNotFoundException.
     */
    public CompletableFuture<Menu> getMenuAsync(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getMenu(name);
            } catch (MenuNotFoundException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Shuts down the MenuManager and its scheduler.
     * This method should be called when the plugin is being disabled.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}