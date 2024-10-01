# AxiumMenu

**AxiumMenu** is a powerful and flexible Minecraft server menu plugin, enabling server administrators to easily create and customize interactive menus, enhancing the overall player experience.

## Features

- **Multiple Configuration Formats**: Supports YAML, CONF, and HOCON.
- **Dynamic Menu Management**: Create and manage menus with ease.
- **Customizable Menu Items**: Define items with specific actions, names, and lore.
- **Sub-menu Support**: Create complex menus with sub-menus.
- **Multi-language Support**: Localize your menus with language files.
- **Rich Formatting**: Supports HEX colors, gradients, and rainbow effects.

## Installation

1. Download the latest `AxiumMenu.jar` file.
2. Place it in your server's `plugins` folder.
3. Restart the server or load the plugin via a plugin manager.

## Configuration

Upon the first run, the plugin will generate default configuration files in the `plugins/AxiumMenu/` directory:

- `settings.yml`: The main configuration file.
- `lang/`: Directory for language files.
- `menus/`: Directory for custom menu configuration files.

### Creating Menus

To create new menus, add YAML, CONF, or HOCON files to the `menus/` directory. For example, a YAML configuration might look like this:

```yaml
menu:
  title: "My Menu"
  rows: 3
  type: "chest"
  items:
    0:
      material: DIAMOND
      name: "Click Me"
      lore:
        - "This is an example item"
      action: "message:You clicked the diamond!"
```

## Commands

- `/axiummenu open <menu>`: Opens the specified menu.
- `/axiummenu reload`: Reloads the plugin configuration.
- `/axiummenu list`: Lists all available menus.

## Permissions

- `axiummenu.use`: Grants permission to use basic menu commands.
- `axiummenu.admin`: Grants permission to use admin commands, such as reload.

## Customization

AxiumMenu offers extensive customization options, including:

- **Custom Layouts**: Fully customize the number of rows and item placements.
- **Custom Actions**: Define actions for items, such as sending messages or running commands.
- **Placeholder Support**: Use placeholders for dynamic content.
- **Advanced Color Formatting**: Apply rich color formats like HEX, gradients, and more.

Visit our [Wiki](https://github.com/Artaphy/AxiumMenu/wiki) for more detailed information and examples.

## Support

For any issues or suggestions, feel free to open an issue on our [issue tracker](https://github.com/Artaphy/AxiumMenu/issues).

## Contributing

We welcome contributions! If you'd like to contribute, please feel free to submit a [pull request](https://github.com/Artaphy/AxiumMenu/pulls). For major changes, kindly open an issue first to discuss what you would like to improve.

## License

AxiumMenu is licensed under the Apache License, Version 2.0. See the [LICENSE](https://github.com/Artaphy/AxiumMenu/blob/master/LICENSE) file for more details.
