package unics.game;

import java.util.Map;
import java.util.UUID;

public class CardInPlay {
	

	    public UUID instanceId;      // UNIQUE en partie
	    public UUID snapshotId;      // référence card_snapshot
	    public int attack;			
	    public int health;
	    public Inclinaison exhausted;

	    public Map<String, Integer>  effects; // STRING du gere, dégat, debuff, etc... Integer : intensité
	
	    public enum Inclinaison {
		    DROIT,
		    TRAVERS,
		    COUCHE
		}
}
