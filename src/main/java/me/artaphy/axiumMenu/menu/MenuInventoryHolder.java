package me.artaphy.axiumMenu.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a custom InventoryHolder for AxiumMenu menus.
 * This class is used to associate a Menu instance with a Bukkit Inventory.
 */
public class MenuInventoryHolder implements InventoryHolder {

    private final Menu menu;
    private final Inventory inventory;

    /**
     * Constructs a new MenuInventoryHolder instance.
     *
     * @param menu The Menu instance associated with this holder.
     */
    public MenuInventoryHolder(Menu menu) {
        this.menu = menu;
        this.inventory = menu.createInventory(this);
    }

    /**
     * Gets the Menu instance associated with this holder.
     *
     * @return The Menu instance.
     */
    public Menu getMenu() {
        return menu;
    }

    /**
     * Gets the inventory associated with this holder.
     * This method is overridden to comply with Bukkit's InventoryHolder interface.
     *
     * @return The Inventory instance.
     */
    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}