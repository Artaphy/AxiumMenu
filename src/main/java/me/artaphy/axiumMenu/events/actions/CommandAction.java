package me.artaphy.axiumMenu.events.actions;

import me.artaphy.axiumMenu.events.Action;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Represents an action that executes a command, either as the player or as the console.
 */
public class CommandAction implements Action {
    private final String command;
    private final boolean asConsole;

    public CommandAction(String command, boolean asConsole) {
        this.command = command;
        this.asConsole = asConsole;
    }

    @Override
    public boolean execute(Player player) {
        String processedCommand = command.replace("%player%", player.getName());
        if (asConsole) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        } else {
            player.performCommand(processedCommand);
        }
        return true;
    }
}
