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

package com.google.devrel.samples.ttt;

/**
 * Defines the state of the board in a game of Tic Tac Toe. This class is only
 * used as a wire format.
 */
public class Board {
  /**
   * The current state of the board, represented as a single dimensional array,
   * e.g. ----X---- which represents a board with an X in the center.
   */
	
  
  private String state;

  
  public Board() {
	  int [] values = new int [] {7, 5, 2, 4, 
			  					  2, 9, 5, 6,
			  					  3, 2, 8, 2,
			  					  4, 2, 7, 4};
	  CellContainer container = new CellContainer();
	  int count = 0;
	  for (int i = 0; i < 4; i++) {
		  for (int j = 0; j < 4; j++) {
			  container.add(new Cell(i,j,values[count++], "None"));
		  }
	  }
	  state = CellContainer.toJson(container);
  }
  
  public Board(String stateIn) {
	  this.state = stateIn;
  }
  
  public String getState() {
    return state;
  }


  public void setState(String state) {
    this.state = state;
  }
}