package unics.api.game.service;

import java.util.List;
import java.util.Set;

import unics.Enum.AbilityType;
import unics.Enum.EffectConstraint;
import unics.api.game.EffectToResolve;
import unics.game.EtatPartie;
import unics.game.JoueurPartie;
import unics.game.Partie;
import unics.snapshot.EffectSnapshot;

public class EffectService {

	private final AbilityService abilityService;
	private final ContrainteService contrainteService;
	
	public EffectService(AbilityService abilityService, ContrainteService contrainteService) {
		this.abilityService = abilityService;
		this.contrainteService = contrainteService;
	}

	public void handleResolveEffect(Partie partie, JoueurPartie joueur, JoueurPartie opposant, EffectToResolve etr,List<String> cards) {
		
		
	}

	public void resolveEffect(Partie partie, JoueurPartie joueur, JoueurPartie opposant) {
		partie.increaseStep();
		partie.setEtat_partie(EtatPartie.RESOLVE_EFFECT);
		
		boolean is_interactiv = false;
		
		while (!is_interactiv && partie.getGamestate().getCurrentEffect() != null) {
			System.out.println("handleResolveEffect : "+partie.getGamestate().getCurrentEffect().getEffet_source().ability);
			EffectToResolve current = partie.getGamestate().getCurrentEffect();
			EffectSnapshot es = current.getEffet_source();

			boolean is_constraint_respected = true;
			//si des contraintes sont pas respectable : on vide l'effet.
			Set<EffectConstraint> ec = es.constraints;
			if (ec.size() == 0) is_constraint_respected = true;
			else {
				for (EffectConstraint constraint : ec) {
				    // traitement
				    boolean respected = contrainteService.is_Constraint_respected(partie,joueur,opposant,constraint,current);
				    if (!respected) {is_constraint_respected = false;break;}
				}
			}
			
			
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
}
