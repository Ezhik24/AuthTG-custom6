package org.ezhik.authtgem.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.ezhik.authtgem.Handler;

import java.io.File;
import java.io.IOException;

public class UnRegisterCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        File file = new File("plugins/Minetelegram/users/" + player.getUniqueId() + ".yml");
        YamlConfiguration userconfig = YamlConfiguration.loadConfiguration(file);
        if (userconfig.getBoolean("unregister")) {
            file.delete();
            Handler.kick(player.getName(), "Вы успешно удалили аккаунт");
            return true;
        } else {
            userconfig.set("unregister", true);
            try {
                userconfig.save(file);
            } catch (IOException e) {
                System.out.println("Error saving config file: " + e);
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&b&lMT&f&l] &c&lЕсли вы действительно хотите удалить аккаунт, введите команду ещё раз"));
            return false;
        }
    }
}
