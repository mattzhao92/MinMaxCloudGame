/* Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devrel.samples.ttt.spi;

import java.util.*;

import sun.misc.CEFormatException;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.devrel.samples.ttt.*;
import com.google.gson.*;

/**
 * Defines v1 of a Board resource as part of the tictactoe API, which provides
 * clients the ability to query for a computer's next move given an input
 * board.
 */
@Api(
    name = "tictactoe",
    version = "v1",
    clientIds = {Ids.WEB_CLIENT_ID, Ids.ANDROID_CLIENT_ID, Ids.IOS_CLIENT_ID},
    audiences = {Ids.ANDROID_AUDIENCE}
)
public class BoardV1 {
  public static final char X = 'X';
  public static final char O = 'O';
  public static final char DASH = '-';
  
  /**
   * Provides the ability to insert a new Score entity.
   * 
   * @param board object representing the state of the board
   * @return the board including the computer's move
   */
  @ApiMethod(name = "board.getmove", path="getmove", httpMethod = "POST")
  public Board getmove(Board board) {
	CellContainer cellContainer = CellContainer.fromJson(board.getState());
	ArrayList<Cell> cells = cellContainer.cells;
	
	ArrayList<Cell> unoccupiedCells = new ArrayList<Cell>();
	for (Cell cell : cells ) {
		if (cell.val <= 10) {
			unoccupiedCells.add(cell);
		}
	}
	
	Random randomG = new Random();
    Cell randomfreeCell = unoccupiedCells.get(randomG.nextInt(unoccupiedCells.size()));
	randomfreeCell.val = randomfreeCell.val + 20;
	
	CellContainer updateContainer = new CellContainer(cells);
	Board updated = new Board(CellContainer.toJson(updateContainer));
    return updated;
  }
  
  
  @ApiMethod(name = "board.getboard", path="getboard", httpMethod = "POST")
  public Board getBoard() {
	  Board newBoard = new Board();
	  return newBoard;
  }
}
