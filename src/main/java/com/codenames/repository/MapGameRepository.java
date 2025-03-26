package com.codenames.repository;

import com.codenames.model.Game;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class MapGameRepository implements GameRepository {
    private Map<String, Game> games = new HashMap<>();

    @Override
    public Map<String, Game> getGames() {
        return games;
    }

    @Override
    public Game getGameForId(String gameId) {
        return games.get(gameId);
    }

    @Override
    public Game updateGame(Game game) {
        games.put(game.getId(), game);
        return games.get(game.getId());
    }

    @Override
    public Game insertGame(Game game) {
        if(game.getId() == null) {
            throw new IllegalArgumentException("Game ID cannot be null");
        }
        if(games.containsKey(game.getId())) {
            throw new IllegalArgumentException("Game already exists");
        }
        games.put(game.getId(), game);
        return game;
    }
}
