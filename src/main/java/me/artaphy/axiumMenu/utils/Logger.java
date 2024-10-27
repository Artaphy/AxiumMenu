package me.artaphy.axiumMenu.utils;

import me.artaphy.axiumMenu.AxiumMenu;
import org.bukkit.Bukkit;

import java.util.logging.Level;

/**
 * Custom logging utility for the AxiumMenu plugin.
 * Provides centralized logging functionality with:
 * - Different log levels (INFO, WARN, ERROR, DEBUG)
 * - Plugin name prefix
 * - Debug mode support
 * - Exception logging
 * <p>
 * This class ensures consistent logging format throughout the plugin
 * and respects the debug mode configuration.
 */
public class Logger {
    private static java.util.logging.Logger bukkitLogger;
    private static AxiumMenu plugin;

    public static void init(AxiumMenu plugin) {
        Logger.plugin = plugin;
        bukkitLogger = Bukkit.getLogger();
    }

    public static void info(String message) {
        bukkitLogger.info("[" + plugin.getName() + "] " + message);
    }

    public static void warn(String message) {
        bukkitLogger.warning("[" + plugin.getName() + "] " + message);
    }

    public static void error(String message) {
        bukkitLogger.severe("[" + plugin.getName() + "] " + message);
    }

    public static void error(String message, Throwable throwable) {
        bukkitLogger.log(Level.SEVERE, "[" + plugin.getName() + "] " + message, throwable);
    }

    public static void debug(String message) {
        if (plugin.getConfigManager().isDebugMode()) {
            bukkitLogger.info("[" + plugin.getName() + "] [DEBUG] " + message);
        }
    }
}
