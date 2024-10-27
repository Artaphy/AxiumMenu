package me.artaphy.axiumMenu.events.conditions;

import me.artaphy.axiumMenu.events.Condition;
import me.artaphy.axiumMenu.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * A condition implementation that evaluates mathematical expressions.
 * This class supports:
 * - Basic comparison operations (>, <, >=, <=, ==, !=)
 * - PlaceholderAPI integration for dynamic values
 * - Numeric value comparison
 * - Error handling for invalid expressions
 * <p>
 * Example expressions:
 * - "%player_level% > 10"
 * - "%vault_eco_balance% >= 1000"
 * - "%player_health% < 20"
 */
public class ExpressionCondition implements Condition {
    private final String expression;

    /**
     * Constructs a new ExpressionCondition with the given expression.
     *
     * @param expression The expression to evaluate
     */
    public ExpressionCondition(String expression) {
        this.expression = expression;
    }

    /**
     * Evaluates the expression for the given player.
     * This method processes PlaceholderAPI placeholders and performs
     * the comparison operation defined in the expression.
     *
     * @param player The player to evaluate the expression for
     * @return true if the expression evaluates to true, false otherwise
     */
    @Override
    public boolean check(Player player) {
        try {
            // 检查 PlaceholderAPI 是否可用
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
                Logger.warn("PlaceholderAPI not found, condition check may not work properly");
                return false;
            }

            String parsedExpression = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, expression);
            return evaluateExpression(parsedExpression);
        } catch (Exception e) {
            Logger.error("Error checking condition: " + expression, e);
            return false;
        }
    }

    /**
     * Evaluates a parsed expression string.
     * Supports basic comparison operations between numeric values.
     *
     * @param parsedExpression The expression to evaluate after placeholder replacement
     * @return true if the expression evaluates to true, false otherwise
     */
    private boolean evaluateExpression(String parsedExpression) {
        String[] parts = parsedExpression.split(" ");
        if (parts.length != 3) {
            return false;
        }

        try {
            double value1 = Double.parseDouble(parts[0]);
            String operator = parts[1];
            double value2 = Double.parseDouble(parts[2]);

            return switch (operator) {
                case ">" -> value1 > value2;
                case "<" -> value1 < value2;
                case ">=" -> value1 >= value2;
                case "<=" -> value1 <= value2;
                case "==" -> value1 == value2;
                case "!=" -> value1 != value2;
                default -> false;
            };
        } catch (NumberFormatException e) {
            Logger.error("Error parsing numbers in condition: " + parsedExpression, e);
            return false;
        }
    }
}
