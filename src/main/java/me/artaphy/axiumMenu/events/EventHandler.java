package me.artaphy.axiumMenu.events;

import java.util.List;

import org.bukkit.entity.Player;

/**
 * Handles the execution of menu events and their associated actions.
 * This class manages:
 * - Condition evaluation
 * - Action execution
 * - Deny action handling
 * <p>
 * Features:
 * - Sequential action execution
 * - Conditional branching
 * - Error handling
 * - Execution flow control
 */
public class EventHandler {
    private final Condition condition;
    private final List<Action> actions;
    private final List<Action> denyActions;

    /**
     * Constructs a new EventHandler.
     *
     * @param condition   The condition to check before executing actions
     * @param actions     The list of actions to execute if the condition is met
     * @param denyActions The list of actions to execute if the condition is not met
     */
    public EventHandler(Condition condition, List<Action> actions, List<Action> denyActions) {
        this.condition = condition;
        this.actions = actions;
        this.denyActions = denyActions;
    }

    /**
     * Handles the event by checking the condition and executing appropriate actions.
     *
     * @param event The MenuEvent to handle
     * @return true if handling should continue, false if it should stop
     */
    public boolean handle(MenuEvent event) {
        Player player = event.player();
        if (condition == null || condition.check(player)) {
            return executeActions(actions, player);
        } else {
            return executeActions(denyActions, player);
        }
    }

    private boolean executeActions(List<Action> actionList, Player player) {
        for (Action action : actionList) {
            if (!action.execute(player)) {
                return false; // Stop execution if an action returns false
            }
        }
        return true;
    }
}
