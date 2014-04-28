package request;

public class PortalRequest {
	
	int portalID;
	Long playerID;
	
	public PortalRequest(int portal, Long player) {
		this.portalID = portal;
		this.playerID = player;
	}
	
}
