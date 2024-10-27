package me.artaphy.axiumMenu.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a menu reload event in the AxiumMenu plugin.
 * The plugin fires this event when:
 * - The file watcher detects menu file changes in serve mode
 * - A player executes the reload command
 * - The plugin reloads its configuration
 * <p>
 * Other plugins can listen to this event to:
 * - Clean up resources before menu reloading
 * - Prepare data for new menu configurations
 * - Synchronize their own menu-related features
 * <p>
 * This event runs asynchronously to prevent server lag during reloading.
 */
public class MenuReloadEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates a new MenuReloadEvent.
     * The constructor marks this event as asynchronous.
     */
    public MenuReloadEvent() {
        super(true); // This event is asynchronous
    }

    /**
     * Gets the handler list for this event.
     * Bukkit requires this method for its event system.
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
     * Bukkit requires this static method for proper event registration.
     *
     * @return The handler list
     */
    @SuppressWarnings("all")
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
