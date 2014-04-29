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
			return mp.execute(g.toJson(mw).toString(), null);
	    } 
		catch (Exception exception) {
			exception.printStackTrace();
	    }
		return "no return";
	}
	
	
	public boolean sendPost(String data, String url, ICallback callback) {
		MakePost mp = new MakePost(url);
		try {
			if (mp.execute(data,callback) != null) {
				return true;
			}
	    } 
		catch (Exception exception) {
			exception.printStackTrace();
	    }	
		return false;
	}
}