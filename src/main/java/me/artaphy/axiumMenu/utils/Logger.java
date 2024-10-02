package me.artaphy.axiumMenu.utils;

import me.artaphy.axiumMenu.AxiumMenu;
import org.bukkit.Bukkit;

import java.util.logging.Level;

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