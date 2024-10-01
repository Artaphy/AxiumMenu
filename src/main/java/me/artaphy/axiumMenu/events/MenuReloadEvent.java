package me.artaphy.axiumMenu.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MenuReloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public MenuReloadEvent() {
        super(true);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }
}