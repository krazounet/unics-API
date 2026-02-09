package unics.game;

import java.util.List;
import java.util.UUID;

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
}
