package unics.api.cards;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import unics.api.cards.dao.RandomExposableSnapshotDao;
import unics.api.cards.dto.CardDto;

@RestController
public class RandomCardController {

    private final RandomExposableSnapshotDao randomDao;
    private final CardController cardController;

    public RandomCardController(
            RandomExposableSnapshotDao randomDao,
            CardController cardController
    ) {
        this.randomDao = randomDao;
        this.cardController = cardController;
    }

    @GetMapping("/api/cards/random")
    public CardDto randomCard() {

        UUID snapshotId = randomDao.findRandomSnapshotId()
            .orElseThrow(() -> new RuntimeException("No exposable card"));

        // 🔥 EXACTEMENT le même format
        return cardController.getCard(snapshotId);
    }
}


