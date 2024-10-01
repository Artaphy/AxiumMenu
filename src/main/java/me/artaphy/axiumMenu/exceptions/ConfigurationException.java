package me.artaphy.axiumMenu.exceptions;

public class ConfigurationException extends Exception {
    @SuppressWarnings("unused")
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}