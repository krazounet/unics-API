package unics.api.game;



import java.util.UUID;

import unics.game.GameState;
import unics.game.JoueurPartie;

public class StateProjector {

    public static GameView project(GameState state, UUID viewerId) {

        GameView view = new GameView();

        // --- méta partie ---
        view.partieId = state.partie;
        view.etat = state.etat_partie.name();
        view.phase = state.phase_partie.name();
        view.joueurActif = state.joueur_actif;
        view.joueur_tour = state.joueur_tour;
        view.tour = state.tour;
        view.step = state.step;
        view.current_effect = state.getCurrentEffect();
        
        if (viewerId == null) {
            // spectateur
        	view.me = projectOpponent(state.J1);
            view.opponent = projectOpponent(state.J2);
            return view;
        }
        if (!viewerId.equals(state.J1.getOwner().getId_joueur())
        		 && !viewerId.equals(state.J2.getOwner().getId_joueur())) {
        		    return view;
        		}

        		JoueurPartie me;
        		JoueurPartie opp;

        		if (viewerId.equals(state.J1.getOwner().getId_joueur())) {
        		    me = state.J1;
        		    opp = state.J2;
        		} else {
        		    me = state.J2;
        		    opp = state.J1;
        		}
        
 
        view.me = projectMe(me);
        view.opponent = projectOpponent(opp);
        view.logs = state.log;
        return view;
    }

    private static PlayerView projectMe(JoueurPartie jp) {
        PlayerView v = new PlayerView();
        
        v.id = jp.getOwner().getId_joueur().toString();
        v.nom = jp.getOwner().getPseudo(); 
        
        v.hp = jp.getHp();
        v.mana = jp.getMana_dispo();
        v.main = jp.getMain();
        v.plateau = jp.getPlateau();
        v.deckCount = jp.getDeck().size();
        v.handCount = jp.getMain().size();
        v.defausse = jp.getDefausse();         // ✅ visible
        return v;
    }

    private static PlayerView projectOpponent(JoueurPartie jp) {
        if (jp == null) return null;

        PlayerView v = new PlayerView();
        v.id = jp.getOwner().getId_joueur().toString();
        v.nom = jp.getOwner().getPseudo();
        v.hp = jp.getHp();
        v.mana = jp.getMana_dispo();
        v.main = null;                         // ❌ cachée
        v.defausse = jp.getDefausse();         // ✅ visible
        v.plateau = jp.getPlateau();
        v.deckCount = jp.getDeck().size();
        v.handCount = jp.getMain().size();
        return v;
    }
}

