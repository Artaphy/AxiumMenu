package me.artaphy.axiumMenu.exceptions;

/**
 * Exception thrown when there's an error loading a menu.
 */
public class MenuLoadException extends Exception {
    
    /**
     * Constructs a new MenuLoadException with the specified detail message.
     *
     * @param message The detail message.
     */
    public MenuLoadException(String message) {
        super(message);
    }

    /**
     * Constructs a new MenuLoadException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause The cause of the exception.
     */
    public MenuLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}