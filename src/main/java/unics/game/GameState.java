package unics.game;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import unics.api.game.EffectToResolve;

public class GameState {
	public UUID partie;
	
	//QUI AVEC QUOI
	public JoueurPartie J1;
	public JoueurPartie J2;
	
	public EtatPartie etat_partie;
	public PhasePartie phase_partie;
	public UUID joueur_actif;
	
	public int tour;
	public int step;
	
	public List<LogEvent> log;
	public List<EffectToResolve> effects_to_resolve;
	
	@JsonIgnore
	public EffectToResolve getCurrentEffect() {
	    if (effects_to_resolve.isEmpty()) {
	        return null;
	    }
	    return effects_to_resolve.get(0);
	}
}
