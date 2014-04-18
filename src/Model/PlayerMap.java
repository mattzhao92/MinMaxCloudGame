package Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.IdGeneratorStrategy;

import Json.GameInfo;

@PersistenceCapable
public class PlayerMap{
	
	@PrimaryKey
	@Persistent
	String key = "players2";
	
	@Persistent
	public String specialStr = ">>>>>>>>>>>>>>>>>>>> None";

	@Persistent(serialized = "true", defaultFetchGroup = "true")
	HashMap<String, GameInfo> players;
	
	public PlayerMap() {
		if (this.players == null)
			this.players = new HashMap<String, GameInfo>();
	}
	
	public HashMap<String, GameInfo> getPlayerMap() {
		return this.players;
	}
	
	public boolean containsKey(String key) {
		return this.players.containsKey(key);
	}
	
	public void put(String key, GameInfo info) {
		this.players.put(key, info);
	}
	
	public GameInfo get(String key) {
		return this.players.get(key);
	}
}
