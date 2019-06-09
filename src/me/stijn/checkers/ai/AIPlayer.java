package me.stijn.checkers.ai;

import java.awt.Point;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

import me.stijn.checkers.Board;
import me.stijn.checkers.Main;

public class AIPlayer {
	
	public static final int ITERATIONDEPTH = 3;
	
	Board bestBoard;
	boolean isThinking = false;
	int maxScore = Integer.MIN_VALUE;
	
	
	public AIPlayer() {
		
	}
	
	public void calculateTree(Board origin) {
		Board b = origin.copy();
		maxScore = -1;
		b.game.hasToStrike = false;
		b.game.calcPossibleSelectable(); //refresh selectable checkers
		System.out.println("-----------------------------Starting points: " + b.game.possibleSelections + " turn: " + b.game.turn + " origin: " + origin.game.possibleSelections + " tesT: ");

		for (Point p : b.game.possibleSelections) {
			b.game.hasToStrike = false;
			for (Point p2 : b.game.calculatePosibilities(p, 1)) {
				Board copy1 = b.copy();
				System.out.println("Possiblemove: " + p + " to "+ p2 );
				copy1.move(b.game.checkers[p.x][p.y], p, p2, false);
				//copy1.game.hasToStrike = false;
				copy1.game.calcPossibleSelectable();
				
				for (Point p3 : copy1.game.possibleSelections) {
					copy1.game.hasToStrike = false;
					for (Point p4 : copy1.game.calculatePosibilities(p3, 1)) {
						Board copy2 = copy1.copy();
						//
						System.out.println("Possiblemove level 2: " + p4);
						copy2.move(copy2.game.checkers[p3.x][p3.y], p3, p4, false);
						//copy2.game.hasToStrike = false;
						//copy2.game.calcPossibleSelectable();
						
						for (Point p5 : copy2.game.possibleSelections) {
							copy2.game.hasToStrike = false;
							for (Point p6 : copy2.game.calculatePosibilities(p5, 1)) {
								Board copy3 = copy2.copy();
								//copy3.game.hasToStrike = false;
								System.out.println("Possiblemove level 3: " + p6);
								copy3.move(copy3.game.checkers[p5.x][p5.y], p5, p6, false);
								
								if (copy3.getScore() > maxScore) {
									bestBoard = copy3;
									maxScore = copy3.getScore();
									System.out.println(p + " BEST MOVE STREAK: to " + p2 + " : score:" + copy2.getScore());
									System.out.println("STREAK: " + p3 + " to: " + p4);
									System.out.println("STREAK: " + p5 + " to: " + p6);
									Main.board.bestMoves.clear();
									Main.board.bestMoves.add(p2);
									Main.board.bestMoves.add(p4);
									Main.board.bestMoves.add(p6);
								}
							}
						}
					}
				}
				
				
				
			}
		}
		
		
		
	}
	
	
	
	public void getBestMove() {
		
	}
	
	
	

}
