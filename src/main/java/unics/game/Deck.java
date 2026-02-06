package unics.game;

import java.util.List;
import java.util.UUID;

import unics.snapshot.CardSnapshot;

public class Deck {
	UUID id_deck;
	Joueur proprietaire;
	String name;
	List<CardSnapshot> list_cartes;
	
	
	public Deck(UUID id_deck, Joueur proprietaire, String name, List<CardSnapshot> list_cartes) {
		super();
		this.id_deck = id_deck;
		this.proprietaire = proprietaire;
		this.name = name;
		this.list_cartes = list_cartes;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public UUID getId_deck() {
		return id_deck;
	}
	public void setId_deck(UUID id_deck) {
		this.id_deck = id_deck;
	}
	public Joueur getProprietaire() {
		return proprietaire;
	}
	public void setProprietaire(Joueur proprietaire) {
		this.proprietaire = proprietaire;
	}
	public List<CardSnapshot> getList_cartes() {
		return list_cartes;
	}
	public void setList_cartes(List<CardSnapshot> list_cartes) {
		this.list_cartes = list_cartes;
	}
	public Object getOwnerId() {
		
		return getProprietaire().id_joueur;
	}
	
	
	
}
