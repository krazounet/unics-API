package unics.game.db;

import java.sql.Timestamp;
import java.util.UUID;

public class DeckDbRow {

    private UUID id;
    private UUID ownerId;
    private String name;
    private Timestamp createdAt;

    // getters / setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
