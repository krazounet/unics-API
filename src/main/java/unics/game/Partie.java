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
		gamestate.etat_partie = etat_partie;
	}
	public PhasePartie getPhase_partie() {
		return phase_partie;
	}
	public void setPhase_partie(PhasePartie phase_partie) {
		this.phase_partie = phase_partie;
		gamestate.phase_partie = phase_partie;
	}
	public UUID getJoueur_actif() {
		return joueur_actif;
	}
	public void setJoueur_actif(UUID joueur_actif) {
		this.joueur_actif = joueur_actif;
		gamestate.joueur_actif = joueur_actif;
	}
	public int getTour() {
		return tour;
	}
	public void setTour(int tour) {
		this.tour = tour;
		gamestate.tour = tour;
	}
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	public void advanceIfBothMulliganed(JoueurPartie joueur) {
		//test si J1 et J2 ont mulligan
		increaseStep();
		if (joueur == J1) { //alors on demande à J2
			updateJoueurActif(J2.getOwner().getId_joueur());
			
		}else {//Si c'est J2 => go phase suivante
			updateJoueurActif(J1.getOwner().getId_joueur());
			phase_partie		  	= PhasePartie.PLAY_CARDS;
			gamestate.phase_partie  = PhasePartie.PLAY_CARDS;
			etat_partie				= EtatPartie.RUNNING;
			gamestate.etat_partie 	= EtatPartie.RUNNING;
			tour = 1;
			gamestate.tour = 1;
			gamestate.J1.setMana_dispo(1);
			gamestate.J2.setMana_dispo(1);
					
			
		}

		

		
	}
	
	public void updateJoueurActif(UUID id_joueur_actif) {
		joueur_actif = id_joueur_actif;
		gamestate.joueur_actif = id_joueur_actif;
	}
	public void increaseStep() {
		step++;
		gamestate.step++;
	}
	public void increaseTour() {
		tour++;
		gamestate.tour++;
		
	}
	public JoueurPartie getJoueurbyUUID(UUID owner_id) {
		if (J1.getOwner().id_joueur.equals(owner_id))
			return J1;
		return J2;
	}
	public JoueurPartie getJoueurActifbyUUID(UUID joueur_id) {
		if (joueur_id.equals(getJ1().getOwner().getId_joueur()))	
			return getJ1();
		return getJ2();
	
	}
	@SuppressWarnings("unused")
	private JoueurPartie getOpposant(UUID joueur_id) {
		if (joueur_id.equals(getJ1().getOwner().getId_joueur()))	
			return getJ2();
		return getJ1();
	}
	public JoueurPartie getOpposant(JoueurPartie joueur) {
		if (getJ1() == joueur)	
			return getJ2();
		return getJ1();
	}
	
	
}
