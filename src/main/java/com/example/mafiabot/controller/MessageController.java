package com.example.mafiabot.controller;

import com.example.mafiabot.enums.Role;
import com.example.mafiabot.enums.State;
import com.example.mafiabot.model.Player;
import com.example.mafiabot.service.GameService;
import com.example.mafiabot.service.PlayerService;
import com.example.mafiabot.util.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.mafiabot.Str.*;
import static com.example.mafiabot.util.Menu.*;

@Component
public class MessageController {
    private final PlayerService playerService;
    private final GameService gameService;

//    private static final String rootPath = "src/main/java/com/example/mafiabot/images/";
    private static final String rootPath = "/usr/projects/mafiabot/images/";
    private static final File civilian = new File(rootPath + "civilian.png");
    private static final File civilian2 = new File(rootPath + "civilian2.png");
    private static final File cop = new File(rootPath + "cop.png");
    private static final File doctor = new File(rootPath + "doctor.png");
    private static final File don = new File(rootPath + "don.png");
    private static final File mafia = new File(rootPath + "mafia.png");
    private static final File maniac = new File(rootPath + "maniac.png");
    private static final File whore = new File(rootPath + "whore.png");
    private static final String[] arrNames = {"аня", "настя", "маша", "лена", "даша", "ира катя", "арина", "полина",
            "оля", "юля", "таня", "наташа", "вика", "лиза", "ксюша", "милана", "вероника", "ника", "алиса", "лера",
            "саша", "ульяна", "кристина", "софья", "марина", "света", "варя", "диана", "яна", "кира", "ангелина",
            "рита", "ева", "алена", "дарина", "карина", "василиса", "олеся", "оксана", "надя", "женя", "веря",
            "галя", "люда", "валя", "нина", "альбина", "люба", "лариса", "инна", "эльвира", "эля", "наташа",
            "анжела", "влада", "майя", "тома", "алия", "мадина", "снежана", "жана"};

    private static final String textAllRolesDone = "Все роли назначены! ✌️\n\nИгрокам необходимо познакомиться для " +
            "того, чтобы понять, кто из игроков может представлять опасность ночью.\n\nПридумайте тему для " +
            "обсуждения, после обсуждения город засыпает\nВы можете проголосовать за подозрительного игрока днем.";

    public MessageController(@Autowired PlayerService playerService,
                             @Autowired GameService gameService) {
        this.playerService = playerService;
        this.gameService = gameService;
    }

    public Object send(Update update) {
        return switch (update.getMessage().getText()) {
            case START_GAME -> newGame(update);
            case REPEAT_GAME -> repeatGame(update);
            case MANUAL_ROLES -> manualRoles(update);
            case AUTO_ROLES -> autoRoles(update);
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

    private Object newGame(Update update) {
        final Long chatId = update.getMessage().getChatId();

        playerService.deleteAllById(chatId);
        playerService.setCachePlayer(chatId, null);
        playerService.setState(chatId, State.INPUT_PLAYERS);
        gameService.startGame(chatId);

        return SendMessage.builder()
                .chatId(chatId)
                .text("\uD83D\uDDE3 НОВАЯ ИГРА! \uD83D\uDE80 \n\n" + "Создайте свою команду игроков \uD83D\uDC6B \n\n" +
                        "Перечислите имена игроков, разделяя их знаком пробела, после чего отправьте сообщение и " +
                        "следуйте дальнейшим инструкциям игры.\n--------------------\n" +
                        "Например, Женя Оля Маша Вася Юля")
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
    }

    private Object backMenu(Update update) {
        final Long chatId = update.getMessage().getChatId();
        final String text = "Выберите дальнейшее действие \uD83D\uDC47";

        if (playerService.getState(chatId).equals(State.VOTE) ||
                playerService.getState(chatId).equals(State.WHORE_MOVE)) {

            playerService.setState(chatId, State.GAME);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(Menu.gameMenu())
                    .build();
        }

        if (playerService.getState(chatId).equals(State.SHOW_ROLES)) {
            playerService.setState(chatId, State.GAME);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(Menu.gameMenu())
                    .build();
        }

        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Это пока не работает \uD83D\uDC81")
                .build();
    }

    private Object changeTeam(Update update) {
        final Long chatId = update.getMessage().getChatId();
        playerService.setState(chatId, State.INPUT_PLAYERS);

        return SendMessage.builder()
                .chatId(chatId)
                .text("Перечислите имена игроков, разделяя их знаком пробела, после чего отправьте сообщение и " +
                        "следуйте дальнейшим инструкциям игры.\n--------------------\n" +
                        "Например, Женя Оля Маша Вася Юля")
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
    }

    private Object repeatGame(Update update) {
        final Long chatId = update.getMessage().getChatId();

        playerService.clearAllById(chatId);
        gameService.startGame(chatId);

        List<Player> players = playerService.getAllAlivePlayers(chatId);

        if (players != null) {
            List<String> participants = players
                    .stream()
                    .map(Player::getName).toList();

            StringBuilder stringBuilder = new StringBuilder();

            for (String s : participants) {
                stringBuilder.append(s).append("\n");
            }

            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Список игроков (" + participants.size() + "): \uD83D\uDC47\n--------------------\n" +
                            stringBuilder + "--------------------\n" +
                            "если меню скрыто, то нажмите на значок виртуальной клавиатуры " +
                            "в поле ввода текста справа ↘️")
                    .replyMarkup(Menu.newGame())
                    .build();
        }

        return SendMessage.builder()
                .chatId(chatId)
                .text("Игроки не найдены, возможно это ошибка игры, пожалуйста начните новую игру \uD83D\uDE4F" +
                        "\n--------------------\n")
                .replyMarkup(Menu.main())
                .build();
    }

    private Object citySuspect(Update update) {
        final Long chatId = update.getMessage().getChatId();
        playerService.setState(chatId, State.VOTE);

        return SendMessage.builder()
                .chatId(chatId)
                .text("Город ищет мафию \uD83E\uDD14\n\n" + "Каждый игрок должен отдать только один свой голос " +
                        "за того, кто по его мнению не является мирным жителем.\n" +
                        "Игрок с наибольшим количеством голосов покидает игру \uD83D\uDC4B\n\n" +
                        "Выберите в меню игрока против которого проголосовала команда")
                .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(chatId)))
                .build();
    }

    private Object citySleep(Update update) {
        final Long chatId = update.getMessage().getChatId();
        playerService.setState(chatId, State.WHORE_MOVE);

        final String mainText = "Внимание всем игрокам:\nВесь город засыпает \uD83D\uDE34\n\n" +
                "Просыпается только любовница и показывает на того, кого она заберет к себе " +
                "и проведет с ним эту ночь \uD83E\uDD70\n\n";

        if (playerService.checkHasRole(chatId, Role.WHORE)) {

            return SendMessage.builder()
                    .chatId(chatId)
                    .text(mainText)
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(chatId, Role.WHORE)))
                    .build();
        }
        else {

            return SendMessage.builder()
                    .chatId(chatId)
                    .text(mainText + "Любовницы нет в игре\nМотай дальше ⬇️")
                    .replyMarkup(Menu.getSkipKeyboard())
                    .build();
        }
    }

    private Object autoRoles(Update update) {
        final Long chatId = update.getMessage().getChatId();

        if (playerService.setRolesAuto(chatId)) {
            playerService.setState(chatId, State.SHOW_ROLES);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Все роли назначены! ✌️\n\n" +
                            "Все игроки засыпают, ведущий подходит к каждому игроку и показывает ему его роль\n\n" +
                            "Для того чтобы получить карточку игрока нажмите на его имя на клавиатуре внизу\n" +
                            "После того, как роли будут всем показаны нажмите на:\n" + BACK_MENU)
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(chatId)))
                    .build();
        }

        return SendMessage.builder()
                .chatId(chatId)
                .text("Не удалось назначить роли автоматически \uD83D\uDE33")
                .replyMarkup(Menu.newGame())
                .build();
    }

    private Object showRole(Update update) {
        final Long chatId = update.getMessage().getChatId();
        final String chatText = update.getMessage().getText();
        final Player player = playerService.getPlayerByName(chatId, chatText);
        File file;

        if (player.getRole().equals(Role.DON)) {
            file = don;
        } else if (player.getRole().equals(Role.MAFIA)) {
            file = mafia;
        } else if (player.getRole().equals(Role.MANIAC)) {
            file = maniac;
        } else if (player.getRole().equals(Role.WHORE)) {
            file = whore;
        } else if (player.getRole().equals(Role.DOCTOR)) {
            file = doctor;
        } else if (player.getRole().equals(Role.COP)) {
            file = cop;
        } else {
            String playerName = player.getName();
            if (Arrays.stream(arrNames).anyMatch(p -> p.equals(playerName.toLowerCase()))) {
                file = civilian;
            }
            else {
                file = civilian2;
            }
        }

        return SendPhoto.builder()
                .chatId(chatId)
                .caption("Игрок " + player.getName() + ":\n" + player.getRole().getName())
                .photo(new InputFile(file))
                .build();
    }

    private Object manualRoles(Update update) {
        final Long chatId = update.getMessage().getChatId();
        Player player = playerService.getPlayerEmptyRole(chatId);

        if (player == null) {
            playerService.setState(chatId, State.GAME);

            return SendMessage.builder()
                    .chatId(chatId)
                    .text(textAllRolesDone)
                    .replyMarkup(Menu.gameMenu())
                    .build();
        }

        playerService.setCachePlayer(chatId, player);

        return SendMessage.builder()
                .chatId(chatId)
                .text("Выберите роль для игрока:\n" + player.getName())
                .replyMarkup(Menu.getRolesMenu())
                .build();
    }

    private Object setCommand(Update update) {
        final Long chatId = update.getMessage().getChatId();
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

        playerService.savePlayers(chatId, players);

        return SendMessage.builder()
                .chatId(chatId)
                .text("Список игроков (" + players.size() + "): \uD83D\uDC47\n--------------------\n" +
                        stringBuilder + "--------------------\n" +
                        "если меню скрыто, то нажмите на значок виртуальной клавиатуры " +
                        "в поле ввода текста справа ↘️")
                .replyMarkup(Menu.newGame())
                .build();
    }

    private Object cityVote(Update update) {
        final Long chatId = update.getMessage().getChatId();
        final String chatText = update.getMessage().getText();

        playerService.setState(update.getMessage().getChatId(), State.GAME);

        Player player = playerService.getAllAlivePlayers(chatId)
                .stream().filter(p -> p.getName().equals(chatText)).findFirst()
                .orElseThrow(() -> new RuntimeException("Player '" + chatText + "' was not found"));

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

        playerService.resetBlockAll(chatId);
        ReplyKeyboard replyKeyboard = Menu.gameMenu();

        if (!gameService.getWinner(chatId).equals("")) {
            stringBuilder.append("\n\n--------------------\n");
            stringBuilder.append(gameService.getWinner(chatId));
            replyKeyboard = Menu.main();
            playerService.setState(chatId, State.GAME);
        }

        return SendMessage.builder()
                .chatId(chatId)
                .text(stringBuilder.toString())
                .replyMarkup(replyKeyboard)
                .build();
    }

    /**
     * ================================== Установка ролей ==================================
     */

    private Object setCivilianRole(Update update) {
        return setRole(Role.CIVILIAN, update);
    }

    private Object setMafiaRole(Update update) {
        return setRole(Role.MAFIA, update);
    }

    private Object setDonRole(Update update) {
        return setRole(Role.DON, update);
    }

    private Object setDoctorRole(Update update) {
        return setRole(Role.DOCTOR, update);
    }

    private Object setWhoreRole(Update update) {
        return setRole(Role.WHORE, update);
    }

    private Object setCopRole(Update update) {
        return setRole(Role.COP, update);
    }

    private Object setManiacRole(Update update) {
        return setRole(Role.MANIAC, update);
    }

    private Object setRole(Role role, Update update) {
        final Long chatId = update.getMessage().getChatId();
        Player player = playerService.getCachePlayer(chatId);

        player.setRole(role);
        playerService.updatePlayers(chatId, player);
        playerService.setCachePlayer(chatId, playerService.getPlayerEmptyRole(chatId));

        if (playerService.getCachePlayer(chatId) != null) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Игрок " + player.getName() + " получил роль: \n" + role.getName() + "\n\n" +
                            "Выберите роль для игрока:\n" +
                            playerService.getCachePlayer(chatId).getName())
                    .replyMarkup(Menu.getRolesMenu())
                    .build();
        }

        return SendMessage.builder()
                .chatId(chatId)
                .text("Игрок " + player.getName() + " получил роль: \n" + role.getName() + "\n\n" +
                        "--------------------\n" + textAllRolesDone)
                .replyMarkup(Menu.gameMenu())
                .build();
    }

    /**
     * =============================== Ночные выборы игроков ===============================
     */

    private Object chooseOfWhore(Update update) {
        final Long chatId = update.getMessage().getChatId();
        final String chatText = update.getMessage().getText();
        final String text = "\n--------------------\n\n" +
                "Просыпается мафия и выбирает кто этой ночью будет убит \uD83D\uDE35\n";

        if (chatText.equals(NEXT_PLAYER)) {
            playerService.setState(chatId, State.MAFIA_MOVE);
            gameService.choseWhore(chatId, null);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Любовница пропустила свой ход" + text)
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(chatId)))
                    .build();
        }

        Player player = playerService.getPlayerByName(chatId, chatText);

        if (gameService.checkChoiceOfWhore(chatId, player)) {

            gameService.choseWhore(chatId, player);
            playerService.setState(chatId, State.MAFIA_MOVE);

            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Любовница сделала свой выбор" + text)
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(chatId)))
                    .build();
        } else {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Любовница не может выбрать этого игрока \uD83D\uDE45\u200D♂️\nНужно выбрать другого игрока")
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(chatId, Role.WHORE)))
                    .build();
        }
    }

    private Object chooseOfMafia(Update update) {
        final Long chatId = update.getMessage().getChatId();
        final String text = "Мафия сделала свой выбор\n--------------------\n\n" +
                "Просыпается Дон мафии и пытается вычислить детектива \uD83D\uDD2E\n\n";

        Player player = playerService.getPlayerByName(chatId, update.getMessage().getText());

        gameService.choseMafia(chatId, player);
        playerService.setState(chatId, State.DON_MOVE);

        if (playerService.checkHasRole(chatId, Role.DON)) {

            return SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text(text)
                    .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(chatId, Role.DON)))
                    .build();
        }
        else {

            return SendMessage.builder()
                    .chatId(chatId)
                    .text(text + "Дона мафии нет в игре\nМотай дальше ⬇")
                    .replyMarkup(Menu.getSkipKeyboard())
                    .build();
        }
    }

    private Object chooseOfDon(Update update) {
        final Long chatId = update.getMessage().getChatId();
        final String chatText = update.getMessage().getText();
        playerService.setState(chatId, State.MANIAC_MOVE);

        if (chatText.equals(NEXT_PLAYER)) {
            if (playerService.checkHasRole(chatId, Role.MANIAC)) {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Дон мафии пропустил свой ход\n--------------------\n\n" +
                                "Просыпается маньяк и выбирает себе ночную жертву \uD83D\uDE08\n")
                        .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(chatId, Role.MANIAC)))
                        .build();
            } else {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Дон мафии пропустил свой ход\n--------------------\n\n" +
                                "Просыпается маньяк и выбирает себе ночную жертву \uD83D\uDE08\n\n" +
                                "Маньяка в игре нет\nМотай дальше ⬇")
                        .replyMarkup(Menu.getSkipKeyboard())
                        .build();
            }
        } else {

            final Player player = playerService.getPlayerByName(chatId, chatText);
            boolean isCop = player.getRole().equals(Role.COP);
            final String answer = isCop ? "\uD83D\uDC4D" : "\uD83D\uDC4E";

            if (playerService.checkHasRole(chatId, Role.MANIAC)) {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Дон мафии проверил игрока и получил ответ " + answer + "\n--------------------\n\n" +
                                "Просыпается маньяк и выбирает себе ночную жертву \uD83D\uDE08\n")
                        .replyMarkup(Menu.getPlayers(playerService.getAllAlivePlayers(chatId, Role.MANIAC)))
                        .build();
            } else {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Дон мафии проверил игрока и получил ответ " + answer + "\n--------------------\n\n" +
                                "Просыпается маньяк и выбирает себе ночную жертву \uD83D\uDE08\n\n" +
                                "Маньяка в игре нет\nМотай дальше ⬇")
                        .replyMarkup(Menu.getSkipKeyboard())
                        .build();
            }
        }
    }

    private Object chooseOfManiac(Update update) {
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

    private Object getSendMessageOfManiac(Message message, String answer) {
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

    private Object chooseOfDoctor(Update update) {
        final Message message = update.getMessage();
        final String answer = "\n--------------------\n\n" +
                "Просыпается детектив и выбирает кого проверить \uD83C\uDFAF\n\n";
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

    private Object getSendMessageDoctor(Message message, String answer) {
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
                    .text(answer + "Детектива в игре нет\nМотай дальше ⬇")
                    .replyMarkup(Menu.getSkipKeyboard())
                    .build();
        }
    }

    private Object chooseOfCop(Update update) {
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
                    .text("Детектив пропустил свой ход" + text)
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
                    .text("Детектив получил ответ: " + answer + text)
                    .replyMarkup(replyKeyboard)
                    .build();
        }
    }

    /**
     * ======================================================================================
     */

    private Object notMenu(Update update) {
        return switch (playerService.getState(update.getMessage().getChatId())) {
            case GAME -> new SendMessage();
            case INPUT_PLAYERS -> setCommand(update);
            case VOTE -> cityVote(update);
            case SHOW_ROLES -> showRole(update);
            case WHORE_MOVE -> chooseOfWhore(update);
            case MAFIA_MOVE -> chooseOfMafia(update);
            case DON_MOVE -> chooseOfDon(update);
            case MANIAC_MOVE -> chooseOfManiac(update);
            case DOCTOR_MOVE -> chooseOfDoctor(update);
            case COP_MOVE -> chooseOfCop(update);
        };
    }
}
