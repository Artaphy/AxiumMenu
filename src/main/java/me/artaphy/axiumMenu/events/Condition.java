package me.artaphy.axiumMenu.events;

import org.bukkit.entity.Player;

/**
 * Interface representing a condition that can be checked in menu interactions.
 * Conditions are used to control whether actions should be executed based on
 * various factors such as:
 * - Player permissions
 * - Player statistics
 * - Server state
 * - Custom expressions
 * <p>
 * Implementations should be thread-safe and handle their own error cases.
 */
public interface Condition {
    /**
     * Checks if the condition is met for the given player.
     *
     * @param player The player to check the condition against
     * @return true if the condition is met, false otherwise
     */
    boolean check(Player player);
}
