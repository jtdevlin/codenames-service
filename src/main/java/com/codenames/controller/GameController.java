package com.codenames.controller;

import com.codenames.model.Game;
import com.codenames.model.User;
import com.codenames.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ResponseEntity<Map<String,Game>> getAllGames() {
        return ResponseEntity.ok(gameService.getGames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable String id) {
        return ResponseEntity.ok(gameService.getGameById(id));
    }

    @PostMapping
    public ResponseEntity<Game> createGame() {
        Game createdGame = gameService.createGame();
        return ResponseEntity.ok(createdGame);
    }

    @PatchMapping("/{id}")
    public Game startGame(@PathVariable String id) {
        return gameService.startGame(id);
    }

    @PatchMapping("/{id}/users")
    public Game addUserToGame(@PathVariable String id, @RequestBody User user) {
        return gameService.addUserToGame(id, user);
    }

    @PatchMapping("/{id}/cards/{cardName}")
    public Game selectCard(@PathVariable String id, @PathVariable String cardName, @RequestBody User user) {
        return gameService.selectCard(id, cardName, user);
    }
}