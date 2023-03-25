package com.example.mafiabot.enums;

import static com.example.mafiabot.Str.*;

public enum Role {
    MAFIA (STR_MAFIA, false),
    DON (STR_DON, false),
    MANIAC (STR_MANIAC, true),
    DOCTOR (STR_DOCTOR, true),
    WHORE (STR_WHORE, true),
    COP (STR_COP, true),
    CIVILIAN (STR_CIVILIAN, true),
    EMPTY (STR_EMPTY, true);

    private final String name;
    private final boolean civilian;

    Role(String name, boolean civilian) {
        this.civilian = civilian;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isCivilian() {
        return civilian;
    }
}
