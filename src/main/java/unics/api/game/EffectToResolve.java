package unics.api.game;

import java.util.UUID;

import unics.snapshot.EffectSnapshot;

public class EffectToResolve {
	
	EffectSnapshot effet_source;
	UUID carte_source;
	UUID owner_id;
	
	public EffectToResolve(EffectSnapshot effet_source, UUID carte_source, UUID owner_id) {
		super();
		this.effet_source = effet_source;
		this.carte_source = carte_source;
		this.owner_id = owner_id;
	}
	
	
}
