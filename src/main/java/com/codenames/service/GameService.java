package com.codenames.service;

import com.codenames.model.*;
import com.codenames.repository.GameRepository;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GameService {

    private final GameRepository gameRepository;

    @Autowired
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Map<String,Game> getGames() {
        return gameRepository.getGames();
    }

    public Game createGame() {
        Game game = Game.builder()
                .id(RandomStringUtils.secure().nextAlphabetic(6))
                .createdTimestamp(LocalDateTime.now())
                .state(GameState.CREATED)
                .build();

        setCardsForGame(game);
        gameRepository.insertGame(game);
        return game;
    }

    public Game getGameById(String gameId) {
        Game game = gameRepository.getGameForId(gameId);
        if(game == null) {
            throw new NotFoundException("Game not found for ID: " + gameId);
        }
        return gameRepository.getGameForId(gameId);
    }

    public Game startGame(String gameId) {
        Game game = getGameById(gameId);
        if(game == null) {
            throw new NotFoundException("Game not found for ID: " + gameId);
        }
        if(game.getState() != GameState.READY) {
            throw new IllegalStateException("Game cannot be started");
        }
        game.setState(GameState.STARTED);
        setTeamsForUsers(game);
        gameRepository.updateGame(game);
        return game;
    }

    public Game addUserToGame(String gameId, User user) {
        Game game = getGameById(gameId);
        if(game == null) {
            throw new NotFoundException("Game not found for ID: " + gameId);
        }
        if(game.getState() != GameState.CREATED && game.getState() != GameState.READY) {
            throw new IllegalStateException("Game is already started");
        }
        user.setTeam(CardType.UNASSIGNED);
        game.addUser(user);
        gameRepository.updateGame(game);
        return game;
    }

    public Game selectCard(String gameId, String cardValue, User user) {
        Game game = getGameById(gameId);
        if(game == null) {
            throw new NotFoundException("Game not found for ID: " + gameId);
        }
        if(game.getState() != GameState.STARTED) {
            throw new IllegalStateException("Game is not started");
        }
        if(game.getTurn() != user.getTeam()) {
            throw new IllegalStateException("Not this user's turn");
        }

        Card card = game.getCards().get(cardValue);
        if(card == null) {
            throw new NotFoundException("Card not found for value: " + cardValue);
        }
        if(card.isSelected()) {
            throw new IllegalStateException("Card already revealed");
        }

        card.setSelected(true);
        game.setGuessesRemaining(game.getGuessesRemaining() - 1);
        if(card.getType() == CardType.RED) {
            game.setRedCardsRemaining(game.getRedCardsRemaining() - 1);
            if(game.getRedCardsRemaining() == 0) {
                game.setWinner(CardType.RED);
                game.setState(GameState.COMPLETED);
            }
        } else if(card.getType() == CardType.BLUE) {
            game.setBlueCardsRemaining(game.getBlueCardsRemaining() - 1);
            if(game.getBlueCardsRemaining() == 0) {
                game.setWinner(CardType.BLUE);
                game.setState(GameState.COMPLETED);
            }
        } else if(card.getType() == CardType.ASSASSIN) {
            game.setWinner(game.getTurn().otherTeam());
            game.setState(GameState.COMPLETED);
        } else {
            if(game.getGuessesRemaining() == 0) {
                game.setTurn(game.getTurn().otherTeam());
                game.setPrompt(null);
            }
        }

        gameRepository.updateGame(game);
        return game;
    }

    private void setTeamsForUsers(Game game) {
        int userSize = game.getUsers().size();
        int redUsersRemaining = userSize / 2;
        int blueUsersRemaining = userSize - redUsersRemaining;

        List<User> redUsers = new ArrayList<>();
        List<User> blueUsers = new ArrayList<>();
        Random rand = new Random();

        while (blueUsersRemaining > 0) {
            int randomUserNumber = rand.nextInt(userSize);
            User currentUser = game.getUsers().get(randomUserNumber);
            if (currentUser.getTeam() == CardType.UNASSIGNED) {
                currentUser.setTeam(CardType.BLUE);
                blueUsers.add(currentUser);
                blueUsersRemaining--;
            }
        }

        while (redUsersRemaining > 0) {
            int randomUserNumber = rand.nextInt(userSize);
            User currentUser = game.getUsers().get(randomUserNumber);
            if (currentUser.getTeam() == CardType.UNASSIGNED) {
                currentUser.setTeam(CardType.RED);
                redUsers.add(currentUser);
                redUsersRemaining--;
            }
        }

        int randomRedSpyMasterNumber = rand.nextInt(redUsers.size());
        redUsers.get(randomRedSpyMasterNumber).setSpyMaster(true);
        int randomBlueSpyMasterNumber = rand.nextInt(blueUsers.size());
        blueUsers.get(randomBlueSpyMasterNumber).setSpyMaster(true);
    }

    private List<String> readCardValuesFromFile() throws IOException {
        List<String> words = new ArrayList<>();
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("words.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            words.add(line);
        }
        return words;
    }

    public void setCardsForGame(Game game) {
        List<String> allCards;
        try {
            allCards = readCardValuesFromFile();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read card values from file", e);
        }

        Map<String, Card> chosenCards = new HashMap<>();
        Random rand = new Random();
        for (int i = 0; i < 25; i++) {
            int randomNumber = rand.nextInt(allCards.size());
            Card card = Card.builder()
                    .value(allCards.get(randomNumber))
                    .type(CardType.CIVILIAN)
                    .build();
            chosenCards.put(card.getValue(), card);
            // Remove card if it was already used
            allCards.remove(randomNumber);
        }

        game.setCards(chosenCards);
        chooseCardTypes(game);
        if (game.getRedCardsRemaining() > game.getBlueCardsRemaining()) {
            game.setTurn(CardType.RED);
        } else {
            game.setTurn(CardType.BLUE);
        }
    }

    private void chooseCardTypes(Game game) {
        Map<String, Card> cards = game.getCards();
        Random rand = new Random();
        int randomNumber = rand.nextInt(2);
        int assassinCardsLeft = 1;

        int totalRedCards = 8;
        int totalBlueCards = 8;
        if (randomNumber == 1) {
            totalRedCards++;
        } else {
            totalBlueCards++;
        }

        List<String> keys = cards.keySet().stream().toList();

        int blueCardsLeft = totalBlueCards;
        int redCardsLeft = totalRedCards;
        while (redCardsLeft > 0) {
            int randomCardNumber = rand.nextInt(keys.size());
            String currentKey = keys.get(randomCardNumber);
            Card currentCard = cards.get(currentKey);
            if (currentCard.getType() == CardType.CIVILIAN) {
                currentCard.setType(CardType.RED);
                redCardsLeft--;
            }
        }

        while (blueCardsLeft > 0) {
            int randomCardNumber = rand.nextInt(keys.size());
            String currentKey = keys.get(randomCardNumber);
            Card currentCard = cards.get(currentKey);
            if (currentCard.getType() == CardType.CIVILIAN) {
                currentCard.setType(CardType.BLUE);
                blueCardsLeft--;
            }
        }

        while (assassinCardsLeft > 0) {
            int randomCardNumber = rand.nextInt(keys.size());
            String currentKey = keys.get(randomCardNumber);
            Card currentCard = cards.get(currentKey);
            if (currentCard.getType() == CardType.CIVILIAN) {
                currentCard.setType(CardType.ASSASSIN);
                assassinCardsLeft--;
            }
        }

        game.setBlueCardsRemaining(totalBlueCards);
        game.setRedCardsRemaining(totalRedCards);
    }
}
