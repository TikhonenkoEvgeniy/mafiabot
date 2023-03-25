package com.example.mafiabot.model;

import com.example.mafiabot.enums.Role;
import lombok.Data;

import java.util.Objects;

@Data
public class Player {
    private String name;
    private Role role;
    private boolean alive;
    private boolean block;

    public Player() {
        clear();
    }

    public void clear() {
        this.role = Role.EMPTY;
        this.alive = true;
        this.block = false;
    }

    public boolean equals(Player player) {
        return this.name.equals(player.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
