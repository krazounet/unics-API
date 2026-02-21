package unics.api.game.service;

import org.springframework.stereotype.Service;

import unics.api.game.EffectToResolve;
import unics.game.JoueurPartie;
import unics.game.LogEvent;
import unics.game.Partie;
import unics.snapshot.CardSnapshot;

@Service
public class AbilityService {

	private final CardSnapshotService cardSnapshotService;
	
	
	
	
	public AbilityService(CardSnapshotService cardSnapshotService) {
		super();
		this.cardSnapshotService = cardSnapshotService;
	}




	public void resolveAbility(Partie partie, JoueurPartie joueur, JoueurPartie opposant, EffectToResolve current) {
		switch (current.getEffet_source().ability) {
		case ENERGY_GAIN_SELF:
			int energy = joueur.getCompteurs().getOrDefault("ENERGY", 0);
			energy += current.getEffet_source().value;
			joueur.getCompteurs().put("ENERGY", energy);
			CardSnapshot snap = cardSnapshotService.getById(current.getCarte_source());
			partie.getGamestate().log.add(new LogEvent(snap.name+" > Gain énergie : "+current.getEffet_source().value,"",null,null,current.getCarte_source().toString(),null));
			break;
		default:
			System.out.print("Ability non géré : ");
            throw new RuntimeException("Ability non géré : " + current.getEffet_source().ability);
		}
		
	}

}
