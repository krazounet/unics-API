package unics.game.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import dbPG18.DbUtil;
import unics.game.Joueur;

public class JdbcJoueurDao implements JoueurDao {

    private final Connection connection;
    @Deprecated
    public JdbcJoueurDao() throws SQLException {
        this.connection = DbUtil.getConnection();
    }
    
    public JdbcJoueurDao(Connection connection) {
		super();
		this.connection = connection;
	}

	// ---------- INSERT ----------

    @Override
    public void insert(Joueur j) {
        try (PreparedStatement ps = connection.prepareStatement("""
            INSERT INTO joueur (id, pseudo, email, elo, valid_user, last_connection)
            VALUES (?, ?, ?, ?, ?, ?)
        """)) {
            ps.setObject(1, j.getId_joueur());
            ps.setString(2, j.getPseudo());
            ps.setString(3, j.getEmail());
            ps.setInt(4, j.getElo());
            ps.setBoolean(5, j.isValid_user());
            ps.setTimestamp(6, j.getLast_connection());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert joueur " + j.getId_joueur(), e);
        }
    }

    // ---------- FIND BY ID ----------

    @Override
    public Optional<Joueur> findById(UUID joueurId) {
        try (PreparedStatement ps = connection.prepareStatement("""
            SELECT id, pseudo, email, elo, valid_user, last_connection
            FROM joueur
            WHERE id = ?
        """)) {
            ps.setObject(1, joueurId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            Joueur joueur = new Joueur();
            joueur.setId_joueur((UUID) rs.getObject("id"));
            joueur.setPseudo(rs.getString("pseudo"));
            joueur.setEmail(rs.getString("email"));
            joueur.setElo(rs.getInt("elo"));
            joueur.setValid_user(rs.getBoolean("valid_user"));
            joueur.setLast_connection(rs.getTimestamp("last_connection"));

            return Optional.of(joueur);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load joueur " + joueurId, e);
        }
    }

    
}
