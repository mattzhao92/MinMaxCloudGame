var flappyMMCJ = {}
/** TicTacToe namespace for this sample. */
flappyMMCJ.model = flappyMMCJ.model || {};

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
		flappyMMCJ.model.waitingForMove = false;

		var board = gameView.getBoard();
		var status = flappyMMCJ.model.checkForVictory({'cells': board});

		if (status == flappyMMCJ.model.NOT_DONE) {
			flappyMMCJ.model.getComputerMove(JSON.stringify({'cells': board}));
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
};

/**
 * Gets the computer's move.
 * @param {string} boardString Current state of the board.
 */
flappyMMCJ.model.getComputerMove = function(boardString) {
	gapi.client.tictactoe.board.getmove({'state': boardString}).execute(
			function(resp) {
				var board = JSON.parse(resp.state);
				gameView.updateBoard(board);
				var status = flappyMMCJ.model.checkForVictory(board);
				if (status != flappyMMCJ.model.NOT_DONE) {
					flappyMMCJ.model.handleFinish(status);
				} else {
					flappyMMCJ.model.waitingForMove = true;
				}
			});
};

/**
 * Sends the result of the game to the server.
 * @param {number} status Result of the game.
 */
flappyMMCJ.model.sendResultToServer = function(status) {
	gapi.client.tictactoe.scores.insert({'outcome':
		flappyMMCJ.model.STATUS_STRINGS[status]}).execute(
				function(resp) {
					flappyMMCJ.model.queryScores();
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



/**
 * Initializes the application.
 */
flappyMMCJ.model.init = function() {
	// Loads the Tic Tac Toe API asynchronously, and triggers login
	// in the UI when loading has completed.
	var callback = function() {
		console.log('set up api key');

		var clientid = '310847526935-r758bkquplt27bk4sb820mpg150rdmib.apps.googleusercontent.com'
		gapi.client.setApiKey(clientid);  
		gapi.client.tictactoe.board.getboard().execute(
			function(resp) {

				var board = JSON.parse(resp.state);
				console.log("initializing game with board "+ resp.state);
				gameView = new GameView(flappyMMCJ.model, 4, board);
			}
		);
	}

	var apiRoot = 'http://localhost:8888/_ah/api';
	gapi.client.load('tictactoe', 'v1', callback, apiRoot);
};

window['flappyMMCJ.model.init'] = flappyMMCJ.model.init;

(function() {
	console.log('set up api key');

	var newScriptElement = document.createElement('script');
	newScriptElement.type = 'text/javascript';
	newScriptElement.async = true;
	newScriptElement.src = 'https://apis.google.com/js/client:plusone.js' +
	'?onload=flappyMMCJ.model.init';
	var firstScriptElement = document.getElementsByTagName('script')[0];
	firstScriptElement.parentNode.insertBefore(newScriptElement,
			firstScriptElement);

})();
