package me.artaphy.axiumMenu.exceptions;

/**
 * Exception thrown when there's an error in the configuration.
 */
public class ConfigurationException extends Exception {
    
    /**
     * Constructs a new ConfigurationException with the specified detail message.
     *
     * @param message The detail message.
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConfigurationException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause The cause of the exception.
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}