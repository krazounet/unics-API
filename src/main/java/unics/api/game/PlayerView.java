package unics.api.game;



import java.util.List;
import java.util.Map;
import unics.game.CardInPlay;
import unics.game.JoueurPartie.Slot;
import unics.snapshot.CardSnapshot;

public class PlayerView {

    public int hp;
    public int mana;

    // visible seulement pour "me"
    public List<CardSnapshot> main;

    // toujours visibles
    public Map<Slot, CardInPlay> plateau;
    public List<CardSnapshot> defausse;  // ✅ TOUJOURS visible
    public int deckCount;
    public int handCount;
}

