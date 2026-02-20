package unics.api.game.service;

import org.springframework.stereotype.Service;

import unics.Enum.EffectConstraint;
import unics.api.game.EffectToResolve;
import unics.game.CardInPlay;
import unics.game.JoueurPartie;
import unics.game.JoueurPartie.Slot;
import unics.game.Partie;

@Service
public class ContrainteService {



    public ContrainteService(CardSnapshotService cardSnapshotService) {
    }
	
	public boolean is_Constraint_respected(Partie partie, JoueurPartie joueur, JoueurPartie opposant,EffectConstraint ec, EffectToResolve source) {
		 switch (ec) {

         	case POSITION_LEFT:
         	case POSITION_CENTER:
         	case POSITION_RIGHT:
         		Slot slot = getEnumSlot(ec);
         		JoueurPartie owner_carte=partie.getJoueurbyUUID(source.getOwner_id());
         		CardInPlay cip = owner_carte.getPlateau().get(slot);
         		if (cip == null) return false;
         		if (cip.snapshotId.equals(source.getCarte_source())) return false;
         		return true;
         		
         	default:
                throw new RuntimeException("Trigger non géré : " + ec);
		
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
}
