package com.codenames.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    private String id;
    private List<User> users = new ArrayList<>();
    private LocalDateTime createdTimestamp;
    private Map<String, Card> cards = new HashMap<>();
    private GameState state;
    private CardType turn;
    private CardType winner;
    private Prompt prompt;
    private int guessesRemaining;
    private int blueCardsRemaining;
    private int redCardsRemaining;

    public void addUser(User user) {
        if(users == null) {
            users = new ArrayList<>();
        }
        users.add(user);
    }
}

