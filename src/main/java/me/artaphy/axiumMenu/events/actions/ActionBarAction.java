package me.artaphy.axiumMenu.events.actions;

import me.artaphy.axiumMenu.events.Action;
import me.artaphy.axiumMenu.utils.ColorUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

/**
 * Represents an action that displays a message in the player's action bar.
 * This action supports:
 * - Color codes
 * - PlaceholderAPI placeholders
 * - Gradient and rainbow text
 * <p>
 * The message is displayed using Spigot's action bar API and remains
 * visible for a short duration.
 */
public class ActionBarAction implements Action {
    private final String message;

    public ActionBarAction(String message) {
        this.message = message;
    }

    @Override
    public boolean execute(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ColorUtils.colorize(message)));
        return true;
    }
}
