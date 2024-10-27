package me.artaphy.axiumMenu.events.conditions;

import me.artaphy.axiumMenu.events.Condition;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Represents a composite condition that combines multiple sub-conditions.
 */
public class CompositeCondition implements Condition {
    private final List<Condition> conditions;
    private final boolean isAll;

    /**
     * Constructs a new CompositeCondition.
     *
     * @param conditions The list of sub-conditions
     * @param isAll      True if all conditions must be met, false if any condition can be met
     */
    public CompositeCondition(List<Condition> conditions, boolean isAll) {
        this.conditions = conditions;
        this.isAll = isAll;
    }

    @Override
    public boolean check(Player player) {
        if (isAll) {
            return conditions.stream().allMatch(condition -> condition.check(player));
        } else {
            return conditions.stream().anyMatch(condition -> condition.check(player));
        }
    }
}
