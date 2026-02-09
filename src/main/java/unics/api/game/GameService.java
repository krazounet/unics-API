package unics.api.game;



import java.sql.Connection;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dbPG18.DbUtil;
import unics.game.GameState;
import unics.game.Partie;
import unics.game.db.JdbcPartieDao;

@Service
public class GameService {

    public GameState loadGameState(UUID partieId) {

        try (Connection connection = DbUtil.getConnection()) {

            JdbcPartieDao partieDao = new JdbcPartieDao(connection);

            Partie partie = partieDao.findById(partieId)
                    .orElseThrow(() ->
                        new IllegalStateException("Partie introuvable : " + partieId)
                    );

            return partie.getGamestate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load game state " + partieId, e);
        }
    }
}
