package unics.api.game;



import java.util.List;
import java.util.UUID;

import unics.game.LogEvent;

public class GameView {

    public UUID partieId;
    public String etat;
    public String phase;
    public UUID joueurActif;
    public int tour;
    public int step;

    public PlayerView me;
    public PlayerView opponent;
    
    public List<LogEvent> logs;
    public EffectToResolve current_effect;
}
