package unics.game.ExecUtil;


import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import dbPG18.DbUtil;
import dbPG18.JdbcCardSnapshotDao;
import unics.game.Deck;
import unics.game.Joueur;
import unics.game.db.JdbcDeckDao;
import unics.game.db.JdbcJoueurDao;
import unics.snapshot.CardSnapshot;

public class InitFakeDecks {

    private static final int DECK_SIZE = 30;

    public static void main(String[] args) {

       

        UUID joueurId = UUID.fromString("bdae434c-b2e4-48fb-9be6-b8b0124e5726");

        try (Connection connection = DbUtil.getConnection()) {

            // DAOs
            JdbcJoueurDao joueurDao = new JdbcJoueurDao(connection);
            JdbcCardSnapshotDao snapshotDao = new JdbcCardSnapshotDao(connection);
            JdbcDeckDao deckDao = new JdbcDeckDao(connection);

            // Charger joueur
            Joueur joueur = joueurDao.findById(joueurId)
                    .orElseThrow(() -> new IllegalStateException("Joueur introuvable"));

            // Cartes aléatoires
            List<CardSnapshot> cards = snapshotDao.findRandom(DECK_SIZE);

            if (cards.size() < DECK_SIZE) {
                throw new IllegalStateException("Pas assez de CardSnapshot disponibles");
            }

            // Créer deck
            Deck deck = new Deck(
                UUID.randomUUID(),
                joueur,
                "Deck Fake " + System.currentTimeMillis(),
                cards
            );

            // Persister
            deckDao.insert(deck);

            System.out.println("✔ Deck créé : " + deck.getName());
            System.out.println("  Joueur : " + joueur.getPseudo());
            System.out.println("  Cartes : " + cards.size());

        } catch (Exception e) {
            System.err.println("❌ Erreur InitFakeDecks");
            e.printStackTrace();
        }
    }
}