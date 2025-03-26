package com.codenames.repository;

import com.codenames.model.Game;

import java.util.Map;

public interface GameRepository {

    Map<String, Game> getGames();
    Game getGameForId(String gameId);

    Game updateGame(Game game);

    Game insertGame(Game game);
}
