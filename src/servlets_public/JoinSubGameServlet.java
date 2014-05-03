package servlets_public;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.devrel.samples.ttt.Cell;
import com.google.devrel.samples.ttt.CellContainer;
import com.google.gson.Gson;

import Json.GameInfo;
import Json.StatusResponse;
import Json.JoinSubGameMessage;
import Model.GameModel;

public class JoinSubGameServlet extends HttpServlet {

	private ChannelService channelService = ChannelServiceFactory.getChannelService();
	private Random random = new Random();
	private Gson gson = new Gson();

	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{
		resp.addHeader("Access-Control-Allow-Origin", "*");
		//System.out.println("JoinSubGameServlet");
		
		// parse the input packet
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		JoinSubGameMessage request = gson.fromJson(reader, JoinSubGameMessage.class);
		Long playerID = request.playerID;
		String playerName = request.playerName;
		boolean isAI = request.isAI;
		String AIUrl = request.AIUrl;
		String gameUrl = request.gameURL;
		
		// generate a channel id/token for the player
		String channelKey = ""+playerID + random.nextDouble() * 100;
		String token = channelService.createChannel(channelKey);
		request.token = token;
		
		// storing <playerName, his token> into database
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		GameInfo gameInfo = new GameInfo(token, false);
		
		Transaction tx = datastore.beginTransaction();
		Entity newPlayer = null;
	    
		boolean errorOccured = false;
		Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");
		try {
			newPlayer = new Entity("Player", playerKey);
			newPlayer.setProperty("playerName", playerName);
			newPlayer.setProperty("playerID", playerID);
			newPlayer.setProperty("token", token);
			newPlayer.setProperty("isAI", isAI);
			newPlayer.setProperty("AIUrl", AIUrl);
			newPlayer.setProperty("gameURL", gameUrl);
			//System.out.println("JoinSubGame isAI "+ isAI);
			datastore.put(newPlayer);
		} catch (Exception e) {
			errorOccured = true;
			e.printStackTrace();
		}
		tx.commit();
	
		tx = datastore.beginTransaction();
	    Query query = new Query("Board", GameModel.boardKey);
	    List<Entity> boardList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
	    
	    if (boardList == null || boardList.size() != 1) {
	    	String msg = "We dont have a board "+ boardList.size();
			StatusResponse status = new StatusResponse("fail",msg);
			resp.getWriter().println(gson.toJson(status, StatusResponse.class));
			System.err.println(msg);
			return;
	    }
	    
	    String board = ((Text) boardList.get(0).getProperty("board")).getValue();
	    CellContainer container = gson.fromJson(board, CellContainer.class);
	    
	    for (int i = 0; i < container.cells.size(); i++) {
	    	Cell cell =  container.cells.get(i);
	    	if (cell.playerName.equals("None")) {
	    		cell.playerName = playerName;
	    		break;
	    	}
	    }
	    
	    datastore.delete(boardList.get(0).getKey());
		tx.commit();
		String newBoard = CellContainer.toJson(container);
		GameModel.storeCurrentBoard(newBoard);
	    
		
		StatusResponse jsonresp;
		if (errorOccured) {
			jsonresp = new StatusResponse("fail", "exception occured when storing player");
			resp.getWriter().println(gson.toJson(jsonresp, StatusResponse.class));

		} else {
			// changeme
			resp.getWriter().println(GameModel.gameServerPath+"/gameGUI?data=" + URLEncoder.encode(gson.toJson(request, JoinSubGameMessage.class), "UTF-8"));
		}
	}
}
