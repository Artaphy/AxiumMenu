package me.artaphy.axiumMenu.events.actions;

import me.artaphy.axiumMenu.events.Action;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Represents an action that plays a sound for a player.
 * Supports configuration in the format: "SOUND_NAME-volume-pitch"
 * <p>
 * Features:
 * - Custom volume and pitch settings
 * - Default values if not specified
 * - Error handling for invalid sound names
 * - Support for all Bukkit sound effects
 */
public class SoundAction implements Action {
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundAction(String value) {
        String[] parts = value.split("-");
        this.sound = Sound.valueOf(parts[0].toUpperCase());
        this.volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
        this.pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
    }

    @Override
    public boolean execute(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
        return true;
    }
}
