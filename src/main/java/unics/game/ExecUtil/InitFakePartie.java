package unics.game.ExecUtil;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import dbPG18.DbUtil;
import unics.game.CardInPlay;
import unics.game.Deck;
import unics.game.EtatPartie;
import unics.game.GameState;
import unics.game.Joueur;
import unics.game.JoueurPartie;
import unics.game.Partie;
import unics.game.PhasePartie;
import unics.game.JoueurPartie.Slot;
import unics.game.db.JdbcDeckDao;
import unics.game.db.JdbcJoueurDao;
import unics.game.db.JdbcPartieDao;
import unics.snapshot.CardSnapshot;

public class InitFakePartie {

    private static final int MAIN_INITIALE = 5;

    public static void main(String[] args) {

        

        UUID joueur1Id = UUID.fromString("45ca93a9-4a52-4d1c-be38-87b5341e5788");
        UUID joueur2Id = UUID.fromString("bdae434c-b2e4-48fb-9be6-b8b0124e5726");

        try (Connection connection = DbUtil.getConnection()) {

            JdbcJoueurDao joueurDao = new JdbcJoueurDao(connection);
            JdbcDeckDao deckDao = new JdbcDeckDao(connection);
            JdbcPartieDao partieDao = new JdbcPartieDao(connection);

            Joueur j1 = joueurDao.findById(joueur1Id).orElseThrow();
            Joueur j2 = joueurDao.findById(joueur2Id).orElseThrow();

            Deck d1 = deckDao.findFirstByOwner(j1.getId_joueur())
                    .orElseThrow(() -> new IllegalStateException("Deck manquant J1"));
            Deck d2 = deckDao.findFirstByOwner(j2.getId_joueur())
                    .orElseThrow(() -> new IllegalStateException("Deck manquant J2"));

            long rngSeed = System.currentTimeMillis();
            Random rng = new Random(rngSeed);

            JoueurPartie jp1 = initJoueurPartie(j1, d1, rng);
            JoueurPartie jp2 = initJoueurPartie(j2, d2, rng);

         // Mettre une carte chez J2 aussi
            if (!jp2.getMain().isEmpty()) {
                CardSnapshot snap = jp2.getMain().get(0);

                CardInPlay cardInPlay = toCardInPlay(
                        snap,
                        CardInPlay.Inclinaison.COUCHE
                );

                jp2.getPlateau().put(JoueurPartie.Slot.RIGHT, cardInPlay);
            }
            
            
            GameState gameState = new GameState();
            
            gameState.J1 = jp1;
            gameState.J2 = jp2;
            
            
            gameState.log = new ArrayList<>();
            gameState.etat_partie = EtatPartie.MULLIGAN;
            gameState.phase_partie = PhasePartie.TURN_START;
            gameState.step = 0;
            gameState.tour = 0;
            gameState.joueur_actif = jp1.getOwner().getId_joueur();
            
            UUID partie_id = UUID.randomUUID();
            
            
            
            //gameState.rngSeed = rngSeed;

            Partie partie = new Partie();
            partie.setId_partie(partie_id);
            gameState.partie = partie_id;
            
            partie.setEtat_partie(EtatPartie.MULLIGAN);
            partie.setPhase_partie(PhasePartie.TURN_START);
            partie.setJoueur_actif(j1.getId_joueur()); 
            partie.setTour(0);
            partie.setStep(0); 
            partie.setGamestate(gameState); 

            partieDao.insert(partie);

            System.out.println("✔ Partie créée : " + partie.getId_partie());
            System.out.println("  J1 = " + j1.getPseudo());
            System.out.println("  J2 = " + j2.getPseudo());
            System.out.println("  RNG seed = " + rngSeed);

        } catch (Exception e) {
            System.err.println("❌ InitFakePartie FAILED");
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    // INIT JOUEUR PARTIE
    // ─────────────────────────────────────────────

    private static JoueurPartie initJoueurPartie(Joueur joueur, Deck deck, Random rng) {

        List<CardSnapshot> pioche = new ArrayList<>(deck.getList_cartes());
        Collections.shuffle(pioche, rng);

        List<CardSnapshot> main = new ArrayList<>();
        for (int i = 0; i < MAIN_INITIALE; i++) {
            main.add(pioche.remove(0));
        }

        JoueurPartie jp = new JoueurPartie();
        jp.setOwner(joueur);
        jp.setDeck_initial(deck.getId_deck()); 

        jp.setHp(20); 
        jp.setMana_dispo(1);

        jp.setMain(main); 
        jp.setDeck(pioche);
        jp.setDefausse(new ArrayList<>()); 

        Map<Slot, CardInPlay> plateau = new EnumMap<>(JoueurPartie.Slot.class);
        for (JoueurPartie.Slot slot : JoueurPartie.Slot.values()) {
        	plateau.put(slot, null);
        }
        jp.setPlateau(plateau);
        
        if (!main.isEmpty()) {
            CardSnapshot snap = main.get(0);

            CardInPlay cardInPlay = toCardInPlay(
                    snap,
                    CardInPlay.Inclinaison.TRAVERS
            );

            jp.getPlateau().put(JoueurPartie.Slot.LEFT, cardInPlay);

            // Optionnel : retirer de la main pour cohérence
            main.remove(0);
        }

        jp.setCompteurs(new HashMap<>()); 

        return jp;
    }
    private static CardInPlay toCardInPlay(CardSnapshot snapshot, CardInPlay.Inclinaison inclinaison) {
        CardInPlay cip = new CardInPlay();
        cip.instanceId = UUID.randomUUID();
        cip.snapshotId = snapshot.snapshotId;
        cip.attack = snapshot.attack;
        cip.health = snapshot.health;
        cip.exhausted = inclinaison;
        cip.effects = new HashMap<>();
        return cip;
    }
}
