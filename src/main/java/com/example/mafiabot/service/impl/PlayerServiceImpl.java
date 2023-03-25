package com.example.mafiabot.service.impl;

import com.example.mafiabot.enums.Role;
import com.example.mafiabot.enums.State;
import com.example.mafiabot.model.Player;
import com.example.mafiabot.service.PlayerService;
import jakarta.ws.rs.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService {
    private Map<Long, List<Player>> allPlayers = new HashMap<>();
    private Map<Long, Player> cachePlayer = new HashMap<>();
    private Map<Long, State> stateUser = new HashMap<>();

    @Override
    public List<Player> setRolesAuto(Long id) {

        return null;
    }

    @Override
    public Player getPlayerEmptyRole(Long id) {
        return allPlayers.get(id).stream().filter(player -> player.getRole().equals(Role.EMPTY))
                .findFirst().orElse(null);
    }

    @Override
    public boolean checkRoleIsBlockedByWhore(Long id, Role role) {
        return allPlayers.get(id).stream()
                .filter(player -> player.getRole().equals(role))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("For chat id: " + id + " rile "
                        + role.getName() + " was not found"))
                .isBlock();
    }

    @Override
    public boolean checkPlayerByCop(Long id, Player player) {
        List<Player> players = allPlayers.get(id);
        boolean hasMafia = players.stream().anyMatch(p -> !p.getRole().isCivilian());

        if (player.getRole().equals(Role.MANIAC) && !hasMafia) {
            return false;
        }

        return player.getRole().isCivilian();
    }

    @Override
    public void updatePlayers(Long id, Player player) {
        List<Player> players = allPlayers.get(id);
        players.removeIf(p -> p.getName().equals(player.getName()));
        players.add(player);
        allPlayers.replace(id, players);
    }

    @Override
    public void savePlayers(Long id, List<Player> players) {
        allPlayers.put(id, players);
    }

    @Override
    public void setCachePlayer(Long id, Player player) {
        cachePlayer.put(id, player);
    }

    @Override
    public Player getCachePlayer(Long id) {
        return cachePlayer.get(id);
    }

    @Override
    public List<Player> getAllAlivePlayers(Long id) {
        if (isNotEmpty(id)) {
            return allPlayers.get(id).stream().filter(Player::isAlive).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    @Override
    public List<Player> getAllAlivePlayers(Long id, Role exceptRole) {
        if (isNotEmpty(id)) {
            return allPlayers.get(id).stream()
                    .filter(player -> player.isAlive() && !player.getRole().equals(exceptRole))
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    @Override
    public boolean checkHasRole(Long id, Role role) {
        return getAllAlivePlayers(id).stream()
                .anyMatch(p -> p.getRole().equals(role));
    }

    @Override
    public void clearAllById(Long id) {
        if (isNotEmpty(id)) {
            allPlayers.get(id).forEach(Player::clear);
        }
    }

    @Override
    public void deleteAllById(Long id) {
        allPlayers.remove(id);
    }

    @Override
    public void setState(Long id, State state) {
        if (stateUser.get(id) == null) {
            stateUser.put(id, state);
        } else {
            stateUser.replace(id, state);
        }
    }

    @Override
    public State getState(Long id) {
        return stateUser.get(id);
    }

    @Override
    public void resetBlockAll(Long id) {
        allPlayers.get(id).forEach(p -> p.setBlock(false));
    }

    @Override
    public Player getPlayerByName(Long id, String name) {
        return allPlayers.get(id).stream()
                .filter(player -> player.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(name + " was not found"));
    }

    private boolean isNotEmpty(Long id) {
        return allPlayers.get(id) != null;
    }
}
