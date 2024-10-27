package me.artaphy.axiumMenu.events;

import me.artaphy.axiumMenu.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is called when a menu is about to be opened for a player.
 * This event can be cancelled to prevent the menu from opening.
 */
public class MenuOpenEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Menu menu;
    private boolean cancelled;

    /**
     * Constructs a new MenuOpenEvent.
     *
     * @param player The player for whom the menu is being opened.
     * @param menu The menu that is being opened.
     */
    public MenuOpenEvent(@NotNull Player player, @NotNull Menu menu) {
        this.player = player;
        this.menu = menu;
    }

    /**
     * Gets the player for whom the menu is being opened.
     *
     * @return The player involved in this event.
     */
    @SuppressWarnings("all")
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the menu that is being opened.
     *
     * @return The menu involved in this event.
     */
    @NotNull
    public Menu getMenu() {
        return menu;
    }

    /**
     * Checks if the event is cancelled.
     *
     * @return true if the event is cancelled, false otherwise.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancelled state of the event.
     *
     * @param cancel true to cancel the event, false to uncancel.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Gets the handler list for this event.
     * This method is required by Bukkit's event system.
     *
     * @return The handler list for this event.
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the handler list for this event.
     * This static method is required by Bukkit's event system for proper event registration.
     *
     * @return The handler list for this event.
     */
    @SuppressWarnings("all")
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}