package unics.api.cards;

import java.sql.SQLException;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import dbPG18.DbUtil;
import dbPG18.JdbcCardSnapshotDao;
import unics.api.cards.dto.CardDto;
import unics.snapshot.CardSnapshot;

@RestController
public class CardController {

    private JdbcCardSnapshotDao snapshotDao;// = new JdbcCardSnapshotDao(DbUtil.getConnection());

    
    
    
    public CardController() {
		super();
		snapshotDao=null;
		try {
			snapshotDao=new JdbcCardSnapshotDao(DbUtil.getConnection());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




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
