package me.artaphy.axiumMenu.events.conditions;

import me.artaphy.axiumMenu.events.Condition;
import me.artaphy.axiumMenu.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory class for creating and managing different types of conditions.
 */
public class ConditionFactory {

    private static final Pattern COMPOSITE_PATTERN = Pattern.compile("(all|any)\\s*\\[(.+)]");

    /**
     * Creates a condition based on the given condition string.
     *
     * @param conditionString The string representation of the condition
     * @return A Condition object, or null if the condition type is not recognized
     */
    public static Condition createCondition(String conditionString) {
        Matcher matcher = COMPOSITE_PATTERN.matcher(conditionString);
        if (matcher.matches()) {
            return createCompositeCondition(matcher.group(1), matcher.group(2));
        }

        String[] parts = conditionString.split("\\s+", 2);
        String type = parts[0].toLowerCase();
        String value = parts.length > 1 ? parts[1] : "";

        return switch (type) {
            case "permission", "perm" -> new PermissionCondition(value);
            case "placeholder", "papi" -> new PlaceholderCondition(value);
            case "check" -> new ExpressionCondition(value);
            default -> {
                Logger.warn("Unknown condition type: " + type);
                yield null;
            }
        };
    }

    /**
     * Creates a composite condition (ALL or ANY) from a list of sub-conditions.
     *
     * @param type The type of composite condition (all or any)
     * @param value The string containing sub-conditions
     * @return A CompositeCondition object
     */
    private static Condition createCompositeCondition(String type, String value) {
        List<Condition> conditions = new ArrayList<>();
        String[] subConditions = value.split(";");
        for (String subCondition : subConditions) {
            Condition condition = createCondition(subCondition.trim());
            if (condition != null) {
                conditions.add(condition);
            }
        }
        return new CompositeCondition(conditions, type.equalsIgnoreCase("all"));
    }
}
