package unics.api.game.service;



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import unics.Enum.CardType;
import unics.Enum.TriggerType;
import unics.api.game.GameActionException;
import unics.game.CardInPlay;
import unics.game.EtatPartie;
import unics.game.GameState;
import unics.game.JoueurPartie;
import unics.game.JoueurPartie.Slot;
import unics.game.LogEvent;
import unics.game.Partie;
import unics.game.PhasePartie;
import unics.game.db.JdbcPartieDao;
import unics.snapshot.CardSnapshot;

@Service
public class GameService {

//	private final CardSnapshotService cardSnapshotService;
	private final JdbcPartieDao partieDao;
	
	private final PlateauService plateauService;
	private final TriggerService triggerService;
	private final AbilityService abilityService;
	private final ContrainteService contrainteService;
	private final EffectService effectService;
	
	public GameService(CardSnapshotService cardSnapshotService,
            JdbcPartieDao partieDao,
            TriggerService triggerService,
            PlateauService plateauService,
            ContrainteService contrainteService,
            AbilityService abilityService,
            EffectService effectService
			) {

//		this.cardSnapshotService = cardSnapshotService;
		this.partieDao = partieDao;
		this.triggerService = triggerService;
		this.plateauService = plateauService;
		this.contrainteService = contrainteService;
		this.abilityService =abilityService;
		this.effectService = effectService;
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
		JoueurPartie joueur = partie.getJoueurActifbyUUID(uuid_joueur_actif);
		
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
		System.out.println("GameService.handlePlayCard : "+playerId+" / "+card_uuid+"/"+position);
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
		JoueurPartie joueur = partie.getJoueurActifbyUUID(uuid_joueur_actif);
		JoueurPartie opposant = partie.getOpposant(joueur);
		
		
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
		
		plateauService.playCard(partie,joueur,opposant,snap,slot);
		
		//12 log event
		LogEvent log = new LogEvent(joueur.getOwner().getPseudo()+" a joué "+snap.name+" à "+position,"en",joueur.getOwner().getId_joueur().toString(),"",snap.snapshotId.toString(),null);
		partie.getGamestate().log.add(log);
		
		//10 check trigger
		triggerService.checkTrigger(new ArrayList<TriggerType>(List.of(
												TriggerType.ON_ENTER, 
												TriggerType.ON_PLAY, 
												TriggerType.ON_ALLIED_UNIT_ENTERS												
											)),	partie,	joueur,snap);
		
		//11 Passer à la phase suivante // si aucune carte dispo, ni slot
		if (!partie.getGamestate().effects_to_resolve.isEmpty()) {
			System.out.println("GameService.handlePlayCard Effet à résoudre à la pause");
			effectService.resolveEffect(partie,joueur,opposant);
		}
		partie.increaseStep();
		
		partieDao.update(partie);

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
		JoueurPartie joueur = partie.getJoueurActifbyUUID(uuid_joueur_actif);
		
		
		CardInPlay cip = joueur.getPlateau().get(slot);
		
		////////////////!!!!!!!!!!!!! FRAPPE IMMEDIATE NON GERREE !!!!!!!!--------
		if (cip != null && cip.cardType == CardType.UNIT && cip.tour_invocation > partie.getTour()) {
			//aut il un test supplémentaire ? y'a til des cas ou on a une unit et on ne peut pas attaquer ?
			throw new GameActionException("Player must Attack with "+cip.snapshotId+" / "+position+" / "+cip.tour_invocation);
		}

		
		PhasePartie nextPhase = switch (slot) {
	    	case LEFT -> PhasePartie.ATTACK_CENTER;
	    	case CENTER -> PhasePartie.ATTACK_RIGHT;
	    	case RIGHT -> PhasePartie.TURN_END; // ou END_PHASE selon ton design
		};
		
		
		partie.increaseStep();
		partie.setPhase_partie(nextPhase);	
		partie.setEtat_partie(EtatPartie.RUNNING);
		
		if (nextPhase  == PhasePartie.TURN_END)
			endOfTurnPhase(partie, joueur,partie.getOpposant(joueur));
		
		partieDao.update(partie);
	    return partie.getGamestate();
	}
	
	public GameState handleAtkAction(String gameId, String playerId, String source, String cible) {
		System.out.println("GameService.handleAtkAction");
		Partie partie = loadPartie(UUID.fromString(gameId));
		UUID uuid_joueur_actif = UUID.fromString(playerId);
		//check joueur actif ok
		if (!uuid_joueur_actif.equals(partie.getJoueur_actif())) {
			throw new GameActionException("joueur actif != playerid");
		}
		//3 verifier phase / etat
		Slot slot = Slot.valueOf(source.toUpperCase());
		PhasePartie expectedPhase = switch (slot) {
	    	case LEFT -> PhasePartie.ATTACK_LEFT;
	    	case CENTER -> PhasePartie.ATTACK_CENTER;
	    	case RIGHT -> PhasePartie.ATTACK_RIGHT;
		};
		if (partie.getPhase_partie() != expectedPhase) {
			    throw new GameActionException("Phase mismatch : " + partie.getPhase_partie() + " / " + slot);
		}
		//4 devait il attaquer. est ce que son slot gauche contenait une unité
		JoueurPartie joueur = partie.getJoueurActifbyUUID(uuid_joueur_actif);
		JoueurPartie opposant = partie.getOpposant(joueur);
		CardInPlay cip = joueur.getPlateau().get(slot);
		if (cip == null || cip.cardType != CardType.UNIT)throw new GameActionException("only unit attack "+cip.snapshotId+" / "+source);
		
		
		
		//2 cas possible cible == heros ou cible == slot.
		if (cible.equals("HERO")) {
			plateauService.handleCardInPlayAttackHero(partie,joueur,opposant, cip);
			
			if (opposant.getHp()==0) {
				checkvictory(partie,opposant);
				partieDao.update(partie);
			    return partie.getGamestate();
			}
			//comme PC_DAMAGED peut être autant sur soit joueur que sur opposant on ne donne a check que celui que ça concerne, donc ici : OPPOSANT
			triggerService.checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.PC_DAMAGED)), partie, opposant, null);
		}
		else {
			Slot slot_adverse = Slot.valueOf(cible.toUpperCase());
			CardInPlay cip_adverse = opposant.getPlateau().get(slot_adverse);
			plateauService.handleCardInPlayAttackCard(partie,joueur,opposant, cip,cip_adverse, slot, slot_adverse);

		}
		if (!partie.getGamestate().effects_to_resolve.isEmpty()) {
			System.out.println("GameService.handleAtkAction Effet à résoudre à la pause");
			effectService.resolveEffect(partie,joueur,opposant);
		}
		else {
			PhasePartie nextPhase = switch (slot) {
    			case LEFT -> PhasePartie.ATTACK_CENTER;
    			case CENTER -> PhasePartie.ATTACK_RIGHT;
    			case RIGHT -> PhasePartie.TURN_END; // ou END_PHASE selon ton design
    			
			};
			partie.setPhase_partie(nextPhase);	
			partie.setEtat_partie(EtatPartie.RUNNING);
		}
		
		partie.increaseStep();
		partieDao.update(partie);
	    return partie.getGamestate();


	}
	
	public GameState handleResolveEffect(String gameId, String playerId, List<String> cards) {
		System.out.println("GameService.handleResolveEffect");
		Partie partie = loadPartie(UUID.fromString(gameId));
		UUID uuid_joueur_actif = UUID.fromString(playerId);
		//check joueur actif ok
		if (!uuid_joueur_actif.equals(partie.getJoueur_actif()))   throw new GameActionException("joueur actif != playerid");
		if (partie.getEtat_partie() != EtatPartie.RESOLVE_EFFECT)  throw new GameActionException("Not in Resolve state");
	    
		
		
		
		JoueurPartie joueur=partie.getJoueurActifbyUUID(uuid_joueur_actif);
		
		effectService.handleResolveEffect(partie,joueur,partie.getOpposant(joueur),partie.getGamestate().getCurrentEffect(),cards);
		
		//check à faire si poursuite ou pas.
		
		partie.increaseStep();
		partieDao.update(partie);
	    return partie.getGamestate();
	}
	


	/**
	 * Appelée apres l'attaque de droite OU pass de droite OU resolve effect en phase AttackRight
	 * @param partie
	 * @param opposant 
	 * @param joueur 
	 */
	private void endOfTurnPhase(Partie partie, JoueurPartie joueur, JoueurPartie opposant) {
		partie.increaseStep();
		partie.setPhase_partie(PhasePartie.TURN_END);
		partie.setEtat_partie(EtatPartie.RUNNING);

		triggerService.checkTrigger(List.of(TriggerType.ON_TURN_END), partie, joueur, null);

		continueFlow(partie, joueur, opposant);
	}



	private void startOfTurnPhase(Partie partie, JoueurPartie joueur, JoueurPartie opposant) {
		partie.increaseStep();
		partie.setPhase_partie(PhasePartie.TURN_START);
		partie.setEtat_partie(EtatPartie.RUNNING);

		// ✅ Nouveau tour si retour à J1
		if (partie.getJ1()==joueur)partie.increaseTour();
		
		LogEvent log = new LogEvent("Nouveau tour : "+joueur.getOwner().getPseudo()+" de jouer !","New Turn",joueur.getOwner().getId_joueur().toString(),null,null,null);
		partie.getGamestate().log.add(log);
		
		triggerService.checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.ON_TURN_START)), partie, joueur, null);
		
		//test si trigger a résoudre ou pas.
		//continueFlow(partie, joueur, opposant);
		
	}
	
	private void energieRefreshPhase(Partie partie, JoueurPartie joueur, JoueurPartie opposant) {
		partie.increaseStep();
		partie.setPhase_partie(PhasePartie.ENERGY_REFRESH);
		partie.setEtat_partie(EtatPartie.RUNNING);
		
		//pending mana
		int energy = joueur.getCompteurs().getOrDefault("ENERGY", 0);
		int mana_dispo = partie.getTour()+energy;
		if (mana_dispo <0) mana_dispo =0;
		joueur.setMana_dispo(mana_dispo);
		if (energy!=0) {
			LogEvent log = new LogEvent("Application Bonus/Malus énergie : "+energy,"",joueur.getOwner().getId_joueur().toString(),null,null,null);
			partie.getGamestate().log.add(log);
		}
			
		
		joueur.getCompteurs().put("ENERGY", 0);
		//###JE NE CONNAIS PAS DE TRIGGER AU MANA
		
	}

	private void activationPhase(Partie partie, JoueurPartie joueur, JoueurPartie opposant) {
		System.out.println("GameService.handleActivation");
		partie.increaseStep();
		partie.setPhase_partie(PhasePartie.ACTIVATION);
		partie.setEtat_partie(EtatPartie.RUNNING);
		triggerService.checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.ON_ACTIVATION)), partie, joueur, null);
		plateauService.handleMOBILEkeyword(partie,joueur,opposant);
		
	}

	private void drawPhase(Partie partie, JoueurPartie joueur, JoueurPartie opposant) {
		partie.increaseStep();
		partie.setPhase_partie(PhasePartie.DRAW);
		partie.setEtat_partie(EtatPartie.RUNNING);
		//verfi deck pas vide
		if (joueur.getDeck().size()==0) {
			LogEvent log = new LogEvent("Deck vide","Empty Deck",joueur.getOwner().getId_joueur().toString(),null,null,null);
			partie.getGamestate().log.add(log);
		}
		else {
			//verif nb carte en main
			
			int nb_cartes_main = joueur.getMain().size();
			int capacite_pioche= 7- nb_cartes_main; //MAGIC NUMBER
			if (capacite_pioche > 0)joueur.piocheXcartes(1);
			LogEvent log = new LogEvent(joueur.getOwner().getPseudo()+" pioche une carte","Draw 1",joueur.getOwner().getId_joueur().toString(),null,null,null);
			partie.getGamestate().log.add(log);
		}
	}
	
	
	private void continueFlow(Partie partie,JoueurPartie joueur,JoueurPartie opposant) {

// 1️⃣ PRIORITÉ ABSOLUE : effets à résoudre
		if (!partie.getGamestate().effects_to_resolve.isEmpty()) {
			partie.setEtat_partie(EtatPartie.RESOLVE_EFFECT);
			return;
		}

// 2️⃣ sinon on avance selon la phase
		switch (partie.getPhase_partie()) {

		case TURN_END:
			JoueurPartie tmp = joueur;
		    joueur = opposant;
		    opposant = tmp;
		    partie.getGamestate().joueur_tour = joueur.getOwner().getId_joueur();
		    partie.setJoueur_actif(joueur.getOwner().getId_joueur());
			startOfTurnPhase(partie, joueur, opposant);
			continueFlow(partie, joueur, opposant);
			break;

		case TURN_START:
			//partie.setPhase_partie(PhasePartie.FADE);
			drawPhase(partie, joueur, opposant);
			continueFlow(partie, joueur, opposant);
			break;
		case DRAW:
			plateauService.handleFade(partie, joueur, opposant);
			continueFlow(partie, joueur, opposant);
			break;
		case FADE:
			energieRefreshPhase(partie, joueur, opposant);
			continueFlow(partie, joueur, opposant);
			break;

		case ENERGY_REFRESH:
			activationPhase(partie, joueur, opposant);
			continueFlow(partie, joueur, opposant);
			break;
		case ACTIVATION ://apres activation, stop la boucle puisque c'est PLAY_CARDS
			partie.setPhase_partie(PhasePartie.PLAY_CARDS);
			partie.setEtat_partie(EtatPartie.RUNNING);
			break;
		default:
			break;
		}
	}

	private void checkvictory(Partie partie, JoueurPartie joueur_mort) {
		System.out.println("GameService.checkvictory");
		if (joueur_mort.getHp()>0) throw new GameActionException("bizarre, il est pas mort..."); 
		JoueurPartie joueur_victorieux = partie.getOpposant(joueur_mort);
		LogEvent log = new LogEvent(joueur_victorieux.getOwner().getPseudo()+" gagne la partie",joueur_victorieux.getOwner().getPseudo()+" win the game",joueur_victorieux.getOwner().getId_joueur().toString(),null,null,null);
		partie.getGamestate().log.add(log);
		partie.increaseStep();
		partie.setEtat_partie(EtatPartie.FINISHED);
		//calcul ELO
		//calcul xp
		//calcul pass
		
	}

	

	
}
