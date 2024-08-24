# MySQLSync Plugin

MySQLSync is a plugin for Minecraft servers that synchronizes player data with a MySQL database. This includes player inventory, achievements, experience, stats, and potion effects. It is designed to work seamlessly with Bukkit/Spigot/Paper servers.

## Features

- Synchronize player inventory with MySQL
- Save and load player achievements
- Manage player experience and stats
- Track and store active potion effects

## Installation

1. **Download the Plugin:**
    - Download the latest version of MySQLSync from the releases page.

2. **Install the Plugin:**
    - Place the downloaded `.jar` file into the `plugins` directory of your Minecraft server.

3. **Configure the Plugin:**
    - Restart your server to generate the configuration files.
    - Edit `config.yml` to configure your MySQL database connection details.

4. **Database Setup:**
    - Ensure that you have a MySQL database set up and accessible by your server.

## Configuration

Edit `config.yml` to set your MySQL connection details:

```yaml
host: "localhost"
port: 3306
database: "mysqlsync"
username: "root"
password: "password"
recreatetables: true
```

## Usage

- **On Player Join:**
    - Player data is automatically loaded from the database.

- **On Player Quit:**
    - Player data is automatically saved to the database.

## License

This project is licensed under the MIT License. You can find the full license text [here](LICENSE).

**Note:** You may not use this software in any way associated with Lucid, nor any projects or networks related to Lucid.

## Contact

For any issues or feature requests, please open an issue on the [GitHub repository](https://github.com/yourusername/mysqlsync).

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.