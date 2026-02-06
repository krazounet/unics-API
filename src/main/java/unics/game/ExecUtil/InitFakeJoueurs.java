package unics.game.ExecUtil;


import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import dbPG18.DbUtil;
import unics.game.Joueur;
import unics.game.db.JdbcJoueurDao;

public class InitFakeJoueurs {

    public static void main(String[] args) {

        try (Connection connection = DbUtil.getConnection()) {

            JdbcJoueurDao joueurDao = new JdbcJoueurDao(connection);

            Joueur j1 = new Joueur();
            j1.setId_joueur(UUID.randomUUID());
            j1.setPseudo("Alice");
            j1.setEmail("alice@test.local");
            j1.setElo(1200);
            j1.setValid_user(true);
            j1.setLast_connection(Timestamp.from(Instant.now()));

            Joueur j2 = new Joueur();
            j2.setId_joueur(UUID.randomUUID());
            j2.setPseudo("Bob");
            j2.setEmail("bob@test.local");
            j2.setElo(1100);
            j2.setValid_user(true);
            j2.setLast_connection(Timestamp.from(Instant.now()));

            joueurDao.insert(j1);
            joueurDao.insert(j2);

            System.out.println("✔ Joueurs insérés :");
            System.out.println(" - " + j1.getPseudo() + " (" + j1.getId_joueur() + ")");
            System.out.println(" - " + j2.getPseudo() + " (" + j2.getId_joueur() + ")");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation des joueurs");
            e.printStackTrace();
        }
    }
}
