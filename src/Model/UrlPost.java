package Model;

import request.MakePost;
import request.MethodWrapper;

import com.google.gson.Gson;

/**
 * @author Joel Baranowski
 * 
 */
public class UrlPost {

	Gson g = new Gson();
	
	public String run(MethodWrapper mw, String url){
		MakePost mp = new MakePost(url);
		try {
			return mp.execute(g.toJson(mw).toString());
	    } 
		catch (Exception exception) {
			exception.printStackTrace();
	    }
		return "no return";
	}
	
	
	public boolean sendPost(String data, String url) {
		MakePost mp = new MakePost(url);
		try {
			if (mp.execute(data) != null) {
				return true;
			}
	    } 
		catch (Exception exception) {
			exception.printStackTrace();
	    }	
		return false;
	}
	

	public String sendCallbackPost(String data, String url) {
		MakePost mp = new MakePost(url);
		try {
			if (mp.execute(data) != null) {
				return mp.execute(data);
			}
	    } 
		catch (Exception exception) {
			exception.printStackTrace();
	    }	
		return "bad callback";
	}
}