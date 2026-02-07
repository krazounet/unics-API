package unics.api.deck;

import java.sql.Connection;

import java.util.UUID;

import org.springframework.stereotype.Service;

import dbPG18.DbUtil;
import unics.game.Deck;
import unics.game.db.JdbcDeckDao;


@Service
public class DeckService {

    public Deck loadDeck(UUID deckId) {
        try (Connection c = DbUtil.getConnection()) {

            //JdbcJoueurDao joueurDao = new JdbcJoueurDao(c);
            JdbcDeckDao deckDao = new JdbcDeckDao(c);

            return deckDao.findById(deckId)
                    .orElseThrow(() -> new IllegalStateException("Deck introuvable"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DeckView toPublicView(Deck deck) {
        DeckView v = new DeckView();
        v.deckId = deck.getId_deck();
        v.name = deck.getName();
        v.ownerId = deck.getProprietaire().getId_joueur();
        v.ownerPseudo = deck.getProprietaire().getPseudo();
        v.cardCount = deck.getList_cartes().size();
        return v;
    }

    public DeckPrivateView toPrivateView(Deck deck) {
        DeckPrivateView v = new DeckPrivateView();
        DeckView base = toPublicView(deck);

        v.deckId = base.deckId;
        v.name = base.name;
        v.ownerId = base.ownerId;
        v.ownerPseudo = base.ownerPseudo;
        v.cardCount = base.cardCount;

        v.cards = deck.getList_cartes();
        return v;
    }
}

