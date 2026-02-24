package unics.api.game.service;

import java.util.List;
import java.util.UUID;

import unics.Enum.AbilityType;
import unics.api.game.EffectToResolve;
import unics.api.game.GameActionException;
import unics.game.EtatPartie;
import unics.game.JoueurPartie;
import unics.game.Partie;
import unics.snapshot.EffectSnapshot;

public class EffectService {

	private final AbilityService abilityService;
	private final ContrainteService contrainteService;
	private final PlateauService plateauService;
	
	public EffectService(AbilityService abilityService, ContrainteService contrainteService,PlateauService plateauService) {
		this.abilityService 	= abilityService;
		this.contrainteService 	= contrainteService;
		this.plateauService		= plateauService;
	}

	public void handleResolveEffect(Partie partie, JoueurPartie joueur, JoueurPartie opposant, EffectToResolve etr,List<String> cards) {
		//check si bon nombre de cible.
		EffectSnapshot es = etr.getEffet_source();
		
		
		int max_cible_attendue = get_max_cibles_attendues(es.ability,es.value);
		if (max_cible_attendue < cards.size()) throw new GameActionException("trop de cible(s)");
		
		//OQP des contraintes.
		//Est ce que c'est une contrainte de possession ou de ciblage ?
		boolean is_constraint_respected = contrainteService.is_all_constraint_respected(partie, joueur, opposant, etr);
		
		
		boolean is_ability_negative_for_owner = es.ability.isNegativeForOwner();
		int nb_target = plateauService.get_nb_cards_on_board(joueur);
		//
		
	}

	public void resolveEffect(Partie partie, JoueurPartie joueur, JoueurPartie opposant) {
		partie.increaseStep();
		partie.setEtat_partie(EtatPartie.RESOLVE_EFFECT);
		
		boolean is_interactiv = false;
		
		while (!is_interactiv && partie.getGamestate().getCurrentEffect() != null) {
			System.out.println("handleResolveEffect : "+partie.getGamestate().getCurrentEffect().getEffet_source().ability);
			EffectToResolve current = partie.getGamestate().getCurrentEffect();
			EffectSnapshot es = current.getEffet_source();
			
			boolean is_constraint_respected = contrainteService.is_all_constraint_respected(partie, joueur, opposant, current);
			
			AbilityType at = es.ability;
			boolean required_target = at.requiresTarget();
			System.out.println("handleResolveEffect is_constraint_respected: "+is_constraint_respected + "    required_target :"+required_target);
			//3 cas possible. 
			// =============================
	        //  CAS 1 : Contrainte impossible
	        // =============================

	        if (!is_constraint_respected) {

	            // On supprime l'effet courant
	            partie.getGamestate().popCurrentEffect();

	            continue; // on passe au suivant
	        }
	        // =============================
	        //  CAS 2 : Pas de cible requise
	        // =============================

	        if (!required_target) {
	        	System.out.println("handleResolveEffect resolve ability");
	            abilityService.resolveAbility(partie, joueur, opposant, current);
	            // On retire l’effet de la pile
	            partie.getGamestate().popCurrentEffect();
	            continue;
	        }
	        // =============================
	        //  CAS 3 : Cible requise
	        // =============================

	        if (required_target) {
	            // On arrête la boucle → attente input joueur
	            is_interactiv = true;
	        }
	        
			
		}
		if (partie.getGamestate().getCurrentEffect() == null) {
			partie.setEtat_partie(EtatPartie.RUNNING);
		}
		
	}

	public int get_max_cibles_attendues(AbilityType ability, Integer value) {
		switch (ability) {
		case BUFF,DAMAGE_UNIT_ALLY,DAMAGE_UNIT_ENEMY,DEBUFF_ALLY,DEBUFF_ENEMY :
			return 1; 
		case DESTROY_STRUCTURE_ENEMY,DESTROY_UNIT_ENEMY,MOVE_ALLY,MOVE_ENEMY,TAP_ALLY,TAP_ENEMY,UNTAP_ALLY,UNTAP_ENEMY :
			return value;
		
		default : return 0;
		}
		
	}
	
	
	
}
