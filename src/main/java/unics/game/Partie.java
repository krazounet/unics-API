package unics.game;

import java.util.Random;
import java.util.UUID;


public class Partie {
	
	//TECHNIQUE
	private UUID id_partie;
	private long rngSeed;
	private Random rng;
	
	//QUI AVEC QUOI
	private JoueurPartie J1;
	private JoueurPartie J2;
	
	//COMMENT
	private EtatPartie etat_partie;
	private PhasePartie phase_partie;
	private UUID joueur_actif;
	
	private int tour;
	private int step;
	
	//state
	private GameState gamestate;
	
	
	
	public GameState getGamestate() {
		return gamestate;
	}
	public void setGamestate(GameState gamestate) {
		this.gamestate = gamestate;
	}
	public UUID getId_partie() {
		return id_partie;
	}
	public void setId_partie(UUID id_partie) {
		this.id_partie = id_partie;
	}
	public long getRngSeed() {
		return rngSeed;
	}
	public void setRngSeed(long rngSeed) {
		this.rngSeed = rngSeed;
	}
	public Random getRng() {
		return rng;
	}
	public void setRng(Random rng) {
		this.rng = rng;
	}
	public JoueurPartie getJ1() {
		return J1;
	}
	public void setJ1(JoueurPartie j1) {
		J1 = j1;
	}
	public JoueurPartie getJ2() {
		return J2;
	}
	public void setJ2(JoueurPartie j2) {
		J2 = j2;
	}
	public EtatPartie getEtat_partie() {
		return etat_partie;
	}
	public void setEtat_partie(EtatPartie etat_partie) {
		this.etat_partie = etat_partie;
	}
	public PhasePartie getPhase_partie() {
		return phase_partie;
	}
	public void setPhase_partie(PhasePartie phase_partie) {
		this.phase_partie = phase_partie;
	}
	public UUID getJoueur_actif() {
		return joueur_actif;
	}
	public void setJoueur_actif(UUID joueur_actif) {
		this.joueur_actif = joueur_actif;
	}
	public int getTour() {
		return tour;
	}
	public void setTour(int tour) {
		this.tour = tour;
	}
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	
	
	
	
}
