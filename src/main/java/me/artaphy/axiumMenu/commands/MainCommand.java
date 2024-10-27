package me.artaphy.axiumMenu.commands;

import me.artaphy.axiumMenu.AxiumMenu;
import me.artaphy.axiumMenu.exceptions.MenuLoadException;
import me.artaphy.axiumMenu.menu.Menu;
import me.artaphy.axiumMenu.utils.ColorUtils;
import me.artaphy.axiumMenu.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the main command execution for the AxiumMenu plugin.
 * Provides the following subcommands:
 * - open <menu>: Opens a specified menu
 * - reload: Reloads all menus and configurations
 * - list: Lists all available menus
 * - serve: Toggles auto-reload mode
 * <p>
 * Features:
 * - Tab completion for all subcommands
 * - Permission checking
 * - Error handling and user feedback
 * - Debug mode support
 */
public class MainCommand implements CommandExecutor, TabCompleter {

    private final AxiumMenu plugin;

    /**
     * Constructs a new MainCommand instance.
     *
     * @param plugin The main plugin instance.
     */
    public MainCommand(AxiumMenu plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the command, handling various subcommands.
     *
     * @param sender  The command sender.
     * @param command The command being executed.
     * @param label   The alias of the command used.
     * @param args    The arguments provided with the command.
     * @return true if the command was processed, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open" -> {
                if (args.length < 2) {
                    sender.sendMessage(plugin.getConfigManager().getLanguageString("command.error.usage_open"));
                    return true;
                }
                openMenu(sender, args[1]);
            }
            case "reload" -> reloadPlugin(sender);
            case "list" -> listMenus(sender);
            case "serve" -> toggleServeMode(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    /**
     * Sends the help message to the command sender.
     *
     * @param sender The command sender to receive the help message.
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.help.title"));
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.help.open"));
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.help.reload"));
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.help.list"));
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.help.serve"));
    }

    /**
     * Opens a specified menu for a player.
     *
     * @param sender   The command sender (must be a player).
     * @param menuName The name of the menu to open.
     */
    private void openMenu(CommandSender sender, String menuName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getLanguageString("command.error.player_only"));
            return;
        }

        if (plugin.getConfigManager().isDebugMode()) {
            Logger.debug("Attempting to open menu '" + menuName + "' for player " + player.getName());
        }

        plugin.getMenuManager().getMenuAsync(menuName)
            .thenAccept(menu -> Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    menu.open(player);
                    sender.sendMessage(plugin.getConfigManager().getLanguageString("command.success.menu_opened")
                        .replace("{title}", ColorUtils.colorize(menu.getTitle())));
                    if (plugin.getConfigManager().isDebugMode()) {
                        Logger.debug("Successfully opened menu '" + menuName + "' for player " + player.getName());
                    }
                } catch (Exception e) {
                    sender.sendMessage(plugin.getConfigManager().getLanguageString("command.error.menu_open_failed"));
                    Logger.error("Error opening menu: " + menuName, e);
                }
            }))
            .exceptionally(e -> {
                sender.sendMessage(plugin.getConfigManager().getLanguageString("command.error.menu_not_found")
                    .replace("%menu%", menuName));
                if (plugin.getConfigManager().isDebugMode()) {
                    Logger.debug("Failed to find menu '" + menuName + "' for player " + player.getName());
                }
                return null;
            });
    }

    /**
     * Reloads the plugin configuration and menus.
     *
     * @param sender The command sender to receive the reload confirmation.
     */
    private void reloadPlugin(CommandSender sender) {
        plugin.getConfigManager().loadConfigs();
        try {
            plugin.getMenuManager().loadMenus();
            sender.sendMessage(plugin.getConfigManager().getLanguageString("command.success.config_reloaded"));
        } catch (MenuLoadException e) {
            sender.sendMessage(plugin.getConfigManager().getLanguageString("command.error.menu_reload_failed"));
            Logger.error("Failed to reload menus", e);
        }
    }

    /**
     * Lists all available menus to the command sender.
     *
     * @param sender The command sender to receive the menu list.
     */
    private void listMenus(CommandSender sender) {
        Map<String, Menu> menus = plugin.getMenuManager().getAllMenus();
        sender.sendMessage(plugin.getConfigManager().getLanguageString("command.list.title"));
        
        if (menus.isEmpty()) {
            sender.sendMessage(plugin.getConfigManager().getLanguageString("command.list.empty"));
        } else {
            for (Menu menu : menus.values()) {
                String message = plugin.getConfigManager().getLanguageString("command.list.format")
                                       .replace("{name}", menu.getName())
                                       .replace("{title}", ColorUtils.colorize(menu.getTitle()));
                sender.sendMessage(message);
            }
        }
    }

    /**
     * Toggles the serve mode (auto-reload) for menu files.
     *
     * @param sender The command sender to receive the toggle confirmation.
     */
    private void toggleServeMode(CommandSender sender) {
        boolean newState = plugin.getConfigManager().toggleServeMode();
        String message = newState ? 
            plugin.getConfigManager().getLanguageString("command.serve.enabled") :
            plugin.getConfigManager().getLanguageString("command.serve.disabled");
        sender.sendMessage(message);

        // 如果启用了serve模式,启动文件监听器;如果禁用了,停止文件监听器
        if (newState) {
            plugin.startFileWatcher();
        } else {
            plugin.stopFileWatcher();
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> subCommands = Arrays.asList("open", "reload", "list", "serve");
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(sc -> sc.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
            return plugin.getMenuManager().getAllMenus().keySet().stream()
                    .filter(menu -> menu.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
