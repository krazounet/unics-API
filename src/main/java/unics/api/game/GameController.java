package unics.api.game;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import unics.api.game.service.GameService;
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
            @RequestHeader(value = "X-PLAYER-UUID", required = false) UUID playerId
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
    @PostMapping("/{gameId}/mulligan")
    public ResponseEntity<GameState> mulligan(
            @PathVariable String gameId,
            @RequestBody MulliganRequest request
    ) {

        GameState updatedGame = gameService.handleMulligan(
                gameId,
                request.getPlayerId(),
                request.getCards()
        );

        return ResponseEntity.ok(updatedGame);
    }
    @PostMapping("/{gameId}/play")
    public ResponseEntity<GameState> play(
            @PathVariable String gameId,
            @RequestBody PlayRequest request
    ) {
    	//{"cardId":"2da5e822-65f2-43d1-aff0-ab0576316acf","playerId":"45ca93a9-4a52-4d1c-be38-87b5341e5788","position":"RIGHT"}
        GameState updatedGame = gameService.handlePlayCard(
                gameId,
                request.getPlayerId(),
                request.getCardId(),
                request.getPosition()
        );

        return ResponseEntity.ok(updatedGame);
    }
    
    @PostMapping("/{gameId}/end-play")
    public ResponseEntity<GameState> endPlay(
            @PathVariable String gameId,
            @RequestBody EndPlayRequest request
    ) {
        return ResponseEntity.ok(
            gameService.handleEndPlayPhase(
                gameId,
                request.getPlayerId()
            )
        );
    }
        @PostMapping("/{gameId}/atk-pass")
        public ResponseEntity<GameState> atkpass(
                @PathVariable String gameId,
                @RequestBody AtkPassRequest request
        ) {
            return ResponseEntity.ok(
                gameService.handleAtkPassPhase(
                    gameId,
                    request.getPlayerId(),
                    request.getPosition()
                )
            );

    }
        @PostMapping("/{gameId}/atk-action")
        public ResponseEntity<GameState> atkpass(
                @PathVariable String gameId,
                @RequestBody AtkActionRequest request
        ) {
            return ResponseEntity.ok(
                gameService.handleAtkAction(
                    gameId,
                    request.getPlayerId(),
                    request.getSource(),
                    request.getCible()
                )
            );

    }
}

