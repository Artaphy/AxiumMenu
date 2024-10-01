package me.artaphy.axiumMenu.exceptions;

public class MenuLoadException extends Exception {
    public MenuLoadException(String message) {
        super(message);
    }

    public MenuLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}