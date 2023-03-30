package com.example.mafiabot.service;

import com.example.mafiabot.model.Player;

public interface GameService {
    String getWinner(Long id);
    void startGame(Long id);
    void choseWhore(Long id, Player player);
    void choseDoctor(Long id, Player player);
    void choseMafia(Long id, Player player);
    void choseManiac(Long id, Player player);
    String getResultGame(Long id);
    boolean checkChoiceOfWhore(Long id, Player player);
    boolean checkChoiceOfDoctor(Long id, Player player);
    void setMessageId(Long id, Long messageId);
    Long getMessageId(Long id);
}
