package me.artaphy.axiumMenu.events;

import me.artaphy.axiumMenu.menu.Menu;
import org.bukkit.entity.Player;

/**
 * Represents a generic menu-related event.
 * This record class encapsulates the basic information needed for menu events:
 * - The menu instance involved
 * - The player triggering the event
 * <p>
 * This class serves as a base for more specific menu events and provides
 * a consistent way to pass menu event data throughout the plugin.
 */
public record MenuEvent(Menu menu, Player player) {
    /**
     * Constructs a new MenuEvent.
     *
     * @param menu   The menu associated with this event
     * @param player The player involved in this event
     */
    public MenuEvent {
    }

    /**
     * Gets the menu associated with this event.
     *
     * @return The Menu instance
     */
    @Override
    public Menu menu() {
        return menu;
    }

    /**
     * Gets the player involved in this event.
     *
     * @return The Player instance
     */
    @Override
    public Player player() {
        return player;
    }
}
