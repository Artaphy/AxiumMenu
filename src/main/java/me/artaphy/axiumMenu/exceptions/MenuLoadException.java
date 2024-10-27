package me.artaphy.axiumMenu.exceptions;

/**
 * Exception thrown when there is an error loading a menu configuration.
 * This exception is used to indicate issues such as:
 * - Invalid menu configuration format
 * - Missing required fields
 * - Invalid action or condition definitions
 * - File access errors
 */
public class MenuLoadException extends Exception {
    
    /**
     * Constructs a new MenuLoadException with the specified detail message.
     *
     * @param message The detail message explaining the cause of the exception
     */
    public MenuLoadException(String message) {
        super(message);
    }

    /**
     * Constructs a new MenuLoadException with the specified detail message and cause.
     *
     * @param message The detail message explaining the cause of the exception
     * @param cause The underlying cause of the exception
     */
    public MenuLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
