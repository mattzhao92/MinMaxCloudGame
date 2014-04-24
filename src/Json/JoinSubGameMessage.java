package Json;

import com.google.gson.Gson;

public class JoinSubGameMessage {
	public Long playerID;
	public String playerName;
	public boolean isAI;
	public String AIUrl; //same with playerServer URL
	public String gameURL;
	public String token;
	
	
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this, JoinSubGameMessage.class);
	}
}
