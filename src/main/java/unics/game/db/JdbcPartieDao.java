package unics.game.db;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import unics.game.EtatPartie;
import unics.game.GameState;
import unics.game.Partie;
import unics.game.PhasePartie;

public class JdbcPartieDao {

    private final Connection connection;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    
    public JdbcPartieDao(Connection connection) {
        this.connection = connection;
    }

    // ─────────────────────────────────────────────
    // INSERT
    // ─────────────────────────────────────────────

    public void insert(Partie partie) {

        String sql = """
            INSERT INTO partie (
                id,
                etat,
                phase,
                joueur_actif,
                tour,
                step,
                rng_seed,
                payload
            )
            VALUES (?, ?, ?, ?, ?, ?,?, ?::jsonb)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, partie.getId_partie());
            ps.setString(2, partie.getEtat_partie().name());
            ps.setString(3, partie.getPhase_partie().name());
            ps.setObject(4, partie.getJoueur_actif());
            ps.setInt(5, partie.getTour());
            ps.setInt(6, partie.getStep());
            ps.setLong(7, partie.getRngSeed());
            ps.setString(8, MAPPER.writeValueAsString(partie.getGamestate()));

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to insert Partie " + partie.getId_partie(), e);
        }
    }

    // ─────────────────────────────────────────────
    // FIND BY ID
    // ─────────────────────────────────────────────

    public Optional<Partie> findById(UUID partieId) {

        String sql = """
            SELECT *
            FROM partie
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, partieId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            Partie partie = new Partie();
            
            String payload = rs.getString("payload");
            GameState gs = MAPPER.readValue(payload, GameState.class);
            partie.setJ1(gs.J1);
            partie.setJ2(gs.J2);
            partie.setGamestate(gs); 
            
            partie.setId_partie((UUID) rs.getObject("id"));
            partie.setEtat_partie(EtatPartie.valueOf(rs.getString("etat"))); 
            partie.setPhase_partie(PhasePartie.valueOf(rs.getString("phase")));
            partie.setJoueur_actif((UUID) rs.getObject("joueur_actif")); 
            partie.setTour(rs.getInt("tour")); 
            partie.setStep(rs.getInt("step")); 

            

            return Optional.of(partie);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load Partie " + partieId, e);
        }
    }

    // ─────────────────────────────────────────────
    // UPDATE (pour plus tard)
    // ─────────────────────────────────────────────

    public void update(Partie partie) {
    	
    	
        String sql = """
            UPDATE partie
            SET
                etat = ?,
                phase = ?,
                joueur_actif = ?,
                tour = ?,
                step = ?,
                payload = ?::jsonb,
                updated_at = now()
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, partie.getEtat_partie().name());
            ps.setString(2, partie.getPhase_partie().name());
            ps.setObject(3, partie.getJoueur_actif());
            ps.setInt(4, partie.getTour());
            ps.setInt(5, partie.getStep());
            ps.setString(6, MAPPER.writeValueAsString(partie.getGamestate()));
            ps.setObject(7, partie.getId_partie());

            ps.executeUpdate();
            //System.out.println("Rows updated = " + rows);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update Partie " + partie.getId_partie(), e);
        }
    }

    
}