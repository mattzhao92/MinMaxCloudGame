package servlets_private;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Json.GameInfo;
import Json.SavePortalIDsServletInput;
import Json.TakeTurnFinishedInput;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.gson.Gson;

public class SavePortalIDServlet extends HttpServlet {

	private static final long serialVersionUID = 319745500766558588L;
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		
		System.out.println("SavePortalIDServlet");
		Gson gson = new Gson();
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		SavePortalIDsServletInput request = gson.fromJson(reader, SavePortalIDsServletInput.class);
		
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Transaction tx = datastore.beginTransaction();
	
		Key newIDsKey = KeyFactory.createKey("PortalList", "MyPortalList");

	    Query query = new Query("Portal", newIDsKey);
	    List<Entity> newIDsList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(500));
	    
	    for (Entity e : newIDsList) {
	    	datastore.delete(e.getKey());
	    }
	
		Entity newIDs = new Entity("Portal", newIDsKey);
		
		System.out.println("inboundPortNumber: " + request.inboundPortNumber);
		System.out.println("outboundPortNumber" + request.outboundPortNumber);

		newIDs.setProperty("inboundPortNumber", request.inboundPortNumber);
		newIDs.setProperty("outboundPortNumber", request.outboundPortNumber);
		
		datastore.put(newIDs);
		tx.commit();
	}
}