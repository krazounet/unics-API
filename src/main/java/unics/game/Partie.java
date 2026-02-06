package unics.game;

import java.util.Random;
import java.util.UUID;


public class Partie {
	
	//TECHNIQUE
	UUID id_partie;
	long rngSeed;
	Random rng;
	
	//QUI AVEC QUOI
	JoueurPartie J1;
	JoueurPartie J2;
	
	//COMMENT
	EtatPartie etat_partie;
	PhasePartie phase_partie;
	JoueurPartie joueur_actif;
	
	int tour;
	int step;
	
}
