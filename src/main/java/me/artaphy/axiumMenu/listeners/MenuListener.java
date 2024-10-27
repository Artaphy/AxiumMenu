package me.artaphy.axiumMenu.listeners;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.menu.Menu;
import me.artaphy.axiumMenu.menu.MenuInventoryHolder;
import me.artaphy.axiumMenu.menu.MenuItem;
import me.artaphy.axiumMenu.exceptions.MenuNotFoundException;
import me.artaphy.axiumMenu.menu.MenuActivator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        registerMenuCommands();
    }

    /**
     * Registers all menu commands as defined in the menu configurations.
     */
    private void registerMenuCommands() {
        for (Menu menu : plugin.getMenuManager().getAllMenus().values()) {
            for (MenuActivator activator : menu.getActivators()) {
                if (activator.getType() == MenuActivator.ActivatorType.COMMAND) {
                    for (String command : activator.getCommands()) {
                        plugin.registerMenuCommand(command, menu);
                    }
                }
            }
        }
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

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage().toLowerCase();
        for (Menu menu : plugin.getMenuManager().getAllMenus().values()) {
            for (MenuActivator activator : menu.getActivators()) {
                if (activator.getType() == MenuActivator.ActivatorType.CHAT) {
                    if (activator.getChatTriggers().contains(message)) {
                        event.setCancelled(true);
                        plugin.getServer().getScheduler().runTask(plugin, () -> menu.open(event.getPlayer()));
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        for (Menu menu : plugin.getMenuManager().getAllMenus().values()) {
            for (MenuActivator activator : menu.getActivators()) {
                if (activator.getType() == MenuActivator.ActivatorType.ITEM) {
                    ItemStack triggerItem = activator.getItemTrigger();
                    if (itemsMatch(item, triggerItem)) {
                        event.setCancelled(true);
                        menu.open(event.getPlayer());
                        return;
                    }
                }
            }
        }
    }

    /**
     * Checks if two ItemStacks match for the purpose of menu activation.
     * This method is more lenient than a strict equality check.
     *
     * @param item The item being interacted with.
     * @param triggerItem The item configured as a menu trigger.
     * @return true if the items match, false otherwise.
     */
    private boolean itemsMatch(ItemStack item, ItemStack triggerItem) {
        if (item.getType() != triggerItem.getType()) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();
        ItemMeta triggerMeta = triggerItem.getItemMeta();

        // If trigger item has no meta, consider it a match
        if (triggerMeta == null) {
            return true;
        }

        // If trigger item has a name, check if it matches
        if (triggerMeta.hasDisplayName()) {
            if (itemMeta == null || !itemMeta.hasDisplayName() || 
                !itemMeta.getDisplayName().equals(triggerMeta.getDisplayName())) {
                return false;
            }
        }

        // If trigger item has lore, check if it matches
        if (triggerMeta.hasLore()) {
            if (itemMeta == null || !itemMeta.hasLore() || 
                !itemMeta.getLore().equals(triggerMeta.getLore())) {
                return false;
            }
        }

        return true;
    }
}
