package unics.api.deck;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import unics.game.Deck;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    // ─────────────────────────────────────
    // GET PUBLIC DECK
    // ─────────────────────────────────────

    @GetMapping("/{deckId}")
    public DeckView getDeckPublic(@PathVariable UUID deckId) {

        Deck deck = deckService.loadDeck(deckId);
        return deckService.toPublicView(deck);
    }

    // ─────────────────────────────────────
    // GET PRIVATE DECK (owner)
    // ─────────────────────────────────────

    @GetMapping("/{deckId}/full")
    public DeckPrivateView getDeckPrivate(
            @PathVariable UUID deckId,
            @RequestHeader("X-PLAYER-ID") UUID playerId
    ) {

        Deck deck = deckService.loadDeck(deckId);

        if (!deck.getProprietaire().getId_joueur().equals(playerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return deckService.toPrivateView(deck);
    }
}
