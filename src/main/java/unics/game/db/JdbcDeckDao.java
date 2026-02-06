package unics.game.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dbPG18.DbUtil;
import dbPG18.JdbcCardSnapshotDao;
import unics.game.Deck;
import unics.game.Joueur;
import unics.snapshot.CardSnapshot;

public class JdbcDeckDao implements DeckDao {

    private final Connection connection;
    private final JdbcJoueurDao joueurDao;
    private final JdbcCardSnapshotDao cardDao;

    public JdbcDeckDao() throws SQLException {
        this.connection = DbUtil.getConnection();
        this.joueurDao = new JdbcJoueurDao(connection);
        this.cardDao = new JdbcCardSnapshotDao();
        
    }

    // ---------- INSERT ----------

    @Override
    public void insert(Deck deck) {
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement psDeck = connection.prepareStatement(
                    "INSERT INTO deck (id, owner_id, name) VALUES (?, ?, ?)"
            )) {
                psDeck.setObject(1, deck.getId_deck());
                psDeck.setObject(2, deck.getOwnerId());
                psDeck.setString(3, deck.getName());
                psDeck.executeUpdate();
            }

            try (PreparedStatement psCard = connection.prepareStatement(
                    "INSERT INTO deck_card (deck_id, card_id, position) VALUES (?, ?, ?)"
            )) {
                int position = 0;
                for (CardSnapshot snapshot : deck.getList_cartes()) {
                    psCard.setObject(1, deck.getId_deck());
                    psCard.setObject(2, snapshot.snapshotId);
                    psCard.setInt(3, position++);
                    psCard.addBatch();
                }
                psCard.executeBatch();
            }

            connection.commit();

        } catch (SQLException e) {
            rollbackQuietly();
            throw new RuntimeException("Failed to insert deck " + deck.getId_deck(), e);
        }
    }

    // ---------- FIND BY ID ----------

    @Override
    public Optional<Deck> findById(UUID deckId) {
        try {
            UUID ownerId;
            String name;

            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT id, owner_id, name FROM deck WHERE id = ?"
            )) {
                ps.setObject(1, deckId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return Optional.empty();
                }

                ownerId = (UUID) rs.getObject("owner_id");
                name = rs.getString("name");
            }

            Joueur proprietaire = joueurDao.findById(ownerId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Deck owner not found: " + ownerId));

            List<CardSnapshot> cards = new ArrayList<>();

            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT card_id FROM deck_card WHERE deck_id = ? ORDER BY position"
            )) {
                ps.setObject(1, deckId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    UUID cardId = (UUID) rs.getObject("card_id");
                    //cards.add(CardSnapshot.fromCardId(cardId));
                    
                    cards.add(cardDao.findById(cardId));
                    // ↑ méthode factory ou loader à toi
                }
            }

            Deck deck = new Deck(deckId, proprietaire, name, cards);
            return Optional.of(deck);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load deck " + deckId, e);
        }
    }


    // ---------- CLOSE ----------

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }
    }

    private void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }
}
