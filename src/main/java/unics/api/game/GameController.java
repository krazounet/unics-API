package unics.api.game;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import unics.game.GameState;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/{partieId}")
    public GameView getGame(
            @PathVariable UUID partieId,
            @RequestHeader(value = "X-PLAYER-ID", required = false) UUID playerId
    ) {
        GameState state = gameService.loadGameState(partieId);
        return StateProjector.project(state, playerId);
    }
    
    @GetMapping("/{partieId}/debug")
    public GameState getGameDebug(
            @PathVariable UUID partieId
    ) {
    	return gameService.loadGameState(partieId);
    }
    
}

