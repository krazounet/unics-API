package unics.api.game.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import unics.Enum.AbilityType;
import unics.Enum.CardType;
import unics.Enum.Faction;
import unics.Enum.Keyword;
import unics.Enum.TargetType;
import unics.Enum.TriggerType;
import unics.api.game.EffectToResolve;
import unics.api.game.GameActionException;
import unics.game.CardInPlay;
import unics.game.CardInPlay.Inclinaison;
import unics.game.JoueurPartie;
import unics.game.JoueurPartie.Slot;
import unics.game.LogEvent;
import unics.game.Partie;
import unics.snapshot.CardSnapshot;
import unics.snapshot.EffectSnapshot;

@Service
public class PlateauService {

    private final CardSnapshotService cardSnapshotService;
    private final TriggerService triggerService;

    public PlateauService(CardSnapshotService cardSnapshotService,
                          TriggerService triggerService) {
        this.cardSnapshotService = cardSnapshotService;
        this.triggerService = triggerService;
    }

    public void handleFade(Partie partie,JoueurPartie joueur,JoueurPartie opposant) {

        partie.increaseStep();
        partie.setPhase_partie(unics.game.PhasePartie.FADE);

        // 1️⃣ Défausse des cartes couchées
        for (Map.Entry<Slot, CardInPlay> entry : joueur.getPlateau().entrySet()) {

            CardInPlay cip = entry.getValue();
            if (cip == null) continue;
            if (cip.exhausted != Inclinaison.COUCHE) continue;

            CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);
            
            sendToGraveyard(partie,joueur,snapshot,entry.getKey(),TriggerType.ON_LEAVE);

        }

        // 2️⃣ TRAVERS → COUCHE
        for (CardInPlay cip : joueur.getPlateau().values()) {

            if (cip == null) continue;
            if (cip.exhausted != Inclinaison.TRAVERS) continue;

            CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);

            if (!snapshot.keywords.contains(Keyword.PERSISTANT)) {
                cip.exhausted = Inclinaison.COUCHE;
            }
        }

        // 3️⃣ DROIT → TRAVERS
        for (CardInPlay cip : joueur.getPlateau().values()) {

            if (cip == null) continue;
            if (cip.exhausted != Inclinaison.DROIT) continue;

            CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);

            if (!snapshot.keywords.contains(Keyword.PERSISTANT)) {
                cip.exhausted = Inclinaison.TRAVERS;
            }
        }
    }
    public void playCard(Partie partie,JoueurPartie joueur,JoueurPartie opposant,CardSnapshot snap,JoueurPartie.Slot slot) {

    	//TEST UNIQUE
    	if ((snap.keywords.contains(Keyword.UNIQUE))&&(has_player_keyword(joueur, Keyword.UNIQUE))) {
    		throw new GameActionException("Un seul UNIQUE possible en jeu");
    	}
    	
        //8 retrait main
        joueur.getMain().remove(snap);

        //9 ajout plateau
        //=> je dois creer une cardInPlay qui correspond au snap
        CardInPlay cip = new CardInPlay(snap);
        cip.tour_invocation = partie.getTour(); //permet de traquer le tour ou est invoquée, donc si elle peut attaquer
        joueur.getPlateau().put(slot, cip);

        //10retrait mana
        joueur.retireMana(snap.cost);

    }


    /***
     * 
     * @param partie
     * @param joueur
     * @param opposant
     * @param cip
     */
	public void handleCardInPlayAttackHero(Partie partie, JoueurPartie joueur, JoueurPartie opposant, CardInPlay cip) {
		CardSnapshot snap = cardSnapshotService.getById(cip.snapshotId);
		if (cip.cardType != CardType.UNIT) {throw new GameActionException("Seules les UNIT peuvent attaquer");}
		//check si le plateau adverse contient un DEFENSIF
		if (has_player_keyword(opposant, Keyword.DEFENSIF)&&(!snap.keywords.contains(Keyword.FURTIF))){
			throw new GameActionException("Vous devez en priorité attaquer les defensifs ou possédez le keyword Furtif");
			} 
		//TEST SANGUINAIRE : si la carte qui attaque a KW Sanguinaire il doit en priorité attaquer le board
		if (snap.keywords.contains(Keyword.SANGUINAIRE)) {
			boolean has_insaisissable = snap.keywords.contains(Keyword.INSAISISSABLE);
			if (has_player_card_on_board(opposant)&&has_insaisissable) {
				throw new GameActionException("Sanguinaire vous oblige a attaquer en priorité le board adverse");
			}
			if (has_player_card_on_board(opposant)&&(!has_player_keyword(opposant, Keyword.INSAISISSABLE))&&(!has_insaisissable)) {
				throw new GameActionException("Sanguinaire vous oblige a attaquer en priorité le board adverse");
			}
		}
		
		int degat = cip.attack; //cip.attack correspond a la valeur modifiée de buff/debuff du snapshot
		int hp_initial = opposant.getHp();
		int hp_final = Math.max(0, hp_initial-degat);
		opposant.setHp(hp_final);
		//LOG A METTRE
		////PAS SUR DU TOUT !!! ////
		///
		triggerService.checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.AFTER_ATTACK)), partie, joueur, snap);
	}
	
	public void handleCardInPlayAttackCard(Partie partie, JoueurPartie joueur, JoueurPartie opposant, CardInPlay cip,CardInPlay cip_adverse,Slot slot,Slot slot_adverse) {
		CardSnapshot snap = cardSnapshotService.getById(cip.snapshotId);
		CardSnapshot snap_adverse = cardSnapshotService.getById(cip_adverse.snapshotId);
		
		if (cip.cardType != CardType.UNIT) {throw new GameActionException("Seules les UNIT peuvent attaquer");}
		
		//si la carte adverse n'a pas DEFENSIF, mais qu'il y a un DEFENSIF en face
		if ((!snap_adverse.keywords.contains(Keyword.DEFENSIF))
			&&(has_player_keyword(opposant, Keyword.DEFENSIF))
			&&(!snap.keywords.contains(Keyword.FURTIF))) {
			throw new GameActionException("Vous devez en priorité attaquer les defensifs ou possédez le keyword Furtif");
		}
		//insaisissable
		if((snap_adverse.keywords.contains(Keyword.INSAISISSABLE))&&(!snap.keywords.contains(Keyword.INSAISISSABLE))) {
			throw new GameActionException("Une carte Insaisissable ne peut être attaqué que par une carte insaisissable");
		}
		//peut attquer/mal d'invocation / frappe immédiate
		if ((cip.tour_invocation >= partie.getTour())&&(!snap.keywords.contains(Keyword.FRAPPE_IMMEDIATE))){
			throw new GameActionException("Seules les cartes avec Frappes Immédiates peuvent attaquer le tour où elles sont posées");
		}
		int degat_sur_cip = cip_adverse.attack;
		int degat_sur_cip_adv = cip.attack;
		
		
		//  immunité 
		if (snap.keywords.contains(getImmunityByFaction(snap_adverse.faction))) {
			degat_sur_cip=0;
		}
		if (snap_adverse.keywords.contains(getImmunityByFaction(snap.faction))) {
			degat_sur_cip_adv=0;
		}
		// first_strike 
		if((snap.keywords.contains(Keyword.FIRST_STRIKE))&&(!snap_adverse.keywords.contains(Keyword.FIRST_STRIKE))){
			//snap frappe en premier.
			if (degat_sur_cip_adv>cip_adverse.health) {
				degat_sur_cip=0;
			}
		}
		if((!snap.keywords.contains(Keyword.FIRST_STRIKE))&&(snap_adverse.keywords.contains(Keyword.FIRST_STRIKE))){
			//snap_adverse frappe en premier.
			if (degat_sur_cip>cip.health) {
				degat_sur_cip_adv = 0;
			}
		}
		
		cip.health			-= degat_sur_cip;
		int current_dmg = cip.effects.getOrDefault("BLESSURE", 0);
		current_dmg +=degat_sur_cip;
		cip.effects.put("BLESSURE", current_dmg);
		
		cip_adverse.health	-= degat_sur_cip_adv;
		current_dmg = cip_adverse.effects.getOrDefault("BLESSURE", 0);
		current_dmg +=degat_sur_cip_adv;
		cip_adverse.effects.put("BLESSURE", current_dmg);
		
		if (cip.health <=0) {
			sendToGraveyard(partie,joueur,snap,slot,TriggerType.ON_DEATH);
		}
		if (cip_adverse.health <= 0) {
			sendToGraveyard(partie,opposant,snap_adverse,slot_adverse,TriggerType.ON_DEATH);
		}
		
		//trigger
	
		if (degat_sur_cip >0) {
			triggerService.checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.AFTER_DAMAGE)), partie, opposant, snap_adverse);
			triggerService.checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.AFTER_RECEIVE_DAMAGE)), partie, joueur,  snap);
			}
		if (degat_sur_cip_adv >0) { 
			triggerService.checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.AFTER_DAMAGE)), partie, joueur,snap);
			triggerService.checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.AFTER_RECEIVE_DAMAGE)), partie, opposant, snap_adverse);
		}
		
		triggerService.checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.AFTER_BEING_ATTACKED)), partie, opposant, snap_adverse);
		triggerService.checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.AFTER_ATTACK)), partie, joueur, snap);
		//KW trample
		if ((snap.keywords.contains(Keyword.TRAMPLE))&&(degat_sur_cip_adv>cip_adverse.health)) {
			int degat_pc_adverse = Math.max(degat_sur_cip_adv-cip_adverse.health,0);
			int hp_initial = opposant.getHp();
			int hp_final = Math.max(0, hp_initial-degat_pc_adverse);
			opposant.setHp(hp_final);
			triggerService.checkTrigger(new ArrayList<TriggerType>(List.of(TriggerType.PC_DAMAGED)), partie, opposant, snap);
		}
		
		
	}  
	
	private void sendToGraveyard(Partie partie, JoueurPartie joueur_concerne, CardSnapshot snap_concerne, Slot slot, TriggerType trigger) {
		joueur_concerne.getDefausse().add(snap_concerne);
        //entry.setValue(null); // on garde la structure
        joueur_concerne.getPlateau().put(slot,null);
        System.out.println("LOG IS NULL ? " + (partie.getGamestate().log == null));
        partie.getGamestate().log.add(new LogEvent(snap_concerne.name+" > est mort",snap_concerne.name+" > is dead",null,null,snap_concerne.snapshotId.toString(),null));
        triggerService.checkTrigger(List.of(trigger),partie,joueur_concerne,snap_concerne);
		
	}

	/***
	 * renvoie true si le joueur concerné a au moins une carte en jeu
	 * @param joueur_concerne
	 * @return
	 */
	private boolean has_player_card_on_board(JoueurPartie joueur_concerne) {
		for (Map.Entry<Slot, CardInPlay> entry : joueur_concerne.getPlateau().entrySet()) {
			CardInPlay cip = entry.getValue();
            if (cip != null) return true;
		}
		return false;
	}

	/***
	 * renvoie true si une carte du plateau du joueur concerné a le kw
	 * @param joueur_concerne
	 * @param kw
	 * @return
	 */
	public boolean has_player_keyword(JoueurPartie joueur_concerne, Keyword kw) {
    	for (Map.Entry<Slot, CardInPlay> entry : joueur_concerne.getPlateau().entrySet()) {
            CardInPlay cip = entry.getValue();
            if (cip == null) continue;
            CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);
            if (snapshot.keywords.contains(kw))return true;
    	}
    	return false;
    }

	public void handleMOBILEkeyword(Partie partie, JoueurPartie joueur, JoueurPartie opposant) {
		for (Map.Entry<Slot, CardInPlay> entry : joueur.getPlateau().entrySet()) {
            CardInPlay cip = entry.getValue();
            if (cip == null) continue;
            CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);
            if (snapshot.keywords.contains(Keyword.MOBILE)) {
				//chercher le snapid de la carte
				partie.getGamestate().effects_to_resolve.add(
	                    new EffectToResolve(
	                            new EffectSnapshot(null, null, AbilityType.MOVE_SELF, 1, TargetType.SLOT, null, null),
	                            snapshot.snapshotId,
	                            joueur.getOwner().getId_joueur()
	                    ));
				
            }
		}
		
	}
	public Faction getFactionbyImmunity(Keyword immunity) {
		Faction faction = switch (immunity) {
    	case IMMUNITE_CONTRE_ASTRAL 	-> Faction.ASTRAL;
    	case IMMUNITE_CONTRE_MECHANICAL -> Faction.MECHANICAL;
    	case IMMUNITE_CONTRE_NOMAD 		-> Faction.NOMAD;
    	case IMMUNITE_CONTRE_OCCULT 	-> Faction.OCCULT;
    	case IMMUNITE_CONTRE_ORGANIC 	-> Faction.ORGANIC;
		default -> throw new IllegalArgumentException("Unexpected value: " + immunity);
	};
		return faction;
	}
	public Keyword getImmunityByFaction(Faction faction) {
		Keyword immunity = switch (faction) {
    	case Faction.ASTRAL 	-> Keyword.IMMUNITE_CONTRE_ASTRAL;
    	case Faction.MECHANICAL 	-> Keyword.IMMUNITE_CONTRE_MECHANICAL;
    	case Faction.NOMAD 	-> Keyword.IMMUNITE_CONTRE_NOMAD;
    	case Faction.OCCULT 	-> Keyword.IMMUNITE_CONTRE_OCCULT;
    	case Faction.ORGANIC 	-> Keyword.IMMUNITE_CONTRE_ORGANIC;
		default -> throw new IllegalArgumentException("Unexpected value: " + faction);
	};
		return immunity;
	}
	
}