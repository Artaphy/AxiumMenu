package me.artaphy.axiumMenu.listeners;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.menu.Menu;
import me.artaphy.axiumMenu.menu.MenuInventoryHolder;
import me.artaphy.axiumMenu.menu.MenuItem;
import me.artaphy.axiumMenu.exceptions.MenuNotFoundException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;
import me.artaphy.axiumMenu.utils.Logger;

/**
 * Handles inventory-related events for AxiumMenu.
 * This class is responsible for managing menu interactions and closures.
 */
public class MenuListener implements Listener {

    private final AxiumMenu plugin;

    /**
     * Constructs a new MenuListener instance.
     *
     * @param plugin The main plugin instance.
     */
    public MenuListener(AxiumMenu plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles inventory click events within menus.
     * This method cancels the event and processes menu item actions.
     *
     * @param event The InventoryClickEvent to handle.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof MenuInventoryHolder menuHolder) {
            event.setCancelled(true);

            int slot = event.getRawSlot();
            if (slot >= 0 && slot < event.getInventory().getSize()) {
                Menu menu = menuHolder.getMenu();
                menu.getItemAt(slot).ifPresent(menuItem -> {
                    if (plugin.getConfigManager().isDebugMode()) {
                        Logger.debug("Player " + player.getName() + " clicked item in menu " + menu.getName() + " at slot " + slot);
                    }
                    handleMenuItemClick(player, menuItem);
                });
            }
        }
    }

    /**
     * Handles inventory drag events within menus.
     * This method cancels the event to prevent players from changing menu items.
     *
     * @param event The InventoryDragEvent to handle.
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MenuInventoryHolder) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles inventory move item events within menus.
     * This method cancels the event to prevent players from moving items between menus.
     *
     * @param event The InventoryMoveItemEvent to handle.
     */
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getDestination().getHolder() instanceof MenuInventoryHolder ||
            event.getSource().getHolder() instanceof MenuInventoryHolder) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles inventory close events for menus.
     * This method can be used to perform actions when a menu is closed.
     *
     * @param event The InventoryCloseEvent to handle.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof MenuInventoryHolder) {
            // Add any necessary logic for menu closure here
            Logger.debug("Menu closed by " + event.getPlayer().getName());
        }
    }

    /**
     * Processes the action associated with a clicked menu item.
     *
     * @param player   The player who clicked the item.
     * @param menuItem The MenuItem that was clicked.
     */
    private void handleMenuItemClick(Player player, MenuItem menuItem) {
        String action = menuItem.getAction();
        if (action == null || action.isEmpty()) {
            return;
        }

        String[] parts = action.split(":", 2);
        String actionType = parts[0].toLowerCase();
        String actionValue = parts.length > 1 ? parts[1] : "";

        try {
            switch (actionType) {
                case "command" -> player.performCommand(actionValue);
                case "message" -> player.sendMessage(actionValue);
                case "open" -> openSubMenu(player, actionValue);
                case "close" -> player.closeInventory();
                default -> Logger.warn("Unknown action type: " + actionType);
            }
        } catch (Exception e) {
            Logger.error("Error handling menu item click", e);
        }
    }

    /**
     * Opens a sub-menu for the player.
     *
     * @param player   The player for whom to open the sub-menu.
     * @param menuName The name of the sub-menu to open.
     */
    private void openSubMenu(Player player, String menuName) {
        try {
            Menu subMenu = plugin.getMenuManager().getMenu(menuName);
            subMenu.open(player);
        } catch (MenuNotFoundException e) {
            Logger.warn("Attempted to open non-existent menu: " + menuName);
            player.sendMessage("Sorry, that menu doesn't exist.");
        }
    }
}