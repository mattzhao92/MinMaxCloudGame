package Model;

import request.MakeGet;
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


//	public boolean sendPost(String data, String url) {
//		MakePost mp = new MakePost(url);
//		try {
//			if (mp.execute(data) != null) {
//				return true;
//			}
//		} 
//		catch (Exception exception) {
//			exception.printStackTrace();
//		}	
//		return false;
//	}


	public String sendGet(String data, String url) {
		MakeGet mp = new MakeGet(url+"/"+data);
		try {
			String resposne = mp.execute();
			if (resposne != null) {
				return resposne;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return "bad request";
	}

	public String sendCallbackPost(String data, String url) {
		while (true) {
			MakePost mp = new MakePost(url);
			try {
				String response = mp.execute(data);
				if (response != null) {
					return response;
				}
			} 
			catch (Exception exception) {
				exception.printStackTrace();
			}	
		}

	}
}