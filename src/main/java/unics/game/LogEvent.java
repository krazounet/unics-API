package unics.game;

import java.util.List;

public record LogEvent(
	    String texte_fr,
	    String texte_en,
	    String uuid_joueur_source,
	    String uuid_joueur_cible,
	    String uuid_carte_source,
	    List<String> uuid_cartes_cibles
	) {}
