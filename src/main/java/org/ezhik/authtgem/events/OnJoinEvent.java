package org.ezhik.authtgem.events;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.ezhik.authtgem.AuthTGEM;
import org.ezhik.authtgem.BotTelegram;
import org.ezhik.authtgem.User;
import org.geysermc.api.Geyser;
import org.geysermc.api.GeyserApiBase;

import java.io.File;
import java.io.IOException;

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
        if (userconfig.getBoolean("active") && userconfig.getString("ipAddress") != null && userconfig.getString("ipAddress").equals(p.getAddress().getAddress().toString())) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&b&lMT&f&l] &a&lУспешная авторизация"));
        } else {
            FreezerEvent.freezeplayer(p.getName());
            if (api != null && api.isBedrockPlayer(p.getUniqueId())) {
                if (userconfig.getBoolean("active")) {
                    FreezerEvent.unfreezeplayer(p.getName());
                } else {
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("account_auth_nessery1_bed")), AuthTGEM.messageMC.get("account_auth_nessery2_bed"), 0, 10000000, 0);
                    String code = User.generateConfirmationCode();
                    MuterEvent.mute(p.getName(), ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.getCodeBedrock(code)));
                    BotTelegram.bedrockPlayer.put(p.getUniqueId(), code);
                    try {
                        userconfig.save(file);
                    } catch (IOException e) {
                        System.out.println("Error saving file: " + e);
                    }
                }
            } else {
                if (AuthTGEM.bot.authNecessarily) user = User.getUser(p.getUniqueId());
                else user = User.getUserJoin(p.getUniqueId());
                if (userconfig.contains("password")) {
                    MuterEvent.mute(p.getName(), ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("login_message")));
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("login_title_login_s1")), AuthTGEM.messageMC.get("login_title_login_s2"), 20, 10000000, 0);
                } else {
                    MuterEvent.mute(p.getName(), ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("register_message")));
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("register_title_s1")), AuthTGEM.messageMC.get("register_title_s2"), 20, 10000000, 0);
                }
                if (user != null) {
                    user.sendMessage(AuthTGEM.messageTG.get("user_login"));
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
