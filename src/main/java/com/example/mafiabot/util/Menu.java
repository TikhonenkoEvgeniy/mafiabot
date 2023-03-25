package com.example.mafiabot.util;

import com.example.mafiabot.enums.Role;
import com.example.mafiabot.model.Player;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;


public class Menu {
    public final static String START_GAME = "Начать новую игру";
    public final static String REPEAT_GAME = "Повторить игру";

    public final static String MANUAL_ROLES = "Назначить роли вручную";
    public final static String AUTO_ROLES = "Распределить роли автоматически";
    public final static String BACK_PLAYER = "\uD83D\uDC48 Вернуться к предыдущему игроку";
    public final static String BACK_MENU = "\uD83D\uDC48 Вернуться назад";
    public final static String NEXT_PLAYER = "Ход следующего игрока \uD83D\uDC49";

    public final static String CHANGE_TEAM = "Изменить состав команды";
    public final static String MAKE_COMMAND = "Собрать команду заново";

    public final static String CITY_SLEEP = "\uD83D\uDE48\n\n Город засыпает, просыпается мафия";
    public final static String CITY_SUSPECT = "\uD83D\uDD75️\u200D\n\n Город подозревает (голосование)";

    public final static String ALL_CITY_SLEEP = "Весь город засыпает";
    public final static String ALL_CITY_WAKE_UP = "Просыпается весь город";

    public static String MAFIA_WAKE_UP = "Просыпается только мафия";
    public static String DON_WAKE_UP = "Просыпается Дон мафии";
    public static String MANIAC_WAKE_UP = "Просыпается маньяк";
    public static String DOCTOR_WAKE_UP = "Просыпается доктор";
    public static String WHORE_WAKE_UP = "Просыпается любовница";
    public static String COP_WAKE_UP = "Просыпается полицейский";

    public static ReplyKeyboard main() {
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(false)
                .keyboard(List.of(new KeyboardRow(List.of(new KeyboardButton(START_GAME))),
                        new KeyboardRow(List.of(new KeyboardButton(REPEAT_GAME)))))
                .build();
    }

    public static ReplyKeyboard newGame() {
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(false)
                .keyboard(List.of(new KeyboardRow(List.of(new KeyboardButton(MANUAL_ROLES))),
                        new KeyboardRow(List.of(new KeyboardButton(CHANGE_TEAM)))))
                .build();
    }

    public static ReplyKeyboard getSkipKeyboard() {
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(false)
                .keyboard(List.of(new KeyboardRow(List.of(new KeyboardButton(NEXT_PLAYER)))))
                .build();
    }

    public static ReplyKeyboard gameMenu() {
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(false)
                .keyboard(List.of(new KeyboardRow(List.of(new KeyboardButton(CITY_SLEEP))),
                        new KeyboardRow(List.of(new KeyboardButton(CITY_SUSPECT)))))
                .build();
    }

    public static ReplyKeyboard getRolesMenu() {
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(false)
                .keyboard(List.of(
                        new KeyboardRow(List.of(new KeyboardButton(Role.CIVILIAN.getName()))),
                        new KeyboardRow(List.of(new KeyboardButton(Role.MAFIA.getName()),
                                new KeyboardButton(Role.DON.getName()))),
                        new KeyboardRow(List.of(new KeyboardButton(Role.MANIAC.getName()),
                                new KeyboardButton(Role.WHORE.getName()))),
                        new KeyboardRow(List.of(new KeyboardButton(Role.DOCTOR.getName()),
                                new KeyboardButton(Role.COP.getName()))),
                        new KeyboardRow(List.of(new KeyboardButton(BACK_PLAYER)))))
                .build();
    }

    public static ReplyKeyboard getPlayers(List<Player> players) {
        List<KeyboardRow> rows = new ArrayList<>();
        List<KeyboardButton> buttons = new ArrayList<>();

        if (players.size() > 0 && players.size() <= 5) {

            for (Player p : players) {
                rows.add(new KeyboardRow(List.of(new KeyboardButton(p.getName()))));
            }
        }
        else if (players.size() > 5 && players.size() <= 10) {

            for (int i = 0; i < players.size(); i++) {
                buttons.add(new KeyboardButton(players.get(i).getName()));
                if (buttons.size() == 2 || i + 1 == players.size()) {
                    rows.add(new KeyboardRow(buttons));
                    buttons = new ArrayList<>();
                }
            }
        }
        else {

            for (int i = 0; i < players.size(); i++) {
                buttons.add(new KeyboardButton(players.get(i).getName()));
                if (buttons.size() == 3 || i + 1 == players.size()) {
                    rows.add(new KeyboardRow(buttons));
                    buttons = new ArrayList<>();
                }
            }
        }
        rows.add(new KeyboardRow(List.of(new KeyboardButton(BACK_MENU))));

        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(false)
                .keyboard(rows)
                .build();
    }

    public static ReplyKeyboard getKeyboard(Update update) {
        KeyboardButton button1 = new KeyboardButton("Новая игра");
        KeyboardButton button2 = new KeyboardButton("Повторить игру");

        KeyboardRow keyboardRow = new KeyboardRow(List.of(button1));
        keyboardRow.add(button1);
        keyboardRow.add(button2);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(List.of(keyboardRow));
        replyKeyboardMarkup.setResizeKeyboard(true);


        return replyKeyboardMarkup;
    }
}
