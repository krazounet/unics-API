package unics.api.game.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import unics.Enum.Keyword;
import unics.Enum.TriggerType;
import unics.game.CardInPlay;
import unics.game.CardInPlay.Inclinaison;
import unics.game.JoueurPartie;
import unics.game.JoueurPartie.Slot;
import unics.game.Partie;
import unics.snapshot.CardSnapshot;

@Service
public class PlateauService {

    private final CardSnapshotService cardSnapshotService;
    private final TriggerService triggerService;

    public PlateauService(CardSnapshotService cardSnapshotService,
                          TriggerService triggerService) {
        this.cardSnapshotService = cardSnapshotService;
        this.triggerService = triggerService;
    }

    public void handleFade(Partie partie,
                           JoueurPartie joueur,
                           JoueurPartie opposant) {

        partie.increaseStep();
        partie.setPhase_partie(unics.game.PhasePartie.FADE);

        // 1️⃣ Défausse des cartes couchées
        for (Map.Entry<Slot, CardInPlay> entry : joueur.getPlateau().entrySet()) {

            CardInPlay cip = entry.getValue();
            if (cip == null) continue;
            if (cip.exhausted != Inclinaison.COUCHE) continue;

            CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);

            joueur.getDefausse().add(snapshot);
            entry.setValue(null); // on garde la structure

            triggerService.checkTrigger(
                    List.of(TriggerType.ON_LEAVE),
                    partie,
                    joueur,
                    opposant,
                    snapshot
            );
        }

        // 2️⃣ TRAVERS → COUCHE
        for (CardInPlay cip : joueur.getPlateau().values()) {

            if (cip == null) continue;
            if (cip.exhausted != Inclinaison.TRAVERS) continue;

            CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);

            if (!snapshot.keywords.contains(Keyword.PERSISTANT)) {
                cip.exhausted = Inclinaison.COUCHE;
            }
        }

        // 3️⃣ DROIT → TRAVERS
        for (CardInPlay cip : joueur.getPlateau().values()) {

            if (cip == null) continue;
            if (cip.exhausted != Inclinaison.DROIT) continue;

            CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);

            if (!snapshot.keywords.contains(Keyword.PERSISTANT)) {
                cip.exhausted = Inclinaison.TRAVERS;
            }
        }
    }
    public void playCard(
            Partie partie,
            JoueurPartie joueur,
            JoueurPartie opposant,
            CardSnapshot snap,
            JoueurPartie.Slot slot
    ) {

        //8 retrait main
        joueur.getMain().remove(snap);

        //9 ajout plateau
        //=> je dois creer une cardInPlay qui correspond au snap
        joueur.getPlateau().put(slot, new CardInPlay(snap));

        //10retrait mana
        joueur.retireMana(snap.cost);

    }

}