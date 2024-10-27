package me.artaphy.axiumMenu.events;

import org.bukkit.entity.Player;

/**
 * Interface representing an executable action in the menu system.
 * Actions are the core mechanism for menu interactivity, allowing:
 * - Command execution
 * - Message sending
 * - Sound playing
 * - Menu navigation
 * - Custom behavior implementation
 * <p>
 * All action implementations should be thread-safe and handle their own error cases.
 */
public interface Action {
    /**
     * Executes the action for the given player.
     *
     * @param player The player to execute the action for
     * @return true if the action was executed successfully, false if further actions should be stopped
     */
    boolean execute(Player player);
}
