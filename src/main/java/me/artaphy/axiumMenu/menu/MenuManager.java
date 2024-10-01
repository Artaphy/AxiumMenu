package me.artaphy.axiumMenu.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.config.ConfigAdapter;
import me.artaphy.axiumMenu.exceptions.MenuLoadException;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the creation, loading, and retrieval of menus for the AxiumMenu plugin.
 */
public class MenuManager {

    private final AxiumMenu plugin;
    private final Map<String, Menu> menus = new ConcurrentHashMap<>();

    /**
     * Constructs a new MenuManager instance.
     *
     * @param plugin The main plugin instance.
     */
    public MenuManager(AxiumMenu plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads all menus from the menu configuration files.
     * This method clears existing menus and reloads them from the config files.
     */
    public void loadMenus() {
        new BukkitRunnable() {
            @Override
            public void run() {
                menus.clear();
                File menuFolder = new File(plugin.getDataFolder(), "menus");
                if (!menuFolder.exists()) {
                    boolean created = menuFolder.mkdirs();
                    if (!created) {
                        plugin.getLogger().warning("Failed to create menus folder");
                    }
                }

                File[] menuFiles = menuFolder.listFiles((dir, name) -> 
                    name.endsWith(".yml") || name.endsWith(".conf") || name.endsWith(".hocon"));

                if (menuFiles != null) {
                    for (File file : menuFiles) {
                        try {
                            loadMenu(file);
                        } catch (MenuLoadException e) {
                            plugin.getLogger().severe("Failed to load menu from file " + file.getName() + ": " + e.getMessage());
                        }
                    }
                }
                plugin.getLogger().info("Menus loaded successfully.");
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Loads a single menu from a configuration file.
     *
     * @param file The configuration file to load the menu from.
     */
    private void loadMenu(File file) throws MenuLoadException {
        String fileName = file.getName();
        String menuName = fileName.substring(0, fileName.lastIndexOf('.'));

        try {
            ConfigurationSection config;
            if (fileName.endsWith(".yml")) {
                config = YamlConfiguration.loadConfiguration(file);
            } else if (fileName.endsWith(".conf") || fileName.endsWith(".hocon")) {
                config = new ConfigAdapter(file);
            } else {
                throw new MenuLoadException("Unsupported menu file format: " + fileName);
            }

            Menu menu = new Menu(menuName, config);
            menus.put(menuName, menu);
            plugin.getLogger().info("Successfully loaded menu: " + menuName);
        } catch (Exception e) {
            throw new MenuLoadException("Error loading menu " + menuName, e);
        }
    }

    /**
     * Retrieves a menu by its name.
     *
     * @param name The name of the menu to retrieve.
     * @return The Menu instance, or null if not found.
     */
    public Menu getMenu(String name) {
        return menus.get(name);
    }

    /**
     * Retrieves all loaded menus.
     *
     * @return A map of all loaded menus, with menu names as keys and Menu instances as values.
     */
    public Map<String, Menu> getAllMenus() {
        return new ConcurrentHashMap<>(menus);
    }

    /**
     * Retrieves a menu by its title.
     *
     * @param title The title of the menu to retrieve.
     * @return The Menu instance, or null if not found.
     */
    public Menu getMenuByTitle(String title) {
        return menus.values().stream()
                .filter(menu -> menu.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    /**
     * Registers a new menu or updates an existing one.
     *
     * @param menu The Menu instance to register.
     */
    public void registerMenu(Menu menu) {
        menus.put(menu.getName(), menu);
    }
}