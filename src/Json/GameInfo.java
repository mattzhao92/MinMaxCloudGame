package Json;

import java.io.Serializable;

import javax.jdo.annotations.PersistenceCapable;

import com.google.gson.Gson;

@PersistenceCapable
/**
 * I don't even know what this is. - carolyn
 *
 */
public class GameInfo implements Serializable{
	
	/**
	 * Serial ID generated automatically by eclipse 
	 */
	private static final long serialVersionUID = 8953562726722132314L;
	public String token;
	public Boolean enableMouse;
	
	public GameInfo(String tokenIn, Boolean enableMouse) {
		this.token = tokenIn;
		this.enableMouse = enableMouse;
	}
	
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this, GameInfo.class);
	}
	
	public void setEnableMosue(boolean b) {
		this.enableMouse = b;
	}
}