package me.artaphy.axiumMenu.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is called when menus are reloaded.
 * This event is fired asynchronously.
 */
public class MenuReloadEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructs a new MenuReloadEvent.
     */
    public MenuReloadEvent() {
        super(true); // This event is asynchronous
    }

    /**
     * Gets the handler list for this event.
     * This method is required by Bukkit's event system.
     *
     * @return The handler list
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
     * @return The handler list
     */
    @NotNull
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}