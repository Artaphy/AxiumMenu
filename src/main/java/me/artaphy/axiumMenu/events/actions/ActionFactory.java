package me.artaphy.axiumMenu.events.actions;

import me.artaphy.axiumMenu.events.Action;
import me.artaphy.axiumMenu.utils.Logger;

/**
 * Factory class for creating menu action instances.
 * This class handles the parsing and instantiation of different action types
 * based on configuration strings.
 * <p>
 * Supported action formats:
 * - tell: Send a message to the player
 * - sound: Play a sound
 * - title: Show a title
 * - actionbar: Display action bar message
 * - command: Execute player command
 * - console: Execute console command
 * - close: Close the menu
 * - open: Open another menu
 */
public class ActionFactory {

    /**
     * Creates an action based on the given action string.
     *
     * @param actionString The string representation of the action
     * @return An Action object, or null if the action type is not recognized
     */
    public static Action createAction(String actionString) {
        String[] parts = actionString.split(":", 2);
        String type = parts[0].toLowerCase().trim();
        String value = parts.length > 1 ? parts[1].trim() : "";

        return switch (type) {
            case "tell" -> new TellAction(value);
            case "sound" -> new SoundAction(value);
            case "title" -> new TitleAction(value);
            case "actionbar" -> new ActionBarAction(value);
            case "command" -> new CommandAction(value, false);
            case "console" -> new CommandAction(value, true);
            case "close" -> new CloseAction(false);
            case "force-close" -> new CloseAction(true);
            case "open" -> new OpenMenuAction(value);
            case "return" -> new ReturnAction();
            default -> {
                Logger.warn("Unknown action type: " + type);
                yield null;
            }
        };
    }
}
