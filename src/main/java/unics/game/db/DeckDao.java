package unics.game.db;

import java.util.Optional;
import java.util.UUID;

import unics.game.Deck;

public interface DeckDao  {

    void insert(Deck deck);

    Optional<Deck> findById(UUID deckId);

   
}