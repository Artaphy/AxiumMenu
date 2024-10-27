package me.artaphy.axiumMenu.exceptions;

/**
 * Exception thrown when attempting to access a menu that does not exist.
 * This exception is typically thrown when:
 * - A menu file is missing
 * - A menu name is invalid
 * - A menu has been removed or renamed
 */
public class MenuNotFoundException extends Exception {
    /**
     * Constructs a new MenuNotFoundException with the specified detail message.
     *
     * @param message The detail message explaining which menu was not found
     */
    public MenuNotFoundException(String message) {
        super(message);
    }
}
