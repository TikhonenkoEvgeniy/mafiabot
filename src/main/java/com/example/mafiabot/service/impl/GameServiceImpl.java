package com.example.mafiabot.service.impl;

import com.example.mafiabot.enums.Role;
import com.example.mafiabot.model.Game;
import com.example.mafiabot.model.Player;
import com.example.mafiabot.service.GameService;
import com.example.mafiabot.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GameServiceImpl implements GameService {
    private final PlayerService playerService;
    private Map<Long, List<Game>> gamePlay = new HashMap<>();

    public GameServiceImpl(@Autowired PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public String getWinner(Long id) {

        boolean hasMafia = playerService.getAllAlivePlayers(id).stream()
                .anyMatch(player -> !player.getRole().isCivilian());

        int numberOfCivilian = playerService.getAllAlivePlayers(id).stream()
                .filter(player -> player.getRole().isCivilian()).toList().size();

        int numberOfMafia = playerService.getAllAlivePlayers(id).stream()
                .filter(player -> !player.getRole().isCivilian()).toList().size();

        boolean hasManiac = playerService.getAllAlivePlayers(id).stream()
                .anyMatch(player -> player.getRole().equals(Role.MANIAC));

        final String civilianWin = "Победили мирные жители! \uD83C\uDF89";
        final String maniacWin = "Победил маньяк! \uD83D\uDC40";
        final String mafiaWin = "Побеждает мафия! \uD83D\uDE08";

        if (!hasMafia && !hasManiac && numberOfCivilian >= 1) {
            return civilianWin;
        }

        if (!hasMafia && hasManiac && numberOfCivilian <= 2) {
            return maniacWin;
        }

        if (numberOfMafia >= numberOfCivilian && !hasManiac) {
            return mafiaWin;
        }

        //todo еще поискаьб варианты победителей

        return "";
    }

    @Override
    public void startGame(Long id) {
        gamePlay.remove(id);
    }

    @Override
    public void choseWhore(Long id, Player player) {
        playerService.resetBlockAll(id);

        if (gamePlay.containsKey(id)) {
            List<Game> games = gamePlay.get(id);
            Game game = new Game();
            game.setChoseWhore(player);
            games.add(game);
            gamePlay.replace(id, games);
        }
        else {
            Game game = new Game();
            game.setChoseWhore(player);
            List<Game> games = new ArrayList<>();
            games.add(game);
            gamePlay.put(id, games);
        }

        if (player != null) {
            playerService.getAllAlivePlayers(id)
                    .stream()
                    .filter(p -> p.equals(player))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(player.getName() + " was not founded"))
                    .setBlock(true);
        }
    }

    @Override
    public void choseDoctor(Long id, Player player) {
        gamePlay.get(id).get(gamePlay.get(id).size() - 1).setChoseDoctor(player);
    }

    @Override
    public void choseMafia(Long id, Player player) {
        gamePlay.get(id).get(gamePlay.get(id).size() - 1).setChoseMafia(player);
    }

    @Override
    public void choseManiac(Long id, Player player) {
        gamePlay.get(id).get(gamePlay.get(id).size() - 1).setChoseManiac(player);
    }

    @Override
    public String getResultGame(Long id) {
        final String goodNews = "Этой ночью никто не пострадал, все живы и здоровы ☺️";

        if (!gamePlay.containsKey(id)) {
            return goodNews;
        }

        // любовница блокирует
        boolean mafiaIsNotBlocked = true;
        boolean maniacIsNotBlocked = true;
        boolean doctorIsNotBlocked = true;

        List<Game> games = gamePlay.get(id);
        Game game = games.get(games.size() - 1);
        List<Player> deadList = new ArrayList<>();
        boolean doctorSaveWhoreFromManiac = false;

        /*** Любовница выбирает */
        if (game.getChoseWhore() != null) {
            final Player whore = playerService.getAllAlivePlayers(id).stream()
                    .filter(player -> player.getRole().equals(Role.WHORE))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Whore was not found"));

            // если любовница выбирает ночным клиентом маньяка
            if (game.getChoseWhore().getRole().equals(Role.MANIAC)) {
                maniacIsNotBlocked = false;
                // если доктор выбрал любовныцу то она остается в игре и сообщается что доктор ее спас
                if (game.getChoseDoctor().getRole().equals(Role.WHORE)) {
                    doctorSaveWhoreFromManiac = true;
                }
                // любовница погибает
                else {
                    deadList.add(whore);
                }
            }
            if (game.getChoseWhore().getRole().equals(Role.MAFIA)) {
                long numberOfMafia = playerService.getAllAlivePlayers(id).stream()
                        .filter(player -> !player.getRole().isCivilian())
                        .count();

                if (numberOfMafia <= 1) {
                    mafiaIsNotBlocked = false;
                }
            }
            if (game.getChoseWhore().getRole().equals(Role.DOCTOR)) {
                doctorIsNotBlocked = false;
            }
        }

        /*** Мафия выбирает */
        if (game.getChoseMafia() != null && mafiaIsNotBlocked) {
            // мафия убивает игрока если не блокирована любовницей
            // если мафия убивает любовницу, то так же умирает игрок, который был у любовницы
            if (game.getChoseMafia().getRole().equals(Role.WHORE)) {
                deadList.add(game.getChoseWhore());
            }
            if (game.getChoseMafia().getRole().equals(Role.MANIAC)) {
                maniacIsNotBlocked = false;
            }
            deadList.add(game.getChoseMafia());
        }

        /*** Маньяк выбирает */
        if (game.getChoseManiac() != null && maniacIsNotBlocked) {
            // маньяк убивает если не выбран любовницей или не выбран мафией
            deadList.add(game.getChoseManiac());
        }

        /*** Доктор выбирает */
        if (game.getChoseDoctor() != null && doctorIsNotBlocked) {
            if (!doctorSaveWhoreFromManiac) {
                deadList.remove(game.getChoseDoctor());
            }
        }

        String whoreWasSaved = "";

        if (doctorSaveWhoreFromManiac && !deadList.contains(game.getChoseDoctor())) {
            whoreWasSaved = "\nДоктор спас любовницу от маньяка этой ночью, у игрока " +
            game.getChoseWhore().getName() + " имунитет";
        }
        if (doctorSaveWhoreFromManiac && deadList.contains(game.getChoseDoctor())) {
            whoreWasSaved = "\nДоктор пытался спасти любовницу от маньяка этой ночью";
        }

        /*** Вывод результата */
        if (deadList.isEmpty()) {
            if (doctorSaveWhoreFromManiac) {
                return goodNews + whoreWasSaved;
            }
            return goodNews;
        }
        else {
            StringBuilder badNews = new StringBuilder("Этой ночью были убиты следующие игроки:\n\n");

            for (Player player : deadList.stream().distinct().toList()) {
                player.setAlive(false);
                badNews.append(player.getName()).append(" (");
                if (player.getRole().isCivilian()) {
                    badNews.append("мир)\n");
                }
                else {
                    badNews.append("маф)\n");
                }
            }

            if (doctorSaveWhoreFromManiac) {
                badNews.append(whoreWasSaved);
            }

            return badNews.toString();
        }
    }

    @Override
    public boolean checkChoiceOfWhore(Long id, Player player) {
        if (gamePlay.get(id) == null || playerService.getAllAlivePlayers(id).size() <= 3) { return true; }
        List<Game> games = gamePlay.get(id);
        Game game = games.get(games.size() - 1);
        return !player.equals(game.getChoseWhore());
    }

    @Override
    public boolean checkChoiceOfDoctor(Long id, Player player) {
        if (gamePlay.get(id) == null) { return true; }

        List<Game> games = gamePlay.get(id);

        final boolean blockDoc = games.stream()
                .map(Game::getChoseDoctor)
                .filter(Objects::nonNull)
                .anyMatch(p -> p.getRole().equals(Role.DOCTOR));


        if (player.getRole().equals(Role.DOCTOR) && blockDoc) {
            return false;
        }

        Game game;
        if (games.size() > 1) {
            game = games.get(games.size() - 2);
        }
        else {
            game = games.get(games.size() - 1);
        }

        if (game.getChoseDoctor() == null) {
            return true;
        }
        return !player.equals(game.getChoseDoctor());
    }
}
