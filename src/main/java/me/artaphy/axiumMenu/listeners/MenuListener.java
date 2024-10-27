package me.artaphy.axiumMenu.listeners;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.menu.Menu;
import me.artaphy.axiumMenu.menu.MenuInventoryHolder;
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

import java.util.Objects;

/**
 * Handles all menu-related events in the plugin.
 * This listener manages:
 * - Menu interaction events (clicks, drags)
 * - Menu opening and closing
 * - Menu command execution
 * - Chat triggers
 * - Item triggers
 * <p>
 * Implements security measures to prevent unauthorized inventory manipulation
 * and ensures proper event cancellation where necessary.
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
                    menu.executeItemActions(player, menuItem);
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
        if (event.getInventory().getHolder() instanceof MenuInventoryHolder menuHolder) {
            Logger.debug("Menu closed by " + event.getPlayer().getName());
            if (event.getPlayer() instanceof Player player) {
                menuHolder.getMenu().executeEvent("close", player);
            }
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

        if (triggerMeta == null) {
            return true;
        }

        if (triggerMeta.hasDisplayName()) {
            if (itemMeta == null || !itemMeta.hasDisplayName() || 
                !itemMeta.getDisplayName().equals(triggerMeta.getDisplayName())) {
                return false;
            }
        }

        if (triggerMeta.hasLore()) {
            return itemMeta != null && itemMeta.hasLore() &&
                    Objects.requireNonNull(itemMeta.getLore()).equals(triggerMeta.getLore());
        }

        return true;
    }
}
