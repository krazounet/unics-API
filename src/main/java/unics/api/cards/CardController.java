package unics.api.cards;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import unics.api.cards.dto.CardDto;
import unics.snapshot.CardSnapshot;

@RestController
public class CardController {

	private final CardSnapshotService cardSnapshotService;

    public CardController(CardSnapshotService cardSnapshotService) {
        this.cardSnapshotService = cardSnapshotService;
    }




    @GetMapping("/api/cards/{snapshotId}")
    public CardDto getCard(@PathVariable UUID snapshotId) {

        CardSnapshot s = cardSnapshotService.getById(snapshotId);

        return new CardDto(
            s.snapshotId,
            s.publicId,
            s.name,
            s.type.name(),
            s.faction.name(),
            s.cost,
            s.attack,
            s.health,
            s.keywords.stream().map(Enum::name).toList(),
            s.effects,
            s.visualSignature
        );
    }

}
