package me.artaphy.axiumMenu.listeners;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.menu.Menu;
import me.artaphy.axiumMenu.menu.MenuItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

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

        Menu menu = plugin.getMenuManager().getMenuByTitle(event.getView().getTitle());

        if (menu != null) {
            event.setCancelled(true);

            int slot = event.getRawSlot();
            MenuItem menuItem = menu.getItems().get(slot);

            if (menuItem != null) {
                handleMenuItemClick(player, menuItem);
            }
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
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Menu menu = plugin.getMenuManager().getMenuByTitle(event.getView().getTitle());

        if (menu != null) {
            handleMenuClose(player, menu);
        }
    }

    /**
     * Processes the action associated with a clicked menu item.
     *
     * @param player The player who clicked the item.
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

        switch (actionType) {
            case "command":
                player.performCommand(actionValue);
                break;
            case "message":
                player.sendMessage(actionValue);
                break;
            case "open":
                Menu subMenu = plugin.getMenuManager().getMenu(actionValue);
                if (subMenu != null) {
                    subMenu.open(player);
                }
                break;
            case "close":
                player.closeInventory();
                break;
            // Additional action types can be added here
        }
    }

    /**
     * Handles the closing of a menu.
     * This method can be extended to perform actions when a menu is closed.
     *
     * @param player The player who closed the menu.
     * @param menu The Menu that was closed.
     */
    @SuppressWarnings("unused")
    private void handleMenuClose(Player player, Menu menu) {
        // Add logic for menu close handling if needed
    }
}