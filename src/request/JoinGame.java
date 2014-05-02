package request;

import java.io.Serializable;

public class JoinGame implements Serializable {

	/**
	 * Serial ID generated automatically by eclipse 
	 */
	private static final long serialVersionUID = -9188330103163596845L;
	private Long playerID;
	private String gameURL;
	
	public JoinGame(){
		
	}
	
	public JoinGame(Long playerID, String gameURL){
		this.playerID = playerID;
		this.gameURL = gameURL;
	}

	public Long getPlayerID() {
		return playerID;
	}

	public void setPlayerID(Long playerID) {
		this.playerID = playerID;
	}

	public String getGameURL() {
		return gameURL;
	}

	public void setGameURL(String gameURL) {
		this.gameURL = gameURL;
	}
}
