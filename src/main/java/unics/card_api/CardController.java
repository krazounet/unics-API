package unics.card_api;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import dbPG18.JdbcCardSnapshotDao;
import unics.snapshot.CardSnapshot;
import unics.card_api.dto.CardDto;

@RestController
public class CardController {

    private final JdbcCardSnapshotDao snapshotDao =
        new JdbcCardSnapshotDao();

    @GetMapping("/api/cards/{snapshotId}")
    public CardDto getCard(@PathVariable UUID snapshotId) {

        CardSnapshot s = snapshotDao.findById(snapshotId);
        if (s == null) {
            throw new RuntimeException("Card not found");
        }
        
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
