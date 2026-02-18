package unics.api.game;

import java.sql.SQLException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dbPG18.DbUtil;
import dbPG18.JdbcCardSnapshotDao;
import unics.game.db.JdbcPartieDao;

@Configuration
public class DaoConfig {

    @Bean
    public JdbcCardSnapshotDao cardSnapshotDao() throws SQLException {
        return new JdbcCardSnapshotDao(DbUtil.getConnection());
    }
    @Bean
    public JdbcPartieDao partieDao() throws Exception {
        return new JdbcPartieDao(DbUtil.getConnection());
    }
}