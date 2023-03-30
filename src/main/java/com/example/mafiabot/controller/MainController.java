package com.example.mafiabot.controller;

import com.example.mafiabot.config.BotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MainController extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final CommandController commandController;
    private final MessageController messageController;

    public MainController(@Autowired BotConfig botConfig,
                          @Autowired CommandController commandController,
                          @Autowired MessageController messageController) {
        this.botConfig = botConfig;
        this.commandController = commandController;
        this.messageController = messageController;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            try {
                if (update.getMessage().isCommand()) {
                    execute(commandController.send(update));
                } else {
                    Object object = messageController.send(update);
                    if (object instanceof SendMessage) {
                        execute((SendMessage) object);
                    }
                    if (object instanceof SendPhoto) {
                        execute((SendPhoto) object);
                    }
                    if (object instanceof EditMessageMedia) {
                        execute((EditMessageMedia) object);
                    }
                }
            } catch (TelegramApiException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }
}
