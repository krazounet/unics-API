package unics.api.game.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import unics.Enum.CardType;
import unics.Enum.EffectConstraint;
import unics.Enum.TargetType;
import unics.api.game.EffectToResolve;
import unics.game.CardInPlay;
import unics.game.JoueurPartie;
import unics.game.JoueurPartie.Slot;
import unics.game.Partie;
import unics.snapshot.EffectSnapshot;

@Service
public class ContrainteService {

	PlateauService plateauService;

    public ContrainteService(CardSnapshotService cardSnapshotService,PlateauService plateauService) {
    	this.plateauService = plateauService;
    }
	
    
    public boolean is_all_constraint_respected (Partie partie, JoueurPartie joueur, JoueurPartie opposant, EffectToResolve source){
    	EffectSnapshot es = source.getEffet_source();
    	
    	boolean is_constraint_respected = true;
		//si des contraintes sont pas respectable : on vide l'effet.
		Set<EffectConstraint> ec = es.constraints;
		if (ec.size() == 0) is_constraint_respected = true;
		else {
			for (EffectConstraint constraint : ec) {
			    // traitement
			    boolean respected = is_Constraint_respected(partie,joueur,opposant,constraint,source);
			    if (!respected) {is_constraint_respected = false;break;}
			}
		}
		
		return is_constraint_respected;
    }
    
    
	public boolean is_Constraint_respected(Partie partie, JoueurPartie joueur, JoueurPartie opposant,EffectConstraint ec, EffectToResolve source) {
		UUID ownerid = source.getOwner_id();
    	JoueurPartie proprio = partie.getJoueurbyUUID(ownerid);
    	JoueurPartie enemy 	 = partie.getOpposant(proprio);
    	boolean is_contrainte_for_target = is_contrainte_for_target(source.getEffet_source(),ec);
    	TargetType target = source.getEffet_source().targetType;
    	JoueurPartie joueur_concerne;
    	if ((is_contrainte_for_target)&&(target == TargetType.ALLY))joueur_concerne=proprio;
    	else joueur_concerne=enemy;
		switch (ec) {

         	case POSITION_LEFT:
         	case POSITION_CENTER:
         	case POSITION_RIGHT:
         		Slot slot = getEnumSlot(ec);
         		
         		CardInPlay cip = proprio.getPlateau().get(slot);
         		if (cip == null) return false;
         		if (cip.snapshotId.equals(source.getCarte_source())) return false;
         		return true;
         	case ALLIED_UNIT_PRESENT :
         		if (plateauService.get_nb_cards_on_board(proprio) > 1) return true;
         		return false;
         	case ENEMY_HAS_STRUCTURE :
         		if (plateauService.get_nb_CardType_on_board(enemy, CardType.STRUCTURE)>0)return true;
         		return false;
         	case HAND_EMPTY :
         		if (proprio.getMain().size()==0) {return true;}
         		return false;
         	case HAND_SIZE_3_OR_LESS :
         		if (proprio.getMain().size()<=3) {return true;}
         		return false;
         	case NO_ENEMY_UNIT ://!!ATTENTION veut dire NO ENEMY CARD
         		if (plateauService.get_nb_cards_on_board(enemy) == 0) return true;
         		return false;
         	case COST_1_OR_LESS :
         		
         	default:
                throw new RuntimeException("Contrainte non géré : " + ec);
		
	}
	}

	private Slot getEnumSlot(EffectConstraint ec) {
		return switch (ec) {
	    case POSITION_LEFT -> Slot.LEFT;
	    case POSITION_CENTER -> Slot.CENTER;
	    case POSITION_RIGHT -> Slot.RIGHT;
		default -> throw new IllegalArgumentException("Unexpected value: " + ec);
		};
	}
	
	boolean is_contrainte_for_target(EffectSnapshot es, EffectConstraint ec ) {
		if (es.ability.isTargetingACard() && ec.appliesToTarget()) return true;
		
		return false;
	}
}
