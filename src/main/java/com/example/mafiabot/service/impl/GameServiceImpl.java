package com.example.mafiabot.service.impl;

import com.example.mafiabot.enums.Role;
import com.example.mafiabot.model.Game;
import com.example.mafiabot.model.Player;
import com.example.mafiabot.service.GameService;
import com.example.mafiabot.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    private final PlayerService playerService;
    private Map<Long, List<Game>> gamePlay = new HashMap<>();


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
            playerService.clearAllById(id);
            startGame(id);
            return civilianWin;
        }
        if (!hasMafia && hasManiac && numberOfCivilian <= 2) {
            playerService.clearAllById(id);
            startGame(id);
            return maniacWin;
        }
        if (numberOfMafia >= numberOfCivilian && !hasManiac) {
            startGame(id);
            playerService.clearAllById(id);
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

        List<Game> games = gamePlay.get(id);
        Game game = games.get(games.size() - 1);
        List<Player> deadList = new ArrayList<>();
        boolean doctorSaveWhoreFromManiac = false;

        /*** Любовница выбирает */
        boolean whoreIsDead = false;

        if (game.getChoseWhore() != null) {
            // если любовница выбирает ночным клиентом маньяка
            if (game.getChoseWhore().getRole().equals(Role.MANIAC)) {
                // если доктор выбрал любовныцу то она остается в игре и сообщается что доктор ее спас
                if (game.getChoseDoctor().getRole().equals(Role.WHORE)) {
                    doctorSaveWhoreFromManiac = true;
                }
                // любовница погибает
                else {
                    deadList.add(game.getChoseWhore());
                    whoreIsDead = true;
                }
            }
        }

        /*** Мафия выбирает */
        if (game.getChoseMafia() != null) {

            // если любовница выбирает ночным клиентом мафию и в игре 1 мафия то мафия промахивается
            final long numberOfMafia = playerService.getAllAlivePlayers(id).stream()
                    .filter(p -> !p.getRole().isCivilian()).count();

            // мафия убивает игрока если не блокирована любовницей
            if (game.getChoseWhore() != null) {
                if (game.getChoseWhore().getRole().isCivilian() || numberOfMafia > 1) {
                    if (game.getChoseMafia().getRole().equals(Role.WHORE)) {
                        deadList.add(game.getChoseWhore());
                    }
                    deadList.add(game.getChoseMafia());
                }
            }
        }

        // маньяк выбирает
        if (game.getChoseManiac() != null) {

            // маньяк убивает если не выбран любовницей или не выбран мафией
            if (!game.getChoseWhore().getRole().equals(Role.MANIAC) ||
                    !game.getChoseMafia().getRole().equals(Role.MANIAC)) {
                deadList.add(game.getChoseManiac());
            }
        }

        // доктор выбирает
        if (game.getChoseDoctor() != null) {
            if (game.getChoseDoctor().getRole().equals(Role.WHORE) && !doctorSaveWhoreFromManiac) {
                deadList.remove(game.getChoseDoctor());
            }

            if (!game.getChoseWhore().getRole().equals(Role.DOCTOR)) {
                deadList.remove(game.getChoseDoctor());
            }
        }

        if (!whoreIsDead) {
            deadList.remove(game.getChoseWhore());
        }

        // вывод результата
        if (deadList.isEmpty()) {
            if (doctorSaveWhoreFromManiac) {
                return goodNews + "\n\nЛюбовница была спасена доктором этой ночью";
            }
            return goodNews;
        }
        else {
            StringBuilder badNews = new StringBuilder("Этой ночью не просыпаются следующие участники:\n\n");
            for (Player player : deadList) {
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
                badNews.append("\n\nЛюбовница была спасена доктором этой ночью");
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
