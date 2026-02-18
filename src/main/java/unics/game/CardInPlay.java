package unics.game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import unics.Enum.Keyword;
import unics.snapshot.CardSnapshot;

public class CardInPlay {
	

	  

		public UUID instanceId;      // UNIQUE en partie
	    public UUID snapshotId;      // référence card_snapshot
	    public int attack;			
	    public int health;
	    public Inclinaison exhausted;

	    public Map<String, Integer>  effects; // STRING du gere, dégat, debuff, etc... Integer : intensité
	
	    
	    public CardInPlay(CardSnapshot snap) {
			this.instanceId = UUID.randomUUID(); //je sais pas
			this.snapshotId = snap.snapshotId;
			
			this.attack 	= snap.attack;
			this.health		= snap.health;
			this.exhausted  = getInclinaison(snap);
			this.effects	= new HashMap<String, Integer>();
		}
	    
	    
	    public CardInPlay() {
			// TODO Auto-generated constructor stub
		}


		private Inclinaison getInclinaison(CardSnapshot snap) {
			if (snap.keywords.contains(Keyword.INSTABLE)) return Inclinaison.TRAVERS;
			return Inclinaison.DROIT;
		}


		public enum Inclinaison {
		    DROIT,
		    TRAVERS,
		    COUCHE
		}
}
