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

  private String board;

  
  public Board() {
     int [] values = new int [] {4, 2, 3, 7, 6, 4, 9, 8, 4, 5, 6, 8, 5, 2, 5, 8};
     int count = 0;
     Gson gson = new Gson();
     ArrayList<Cell> cells = new ArrayList<Cell>();
     for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            cells.add(new Cell(i, j, values[count++]));
        }
     }
     this.board = gson.toJson(cells);
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }
}