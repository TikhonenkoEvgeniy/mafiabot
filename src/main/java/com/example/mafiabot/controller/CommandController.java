package com.example.mafiabot.controller;

import com.example.mafiabot.util.Keyboards;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Component
public class CommandController {

    public SendMessage send(Update update) {
        switch (update.getMessage().getText().toLowerCase()) {
            case "/start": return start(update);
            case "/help": return help(update);
            case "/stop": return stop(update);
            default: return error(update);
        }
    }

    private SendMessage start(Update update) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Greeting")
                .replyMarkup(Keyboards.getKeyboard(update))
                .build();
    }

    private SendMessage stop(Update update) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .replyMarkup(new ReplyKeyboardRemove())
                .text("От мафии просто так не уходят")
                .build();
    }

    private SendMessage help(Update update) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Текст помощи")
                .build();
    }

    private SendMessage error(Update update) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Давай по новой, " + update.getMessage().getFrom().getFirstName() + ", все хуйня. " +
                        "Не понимаю, что ты имел ввиду под словом " +
                        update.getMessage().getText().replaceFirst("/", "") + ".")
                .build();
    }
}
