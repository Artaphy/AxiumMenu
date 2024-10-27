package me.artaphy.axiumMenu.menu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.artaphy.axiumMenu.utils.ColorUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a mechanism for activating (opening) a menu.
 * Supports multiple activation methods:
 * - Commands
 * - Chat triggers (specific messages)
 * - Item triggers (right-clicking specific items)
 * <p>
 * Features:
 * - Multiple triggers per menu
 * - Customizable item triggers with names and lore
 * - Case-insensitive chat triggers
 * - Permission-based command triggers
 */
public class MenuActivator {

    private final ActivatorType type;
    private final List<String> commands;
    private final List<String> chatTriggers;
    private final ItemStack itemTrigger;

    /**
     * Constructs a MenuActivator from a configuration section.
     *
     * @param config The configuration section containing activator details.
     */
    public MenuActivator(ConfigurationSection config) {
        if (config.contains("command")) {
            this.type = ActivatorType.COMMAND;
            this.commands = config.isList("command") ? config.getStringList("command") : List.of(Objects.requireNonNull(config.getString("command")));
            this.chatTriggers = null;
            this.itemTrigger = null;
        } else if (config.contains("chat")) {
            this.type = ActivatorType.CHAT;
            this.chatTriggers = config.isList("chat") ? config.getStringList("chat") : List.of(Objects.requireNonNull(config.getString("chat")));
            this.commands = null;
            this.itemTrigger = null;
        } else if (config.contains("item") || config.contains("material")) {
            this.type = ActivatorType.ITEM;
            this.itemTrigger = createItemTrigger(config);
            this.commands = null;
            this.chatTriggers = null;
        } else {
            throw new IllegalArgumentException("Invalid activator configuration: " + config);
        }
    }

    /**
     * Gets the type of this activator.
     *
     * @return The ActivatorType of this activator.
     */
    public ActivatorType getType() {
        return type;
    }

    /**
     * Gets the list of command triggers for this activator.
     *
     * @return The list of command triggers, or null if this is not a command activator.
     */
    public List<String> getCommands() {
        return commands;
    }

    /**
     * Gets the list of chat triggers for this activator.
     *
     * @return The list of chat triggers, or null if this is not a chat activator.
     */
    public List<String> getChatTriggers() {
        return chatTriggers;
    }

    /**
     * Gets the item trigger for this activator.
     *
     * @return The ItemStack representing the item trigger, or null if this is not an item activator.
     */
    public ItemStack getItemTrigger() {
        return itemTrigger;
    }

    /**
     * Creates an ItemStack from a configuration section for item triggers.
     *
     * @param config The configuration section containing item details.
     * @return An ItemStack representing the item trigger.
     */
    private ItemStack createItemTrigger(ConfigurationSection config) {
        Material material;
        if (config.contains("item")) {
            ConfigurationSection itemSection = config.getConfigurationSection("item");
            assert itemSection != null;
            material = Material.matchMaterial(itemSection.getString("material", ""));
        } else {
            material = Material.matchMaterial(config.getString("material", ""));
        }
        if (material == null) {
            material = Material.COMPASS; // Default to compass if material is invalid
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = config.getString("name");
            if (name != null && !name.isEmpty()) {
                meta.setDisplayName(ColorUtils.colorize(name));
            }
            List<String> lore = config.getStringList("lore");
            if (!lore.isEmpty()) {
                meta.setLore(lore.stream().map(ColorUtils::colorize).collect(Collectors.toList()));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Enum defining the available types of menu activators.
     * Each type represents a different way to trigger menu opening.
     */
    public enum ActivatorType {
        /** Activation via command */
        COMMAND,
        /** Activation via chat message */
        CHAT,
        /** Activation via item interaction */
        ITEM
    }
}
