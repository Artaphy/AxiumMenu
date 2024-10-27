package me.artaphy.axiumMenu.events.actions;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.events.Action;
import me.artaphy.axiumMenu.exceptions.MenuNotFoundException;
import me.artaphy.axiumMenu.menu.Menu;
import me.artaphy.axiumMenu.utils.Logger;
import org.bukkit.entity.Player;

/**
 * Represents an action that opens another menu for the player.
 */
public class OpenMenuAction implements Action {
    private final String menuName;

    public OpenMenuAction(String menuName) {
        this.menuName = menuName;
    }

    @Override
    public boolean execute(Player player) {
        try {
            Menu menu = AxiumMenu.getInstance().getMenuManager().getMenu(menuName);
            menu.open(player);
            return true;
        } catch (MenuNotFoundException e) {
            Logger.warn("Attempted to open non-existent menu: " + menuName);
            return false;
        }
    }
}
