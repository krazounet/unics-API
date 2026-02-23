package unics.game.ExecUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import dbPG18.JdbcCardSnapshotDao;
import unics.BoosterManaCurve;
import unics.FactionDistribution;
import unics.Enum.CardType;
import unics.Enum.Faction;
import unics.Enum.ManaCurveProfile;
import unics.snapshot.CardSnapshot;

public class RandomBooster  {

	public final UUID id;

	
    protected List<CardSnapshot> cards;
    protected List<Integer> manacurve;

    ThreadLocalRandom random;
    

    protected ManaCurveProfile manaCurveProfile;
    protected List<Integer> manaCurve;
	
    private final JdbcCardSnapshotDao dao;
    private int index = 0;
    public final List<Faction> factions;

    public RandomBooster(ThreadLocalRandom random, JdbcCardSnapshotDao dao) {
        
    	id= UUID.randomUUID();

		this.dao = dao;
		this.manaCurveProfile = ManaCurveProfile.random();
		this.manacurve = new BoosterManaCurve(18, manaCurveProfile).getCurve();
        this.cards = new ArrayList<>();
        this.factions = FactionDistribution.generate(List.of(12, 6));
        this.random=random;

		cards= new ArrayList<>();
    }
    public void generate() {
        for (int i = 0; i < 10; i++) addFromDB(CardType.UNIT);
        for (int i = 0; i < 3; i++) addFromDB(CardType.STRUCTURE);
        for (int i = 0; i < 2; i++) addFromDB(CardType.ACTION);

        for (int i = 0; i < 3; i++) {
            CardType type = random.nextInt(100) < 33 ? CardType.UNIT
                : random.nextInt(100) < 66 ? CardType.ACTION
                : CardType.STRUCTURE;
            addFromDB(type);
        }
    }

    private void addFromDB(CardType type) {
        if (index >= manacurve.size()) {
            throw new IllegalStateException("Dépassement de la courbe de mana");
        }

        int mana = manacurve.get(index);
        //Faction faction = factions.get(index);
        index++;

        CardSnapshot card = dao.findRandom(type.name(), mana);

		

		cards.add(card);
    }
}