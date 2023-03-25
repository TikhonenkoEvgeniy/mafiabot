package com.example.mafiabot.controller;

import com.example.mafiabot.enums.Role;
import com.example.mafiabot.enums.State;
import com.example.mafiabot.model.Player;
import com.example.mafiabot.service.GameService;
import com.example.mafiabot.service.PlayerService;
import com.example.mafiabot.util.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.mafiabot.Str.*;
import static com.example.mafiabot.util.Menu.*;

@Component
@RequiredArgsConstructor
public class MessageController {
    private final PlayerService playerService;
    private final GameService gameService;

    public SendMessage send(Update update) {
        return switch (update.getMessage().getText()) {
            case START_GAME -> newGame(update);
            case REPEAT_GAME -> repeatGame(update);
            case MANUAL_ROLES -> manualRoles(update);
            case CHANGE_TEAM -> changeTeam(update);
            case BACK_MENU -> backMenu(update);

            case STR_CIVILIAN -> setCivilianRole(update);
            case STR_MAFIA -> setMafiaRole(update);
            case STR_DON -> setDonRole(update);
            case STR_MANIAC -> setManiacRole(update);
            case STR_WHORE -> setWhoreRole(update);
            case STR_DOCTOR -> setDoctorRole(update);
            case STR_COP -> setCopRole(update);

            case CITY_SUSPECT -> citySuspect(update);
            case CITY_SLEEP -> citySleep(update);

            default -> notMenu(update);
        };
    }

    private SendMessage newGame(Update update) {
        playerService.deleteAllById(update.getMessage().getChatId());
        gameService.startGame(update.getMessage().getChatId());
        playerService.setCachePlayer(update.getMessage().getChatId(), null);
        playerService.setState(update.getMessage().getChatId(), State.INPUT_PLAYERS);
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("\uD83D\uDDE3 НОВАЯ ИГРА! \uD83D\uDE80 \n\n" + "Создайте свою команду игроков \uD83D\uDC6B \n\n" +
                        "Перечислите имена игроков, разделяя их знаком пробела, после чего отправьте сообщение и " +
                        "следуйте дальнейшим инструкциям игры.\n--------------------\n" +
                        "Например, Женя Оля Маша Вася Юля")
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
    }

    private SendMessage backMenu(Update update) {
        final long id = update.getMessage().getChatId();
        final String text = "Выберите дальнейшее действие \uD83D\uDC47";

        if (playerService.getState(id).equals(State.VOTE) ||
                playerService.getState(id).equals(State.WHORE_MOVE)) {

            playerService.setState(id, State.GAME);
            return SendMessage.builder()
                    .chatId(id)
                    .text(text)
                    .replyMarkup(Menu.gameMenu())
                    .build();
        }

//        if (playerService.getState(id).equals(State.WHORE_MOVE)) {
//            playerService.setState(id, State.GAME);
//
//            return SendMessage.builder()
//                    .chatId(id)
//                    .text(text)
//                    .replyMarkup(Menu.gameMenu())
//                    .build();
//        }



        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Это пока не работает \uD83D\uDC81")
                .build();
    }

    private SendMessage changeTeam(Update update) {
        playerService.setState(update.getMessage().getChatId(), State.INPUT_PLAYERS);
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Перечислите имена игроков, разделяя их знаком пробела, после чего отправьте сообщение и " +
                        "следуйте дальнейшим инструкциям игры.\n--------------------\n" +
                        "Например, Женя Оля Маша Вася Юля")
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
    }

    private SendMessage repeatGame(Update update) {
        playerService.clearAllById(update.getMessage().getChatId());
        List<Player> players = playerService.getAllAlivePlayers(update.getMessage().getChatId());

        if (players != null) {
            List<String> participants = players
                    .stream()
                    .map(Player::getName).toList();

            StringBuilder stringBuilder = new StringBuilder();

            for (String s : participants) {
                stringBuilder.append(s).append("\n");
            }

            return SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Список игроков (" + participants.size() + "): \uD83D\uDC47\n--------------------\n" +
                            stringBuilder + "--------------------\n" +
                            "если меню скрыто, то нажмите на значок виртуальной клавиатуры " +
                            "в поле ввода текста справа ↘️")
                    .replyMarkup(Menu.newGame())
                    .build();
        }

        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Игроки не найдены, возможно это ошибка игры, пожалуйста начните новую игру \uD83D\uDE4F" +
                        "\n--------------------\n")
                .replyMarkup(Menu.main())
                .build();
    }

    private SendMessage citySuspect(Update update) {
        final Message message = update.getMessage();
        playerService.setState(message.getChatId(), State.VOTE);
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Город ищет мафию \uD83E\uDD14\n\n" + "Каждый игрок должен отдать только один свой голос " +
                        "за того, кто по его мнению не является мирным жителем.\n" +
                        "Игрок с наибольшим количеством голосов покидает игру \uD83D\uDC4B\n\n" +
                        "Выберите в меню игрока против которого проголосовала команда")
                .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(message.getChatId())))
                .build();
    }

    private SendMessage citySleep(Update update) {
        final Message message = update.getMessage();
        playerService.setState(message.getChatId(), State.WHORE_MOVE);

        final String mainText = "Внимание всем игрокам:\nВесь город засыпает \uD83D\uDE34\n\n" +
                "Просыпается только любовница и показывает на того, кого она заберет к себе " +
                "и проведет с ним эту ночь \uD83E\uDD70\n\n";

        if (playerService.checkHasRole(message.getChatId(), Role.WHORE)) {

            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(mainText)
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(message.getChatId(), Role.WHORE)))
                    .build();
        }
        else {

            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(mainText + "Любовницы нет в игре\nМотай дальше ⬇️")
                    .replyMarkup(Menu.getSkipKeyboard())
                    .build();
        }
    }

    private SendMessage manualRoles(Update update) {
        Player player = playerService.getPlayerEmptyRole(update.getMessage().getChatId());
        if (player == null) {

            // todo: все роли заполнены

            return SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Все роли назначены! ✌️\n\nИгрокам необходимо познакомиться для того, чтобы понять, " +
                            "кто из игроков может представлять опасность ночью.\n\nПридумайте тему для обсуждения, " +
                            "после обсуждения город засыпает\nВы можете проголосовать за подозрительного игрока днем.")
                    .replyMarkup(Menu.gameMenu())
                    .build();

        }

        playerService.setCachePlayer(update.getMessage().getChatId(), player);

        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Выберите роль для игрока:\n" + player.getName())
                .replyMarkup(Menu.getRolesMenu())
                .build();
    }

    private SendMessage setCommand(Update update) {
        List<String> participants = Arrays.stream(update.getMessage().getText().trim().split(" "))
                .distinct().filter(p -> !p.equals("")).toList();


        StringBuilder stringBuilder = new StringBuilder();
        List<Player> players = new ArrayList<>();

        for (String s : participants) {
            Player player = new Player();
            player.setName(s);
            players.add(player);
            stringBuilder.append(s).append("\n");
        }

        playerService.savePlayers(update.getMessage().getChatId(), players);

        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Список игроков (" + players.size() + "): \uD83D\uDC47\n--------------------\n" +
                        stringBuilder + "--------------------\n" +
                        "если меню скрыто, то нажмите на значок виртуальной клавиатуры " +
                        "в поле ввода текста справа ↘️")
                .replyMarkup(Menu.newGame())
                .build();
    }

    private SendMessage cityVote(Update update) {
        final Message message = update.getMessage();
        playerService.setState(update.getMessage().getChatId(), State.GAME);

        Player player = playerService.getAllAlivePlayers(message.getChatId())
                .stream().filter(p -> p.getName().equals(message.getText())).findFirst()
                .orElseThrow(() -> new RuntimeException("Player '" + message.getText() + "' was not found"));

        StringBuilder stringBuilder = new StringBuilder("Команда выбрала игрока:\n" + player.getName() + "\n\n");

        if (player.isBlock()) {
            stringBuilder.append(player.getName())
                    .append(" провел ночь с любовницей и на этом голосовании не может быть исключен, остается в игре!");
        }
        else {
            player.setAlive(false);
            stringBuilder.append("Вы выгнали ");

            if (player.getRole().isCivilian()) {
                stringBuilder.append(" мирного жителя \uD83D\uDE14");
            }
            else {
                stringBuilder.append(" мафию \uD83D\uDE0E");
            }
        }

        playerService.resetBlockAll(message.getChatId());
        ReplyKeyboard replyKeyboard = Menu.gameMenu();

        if (!gameService.getWinner(message.getChatId()).equals("")) {
            stringBuilder.append("\n\n--------------------\n");
            stringBuilder.append(gameService.getWinner(message.getChatId()));
            replyKeyboard = Menu.main();
            playerService.setState(message.getChatId(), State.GAME);
        }

        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(stringBuilder.toString())
                .replyMarkup(replyKeyboard)
                .build();
    }

    /**
     * ================================== Установка ролей ==================================
     */

    private SendMessage setCivilianRole(Update update) {
        return setRole(Role.CIVILIAN, update);
    }

    private SendMessage setMafiaRole(Update update) {
        return setRole(Role.MAFIA, update);
    }

    private SendMessage setDonRole(Update update) {
        return setRole(Role.DON, update);
    }

    private SendMessage setDoctorRole(Update update) {
        return setRole(Role.DOCTOR, update);
    }

    private SendMessage setWhoreRole(Update update) {
        return setRole(Role.WHORE, update);
    }

    private SendMessage setCopRole(Update update) {
        return setRole(Role.COP, update);
    }

    private SendMessage setManiacRole(Update update) {
        return setRole(Role.MANIAC, update);
    }

    private SendMessage setRole(Role role, Update update) {
        Player player = playerService.getCachePlayer(update.getMessage().getChatId());
        player.setRole(role);
        playerService.updatePlayers(update.getMessage().getChatId(), player);

        playerService.setCachePlayer(update.getMessage().getChatId(),
                playerService.getPlayerEmptyRole(update.getMessage().getChatId()));

        if (playerService.getCachePlayer(update.getMessage().getChatId()) != null) {
            return SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Игрок " + player.getName() + " получил роль: \n" + role.getName() + "\n\n" +
                            "Выберите роль для игрока:\n" +
                            playerService.getCachePlayer(update.getMessage().getChatId()).getName())
                    .replyMarkup(Menu.getRolesMenu())
                    .build();
        }

        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Игрок " + player.getName() + " получил роль: \n" + role.getName() + "\n\n" +
                        "--------------------\n" +
                        "Все роли назначены! ✌️\n\nИгрокам необходимо познакомиться для того, чтобы понять, " +
                        "кто из игроков может представлять опасность ночью.\n\nПридумайте тему для обсуждения, " +
                        "после обсуждения город засыпает или вы можете проголосовать за подозрительного игрока днем.")
                .replyMarkup(Menu.gameMenu())
                .build();
    }

    /**
     * =============================== Ночные выборы игроков ===============================
     */

    private SendMessage chooseOfWhore(Update update) {

        final String text = "\n--------------------\n\n" +
                "Просыпается мафия и выбирает кто этой ночью будет убит \uD83D\uDE35\n";

        if (update.getMessage().getText().equals(NEXT_PLAYER)) {
            playerService.setState(update.getMessage().getChatId(), State.MAFIA_MOVE);
            gameService.choseWhore(update.getMessage().getChatId(), null);
            return SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Любовница пропустила свой ход" + text)
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(update.getMessage().getChatId())))
                    .build();
        }

        Player player = playerService.getPlayerByName(update.getMessage().getChatId(), update.getMessage().getText());

        if (gameService.checkChoiceOfWhore(update.getMessage().getChatId(), player)) {

            gameService.choseWhore(update.getMessage().getChatId(), player);
            playerService.setState(update.getMessage().getChatId(), State.MAFIA_MOVE);

            return SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Любовница сделала свой выбор" + text)
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(update.getMessage().getChatId())))
                    .build();
        } else {
            return SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Любовница не может выбрать этого игрока \uD83D\uDE45\u200D♂️\nНужно выбрать другого игрока")
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(
                            update.getMessage().getChatId(), Role.WHORE)))
                    .build();
        }
    }

    private SendMessage chooseOfMafia(Update update) {
        final Message message = update.getMessage();
        final String text = "Мафия сделала свой выбор\n--------------------\n\n" +
                "Просыпается Дон мафии и пытается вычислить полицейского \uD83D\uDD2E\n\n";

        Player player = playerService.getPlayerByName(message.getChatId(), message.getText());

        gameService.choseMafia(message.getChatId(), player);
        playerService.setState(message.getChatId(), State.DON_MOVE);

        if (playerService.checkHasRole(message.getChatId(), Role.DON)) {

            return SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text(text)
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(message.getChatId(), Role.DON)))
                    .build();
        }
        else {

            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(text + "Дона мафии нет в игре\nМотай дальше ⬇")
                    .replyMarkup(Menu.getSkipKeyboard())
                    .build();
        }
    }

    private SendMessage chooseOfDon(Update update) {
        final Message message = update.getMessage();
        playerService.setState(message.getChatId(), State.MANIAC_MOVE);

        if (message.getText().equals(NEXT_PLAYER)) {
            if (playerService.checkHasRole(message.getChatId(), Role.MANIAC)) {
                return SendMessage.builder()
                        .chatId(message.getChatId())
                        .text("Дон мафии пропустил свой ход\n--------------------\n\n" +
                                "Просыпается маньяк и выбирает себе ночную жертву \uD83D\uDE08\n")
                        .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(message.getChatId(), Role.MANIAC)))
                        .build();
            } else {
                return SendMessage.builder()
                        .chatId(message.getChatId())
                        .text("Дон мафии пропустил свой ход\n--------------------\n\n" +
                                "Просыпается маньяк и выбирает себе ночную жертву \uD83D\uDE08\n\n" +
                                "Маньяка в игре нет\nМотай дальше ⬇")
                        .replyMarkup(Menu.getSkipKeyboard())
                        .build();
            }
        } else {

            final Player player = playerService.getPlayerByName(message.getChatId(), message.getText());
            boolean isCop = player.getRole().equals(Role.COP);
            final String answer = isCop ? "\uD83D\uDC4D" : "\uD83D\uDC4E";

            if (playerService.checkHasRole(message.getChatId(), Role.MANIAC)) {
                return SendMessage.builder()
                        .chatId(message.getChatId())
                        .text("Дон мафии проверил игрока и получил ответ " + answer + "\n--------------------\n\n" +
                                "Просыпается маньяк и выбирает себе ночную жертву \uD83D\uDE08\n")
                        .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(message.getChatId(), Role.MANIAC)))
                        .build();
            } else {
                return SendMessage.builder()
                        .chatId(message.getChatId())
                        .text("Дон мафии проверил игрока и получил ответ " + answer + "\n--------------------\n\n" +
                                "Просыпается маньяк и выбирает себе ночную жертву \uD83D\uDE08\n\n" +
                                "Маньяка в игре нет\nМотай дальше ⬇")
                        .replyMarkup(Menu.getSkipKeyboard())
                        .build();
            }
        }
    }

    private SendMessage chooseOfManiac(Update update) {
        final Message message = update.getMessage();
        final String answer = "\n--------------------\n\n" +
                "Просыпается доктор и выбирает кого он будет лечить \uD83D\uDE91\n\n";
        playerService.setState(message.getChatId(), State.DOCTOR_MOVE);

        if (message.getText().equals(NEXT_PLAYER)) {
            return getSendMessageOfManiac(message, "Маньяк пропустил свой ход" + answer);
        }
        else {
            gameService.choseManiac(message.getChatId(),
                    playerService.getPlayerByName(message.getChatId(), message.getText()));
            return getSendMessageOfManiac(message, "Маньяк сделал свой выбор" + answer);
        }
    }

    private SendMessage getSendMessageOfManiac(Message message, String answer) {
        if (playerService.checkHasRole(message.getChatId(), Role.DOCTOR)) {
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(answer)
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(message.getChatId())))
                    .build();
        }
        else {
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(answer + "Доктора в игре нет\nМотай дальше ⬇")
                    .replyMarkup(Menu.getSkipKeyboard())
                    .build();
        }
    }

    private SendMessage chooseOfDoctor(Update update) {
        final Message message = update.getMessage();
        final String answer = "\n--------------------\n\n" +
                "Просыпается полицейский и выбирает кого проверить \uD83C\uDFAF\n\n";
        playerService.setState(message.getChatId(), State.COP_MOVE);

        if (message.getText().equals(NEXT_PLAYER)) {
            return getSendMessageDoctor(message, "Доктор пропустил свой ход" + answer);
        }

        final Player player = playerService.getPlayerByName(message.getChatId(), message.getText());

        if (gameService.checkChoiceOfDoctor(message.getChatId(), player)) {
            gameService.choseDoctor(message.getChatId(), player);
            return getSendMessageDoctor(message, "Доктор сделал свой выбор" + answer);
        }

        playerService.setState(message.getChatId(), State.DOCTOR_MOVE);
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Доктор не может выбрать данного игрока \uD83D\uDE45\u200D♂️")
                .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(message.getChatId())))
                .build();
    }

    private SendMessage getSendMessageDoctor(Message message, String answer) {
        if (playerService.checkHasRole(message.getChatId(), Role.COP)) {
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(answer)
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(message.getChatId(), Role.COP)))
                    .build();
        }
        else {
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(answer + "Полицейского в игре нет\nМотай дальше ⬇")
                    .replyMarkup(Menu.getSkipKeyboard())
                    .build();
        }
    }

    private SendMessage chooseOfCop(Update update) {
        final Message message = update.getMessage();
        final String winnerText = gameService.getWinner(message.getChatId());
        String text = "\n--------------------\n\n" +
                "Просыпается весь город и получает сводку новостей за прошлую ночь:\n\n" +
                gameService.getResultGame(message.getChatId());

        if (!winnerText.equals("")) {
            text = text + "\n\n" + winnerText;
        }

        playerService.setState(message.getChatId(), State.GAME);

        final ReplyKeyboard replyKeyboard = winnerText.equals("") ? Menu.gameMenu() : Menu.main();

        if (message.getText().equals(NEXT_PLAYER)) {
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("Полицейский пропустил свой ход" + text)
                    .replyMarkup(replyKeyboard)
                    .build();
        }
        else {



            final String answer = playerService.checkRoleIsBlockedByWhore(message.getChatId(), Role.COP) ?
                            "\uD83D\uDE45\u200D♂️" :
                    playerService.checkPlayerByCop(message.getChatId(),
                            playerService.getPlayerByName(message.getChatId(), message.getText())) ?
                            "\uD83D\uDC4D" : "\uD83D\uDC4E";

            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("Полицейский получил ответ: " + answer + text)
                    .replyMarkup(replyKeyboard)
                    .build();
        }
    }

    /**
     * ======================================================================================
     */

    private SendMessage notMenu(Update update) {
        return switch (playerService.getState(update.getMessage().getChatId())) {
            case GAME -> new SendMessage();
            case INPUT_PLAYERS -> setCommand(update);
            case VOTE -> cityVote(update);
            case WHORE_MOVE -> chooseOfWhore(update);
            case MAFIA_MOVE -> chooseOfMafia(update);
            case DON_MOVE -> chooseOfDon(update);
            case MANIAC_MOVE -> chooseOfManiac(update);
            case DOCTOR_MOVE -> chooseOfDoctor(update);
            case COP_MOVE -> chooseOfCop(update);
        };
    }
}
