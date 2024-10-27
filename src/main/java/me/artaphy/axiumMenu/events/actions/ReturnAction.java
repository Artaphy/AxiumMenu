package me.artaphy.axiumMenu.events.actions;

import me.artaphy.axiumMenu.events.Action;
import org.bukkit.entity.Player;

/**
 * Represents an action that stops the execution of further actions.
 */
public class ReturnAction implements Action {
    @Override
    public boolean execute(Player player) {
        return false; // Always return false to stop further action execution
    }
}
