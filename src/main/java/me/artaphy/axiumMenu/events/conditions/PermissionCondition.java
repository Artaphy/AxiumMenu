package me.artaphy.axiumMenu.events.conditions;

import me.artaphy.axiumMenu.events.Condition;
import org.bukkit.entity.Player;

/**
 * A condition that checks if a player has a specific permission.
 */
public class PermissionCondition implements Condition {
    private final String permission;

    public PermissionCondition(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean check(Player player) {
        return player.hasPermission(permission);
    }
}
