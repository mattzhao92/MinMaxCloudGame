var flappyMMCJ = {}
/** TicTacToe namespace for this sample. */
flappyMMCJ.model = flappyMMCJ.model || {};

//flappyMMCJ.TCServer = 'https://1-dot-striped-buckeye-555.appspot.com';
flappyMMCJ.TCServer = 'http://localhost:8887';
flappyMMCJ.gameServer = 'http://localhost:8888';

/**
 * Status for an unfinished game.
 * @type {number}
 */
flappyMMCJ.model.NOT_DONE = 0;

/**
 * Status for a victory.
 * @type {number}
 */
flappyMMCJ.model.WON = 1;

/**
 * Status for a loss.
 * @type {number}
 */
flappyMMCJ.model.LOST = 2;

/**
 * Status for a tie.
 * @type {number}
 */
flappyMMCJ.model.TIE = 3;

/**
 * Strings for each numerical status.
 * @type {Array.number}
 */
flappyMMCJ.model.STATUS_STRINGS = [
                                   'NOT_DONE',
                                   'WON',
                                   'LOST',
                                   'TIE'
                                   ];

flappyMMCJ.model.cells = [];
flappyMMCJ.model.humanPlayerCells = [];
flappyMMCJ.model.computerPlayerCells = [];


/**
 * Whether or not the user is signed in.
 * @type {boolean}
 */
flappyMMCJ.model.signedIn = false;

/**
 * Whether or not the game is waiting for a user's move.
 * @type {boolean}
 */
flappyMMCJ.model.waitingForMove = true;


var gameView = null;
/**
 * Handles a square click.
 * @param {MouseEvent} e Mouse click event.
 */
flappyMMCJ.model.clickSquare = function(x, y) {
	
	if (flappyMMCJ.model.waitingForMove) {
		gameView.disableMouseListeners();

		flappyMMCJ.model.waitingForMove = false;

		var board = gameView.getBoard();

		flappyMMCJ.model.broadCastChange(JSON.stringify({'cells': board}));
		flappyMMCJ.model.onTakeTurnFinished(JSON.stringify({'cells': board, 'x':x, 'y':y}));

		var status = flappyMMCJ.model.checkForVictory({'cells': board});


		if (status == flappyMMCJ.model.NOT_DONE) {
			//flappyMMCJ.model.getComputerMove(JSON.stringify({'cells': board}));
		} else {
			flappyMMCJ.model.handleFinish(status);
		}
	}
};



/**
 * Resets the game board.
 */
flappyMMCJ.model.resetGame = function() {
	var buttons = document.querySelectorAll('td');
	for (var i = 0; i < buttons.length; i++) {
		var button = buttons[i];
		button.removeEventListener('click', flappyMMCJ.model.clickSquare);
		button.addEventListener('click', flappyMMCJ.model.clickSquare);
		button.innerHTML = '-';
		button.bgColor = '#FFFFFF';
	}
	document.getElementById('victory').innerHTML = '';
	flappyMMCJ.model.waitingForMove = true;
	gameView.enableMouseListeners();
};

/**
 * Gets the computer's move.
 * @param {string} boardString Current state of the board.
 */
flappyMMCJ.model.getComputerMove = function(boardString) {

//	$.post("http://localhost:8887/getRandomMove", boardString, function(boardState){
//		console.log("getComputerMove: "+boardState);
//		var board = JSON.parse(boardState);
//		gameView.updateBoard(board);
//		var status = flappyMMCJ.model.checkForVictory(board);
//		if (status != flappyMMCJ.model.NOT_DONE) {
//			flappyMMCJ.model.handleFinish(status);
//		} else {
//			flappyMMCJ.model.waitingForMove = true;
//			gameView.enableMouseListeners();
//		}
//	});
};

/**
 * Sends the result of the game to the server.
 * @param {number} status Result of the game.
 */
flappyMMCJ.model.sendResultToServer = function(status) {
	
};


flappyMMCJ.model.onTakeTurnFinished = function(board) {
	var msg = {
		'board' : board
	};

	console.log('takeTurnFinished');
	console.log(msg);

	$.post(flappyMMCJ.gameServer + "/takeTurnFinished", JSON.stringify(msg), function(resp){
		console.log("takeTurnFinished callback");
	});
};


flappyMMCJ.model.broadCastChange = function(board) {
	var msg = {
		'playername' : flappyMMCJ.model.playername,
		'board' : board
	};

	console.log('broadCastChange');
	console.log(msg);

	$.post(flappyMMCJ.gameServer + "/broadCastMove", JSON.stringify(msg), function(resp){
		console.log("broadCastChange callback");
	});
};


/**
 * Queries for results of previous games.
 */
flappyMMCJ.model.queryScores = function() {
	gapi.client.tictactoe.scores.list().execute(function(resp) {
		var history = document.getElementById('gameHistory');
		history.innerHTML = '';
		if (resp.items) {
			for (var i = 0; i < resp.items.length; i++) {
				var score = document.createElement('li');
				score.innerHTML = resp.items[i].outcome;
				history.appendChild(score);
			}
		}
	});
};


/**
 * Checks for a victory condition.
 * @param {string} boardString Current state of the board.
 * @return {number} Status code for the victory state.
 */
flappyMMCJ.model.checkForVictory = function(board) {
	var cells = board.cells;

	var num_free = 0;
	var score_player1 = 0;
	var score_player2 = 0;
	for (var i = 0; i < cells.length; i++) {
		var cell = cells[i];
		if (0 < cell.val && cell.val <= 10) {
			num_free += 1;
		} else if (10 < cell.val && cell.val <= 20) {
			score_player1 += cell.val - 10;
		} else if (20 < cell.val && cell.val <= 30) {
			score_player2 += cell.val - 20;
		}
	}

	if (num_free > 0) {
		return flappyMMCJ.model.NOT_DONE;
	}

	if (score_player1 > score_player2) {
		return flappyMMCJ.model.WON;
	}

	if (score_player1 < score_player2) {
		return flappyMMCJ.model.LOST;
	}

	return flappyMMCJ.model.TIE;

};

/**
 * Handles the end of the game.
 * @param {number} status Status code for the victory state.
 */
flappyMMCJ.model.handleFinish = function(status) {
	var victory = document.getElementById('victory');
	if (status == flappyMMCJ.model.WON) {
		alert('You win!');
	} else if (status == flappyMMCJ.model.LOST) {
		alert('You lost!');
	} else {
		alert('TIE');
	}
};

flappyMMCJ.socket = {};

flappyMMCJ.socket.onOpened = function () {
    if (flappyMMCJ.model.initCallback) {
    	console.log("initCallback");
    	flappyMMCJ.model.initCallback();
    }
};

flappyMMCJ.socket.onMessage = function (msg) {

    var packet = JSON.parse(msg.data);

    if (packet.type == "updateView") {
    	var content = JSON.parse(packet.content);
    	console.log("updateView");
		var board = JSON.parse(packet.content);
		console.log(board);
		gameView.updateBoard(board);
        
        if (content.lockScreen) {
		    flappyMMCJ.model.waitingForMove = false;
        } else {
		    flappyMMCJ.model.waitingForMove = true;
        }
		gameView.enableMouseListeners();
    }
    if (packet.type == "redirect") {
    	var content = JSON.parse(packet.content);
    	console.log("redirecting to a new game");
    	console.log(content);
    	if (content.status == "ok") {
    		var request = {
    				'playerName' : content.playerName,
    				'isAI' : content.isAI,
    				'playerID' : content.playerId,
    				'inboundingPortID' : content.inboundPortalID,
    				'AIUrl' : content.AIUrl
    		};

			$.post(content.gameURL+'/joinSubGame', JSON.stringify(request),
    			function(data) {
					var packet = JSON.parse(data);
					if (packet.status == "ok") {
						window.location.replace(packet.redirect);
					}
    			}
			);
    	}
    }
};

flappyMMCJ.socket.onError = function (err) {
    //alert("Channel opened!");
};

flappyMMCJ.socket.onClose = function () {
    //alert("Channel opened!");
};


flappyMMCJ.model.setupChannel = function(channelToken, initCallback) {
	flappyMMCJ.model.initCallback = initCallback;
	var channel = new goog.appengine.Channel(channelToken);
	console.log(">>>>> channelToken:" + channelToken);
	debugger;
    var socket = channel.open();
    socket.onopen = flappyMMCJ.socket.onOpened;
    socket.onmessage = flappyMMCJ.socket.onMessage;
    socket.onerror = flappyMMCJ.socket.onError;
    socket.onclose = flappyMMCJ.socket.onClose;

    $(window).on('beforeunload', function() {
    	clearTimeout(socket.pollingTimer_);
	});
};

/**
 * Initializes the application.
 */
flappyMMCJ.model.init = function() {
	//changme
	$.get(flappyMMCJ.gameServer + "/getBoard", function(boardState){
		console.log("initializing game with board "+ boardState);
		var board = JSON.parse(boardState);

		gameView = new GameView(flappyMMCJ.model, 4, board);
	});
};

window['flappyMMCJ.model.init'] = flappyMMCJ.model.init;

$(function() {
	var newScriptElement = document.createElement('script');
	newScriptElement.type = 'text/javascript';
	newScriptElement.async = true;
	newScriptElement.src = 'https://apis.google.com/js/client:plusone.js' +
	'?onload=flappyMMCJ.model.init';
	var firstScriptElement = document.getElementsByTagName('script')[0];
	firstScriptElement.parentNode.insertBefore(newScriptElement,
			firstScriptElement);

});
