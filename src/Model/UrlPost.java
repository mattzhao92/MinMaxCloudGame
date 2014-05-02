package Model;

import javax.servlet.http.HttpServletResponse;

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
			return mp.execute(g.toJson(mw).toString(), false);
	    } 
		catch (Exception exception) {
			exception.printStackTrace();
	    }
		return "no return";
	}
	
	public void sendPostAync(String data, String url) {
		MakePost mp = new MakePost(url);
		try {
			mp.execute(data, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public boolean sendPost(String data, String url) {
		MakePost mp = new MakePost(url);
		try {
			if (mp.execute(data, false) != null) {
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
			if (mp.execute(data, false) != null) {
				return mp.execute(data, false);
			}
	    } 
		catch (Exception exception) {
			exception.printStackTrace();
	    }	
		return "bad callback";
	}	
}