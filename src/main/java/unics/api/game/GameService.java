package unics.api.game;



import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dbPG18.DbUtil;
import unics.game.EtatPartie;
import unics.game.GameState;
import unics.game.JoueurPartie;
import unics.game.LogEvent;
import unics.game.Partie;
import unics.game.db.JdbcPartieDao;
import unics.snapshot.CardSnapshot;

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
		if (!uuid_joueur_actif.equals(partie.getJoueur_actif())) {
			throw new IllegalStateException("joueur actif != playerid");
		}

		//3 verifier phase / etat
		if (partie.getEtat_partie() != EtatPartie.MULLIGAN) {
	        throw new IllegalStateException("Not in mulligan phase : "+partie.getEtat_partie());
	    }
		
		//4 Vérifier que les cartes sont dans la main
		JoueurPartie joueur = null;
		if (uuid_joueur_actif.equals(partie.getJ1().getOwner().getId_joueur())) {
			//System.out.println("J1");
			joueur = partie.getJ1();
		}else {
			//System.out.println("J2");
			joueur = partie.getJ2();
		}
			
		
		
		for (String cardId : cards) {
		    if (!joueur.hasCardInHand(cardId)) {
		        throw new IllegalArgumentException("Card not in hand : "+cardId+"main : "+joueur.getMain());
		    }
		}
		
		//5 nouvelle main sans les cartes, mais sans remettre les cartes dans le deck
		List<CardSnapshot> main_avant = joueur.getMain();
		Set<String> ids_a_supp = new HashSet<>(cards);
		List<CardSnapshot> cartesSupprimes = new ArrayList<>();
		
		Iterator<CardSnapshot> iterator = main_avant.iterator();

		while (iterator.hasNext()) {
			CardSnapshot snap = iterator.next();
		    if (ids_a_supp.contains(snap.snapshotId.toString())) {
		    	cartesSupprimes.add(snap); // on conserve
		        iterator.remove();        // on supprime proprement
		    }
		}
		
		
		//joueur.getMain().removeIf(c -> cards.contains(c.snapshotId.toString()));
		
		//6 on tire N nouvelle cartes avant de rajouter les cartes dans le deck et remélanger
		joueur.piocheXcartes(cards.size());
		
		//7 on rajoute les cartes dans le deck.
		joueur.getDeck().addAll(cartesSupprimes);

		Collections.shuffle(joueur.getDeck());

		//8 Passer à la phase suivante si les 2 joueurs ont mulligan
		partie.advanceIfBothMulliganed(joueur);

		LogEvent log = new LogEvent(joueur.getOwner().getPseudo()+" a mulligan "+cards.size()+" cartes","en",joueur.getOwner().getId_joueur().toString(),"","",null);
		partie.getGamestate().log.add(log);
		
		//9  sauvegarde partie
		try (Connection connection = DbUtil.getConnection()) {

            JdbcPartieDao partieDao = new JdbcPartieDao(connection);
            partieDao.update(partie);
            
		} catch (Exception e) {
            throw new RuntimeException("Failed to update game" , e);
        }
		
		//10 on retourne le gamestate
		return partie.getGamestate();
		    
		
	}
}
