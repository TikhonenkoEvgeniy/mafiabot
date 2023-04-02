package com.example.mafiabot.controller;

import com.example.mafiabot.service.PlayerService;
import com.example.mafiabot.util.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Component
public class CommandController {
    private final PlayerService playerService;
    public static final String START = "/start";
    public static final String STOP = "/stop";
    public static final String HELP = "/help";

    public CommandController(@Autowired PlayerService playerService) {
        this.playerService = playerService;
    }

    public SendMessage send(Update update) {
        return switch (update.getMessage().getText().toLowerCase()) {
            case START -> start(update);
            case HELP -> help(update);
            case STOP -> stop(update);
            default -> error(update);
        };
    }

    private SendMessage start(Update update) {
        playerService.clearAllById(update.getMessage().getChatId());
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Приветствую тебя, " + update.getMessage().getFrom().getFirstName() + "\n\n" +
                        "Игра в классическую мафию, для которой тебе потребуется собрать команду из нескольких " +
                        "игроков.\nДля старта игры необходимо выбрать 'Начать новую игру' в меню или 'Повторить " +
                        "игру', если хотите еще раз сыграть с теми же игроками.\n\n--------------------\n" +
                        "Если бот заглючит, то выберите команду /start")
                .replyMarkup(Menu.main())
                .build();
    }

    private SendMessage stop(Update update) {
        playerService.clearAllById(update.getMessage().getChatId());
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .replyMarkup(new ReplyKeyboardRemove(true))
                .text("От мафии просто так не уходят \uD83D\uDE0E\nЖдем тебя снова за игрой \uD83D\uDD2B")
                .build();
    }

    private SendMessage help(Update update) {
        final String infoText = "Очередность действий ночью:\n\n" +
                "1. «Любовница» выбирает одного из игроков, кого она забирает к себе на ночь. Замораживает способность выбранного игрока и дает имунитет на дневном голосовании. Если был выбран маньяк, то любовница погибает \uD83D\uDC80\n\n" +
                "2. «Мафия» выбирают себе жертву на ночную казнь и производят ее (если в игре осталась одна мафия и любовница выбрала этого игрока, то ведущий игнорирует выбор мафии)\n\n" +
                "3. «Дон» выбирает одного из игроков и проверяет его (если действия персонажа заморожены ему дают знак \uD83D\uDE45\u200D♂️, если Дон угадывает детектива, то \uD83D\uDC4D\n\n" +
                "4. «Маньяк» выбирает одного из игроков и производит ночную казнь (если любовница выбрала маньяка ведущий игнорирует выбор маньяка)\n\n" +
                "5. «Детектив» выбирает одного из подозрительных игроков и проверяет его (если роль детектива заморожена, то дают знак \uD83D\uDE45\u200D♂️, если детектив указал на мафию, то знак \uD83D\uDC4E а если на мирного, то \uD83D\uDC4D)\n\n" +
                "6. «Доктор» выбирает одного из игроков, кого, по его мнению, он хочет спасти (если любовница выбрала доктора, то ведущий игнорирует его выбор) в случае если любовница выбирает на ночь доктора, мафия или маньяк убивают ночью любовницу, а доктор лечит любовницу игру покидают и доктор и любовница, так как способность доктора к ночному лечению заморожена.\n\n" +
                "7. Если мафия убивает маньяка, то выбор маньяка игнорируется ведущим, даже если маньяк впоследствии был спасен доктором.\n\n-----------------------------------------\n\n" +
                "Желаем вам приятной игры \uD83D\uDE42\n" +
                "По багам писать сюда:\n@etikhonenko";

        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(infoText)
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
