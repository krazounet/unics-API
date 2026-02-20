package unics.api.game;



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import unics.Enum.CardType;
import unics.Enum.TriggerType;
import unics.api.cards.CardSnapshotService;
import unics.game.CardInPlay;
import unics.game.CardInPlay.Inclinaison;
import unics.game.EtatPartie;
import unics.game.GameState;
import unics.game.JoueurPartie;
import unics.game.JoueurPartie.Slot;
import unics.game.LogEvent;
import unics.game.Partie;
import unics.game.PhasePartie;
import unics.game.db.JdbcPartieDao;
import unics.snapshot.CardSnapshot;
import unics.snapshot.EffectSnapshot;

@Service
public class GameService {

	private final CardSnapshotService cardSnapshotService;
	private final JdbcPartieDao partieDao;

	public GameService(CardSnapshotService cardSnapshotService,
            JdbcPartieDao partieDao) {
		this.cardSnapshotService = cardSnapshotService;
		this.partieDao = partieDao;
	}
	
	private Partie loadPartie(UUID partieID) {
	    return partieDao.findById(partieID)
	            .orElseThrow(() ->
	                new GameActionException("Partie introuvable : " + partieID)
	            );
	}
	
    public GameState loadGameState(UUID partieId) {

    	return loadPartie(partieId).getGamestate();
    	
    }

	public GameState handleMulligan(String gameId, String playerId, List<String> cards) {
		System.out.println("GameService.handleMulligan");
		//1 recupérer la game ou le GameState
		
		Partie partie = loadPartie(UUID.fromString(gameId));
		
		//2 verif que l'envoyeur est bien le joueur actif
		UUID uuid_joueur_actif = UUID.fromString(playerId);
		if (!uuid_joueur_actif.equals(partie.getJoueur_actif())) {
			throw new GameActionException("joueur actif != playerid");
		}

		//3 verifier phase / etat
		if (partie.getEtat_partie() != EtatPartie.MULLIGAN) {
	        throw new GameActionException("Not in mulligan phase : "+partie.getEtat_partie());
	    }
		
		//4 Vérifier que les cartes sont dans la main
		JoueurPartie joueur = getJoueur(partie, uuid_joueur_actif);
		
			
		
		
		for (String cardId : cards) {
		    if (!joueur.hasCardInHand(cardId)) {
		        throw new GameActionException("Card not in hand : "+cardId+"main : "+joueur.getMain());
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
		partieDao.update(partie);
		System.out.println("GameService.handleMulligan updateDone");
		//10 on retourne le gamestate
		return partie.getGamestate();
		    
		
	}

	public GameState handlePlayCard(String gameId, String playerId, String card_uuid, String position) {
		//1 recupérer la game ou le GameState
		
		Partie partie = loadPartie(UUID.fromString(gameId));
				
		//2 verif que l'envoyeur est bien le joueur actif
		UUID uuid_joueur_actif = UUID.fromString(playerId);
		if (!uuid_joueur_actif.equals(partie.getJoueur_actif())) {
			throw new GameActionException("joueur actif != playerid");
		}

		//3 verifier phase / etat
		if (partie.getPhase_partie() != PhasePartie.PLAY_CARDS) {
			throw new GameActionException("Not in play phase : "+partie.getPhase_partie());
	    }
		//4 Vérifier que les cartes sont dans la main
		JoueurPartie joueur = null;
		JoueurPartie opposant = null;
		if (uuid_joueur_actif.equals(partie.getJ1().getOwner().getId_joueur())) {
		//System.out.println("J1");
			joueur 	= partie.getJ1();
			opposant= partie.getJ2();
		}else {
		//System.out.println("J2");
			joueur = partie.getJ2();
			opposant= partie.getJ1();
		}
		CardSnapshot snap = joueur.getCardFromHandByUuid(UUID.fromString(card_uuid));
		if (snap==null) {
		    throw new GameActionException("Card not in hand : "+card_uuid+" main : "+joueur.getMain());
		}
		
		//5 verif slot vide
		Slot slot = Slot.valueOf(position);
		CardInPlay cip = joueur.getPlateau().get(slot);
		if (cip != null) throw new GameActionException("Slot oqp : "+slot+" card : "+cip.snapshotId);
		
				
		//6 verif assez mana
		if (snap.cost > joueur.getMana_dispo()) throw new GameActionException("NOTMANA");
		
		//7 verif type != action
		if (snap.type == CardType.ACTION) throw new GameActionException("CARTE ACTION NOT IN SLOT");
		
		//8 retrait main
		joueur.getMain().remove(snap);
		
		//9 ajout plateau
		//=> je dois creer une cardInPlay qui correspond au snap
		joueur.getPlateau().put(slot, new CardInPlay(snap));
		
		//10retrait mana
		joueur.retireMana(snap.cost); 
		
		//12 log event
		LogEvent log = new LogEvent(joueur.getOwner().getPseudo()+" a joué "+snap.name+" à "+position,"en",joueur.getOwner().getId_joueur().toString(),"",snap.snapshotId.toString(),null);
		partie.getGamestate().log.add(log);
		
		//10 check trigger
		checkTrigger(new ArrayList<TriggerType>(List.of(
												TriggerType.ON_ENTER, 
												TriggerType.ON_PLAY, 
												TriggerType.ON_ALLIED_UNIT_ENTERS												
											)),
					partie,
					joueur,opposant,
					snap);
		
		//11 Passer à la phase suivante // si aucune carte dispo, ni slot
		if (partie.getGamestate().getCurrentEffect() != null) {
			
			handleResolveEffect(partie,joueur,opposant);
		}
		
		//12maj step
		partie.increaseStep();
		
		//13 save game
		partieDao.update(partie);
		
		
		//20 on retourne le gamestate
		return partie.getGamestate();
	}
	
	public GameState handleEndPlayPhase(String gameId, String playerId) {

	    Partie partie = loadPartie(UUID.fromString(gameId));

	    UUID uuid_joueur_actif = UUID.fromString(playerId);

	    if (!uuid_joueur_actif.equals(partie.getJoueur_actif())) {
	        throw new GameActionException("Not active player");
	    }

	    if (partie.getPhase_partie() != PhasePartie.PLAY_CARDS) {
	        throw new GameActionException("Not in PLAY_CARDS phase");
	    }
	    
	    partie.increaseStep();
			//partie.updateJoueurActif(joueur.getOwner().getId_joueur());
			partie.setPhase_partie(PhasePartie.ATTACK_LEFT);	
			partie.setEtat_partie(EtatPartie.RUNNING);
			
	    partieDao.update(partie);

	    return partie.getGamestate();
	}
	
	//un joueur pass sa phase d'attaque (car il a pas le choix)
	public GameState handleAtkPassPhase(String gameId, String playerId, String position) {
		System.out.println("GameService.handleAtkPassPhase");
		//1 recupérer la game ou le GameState
		Partie partie = loadPartie(UUID.fromString(gameId));
		//2 verif que l'envoyeur est bien le joueur actif
		UUID uuid_joueur_actif = UUID.fromString(playerId);
		if (!uuid_joueur_actif.equals(partie.getJoueur_actif())) {
			throw new GameActionException("joueur actif != playerid");
		}

		//3 verifier phase / etat
		
		Slot slot = Slot.valueOf(position.toUpperCase());
		PhasePartie expectedPhase = switch (slot) {
	    	case LEFT -> PhasePartie.ATTACK_LEFT;
	    	case CENTER -> PhasePartie.ATTACK_CENTER;
	    	case RIGHT -> PhasePartie.ATTACK_RIGHT;
		};
		if (partie.getPhase_partie() != expectedPhase) {
		    throw new GameActionException("Phase mismatch : " + partie.getPhase_partie() + " / " + slot);
		}
		
		//4 devait il attaquer. est ce que son slot gauche contenait une unité
		JoueurPartie joueur = getJoueur(partie, uuid_joueur_actif);
		
		
		CardInPlay cip = joueur.getPlateau().get(slot);
		
		if (cip != null && cip.cardType == CardType.UNIT)throw new GameActionException("Player must Attack with "+cip.snapshotId+" / "+position);

		
		PhasePartie nextPhase = switch (slot) {
	    	case LEFT -> PhasePartie.ATTACK_CENTER;
	    	case CENTER -> PhasePartie.ATTACK_RIGHT;
	    	case RIGHT -> PhasePartie.TURN_END; // ou END_PHASE selon ton design
		};
		
		
		partie.increaseStep();
		partie.setPhase_partie(nextPhase);	
		partie.setEtat_partie(EtatPartie.RUNNING);
		
		if (nextPhase  == PhasePartie.TURN_END)
			handleEndOfTurn(partie, joueur,getOpposant(partie, joueur));
		
		partieDao.update(partie);
	    return partie.getGamestate();
	}
	
	/**
	 * Appelée apres l'attaque de droite OU pass de droite OU resolve effect en phase AttackRight
	 * @param partie
	 * @param opposant 
	 * @param joueur 
	 */
	private void handleEndOfTurn(Partie partie, JoueurPartie joueur, JoueurPartie opposant) {
		//1 check trigger
		checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.ON_TURN_END)),
							partie,
							joueur,opposant,
							null);
		//test si trigger a résoudre ou pas.
		if (partie.getGamestate().effects_to_resolve.isEmpty()) {
			//passage au tour suivant
			handleStartOfTurn(partie,joueur,opposant);
		}
		else {
			//direction résolution
			handleResolveEffect(partie,joueur,opposant);
		}
	}



	private void handleStartOfTurn(Partie partie, JoueurPartie joueur, JoueurPartie opposant) {
		partie.increaseStep();
		
		//inversion des joueurs
		JoueurPartie tmp = joueur;
		joueur=opposant;
		opposant=tmp;
		partie.setJoueur_actif(joueur.getOwner().getId_joueur());
		partie.setPhase_partie(PhasePartie.TURN_START);
		// Est un nouveau TOUR ?
		// Si joueur est J1, on est donc revenu au premier joueur => nouveau TOUR
		if (partie.getJ1()==joueur)partie.increaseTour();
		
		LogEvent log = new LogEvent("Nouveau tour : "+partie.getTour(),"New Turn",null,null,null,null);
		partie.getGamestate().log.add(log);
		
		checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.ON_TURN_START)), partie, joueur, opposant, null);
		
		//test si trigger a résoudre ou pas.
		if (partie.getGamestate().effects_to_resolve.isEmpty()) {
			handleFade(partie,joueur,opposant);
		}
		
			else {
				handleResolveEffect(partie,joueur,opposant);
		}
		
	}
	
	private void handleFade(Partie partie, JoueurPartie joueur, JoueurPartie opposant) {
		partie.increaseStep();
		partie.setPhase_partie(PhasePartie.FADE);
		//on fade d'abord les cartes qui vont mourrir
		for (Map.Entry<Slot, CardInPlay> entry : joueur.getPlateau().entrySet()) {
		    CardInPlay cip = entry.getValue();
		    if (cip == null) continue;
		    if (cip.exhausted != Inclinaison.COUCHE) continue;
		    CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);
		    joueur.getDefausse().add(snapshot);
		    entry.setValue(null); // on vide le slot sans le supprimer
		    checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.ON_LEAVE)), partie, joueur, opposant, snapshot);
		}
		//check si trigger a résoudre
		
	}

	private void handleResolveEffect(Partie partie, JoueurPartie joueur, JoueurPartie opposant) {
		partie.setEtat_partie(EtatPartie.RESOLVE_EFFECT);
	}
	/***
	 * test pour chaque trigger, qui quoi, pourqoi
	 * @param triggers
	 * @param partie
	 * @param joueur
	 * @param snap CarteJouée
	 */
	private void checkTrigger(ArrayList<TriggerType> triggers, Partie partie, JoueurPartie joueur,JoueurPartie opposant ,CardSnapshot carte_jouee) {
		for(TriggerType trigger : triggers) {
			switch (trigger) {
			case TriggerType.ON_PLAY :
			case TriggerType.ON_ENTER :
				//seule la carte jouée est testée
				resolveEffectsForSnapshot(carte_jouee, trigger, joueur, partie);
				break;
			case TriggerType.ON_ALLIED_UNIT_ENTERS :
				//On teste les autres cartes du plateau joueur
				for(CardInPlay cip : joueur.getPlateau().values()) {
					//cip peut être null car une case de plateau vide contient cip null
					if (cip == null) continue;
					//j'exlue la carte qui a été jouée des tess
					if (!cip.snapshotId.equals(carte_jouee.snapshotId)) continue;
						//donc ici j'ai potentiellement 2 cartes
						CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);
						resolveEffectsForSnapshot(snapshot, trigger, joueur, partie);
				}
				break;
			case TriggerType.ON_TURN_END :
			case TriggerType.ON_TURN_START :
				resolveForBoard(joueur, trigger, partie);
				break;
			default:
				throw new GameActionException("TRIGGER NON GERE");
			}
			
			
		}
		
	}
	
	//une carte pouvant avoir plusieurs effets, parcours les effets de la carte
	private void resolveEffectsForSnapshot(CardSnapshot snapshot,TriggerType trigger,JoueurPartie joueur,Partie partie) {
			for (EffectSnapshot effect : getEffectsByTrigger(snapshot, trigger)) {
				partie.getGamestate().effects_to_resolve.add(new EffectToResolve(effect,snapshot.snapshotId,joueur.getOwner().getId_joueur()));
			}
	}
	//parcour le plateau
	private void resolveForBoard(JoueurPartie joueur,TriggerType trigger,Partie partie) {
		for (CardInPlay cip : joueur.getPlateau().values()) {
			if (cip == null) continue;
			CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);
			resolveEffectsForSnapshot(snapshot, trigger, joueur, partie);
		}
	}
	/**
	 * renvoie les effect Snapshot d'un CardSnapshot qui match avec le Trigger
	 * @param cardSnapshot
	 * @param triggerType
	 * @return
	 */
	private List<EffectSnapshot> getEffectsByTrigger(CardSnapshot cardSnapshot, TriggerType triggerType) {
	    if (cardSnapshot == null || cardSnapshot.effects == null) return Collections.emptyList();
	    return cardSnapshot.effects
	            .stream()
	            .filter(effect -> effect.trigger == triggerType)
	            .collect(Collectors.toList());
	}
	
	private JoueurPartie getJoueur(Partie partie, UUID joueur_id) {
		if (joueur_id.equals(partie.getJ1().getOwner().getId_joueur()))	
			return partie.getJ1();
		return partie.getJ2();

	}
	
	@SuppressWarnings("unused")
	private JoueurPartie getOpposant(Partie partie, UUID joueur_id) {
		if (joueur_id.equals(partie.getJ1().getOwner().getId_joueur()))	
			return partie.getJ2();
		return partie.getJ1();
	}
	private JoueurPartie getOpposant(Partie partie, JoueurPartie joueur) {
		if (partie.getJ1() == joueur)	
			return partie.getJ2();
		return partie.getJ1();
	}
	
}
