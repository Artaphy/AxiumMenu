package me.artaphy.axiumMenu.menu;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.inventory.InventoryType;
import me.artaphy.axiumMenu.utils.ColorUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a menu in the AxiumMenu plugin.
 * This class handles the creation and management of individual menus.
 */
public class Menu {

    private final String name;
    private final String title;
    private final String type;
    private final int rows;
    private final Map<Integer, MenuItem> items;

    /**
     * Constructs a new Menu instance with basic properties.
     *
     * @param name The name of the menu.
     * @param title The title of the menu.
     * @param rows The number of rows in the menu.
     */
    public Menu(String name, String title, int rows) {
        this.name = name;
        this.title = title;
        this.type = "";
        this.rows = rows;
        this.items = new HashMap<>();
        // Other initialization logic...
    }

    /**
     * Constructs a new Menu instance from a configuration section.
     *
     * @param name The name of the menu.
     * @param config The configuration section containing menu details.
     */
    public Menu(String name, ConfigurationSection config) {
        this.name = name;
        this.items = new HashMap<>();
        
        ConfigurationSection menuSection = config.getConfigurationSection("menu");
        if (menuSection == null) {
            // Use simplified format
            this.title = config.getString("title", "Default Title");
            this.type = config.getString("type", "chest");
            this.rows = config.getInt("rows", 6);
            loadItems(config.getConfigurationSection("items"));
        } else {
            // Use standard format
            this.title = menuSection.getString("title", "Default Title");
            this.type = menuSection.getString("type", "chest");
            this.rows = menuSection.getInt("rows", 6);
            loadItems(menuSection.getConfigurationSection("items"));
        }
    }

    /**
     * Loads menu items from a configuration section.
     *
     * @param itemsSection The configuration section containing item details.
     */
    private void loadItems(ConfigurationSection itemsSection) {
        if (itemsSection == null) return;

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection != null) {
                MenuItem menuItem = new MenuItem(itemSection);
                int slot = itemSection.getInt("slot", -1);
                if (slot >= 0) {
                    items.put(slot, menuItem);
                }
            }
        }
    }

    /**
     * Opens the menu for a specific player.
     *
     * @param player The player to open the menu for.
     */
    public void open(Player player) {
        Inventory inventory;
        String colorizedTitle = ColorUtils.colorize(title);
        if ("chest".equalsIgnoreCase(type)) {
            inventory = Bukkit.createInventory(null, rows * 9, colorizedTitle);
        } else {
            InventoryType invType = InventoryType.valueOf(type.toUpperCase());
            inventory = Bukkit.createInventory(null, invType, colorizedTitle);
        }
        
        for (Map.Entry<Integer, MenuItem> entry : items.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
        }

        player.openInventory(inventory);
    }

    /**
     * Gets all items in this menu.
     *
     * @return A map of slot numbers to MenuItem objects.
     */
    public Map<Integer, MenuItem> getItems() {
        return items;
    }

    /**
     * Gets the name of this menu.
     *
     * @return The name of the menu.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the title of this menu.
     *
     * @return The title of the menu.
     */
    public String getTitle() {
        return title;
    }

    // Getter methods...
}