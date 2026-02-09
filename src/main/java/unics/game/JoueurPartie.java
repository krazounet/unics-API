package unics.game;

import java.util.List;
import java.util.Map;

import unics.snapshot.CardSnapshot;

public class JoueurPartie {
	private Joueur owner;
	private Deck deck_initial;
	private int hp;
	private int mana_dispo;
	private List<CardSnapshot> main;
	private List<CardSnapshot> deck;
	private List<CardSnapshot> defausse;
	private Map<Slot, CardInPlay> plateau;
	private Map<String, Integer> compteurs;
	
	
	
	
	public void setOwner(Joueur owner) {
		this.owner = owner;
	}




	public void setDeck_initial(Deck deck_initial) {
		this.deck_initial = deck_initial;
	}




	public void setHp(int hp) {
		this.hp = hp;
	}




	public void setMana_dispo(int mana_dispo) {
		this.mana_dispo = mana_dispo;
	}




	public void setMain(List<CardSnapshot> main) {
		this.main = main;
	}




	public void setDeck(List<CardSnapshot> deck) {
		this.deck = deck;
	}




	public void setDefausse(List<CardSnapshot> defausse) {
		this.defausse = defausse;
	}




	public void setPlateau(Map<Slot, CardInPlay> plateau) {
		this.plateau = plateau;
	}




	public void setCompteurs(Map<String, Integer> compteurs) {
		this.compteurs = compteurs;
	}




	public Joueur getOwner() {
		return owner;
	}




	public Deck getDeck_initial() {
		return deck_initial;
	}




	public int getHp() {
		return hp;
	}




	public int getMana_dispo() {
		return mana_dispo;
	}




	public List<CardSnapshot> getMain() {
		return main;
	}




	public List<CardSnapshot> getDeck() {
		return deck;
	}




	public List<CardSnapshot> getDefausse() {
		return defausse;
	}




	public Map<Slot, CardInPlay> getPlateau() {
		return plateau;
	}




	public Map<String, Integer> getCompteurs() {
		return compteurs;
	}




	public enum Slot {
	    LEFT,
	    CENTER,
	    RIGHT
	}
	
}
