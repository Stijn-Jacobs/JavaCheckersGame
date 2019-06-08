package me.stijn.checkers.ai;

import java.awt.Point;

import me.stijn.checkers.Board;

public class AIPlayer {
	
	public static final int ITERATIONDEPTH = 3;
	
	Board originBoard, bestBoard;
	boolean isThinking = false;
	int maxScore = -1;
	
	
	public AIPlayer(Board b) {
		this.originBoard = b;
	}
	
	public void calculateTree() {
		Board b = originBoard.copy();
	
		b.game.calcPossibleSelectable(); //refresh selectable checkers
		System.out.println("Starting points: " + b.game.possibleSelections + " turn: " + b.game.turn + " origin: " + originBoard.game.possibleSelections );
		//for (int i = 0; i < ITERATIONDEPTH; i++) {
		for (Point p : originBoard.game.possibleSelections) { // loop through all possible selectable checkers
			System.out.println("Point: " + p);
			Board temp = b.copy();
		}
		
		
		
		
	}
	
	public void getBestMove() {
		
	}
	
	
	

}
