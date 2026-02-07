package unics.api.cards.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import dbPG18.DbUtil;

@Repository
public class RandomExposableSnapshotDao {

    private static final String SQL = """
        SELECT s.id
        FROM card_render r
        JOIN card_snapshot s
          ON s.visual_signature = r.visual_signature
        WHERE r.finished_at IS NOT NULL
          AND r.status = 'DONE'
        ORDER BY random()
        LIMIT 1
        """;

    public Optional<UUID> findRandomSnapshotId() {

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(
                UUID.fromString(rs.getString("id"))
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to fetch random exposable snapshot", e
            );
        }
    }
}

