package me.artaphy.axiumMenu.events.actions;

import me.artaphy.axiumMenu.events.Action;
import me.artaphy.axiumMenu.utils.ColorUtils;
import org.bukkit.entity.Player;

/**
 * Represents an action that displays a title to a player.
 */
public class TitleAction implements Action {
    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public TitleAction(String value) {
        String[] parts = value.split("`");
        this.title = parts[0].trim();
        this.subtitle = parts.length > 1 ? parts[1].trim() : "";
        String[] times = parts[parts.length - 1].split(" ");
        this.fadeIn = times.length > 0 ? Integer.parseInt(times[0]) : 10;
        this.stay = times.length > 1 ? Integer.parseInt(times[1]) : 70;
        this.fadeOut = times.length > 2 ? Integer.parseInt(times[2]) : 20;
    }

    @Override
    public boolean execute(Player player) {
        player.sendTitle(
            ColorUtils.colorize(title),
            ColorUtils.colorize(subtitle),
            fadeIn, stay, fadeOut
        );
        return true;
    }
}
