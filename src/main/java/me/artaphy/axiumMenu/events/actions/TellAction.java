package me.artaphy.axiumMenu.events.actions;

import me.artaphy.axiumMenu.events.Action;
import me.artaphy.axiumMenu.utils.ColorUtils;
import org.bukkit.entity.Player;

/**
 * Represents an action that sends a message to a player.
 */
public class TellAction implements Action {
    private final String message;

    public TellAction(String message) {
        this.message = message;
    }

    @Override
    public boolean execute(Player player) {
        player.sendMessage(ColorUtils.colorize(message));
        return true;
    }
}
