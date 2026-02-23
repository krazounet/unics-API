package unics.game.ExecUtil;


import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import dbPG18.DbUtil;
import dbPG18.JdbcCardSnapshotDao;
import unics.game.Deck;
import unics.game.Joueur;
import unics.game.db.JdbcDeckDao;
import unics.game.db.JdbcJoueurDao;
import unics.snapshot.CardSnapshot;

public class InitFakeDecks {

    

    public static void main(String[] args) {

       

        UUID joueurId = UUID.fromString("ce34cabf-b43b-49c0-a675-7a9aff0b8129");

        try (Connection connection = DbUtil.getConnection()) {

            // DAOs
            JdbcJoueurDao joueurDao = new JdbcJoueurDao(connection);
            JdbcCardSnapshotDao snapshotDao = new JdbcCardSnapshotDao(connection);
            JdbcDeckDao deckDao = new JdbcDeckDao(connection);

            // Charger joueur
            Joueur joueur = joueurDao.findById(joueurId)
                    .orElseThrow(() -> new IllegalStateException("Joueur introuvable"));

            // Cartes aléatoires
            RandomBooster booster = new RandomBooster(ThreadLocalRandom.current(), snapshotDao);
            booster.generate();
            List<CardSnapshot> cards = booster.cards;



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