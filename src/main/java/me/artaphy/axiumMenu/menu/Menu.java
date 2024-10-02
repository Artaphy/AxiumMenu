package me.artaphy.axiumMenu.menu;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.events.MenuOpenEvent;
import me.artaphy.axiumMenu.exceptions.MenuLoadException;
import me.artaphy.axiumMenu.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a menu in the AxiumMenu plugin.
 * A menu consists of a title, a layout, and a collection of menu items.
 */
public class Menu {
    private final String name;
    private final String title;
    private final int rows;
    private final MenuType type;
    private final String layout;
    private final Map<Integer, MenuItem> items;
    private boolean isExpired = false;

    /**
     * Constructs a new Menu instance.
     *
     * @param name   The unique name of the menu.
     * @param config The configuration section containing the menu details.
     * @throws MenuLoadException If there's an error in the menu configuration.
     */
    public Menu(String name, ConfigurationSection config) throws MenuLoadException {
        this.name = name;
        this.title = config.getString("title", "");
        this.rows = config.getInt("rows", 3);
        this.type = MenuType.fromString(config.getString("type", "CHEST"));
        this.layout = config.getString("layout", "");
        this.items = new HashMap<>();

        validateConfig(config);
        loadItems(config.getConfigurationSection("items"));
    }

    /**
     * Loads menu items from the configuration.
     *
     * @param itemsSection The configuration section containing item details.
     * @throws MenuLoadException If there's an error loading the items.
     */
    private void loadItems(ConfigurationSection itemsSection) throws MenuLoadException {
        if (itemsSection == null) {
            throw new MenuLoadException("Missing 'items' section for menu: " + name);
        }

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection != null) {
                MenuItem menuItem = new MenuItem(itemSection);
                if (key.equals("close") && itemSection.contains("slot")) {
                    int slot = itemSection.getInt("slot");
                    items.put(slot, menuItem);
                } else {
                    placeItemInLayout(key, menuItem);
                }
            }
        }
    }

    /**
     * Places an item in the menu layout based on the layout string.
     *
     * @param key      The key representing the item in the layout.
     * @param menuItem The MenuItem to place.
     */
    private void placeItemInLayout(String key, MenuItem menuItem) {
        int index = 0;
        for (char c : layout.toCharArray()) {
            if (c != '\n' && String.valueOf(c).equals(key)) {
                items.put(index, menuItem);
            }
            if (c != '\n') {
                index++;
            }
        }
    }

    /**
     * Opens the menu for a player.
     *
     * @param player The player to open the menu for.
     */
    public void open(Player player) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(AxiumMenu.getInstance(), () -> open(player));
            return;
        }

        MenuOpenEvent event = new MenuOpenEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        Inventory inventory = createInventory(new MenuInventoryHolder(this));
        player.openInventory(inventory);
    }

    /**
     * Creates an inventory for this menu.
     *
     * @param holder The InventoryHolder for this inventory.
     * @return The created Inventory.
     */
    public Inventory createInventory(InventoryHolder holder) {
        Inventory inventory;
        if (type == MenuType.CHEST) {
            inventory = Bukkit.createInventory(holder, rows * 9, ColorUtils.colorize(title));
        } else {
            inventory = Bukkit.createInventory(holder, type.getBukkitType(), ColorUtils.colorize(title));
        }

        for (Map.Entry<Integer, MenuItem> entry : items.entrySet()) {
            int slot = entry.getKey();
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, entry.getValue().getItemStack());
            } else {
                Bukkit.getLogger().warning("Attempted to set item at invalid slot " + slot + " in menu " + name);
            }
        }
        return inventory;
    }

    /**
     * Gets the name of the menu.
     *
     * @return The menu's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the title of the menu.
     *
     * @return The menu's title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the MenuItem at a specific slot.
     *
     * @param slot The slot to get the item from.
     * @return An Optional containing the MenuItem at the specified slot, or an empty Optional if not found.
     */
    public Optional<MenuItem> getItemAt(int slot) {
        return Optional.ofNullable(items.get(slot));
    }

    /**
     * Validates the configuration of the menu.
     *
     * @param config The configuration section to validate.
     * @throws MenuLoadException If the configuration is invalid.
     */
    private void validateConfig(ConfigurationSection config) throws MenuLoadException {
        if (title.isEmpty()) {
            throw new MenuLoadException("Missing or empty 'title' for menu: " + name);
        }
        if (rows < 1 || rows > 6) {
            throw new MenuLoadException("Invalid 'rows' value: " + rows + ". Must be between 1 and 6 for menu: " + name);
        }
        if (layout.isEmpty()) {
            throw new MenuLoadException("Missing or empty 'layout' for menu: " + name);
        }
        if (!config.contains("items")) {
            throw new MenuLoadException("Missing 'items' section for menu: " + name);
        }
    }

    /**
     * Gets the number of items in this menu.
     *
     * @return The number of items in the menu.
     */
    public int getItemCount() {
        return items.size();
    }

    public void markAsExpired() {
        this.isExpired = true;
    }

    public boolean isExpired() {
        return isExpired;
    }
}