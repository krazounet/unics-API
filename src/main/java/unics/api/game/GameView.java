package unics.api.game;



import java.util.UUID;

public class GameView {

    public UUID partieId;
    public String etat;
    public String phase;
    public UUID joueurActif;
    public int tour;
    public int step;

    public PlayerView me;
    public PlayerView opponent;
}
