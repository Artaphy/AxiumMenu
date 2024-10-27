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
 * Manages the lifecycle and access of menus in the AxiumMenu plugin.
 * This class provides thread-safe menu operations, caching, and asynchronous loading capabilities.
 * It handles menu registration, retrieval, and cleanup operations.
 */
public class MenuManager {

    private final AxiumMenu plugin;
    private final Map<String, Menu> menus = new ConcurrentHashMap<>();
    private final Map<String, Menu> menuCache = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // 在类的顶部添加常量
    private static final long MENU_CACHE_TIMEOUT = 30 * 60 * 1000; // 30分钟的超时时间

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
     * Loads all menu files from the menus directory.
     * This method is called during plugin startup and reload operations.
     * It clears existing menus and reloads them from the filesystem.
     *
     * @throws MenuLoadException if there are errors loading the menus
     */
    public void loadMenus() throws MenuLoadException {
        menus.values().forEach(Menu::markAsExpired);
        
        menus.clear();
        menuCache.clear();
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
     * Retrieves a menu by its name with caching support.
     * This method is thread-safe and uses a read lock to ensure consistency.
     *
     * @param name The name of the menu to retrieve
     * @return The requested Menu instance
     * @throws MenuNotFoundException if the menu doesn't exist
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
     * Performs periodic cleanup of the menu cache.
     * This method is automatically called by the scheduler to prevent memory leaks.
     */
    private void cleanCache() {
        menuCache.clear();
        Logger.debug("Menu cache cleared");
    }

    /**
     * Cleans up unused menus based on their last access time.
     * This method removes menus that haven't been accessed for longer than the timeout period.
     * Currently unused but retained for future implementation.
     */
    @SuppressWarnings("unused")
    private void cleanupUnusedMenus() {
        long currentTime = System.currentTimeMillis();
        menus.entrySet().removeIf(entry -> {
            Menu menu = entry.getValue();
            return currentTime - menu.getLastAccessTime() > MENU_CACHE_TIMEOUT;
        });
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
     * Retrieves a menu asynchronously.
     * This method is useful for loading menus without blocking the main server thread.
     *
     * @param name The name of the menu to retrieve
     * @return A CompletableFuture that will complete with the Menu instance
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
     * Performs cleanup operations when the plugin is being disabled.
     * Ensures proper shutdown of the scheduler and cleanup of resources.
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
