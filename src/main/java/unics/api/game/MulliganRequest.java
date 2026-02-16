package unics.api.game;

import java.util.List;

public class MulliganRequest {
    private List<String> cards;
    private String playerId;
    
	public List<String> getCards() {
		return cards;
	}
	public void setCards(List<String> cards) {
		this.cards = cards;
	}
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

    
}
