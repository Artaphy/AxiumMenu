package me.artaphy.axiumMenu.events.conditions;

import me.artaphy.axiumMenu.events.Condition;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/**
 * A condition that checks a PlaceholderAPI placeholder.
 */
public class PlaceholderCondition implements Condition {
    private final String placeholder;

    public PlaceholderCondition(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public boolean check(Player player) {
        String result = PlaceholderAPI.setPlaceholders(player, "%" + placeholder + "%");
        return Boolean.parseBoolean(result);
    }
}
