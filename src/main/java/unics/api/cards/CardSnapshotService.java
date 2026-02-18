package unics.api.cards;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import dbPG18.JdbcCardSnapshotDao;
import unics.snapshot.CardSnapshot;

@Service
public class CardSnapshotService {

    private final JdbcCardSnapshotDao cardSnapshotDAO;
    private final Map<UUID, CardSnapshot> cache = new ConcurrentHashMap<>();

    public CardSnapshotService(JdbcCardSnapshotDao cardSnapshotDAO) {
        this.cardSnapshotDAO = cardSnapshotDAO;
    }

    public CardSnapshot getById(UUID id) {
        return cache.computeIfAbsent(id, this::loadFromDb);
    }

    private CardSnapshot loadFromDb(UUID id) {
        CardSnapshot snapshot = cardSnapshotDAO.findById(id);

        if (snapshot == null) {
            throw new RuntimeException("Card not found: " + id);
        }

        return snapshot;
    }
}