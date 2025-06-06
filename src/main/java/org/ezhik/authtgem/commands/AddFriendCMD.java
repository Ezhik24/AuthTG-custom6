package org.ezhik.authtgem.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ezhik.authtgem.AuthTGEM;
import org.ezhik.authtgem.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class AddFriendCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("addfriends_wrong_command")));
        } else {
            Player player1 = (Player) commandSender;
            User user1 = User.getUser(player1.getUniqueId());
            if (user1 == null) {
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("addfriends_tg_noasign")));
            } else {
                if (user1.friends.contains(strings[0]) || user1.playername == strings[0]) {
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("friend_already_added")));
                } else {
                    InlineKeyboardMarkup keyb = new InlineKeyboardMarkup();
                    List<InlineKeyboardButton> colkeyb = new ArrayList<>();
                    InlineKeyboardButton yesbtn = new InlineKeyboardButton();
                    InlineKeyboardButton nobtn = new InlineKeyboardButton();
                    yesbtn.setText(AuthTGEM.messageTG.get("addfriends_yes"));
                    yesbtn.setCallbackData("addfrys" + commandSender.getName());
                    nobtn.setText(AuthTGEM.messageTG.get("addfriends_no"));
                    nobtn.setCallbackData("addfrno" + commandSender.getName());
                    colkeyb.add(yesbtn);
                    colkeyb.add(nobtn);
                    List<List<InlineKeyboardButton>> rowkeyb = new ArrayList<>();
                    rowkeyb.add(colkeyb);
                    keyb.setKeyboard(rowkeyb);
                    SendMessage sendMessage = new SendMessage();

                    User user = User.getUser(strings[0]);
                    if (user != null) {
                        sendMessage.setChatId(user.chatid);
                        sendMessage.setText(AuthTGEM.messageTG.getAddFriendsReq(commandSender));
                        sendMessage.setReplyMarkup(keyb);
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&f&l[&b&lMT&f&l] &a&lЗаявка в друзья успешно отправлена"));
                        try {
                            AuthTGEM.bot.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            System.out.println("Error sending message: " + e);
                            }
                    } else {
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', AuthTGEM.messageMC.get("friend_tg_noasign")));
                        }
                    }

            }
        }
        return true;
    }
}
