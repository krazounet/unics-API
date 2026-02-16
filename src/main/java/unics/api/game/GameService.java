package unics.api.game;



import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dbPG18.DbUtil;
import unics.game.EtatPartie;
import unics.game.GameState;
import unics.game.Partie;
import unics.game.db.JdbcPartieDao;

@Service
public class GameService {

	public Partie loadPartie(UUID partieID) {
		try (Connection connection = DbUtil.getConnection()) {

            JdbcPartieDao partieDao = new JdbcPartieDao(connection);

            Partie partie = partieDao.findById(partieID)
                    .orElseThrow(() ->
                        new IllegalStateException("Partie introuvable : " + partieID)
                    );
            return partie;
		} catch (Exception e) {
            throw new RuntimeException("Failed to load game" + partieID, e);
        }
	}
	
    public GameState loadGameState(UUID partieId) {

    	return loadPartie(partieId).getGamestate();
    	/*
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
        */
    }

	public GameState handleMulligan(String gameId, String playerId, List<String> cards) {
		//1 recupérer la game ou le GameState
		
		Partie partie = loadPartie(UUID.fromString(gameId));
		
		//2 verif que l'envoyeur est bien le joueur actif
		UUID uuid_joueur_actif = UUID.fromString(playerId);
		if (uuid_joueur_actif != partie.getJoueur_actif()) {
			throw new IllegalStateException("joueur actif != playerid");
		}

		//3 verifier phase / etat
		if (partie.getEtat_partie() != EtatPartie.MULLIGAN) {
	        throw new IllegalStateException("Not in mulligan phase");
	    }
		
		//4 Vérifier que les cartes sont dans la main

		    /*
		    for (String cardId : cards) {
		        if (!player.hasCardInHand(cardId)) {
		            throw new IllegalArgumentException("Card not in hand");
		        }
		    }

		    // Retirer cartes
		    List<Card> mulliganed = player.removeCards(cardIds);

		    // Remettre dans deck + mélanger
		    player.getDeck().addAll(mulliganed);
		    Collections.shuffle(player.getDeck());

		    // Piocher autant
		    player.draw(cardIds.size());

		    // Passer à la phase suivante si les 2 joueurs ont mulligan
		    game.advanceIfBothMulliganed();

		    return gameMapper.toDto(game);
		    */
		    return null;
	}
}
