package me.artaphy.axiumMenu.events.actions;

import me.artaphy.axiumMenu.events.Action;
import org.bukkit.entity.Player;

/**
 * Represents an action that closes the player's current inventory.
 */
public class CloseAction implements Action {
    private final boolean force;

    public CloseAction(boolean force) {
        this.force = force;
    }

    @Override
    public boolean execute(Player player) {
        player.closeInventory();
        // The 'force' parameter could be used to skip the menu's close event if needed
        // This would require additional logic in the MenuListener class
        return !force; // Return false if it's a force close to stop further actions
    }
}
