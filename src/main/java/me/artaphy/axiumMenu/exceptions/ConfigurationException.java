package me.artaphy.axiumMenu.exceptions;

/**
 * Exception thrown when there is an error in the plugin configuration.
 * This exception handles issues related to:
 * - Invalid configuration format
 * - Missing required settings
 * - Invalid setting values
 * - Database configuration errors
 */
public class ConfigurationException extends Exception {
    
    /**
     * Constructs a new ConfigurationException with the specified detail message.
     *
     * @param message The detail message explaining the cause of the exception
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConfigurationException with the specified detail message and cause.
     *
     * @param message The detail message explaining the cause of the exception
     * @param cause The underlying cause of the exception
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
