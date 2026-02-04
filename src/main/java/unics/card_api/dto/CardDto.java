package unics.card_api.dto;

import java.util.List;
import java.util.UUID;


import unics.snapshot.EffectSnapshot;

public record CardDto(
    UUID snapshotId,
    String publicId,
    String name,
    String type,
    String faction,
    int cost,
    Integer attack,
    Integer health,
    List<String> keywords,
    List<EffectSnapshot> Effects,
    String visualSignature
) {}
