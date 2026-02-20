package unics.api.game;

import java.util.UUID;

import unics.snapshot.EffectSnapshot;

public class EffectToResolve {

    private EffectSnapshot effet_source;
    private UUID carte_source;
    private UUID owner_id;

    public EffectToResolve() {
        // nécessaire pour Jackson
    }

    public EffectToResolve(EffectSnapshot effet_source, UUID carte_source, UUID owner_id) {
        this.effet_source = effet_source;
        this.carte_source = carte_source;
        this.owner_id = owner_id;
    }

    public EffectSnapshot getEffet_source() {
        return effet_source;
    }

    public UUID getCarte_source() {
        return carte_source;
    }

    public UUID getOwner_id() {
        return owner_id;
    }
}
