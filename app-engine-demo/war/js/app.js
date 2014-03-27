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


var game = null;
/**
 * Handles a square click.
 * @param {MouseEvent} e Mouse click event.
 */
flappyMMCJ.model.clickSquare = function(e) {
	if (flappyMMCJ.model.waitingForMove) {
    var button = e.target;
    button.innerHTML = 'X';
    button.bgColor='#FFF000';
    button.removeEventListener('click', flappyMMCJ.model.clickSquare);
    flappyMMCJ.model.waitingForMove = false;

    var boardString = flappyMMCJ.model.getBoardString();
    var status = flappyMMCJ.model.checkForVictory(boardString);
    if (status == flappyMMCJ.model.NOT_DONE) {
      flappyMMCJ.model.getComputerMove(boardString);
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
    flappyMMCJ.model.setBoardFilling(resp.state);
    var status = flappyMMCJ.model.checkForVictory(resp.state);
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
 * Shows or hides the board and game elements.
 * @param {boolean} state Whether to show or hide the board elements.
 */
flappyMMCJ.model.setBoardEnablement = function(state) {
  if (!state) {
    document.getElementById('board').classList.add('hidden');
    document.getElementById('gameHistoryWrapper').classList.add('hidden');
    document.getElementById('warning').classList.remove('hidden');
  } else {
    document.getElementById('board').classList.remove('hidden');
    //document.getElementById('gameHistoryWrapper').classList.remove('hidden');
    //document.getElementById('warning').classList.add('hidden');
  }
};

/**
 * Sets the filling of the squares of the board.
 * @param {string} boardString Current state of the board.
 */
flappyMMCJ.model.setBoardFilling = function(boardString) {
  var buttons = document.querySelectorAll('td');
  for (var i = 0; i < buttons.length; i++) {
    var button = buttons[i];
    button.innerHTML = boardString.charAt(i);
  }
};

/**
 * Checks for a victory condition.
 * @param {string} boardString Current state of the board.
 * @return {number} Status code for the victory state.
 */
flappyMMCJ.model.checkForVictory = function(boardString) {
  var status = flappyMMCJ.model.NOT_DONE;

  // Checks rows and columns.
  for (var i = 0; i < 3; i++) {
    var rowString = flappyMMCJ.model.getStringsAtPositions(
        boardString, i*3, (i*3)+1, (i*3)+2);
    status |= flappyMMCJ.model.checkSectionVictory(rowString);

    var colString = flappyMMCJ.model.getStringsAtPositions(
      boardString, i, i+3, i+6);
    status |= flappyMMCJ.model.checkSectionVictory(colString);
  }

  // Check top-left to bottom-right.
  var diagonal = flappyMMCJ.model.getStringsAtPositions(boardString,
      0, 4, 8);
  status |= flappyMMCJ.model.checkSectionVictory(diagonal);

  // Check top-right to bottom-left.
  diagonal = flappyMMCJ.model.getStringsAtPositions(boardString, 2,
      4, 6);
  status |= flappyMMCJ.model.checkSectionVictory(diagonal);

  if (status == flappyMMCJ.model.NOT_DONE) {
    if (boardString.indexOf('-') == -1) {
      return flappyMMCJ.model.TIE;
    }
  }

  return status;
};

/**
 * Checks whether a set of three squares are identical.
 * @param {string} section Set of three squares to check.
 * @return {number} Status code for the victory state.
 */
flappyMMCJ.model.checkSectionVictory = function(section) {
  var a = section.charAt(0);
  var b = section.charAt(1);
  var c = section.charAt(2);
  if (a == b && a == c) {
    if (a == 'X') {
      return flappyMMCJ.model.WON;
    } else if (a == 'O') {
      return flappyMMCJ.model.LOST
    }
  }
  return flappyMMCJ.model.NOT_DONE;
};

/**
 * Handles the end of the game.
 * @param {number} status Status code for the victory state.
 */
flappyMMCJ.model.handleFinish = function(status) {
  var victory = document.getElementById('victory');
  if (status == flappyMMCJ.model.WON) {
    victory.innerHTML = 'You win!';
  } else if (status == flappyMMCJ.model.LOST) {
    victory.innerHTML = 'You lost!';
  } else {
    victory.innerHTML = 'You tied!';
  }
  flappyMMCJ.model.sendResultToServer(status);
};

/**
 * Gets the current representation of the board.
 * @return {string} Current state of the board.
 */
flappyMMCJ.model.getBoardString = function() {
  var boardStrings = [];
  var buttons = document.querySelectorAll('td');
  for (var i = 0; i < buttons.length; i++) {
    boardStrings.push(buttons[i].innerHTML);
  }
  return boardStrings.join('');
};

/**
 * Gets the values of the board at the given positions.
 * @param {string} boardString Current state of the board.
 * @param {number} first First element to retrieve.
 * @param {number} second Second element to retrieve.
 * @param {number} third Third element to retrieve.
 */
flappyMMCJ.model.getStringsAtPositions = function(boardString, first,
    second, third) {
  return [boardString.charAt(first),
          boardString.charAt(second),
          boardString.charAt(third)].join('');
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

document.addEventListener('DOMContentLoaded', function() {
	   game = new GameView(flappyMMCJ.model, 4);
});