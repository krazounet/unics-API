package unics.api.game.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import unics.Enum.TriggerType;
import unics.api.game.EffectToResolve;
import unics.game.CardInPlay;
import unics.game.JoueurPartie;
import unics.game.Partie;
import unics.snapshot.CardSnapshot;
import unics.snapshot.EffectSnapshot;

@Service
public class TriggerService {

    private final CardSnapshotService cardSnapshotService;

    public TriggerService(CardSnapshotService cardSnapshotService) {
        this.cardSnapshotService = cardSnapshotService;
    }

    public void checkTrigger(
            List<TriggerType> triggers,
            Partie partie,
            JoueurPartie joueur,
            JoueurPartie opposant,
            CardSnapshot carteJouee
    ) {

        for (TriggerType trigger : triggers) {

            switch (trigger) {

                case ON_PLAY:
                case ON_ENTER:
                case ON_LEAVE:
                    if (carteJouee != null) {
                        resolveEffectsForSnapshot(carteJouee, trigger, joueur, partie);
                    }
                    break;

                case ON_ALLIED_UNIT_ENTERS:
                    for (CardInPlay cip : joueur.getPlateau().values()) {
                        if (cip == null) continue;
                        if (!cip.snapshotId.equals(carteJouee.snapshotId)) continue;

                        CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);
                        resolveEffectsForSnapshot(snapshot, trigger, joueur, partie);
                    }
                    break;

                case ON_TURN_START:
                case ON_TURN_END:
                    resolveForBoard(joueur, trigger, partie);
                    break;

                default:
                    throw new RuntimeException("Trigger non géré : " + trigger);
            }
        }
    }

    private void resolveEffectsForSnapshot(CardSnapshot snapshot,TriggerType trigger,JoueurPartie joueur,Partie partie) {

        for (EffectSnapshot effect : getEffectsByTrigger(snapshot, trigger)) {
        	System.out.println("TriggerService.resolveEffectForSnapshot ajout effet");
            partie.getGamestate().effects_to_resolve.add(
                    new EffectToResolve(
                            effect,
                            snapshot.snapshotId,
                            joueur.getOwner().getId_joueur()
                    )
            );
        }
    }

    private void resolveForBoard(JoueurPartie joueur,TriggerType trigger,Partie partie) {

        for (CardInPlay cip : joueur.getPlateau().values()) {
            if (cip == null) continue;
            CardSnapshot snapshot = cardSnapshotService.getById(cip.snapshotId);
            resolveEffectsForSnapshot(snapshot, trigger, joueur, partie);
        }
        
    }

    private List<EffectSnapshot> getEffectsByTrigger(CardSnapshot cardSnapshot,TriggerType triggerType) {

        if (cardSnapshot == null || cardSnapshot.effects == null) {
            return Collections.emptyList();
        }

        return cardSnapshot.effects.stream()
                .filter(effect -> effect.trigger == triggerType)
                .collect(Collectors.toList());
    }
}