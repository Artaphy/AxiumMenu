package me.artaphy.axiumMenu.menu;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import me.artaphy.axiumMenu.utils.ColorUtils;

import java.util.List;
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
        Material material = Material.valueOf(config.getString("material", "STONE").toUpperCase());
        int amount = config.getInt("amount", 1);
        this.itemStack = new ItemStack(material, amount);

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            String displayName = config.getString("name");
            if (displayName != null) {
                meta.setDisplayName(ColorUtils.colorize(displayName));
            }

            List<String> lore = config.getStringList("lore");
            if (!lore.isEmpty()) {
                meta.setLore(lore.stream().map(ColorUtils::colorize).collect(Collectors.toList()));
            }

            itemStack.setItemMeta(meta);
        }

        this.action = config.getString("action", "");
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