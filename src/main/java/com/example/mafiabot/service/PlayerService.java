package com.example.mafiabot.service;

import com.example.mafiabot.enums.Role;
import com.example.mafiabot.enums.State;
import com.example.mafiabot.model.Player;

import java.util.List;

public interface PlayerService {
    List<Player> setRolesAuto(Long id);
    Player getPlayerEmptyRole(Long id);
    boolean checkRoleIsBlockedByWhore(Long id, Role role);
    boolean checkPlayerByCop(Long id, Player player);
    void updatePlayers(Long id, Player player);
    void savePlayers(Long id, List<Player> players);
    void setCachePlayer(Long id, Player player);
    Player getCachePlayer(Long id);
    List<Player> getAllAlivePlayers(Long id);
    List<Player> getAllAlivePlayers(Long id, Role exceptRole);
    boolean checkHasRole(Long id, Role role);
    void clearAllById(Long id);
    void deleteAllById(Long id);
    void setState(Long id, State state);
    State getState(Long id);
    void resetBlockAll(Long id);
    Player getPlayerByName(Long id, String name);

}
