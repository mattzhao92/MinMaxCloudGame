package post;

import request.MakePost;
import request.MethodWrapper;
import request.RegisterGame;
import com.google.gson.Gson;

/**
 * @author Joel Baranowski
 * 
 */
public class RegisterGamePost {

	Gson g = new Gson();
	
	public String run(){
		MakePost mp = new MakePost("http://1-dot-utopian-hearth-532.appspot.com/test2");
		//CHANGEIT
		RegisterGame rg = new RegisterGame("http://1-dot-utopian-hearth-533.appspot.com/test");
		String gts = g.toJson(rg);
		MethodWrapper mw = new MethodWrapper("registerGame", gts);
		try {
			return mp.execute(mw);
	    } 
		catch (Exception exception) {
			exception.printStackTrace();
	    }
		return "no return";
	}
}
