package de.ld338.mysqlsync.commands;

import de.ld338.mysqlsync.tools.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SyncCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("system.admin")) {
            commandSender.sendMessage("§cYou do not have permission to use this command.");
            return false;
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            PlayerUtil.saveData(onlinePlayer);
        }
        return false;
    }
}
