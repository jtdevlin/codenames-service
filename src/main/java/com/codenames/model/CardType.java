package com.codenames.model;

public enum CardType {
    RED,
    BLUE,
    CIVILIAN,
    ASSASSIN,
    UNASSIGNED;

    public CardType otherTeam() {
        if (this == BLUE) {
            return RED;
        } else if (this == RED) {
            return BLUE;
        }
        return UNASSIGNED;
    }
}
