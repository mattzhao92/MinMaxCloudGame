package servlets_public;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import servlets_private.TakeFinishedServlet;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.devrel.samples.ttt.Cell;
import com.google.devrel.samples.ttt.CellContainer;
import com.google.gson.Gson;

import Json.ApplyNextMoveInput;
import Json.ApplyNextMoveResponse;
import Json.GetMoveInput;
import Json.MakeAIMoveInput;
import Json.SocketMessage;
import Json.TakeTurnFinishedInput;
import Model.GameModel;
import Model.UrlPost;

public class MakeAIMoveServlet extends HttpServlet{
	private static final long serialVersionUID = -977766106447860017L;
	private Gson gson = new Gson();

	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException{

		System.out.println("MakeAIMoveServlet: ");

		// parse the input packet
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		MakeAIMoveInput request = gson.fromJson(reader, MakeAIMoveInput.class);
		UrlPost postUtil = new UrlPost();
		GetMoveInput gmi = new GetMoveInput();
		gmi.playerID = request.playerID;
		gmi.GameURL = "https://1-dot-gameserver4052.appspot.com";
		gmi.TreeDepth = 1;
		//System.out.println(">>>>>>>>>>>>> board" + request.board);
		gmi.ValidMoves = GameModel.getValidMovesForPlayer(request.playerID, request.board);
		//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>"+gson.toJson(gmi));

		String data = postUtil.sendCallbackPost(gson.toJson(gmi), request.AIURL+"/getMove");
		//System.out.println(">>>>>>>>>> data "+ data);

		
		CellContainer oldBoardContainer = gson.fromJson(GameModel.getCurrentBoard(), CellContainer.class);
		CellContainer newBoardContainer = gson.fromJson(data, CellContainer.class);
		ArrayList<Cell> oldCells = oldBoardContainer.cells;
		ArrayList<Cell> newCells = newBoardContainer.cells;
		
		int moveX = 0;
		int moveY = 0;
		for (int i = 0; i < oldCells.size(); i++) {
			for (int j = 0; j < newCells.size(); j++) {
				Cell oldCell = oldCells.get(i);
				Cell newCell = newCells.get(j);
				
				if (oldCell.x == newCell.x && oldCell.y == newCell.y &&
					!oldCell.playerName.equals(newCell.playerName)) {
					moveX = oldCell.x;
					moveY = oldCell.y;
				}
			}
		}

		
		GameModel.storeCurrentBoard(data);
		TakeTurnFinishedInput ttfi = new TakeTurnFinishedInput();
		System.out.println("MakeAIMoveServlet: "+moveX +" "+ moveY);
		ttfi.board = data;
		ttfi.x = moveX;
		ttfi.y = moveY;

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Key playerKey = KeyFactory.createKey("PlayerList", "MyPlayerList");

		// Run an ancestor query to ensure we see the most up-to-date
		// view of the Greetings belonging to the selected Guestbook.

		Query query = new Query("Player", playerKey);
		List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

		ChannelService service = ChannelServiceFactory.getChannelService();

		for (Entity entity: playerList) {
			String playername = (String) entity.getProperty("playerName");
			String token = (String) entity.getProperty("token");
			//System.out.println("broadCasting to player: "+playername);

			SocketMessage packet = new SocketMessage("updateView", data, true);
			ChannelMessage message = new ChannelMessage(token, gson.toJson(packet, SocketMessage.class));
			service.sendMessage(message);
			//channelService.sendMessage(message);
		}
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		postUtil.sendPost(gson.toJson(ttfi), GameModel.gameServerPath+"/takeTurnFinished");
	}	
}

