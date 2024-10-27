package me.artaphy.axiumMenu.menu;

import org.bukkit.event.inventory.InventoryType;

/**
 * Enumeration of supported menu types in the AxiumMenu plugin.
 * Maps custom menu types to Bukkit's InventoryType.
 * <p>
 * Features:
 * - Support for all vanilla inventory types
 * - Fallback to CHEST type for unrecognized values
 * - Case-insensitive type matching
 * - Flexible naming convention support
 */
public enum MenuType {
    CHEST(InventoryType.CHEST),
    DISPENSER(InventoryType.DISPENSER),
    DROPPER(InventoryType.DROPPER),
    FURNACE(InventoryType.FURNACE),
    WORKBENCH(InventoryType.WORKBENCH),
    CRAFTING(InventoryType.CRAFTING),
    ENCHANTING(InventoryType.ENCHANTING),
    BREWING(InventoryType.BREWING),
    PLAYER(InventoryType.PLAYER),
    CREATIVE(InventoryType.CREATIVE),
    MERCHANT(InventoryType.MERCHANT),
    ENDER_CHEST(InventoryType.ENDER_CHEST),
    ANVIL(InventoryType.ANVIL),
    SMITHING(InventoryType.SMITHING),
    BEACON(InventoryType.BEACON),
    HOPPER(InventoryType.HOPPER),
    SHULKER_BOX(InventoryType.SHULKER_BOX),
    BARREL(InventoryType.BARREL),
    BLAST_FURNACE(InventoryType.BLAST_FURNACE),
    LECTERN(InventoryType.LECTERN),
    SMOKER(InventoryType.SMOKER),
    LOOM(InventoryType.LOOM),
    CARTOGRAPHY(InventoryType.CARTOGRAPHY),
    GRINDSTONE(InventoryType.GRINDSTONE),
    STONECUTTER(InventoryType.STONECUTTER),
    COMPOSTER(InventoryType.COMPOSTER),
    CHISELED_BOOKSHELF(InventoryType.CHISELED_BOOKSHELF);

    private final InventoryType bukkitType;

    MenuType(InventoryType bukkitType) {
        this.bukkitType = bukkitType;
    }

    public InventoryType getBukkitType() {
        return bukkitType;
    }

    public static MenuType fromString(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try to find a close match
            for (MenuType menuType : values()) {
                if (menuType.name().replace("_", "").equalsIgnoreCase(type.replace("_", "").replace(" ", ""))) {
                    return menuType;
                }
            }
            return CHEST; // Default to CHEST if not recognized
        }
    }
}
