package unics.game;

import java.util.List;
import java.util.Map;

import unics.snapshot.CardSnapshot;

public class JoueurPartie {
	Joueur owner;
	Deck deck_initial;
	int hp;
	int mana_dispo;
	List<CardSnapshot> main;
	List<CardSnapshot> deck;
	List<CardSnapshot> defausse;
	Map<Slot, CardSnapshot> plateau;
	
	enum Slot {
	    LEFT,
	    CENTER,
	    RIGHT
	}
	
}
