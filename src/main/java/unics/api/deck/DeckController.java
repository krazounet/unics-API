package unics.api.deck;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public DeckView getDeck(
            @PathVariable UUID deckId,
            @RequestHeader(value = "X-PLAYER-ID", required = false) UUID playerId
    ) {

        Deck deck = deckService.loadDeck(deckId);
        // Pas de joueur → vue publique
        if (playerId == null) {
            return deckService.toPublicView(deck);
        }

        // Joueur non propriétaire → vue publique
        if (!deck.getProprietaire().getId_joueur().equals(playerId)) {
            return deckService.toPublicView(deck);
        }
        return deckService.toPrivateView(deck);
    }

    // ─────────────────────────────────────
    // GET PRIVATE DECK (owner)
    // ─────────────────────────────────────

    @GetMapping("/{deckId}/debug")
    public DeckPrivateView getDeckPrivate(
            @PathVariable UUID deckId
            
    ) {

        Deck deck = deckService.loadDeck(deckId);

        

        return deckService.toPrivateView(deck);
    }
}
