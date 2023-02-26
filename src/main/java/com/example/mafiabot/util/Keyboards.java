package com.example.mafiabot.util;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;


public class Keyboards {

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
