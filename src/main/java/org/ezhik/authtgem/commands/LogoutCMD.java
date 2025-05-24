package org.ezhik.authtgem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.ezhik.authtgem.Handler;

import java.io.File;

public class LogoutCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        File file = new File("plugins/Minetelegram/users/" + player.getUniqueId() + ".yml");
        YamlConfiguration userconfig = YamlConfiguration.loadConfiguration(file);
        userconfig.set("ipAddress", "none");
        Handler.kick(player.getName(), "Вы успешно вышли с аккаунта");
        return true;
    }
}
