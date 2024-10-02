package me.artaphy.axiumMenu.menu;

import me.artaphy.axiumMenu.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents an item within a menu in the AxiumMenu plugin.
 * This class handles the creation and properties of individual menu items.
 */
public class MenuItem {

    private final ItemStack itemStack;
    private final String action;

    /**
     * Constructs a new MenuItem instance from a configuration section.
     *
     * @param config The configuration section containing item details.
     */
    public MenuItem(ConfigurationSection config) {
        this(config.getValues(false));
    }

    /**
     * Constructs a new MenuItem instance from a map of configuration values.
     *
     * @param config The map containing item configuration details.
     */
    public MenuItem(Map<String, Object> config) {
        // Parse material
        String materialName = Optional.ofNullable(config.get("material"))
                .map(Object::toString)
                .map(s -> s.replace("\"", ""))
                .orElse("STONE");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            Bukkit.getLogger().warning("Invalid material: " + materialName + ". Using STONE instead.");
            material = Material.STONE;
        }

        // Parse amount
        int amount = Optional.ofNullable(config.get("amount"))
                .map(Object::toString)
                .map(Integer::parseInt)
                .orElse(1);

        // Create ItemStack
        this.itemStack = new ItemStack(material, amount);

        // Set item meta
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            // Set display name
            Optional.ofNullable(config.get("name"))
                    .map(Object::toString)
                    .map(s -> s.replace("\"", ""))
                    .ifPresent(displayName -> meta.setDisplayName(ColorUtils.colorize(displayName)));

            // Set lore
            @SuppressWarnings("unchecked")
            List<String> lore = (List<String>) config.get("lore");
            if (lore != null && !lore.isEmpty()) {
                List<String> colorizedLore = lore.stream()
                        .map(s -> s.replace("\"", ""))
                        .map(ColorUtils::colorize)
                        .collect(Collectors.toList());
                meta.setLore(colorizedLore);
            }

            itemStack.setItemMeta(meta);
        }

        // Set action
        this.action = Optional.ofNullable(config.get("action"))
                .map(Object::toString)
                .map(s -> s.replace("\"", ""))
                .orElse("");
    }

    /**
     * Gets the ItemStack associated with this menu item.
     *
     * @return The ItemStack for this menu item.
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Gets the action associated with this menu item.
     *
     * @return The action string for this menu item.
     */
    public String getAction() {
        return action;
    }
}