package Json;

public class PortalRedirectToClient {
	public String status;
	public Long playerID;
	public String gameURL;
	public int inboundPortalID;	
	public String playerName;
	public boolean isAI;
	public String AIUrl;
	
	public PortalRedirectToClient(String status, Long playerID, String gameURL,
								  int inboundPortalID, String playerName, boolean isAI,
								  String AIURL) {
		this.status = status;
		this.playerID = playerID;
		this.gameURL = gameURL;
		this.inboundPortalID = inboundPortalID;
		this.playerName = playerName;
		this.isAI = isAI;
		this.AIUrl = AIURL;
		
	}
}
