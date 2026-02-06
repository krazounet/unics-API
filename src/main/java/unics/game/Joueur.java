package unics.game;


import java.sql.Timestamp;
import java.util.UUID;

public class Joueur {
	UUID id_joueur;
	String Pseudo;
	String email;
	Timestamp last_connection;
	boolean valid_user;
	int elo;
	
	public UUID getId_joueur() {
		return id_joueur;
	}
	public void setId_joueur(UUID id_joueur) {
		this.id_joueur = id_joueur;
	}
	public String getPseudo() {
		return Pseudo;
	}
	public void setPseudo(String pseudo) {
		Pseudo = pseudo;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Timestamp getLast_connection() {
		return last_connection;
	}
	public void setLast_connection(Timestamp last_connection) {
		this.last_connection = last_connection;
	}
	public boolean isValid_user() {
		return valid_user;
	}
	public void setValid_user(boolean valid_user) {
		this.valid_user = valid_user;
	}
	public int getElo() {
		return elo;
	}
	public void setElo(int elo) {
		this.elo = elo;
	}
	
	public Joueur(UUID id_joueur, String pseudo, String email, Timestamp last_connection, boolean valid_user, int elo) {
		super();
		this.id_joueur = id_joueur;
		Pseudo = pseudo;
		this.email = email;
		this.last_connection = last_connection;
		this.valid_user = valid_user;
		this.elo = elo;
	}
	public Joueur() {
		
	}
	
	
	
}
