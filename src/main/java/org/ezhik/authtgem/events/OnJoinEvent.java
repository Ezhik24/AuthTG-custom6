package org.ezhik.authtgem.events;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.ezhik.authtgem.AuthTGEM;
import org.ezhik.authtgem.User;
import org.geysermc.api.Geyser;
import org.geysermc.api.GeyserApiBase;

import java.io.File;

public class OnJoinEvent implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        File file = new File("plugins/Minetelegram/users/" + p.getUniqueId() + ".yml");
        YamlConfiguration userconfig = YamlConfiguration.loadConfiguration(file);
        User user;
        GeyserApiBase api;
        try {
            api = Geyser.api();
        } catch (Error error) {
            api = null;
            System.out.println("[AuthTG] Please, download GeyserMC and floodgate | Пожалуйста,загрузить GeyserMC и floodgate");
       }
        if (userconfig.getString("ipAddress") != null && userconfig.getString("ipAddress").equals(p.getAddress().getAddress().toString())) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&b&lMT&f&l] &a&lУспешная авторизация"));
        } else {
            FreezerEvent.freezeplayer(p.getName());
            if (api != null && api.isBedrockPlayer(p.getUniqueId())) {
                user = User.getUser(p.getUniqueId());
                if (userconfig.getBoolean("active")) {
                    user.sendLoginAccepted("Это вы вошли в игру?");
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', "Потвердите вход"), "через Телеграм", 20, 1000000000, 0);
                    MuterEvent.mute(p.getName(), ChatColor.translateAlternateColorCodes('&', "&a&lПотвердите вход через телеграм"));
                } else {
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&c&lПривяжи аккаунт"), "Введите команду /start в боту", 0, 10000000, 0);
                    MuterEvent.mute(p.getName(), ChatColor.translateAlternateColorCodes('&', "&c&lПривяжи аккаунт к боту"));
                }
            } else {
                if (AuthTGEM.bot.authNecessarily) user = User.getUser(p.getUniqueId());
                else user = User.getUserJoin(p.getUniqueId());
                if (!AuthTGEM.bot.notRegAndLogin) {
                    if (user != null || userconfig.contains("password")) {
                        MuterEvent.mute(p.getName(), ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("login_message")));
                        p.sendTitle(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("login_title_login_s1")), AuthTGEM.messageMC.get("login_title_login_s2"), 20, 10000000, 0);
                    } else {
                        MuterEvent.mute(p.getName(), ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("register_message")));
                        p.sendTitle(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("register_title_s1")), AuthTGEM.messageMC.get("register_title_s2"), 20, 10000000, 0);
                    }
                    if (user != null) {
                        for (User u : user.getUnicFriends()) {
                            u.sendMessageB(AuthTGEM.messageTG.getPNFriendOnJoin(p.getPlayer()), p.getName());
                        }
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("joinplayer_tgasign")));
                    }
                }
            }
        }
    }
}
