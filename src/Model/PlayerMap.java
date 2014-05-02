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

/**
 * A persistent HashMap of Player string to GameInfo
 * @author CShuford
 *
 */
@PersistenceCapable
public class PlayerMap{
	
	@PrimaryKey
	@Persistent
	String key = "players2";
	
	@Persistent
	public String specialStr = ">>>>>>>>>>>>>>>>>>>> None";

	@Persistent(serialized = "true", defaultFetchGroup = "true")
	HashMap<String, GameInfo> players;
	
	/**
	 * Constructor
	 */
	public PlayerMap() {
		if (this.players == null)
			this.players = new HashMap<String, GameInfo>();
	}
	
	/**
	 * Returns the string, gameinfo hashmap
	 * @return
	 */
	public HashMap<String, GameInfo> getPlayerMap() {
		return this.players;
	}
	
	/**
	 * Returns true if key is a key in the hashmap
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key) {
		return this.players.containsKey(key);
	}
	
	/**
	 * Put the key,info pairing into the hashmap
	 * @param key
	 * @param info
	 */
	public void put(String key, GameInfo info) {
		this.players.put(key, info);
	}
	
	/**
	 * Get the GameInfo from the map associated with key
	 * @param key
	 * @return
	 */
	public GameInfo get(String key) {
		return this.players.get(key);
	}
}
