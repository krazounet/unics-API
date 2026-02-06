package unics.game.db;

import java.util.Optional;
import java.util.UUID;

import unics.game.Joueur;

public interface JoueurDao {

    void insert(Joueur joueur);

    Optional<Joueur> findById(UUID joueurId);

    
}
