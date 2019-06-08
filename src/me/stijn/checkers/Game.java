package me.stijn.checkers;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.stijn.checkers.ai.AIPlayer;
import me.stijn.checkers.objects.Checker;
import me.stijn.checkers.objects.FinishScreen;
import me.stijn.checkers.objects.Checker.CheckerType;

public class Game implements Serializable {

	private static final int CHECKERS = 0;

	Board b;
	public Checker[][] checkers;
	int selectedX, selectedY;
	int blackCheckers, whiteCheckers;

	public Set<Point> possibleSelections = new HashSet<>();
	public Set<Point> selectedPosibilities = new HashSet<>();

	boolean hasToStrike = false;

	GameState state;
	public Player turn;

	/**
	 * Create a new game object, which holds the current game status
	 * 
	 * @param b Board on which the game is played on
	 */
	public Game(Board b) {
		this.b = b;
		checkers = new Checker[Board.BOARDSIZE][Board.BOARDSIZE];

		state = GameState.RUNNING;
		turn = Player.WHITE;
		selectedX = -1;
		selectedY = -1;
		initCheckers();
	}

	/**
	 * Initially setup checkers in starting positions
	 */
	private void initCheckers() {
		int temp = 0;
		for (int y = 0; y < Board.BOARDSIZE; y++) { // fill black checkers
			for (int x = y % 2; x < Board.BOARDSIZE; x += 2) {
				if (temp >= CHECKERS)
					break;
				temp++;
				checkers[x][y] = new Checker(CheckerType.BLACK);
				blackCheckers++;
			}
		}
		temp = 0;
		for (int y = Board.BOARDSIZE - 1; y > 0; y--) { // fill white checkers
			for (int x = y % 2; x < Board.BOARDSIZE; x += 2) {
				if (temp >= CHECKERS)
					break;
				temp++;
				checkers[x][y] = new Checker(CheckerType.WHITE);
				whiteCheckers++;
			}
		}

		//debug checkers
		checkers[3][5] = new Checker(CheckerType.BLACK); 
		checkers[7][5] = new Checker(CheckerType.BLACK);
		checkers[2][6] = new Checker(CheckerType.WHITEKING);
		calcPossibleSelectable();
	}

	/**
	 * Get the current state of the game
	 * 
	 * @return game state
	 */
	public GameState getState() {
		return state;
	}

	/**
	 * Set selected checker field, and automatically calculate next valid moves
	 * 
	 * @param x coord of to be selected checker field
	 * @param y coord of to be selected checker field
	 */
	public void setSelected(int x, int y) {
		selectedX = x;
		selectedY = y;
		hasToStrike = false; // TODO VERIFY
		selectedPosibilities.clear();
		selectedPosibilities.addAll(calculatePosibilities(new Point(x, y), 1));
	}

	/**
	 * Change turns
	 * 
	 * @param x coord of selected piece
	 * @param y coord of selected piece
	 */
	public void changeTurns(int x, int y) {
		if (state != GameState.RUNNING)
			return;
		selectedPosibilities.clear();
		possibleSelections.clear();
		selectedX = -1;
		selectedY = -1;
		if (checkGameFinished())
			return;
		//check strike again mechanic same turn
		if (hasToStrike && validSelected(new Point(x, y))) { // has striked previous turn check if he can strike again
			hasToStrike = false;
			calculatePosibilities(new Point(x, y), 1); // check if he can strike again
			if (hasToStrike) { // can strike again
				System.out.println("Strike again");
				selectedX = x;
				selectedY = y;
				selectedPosibilities.addAll(calculatePosibilities(new Point(x, y), 1));
				b.repaint();
				return;
			}
		}

		hasToStrike = false;

		// handle king when reaching edge
		if (validSelected(new Point(x, y))) { // only when real move and not shortcut
			Checker selected = checkers[x][y];
			if (y == 0 && selected.getType() == CheckerType.WHITE)
				selected.setType(CheckerType.WHITEKING);
			if (y == Board.BOARDSIZE - 1 && selected.getType() == CheckerType.BLACK)
				selected.setType(CheckerType.BLACKKING);
		}
		
		if (turn == Player.BLACK)
			turn = Player.WHITE;
		else
			turn = Player.BLACK;

		calcPossibleSelectable();
		
		if (Main.AI) {
			AIPlayer p = new AIPlayer(b);
			p.calculateTree();
		}
		
		if (possibleSelections.isEmpty()) {
			//no more moves available for current player
			if (turn == Player.BLACK)
				finishGame(WinReason.WHITE);
			else 
				finishGame(WinReason.BLACK);
		}
		b.repaint();
	}
	
	private boolean checkGameFinished() {
		int black = -1,white = -1;
		for (int x = 0; x < Board.BOARDSIZE; x++) {
			for (int y = 0; y < Board.BOARDSIZE; y++) {
				if (checkers[x][y] != null) {
					if (checkers[x][y].isBlack())
						black++;
					else if (checkers[x][y].isWhite())
						white++;
				}
			}
		}
		
		//game finished
		if (black == -1) {
			finishGame(WinReason.WHITE);
			return true;
		}
		else if (white == -1) {
			finishGame(WinReason.BLACK);
			return true;
		}
		
		return false;
	}
	
	private void finishGame(WinReason win) {
		System.out.println("Player: " + win + " has won the game");
		
		FinishScreen scrn = new FinishScreen(win);
		Main.displayFinishScreen(scrn);
		state = GameState.STOPPED;
	}
	

	/**
	 * Calculate all possible moves across the board for the current player
	 */
	public void calcPossibleSelectable() {
		possibleSelections.clear();
		for (int x = 0; x < Board.BOARDSIZE; x++) {
			for (int y = 0; y < Board.BOARDSIZE; y++) {
				Checker c = checkers[x][y];
				if (c == null)
					continue;
				if (!canBeSelected(c))
					continue;
				if (!checkSkips(new Point(x, y), 1, false, c.isKing()).isEmpty()) {
					System.out.println("Skip available");
					if (!hasToStrike) {
						possibleSelections.clear();
						hasToStrike = true;
					}
					possibleSelections.add(new Point(x, y));
					continue;
				}
				if (!calculatePosibilities(new Point(x, y), 1).isEmpty() && !hasToStrike) {
					possibleSelections.add(new Point(x, y));
				}
			}
		}
		// b.repaint();
	}

	/**
	 * Calculate possible moves for point p
	 * 
	 * @param p Point from where to calculate
	 * @param delta depth delta
	 * @return list of possible selectable points
	 */
	public List<Point> calculatePosibilities(Point p, int delta) {
		int direction = getDirection();
		if ((p.x == -1 && p.y == -1) || checkers[p.x][p.y] == null) // check if checker selected is null and if so stop checking posibilities
			return new ArrayList<>();
		Checker c = checkers[p.x][p.y];
		boolean king = c.isKing();
		if (!checkSkips(p, delta, false, king).isEmpty()) {// skips available
			if (!hasToStrike) {
				selectedPosibilities.clear();
				hasToStrike = true;
			}
			System.out.println("Skip calced");
			// selectedPosibilities.addAll(checkSkips(p, delta)); //TODO TEST IF UNNESSESARY
			return checkSkips(p, delta, true, king);
		}
		List<Point> temp = new ArrayList<>();

		temp.add(new Point(p.x + delta, p.y + (direction * (delta))));
		temp.add(new Point(p.x - delta, p.y + (direction * (delta))));
		if (king) {
			temp.add(new Point(p.x + delta, p.y + ((direction == -1 ? +1 : -1) * (delta))));
			temp.add(new Point(p.x - delta, p.y + ((direction == -1 ? +1 : -1) * (delta))));
			if (delta == 1) {//itterate
				int dtemp = 2;
				List<Point> templist = new ArrayList<>();
				while (!calculatePosibilities(p, dtemp).isEmpty()) { // checkSkips(p,dtemp, true)
					if (hasToStrike) { // check if he is calculating strike
						if (!temp.isEmpty()) { // check if this is first available tile after strike has been spottet
							possibleSelections.clear();
							possibleSelections.add(p);
							selectedPosibilities.clear();
							temp.clear();
							templist.clear();
						}
						if (checkSkips(p, dtemp, false, king).isEmpty())
							break;
						templist.addAll(checkSkips(p, dtemp, true, king)); // TODO REMOVE LOOP
					} else {
						templist.addAll(calculatePosibilities(p, dtemp));
					}
					dtemp++;
				}
				templist.addAll(checkValidLandingLocations(temp, p));
				if (hasToStrike)
					return templist;
				
				// remove unreachable points when jumping
				List<Point> returnlist = new ArrayList<>(); 
				for (Direction d : Direction.values()) {
					Point tempp = (Point) p.clone();
					templist.remove(tempp);
					do {
						if (templist.contains((Point) tempp.clone()))
							returnlist.add((Point) tempp.clone());
						tempp.x = tempp.x + d.x;
						tempp.y = tempp.y + d.y;
					} while (b.checkBounds(tempp) && (checkers[tempp.x][tempp.y] == null));
				}
				return returnlist;
			}
		}
		return checkValidLandingLocations(temp, p);
	}

	/**
	 * Check if there are skips available for point p
	 * 
	 * @param p Point where to check from
	 * @param delta depth delta
	 * @param extend boolean to extend the selection until it can't no more
	 * @return List of points where you can skip to
	 */
	private List<Point> checkSkips(Point p, int delta, boolean extend, boolean king) { //grootste beun methode van heel het spel
		List<Point> temp = new ArrayList<>();
		int direction = getDirection();
		
		for (int i = 0; i < 2; i++) {
			if (i == 1)// reverse direction
				direction = (direction == -1 ? +1 : -1);
			Point p1 = new Point(p.x + (delta), p.y + (direction * (delta)));
			if (b.checkBounds(new Point(p.x + (delta - 1), p.y + (direction * (delta - 1)))) //check if prev checker is empty
					&& (checkers[p.x + (delta - 1)][p.y + (direction * (delta - 1))] == null || canBeSelected(checkers[p.x + (delta - 1)][p.y + (direction * (delta - 1))])) && b.checkBounds(p1)
					&& checkers[p1.x][p1.y] != null && !canBeSelected(checkers[p1.x][p1.y]) && // check if strike isn't empty and on opposite team
					b.checkBounds(new Point(p.x + (delta + 1), p.y + (direction * (delta + 1)))) && checkers[p.x + (delta + 1)][p.y + (direction * (delta + 1))] == null) { // check if landing is empty
				temp.add(new Point(p.x + (delta + 1), p.y + (direction * (delta + 1))));
				if (extend && king)
					temp.addAll(addAfter(new Point(p.x + (delta + 1), p.y + (direction * (delta + 1))), i == 1 ? Direction.RIGHTDOWN : Direction.RIGHTUP));
			}
			Point p2 = new Point(p.x - (delta), p.y + (direction * (delta)));
			if (b.checkBounds(new Point(p.x - (delta - 1), p.y + (direction * (delta - 1)))) //check if prev checker is empty
					&& (checkers[p.x - (delta - 1)][p.y + (direction * (delta - 1))] == null || canBeSelected(checkers[p.x - (delta - 1)][p.y + (direction * (delta - 1))])) && b.checkBounds(p2)
					&& checkers[p2.x][p2.y] != null && !canBeSelected(checkers[p2.x][p2.y]) &&// check if strike isn't empty and on opposite team
					b.checkBounds(new Point(p.x - (delta + 1), p.y + (direction * (delta + 1)))) && checkers[p.x - (delta + 1)][p.y + (direction * (delta + 1))] == null) {// check if landing is empty
				temp.add(new Point(p.x - (delta + 1), p.y + (direction * (delta + 1))));
				if (extend && king)
					temp.addAll(addAfter(new Point(p.x - (delta + 1), p.y + (direction * (delta + 1))), i == 1 ? Direction.LEFTDOWN : Direction.LEFTUP));
			}
		}
		return temp;
	}

	/**
	 * Add all availanble open spaces after point start in direction dir
	 * @param start start point
	 * @param dir Direction
	 * @return list of points
	 */
	private List<Point> addAfter(Point start, Direction dir) {
		List<Point> list = new ArrayList<>();
		int delta = 1;
		while (b.checkBounds(new Point(start.x + (dir.x * delta), start.y + (dir.y * delta))) && checkers[start.x + (dir.x * delta)][start.y + (dir.y * delta)] == null) {
			list.add(new Point(start.x + (dir.x * delta), start.y + (dir.y * delta)));
			delta++;
		}
		return list;
	}

	/**
	 * Returns true if given checker is valid
	 * 
	 * @return
	 */
	public boolean validSelected(Point p) {
		if (p.x == -1 || p.y == -1)
			return false;
		return true;
	}

	/**
	 * Get the direction of current team
	 * 
	 * @return
	 */
	private int getDirection() {
		return turn == Player.BLACK ? +1 : -1;
	}

	/**
	 * Check if given list contains valid, empty locations. And return that.
	 * 
	 * @param list
	 * @return
	 */
	private List<Point> checkValidLandingLocations(List<Point> list, Point origin) {
		List<Point> temp = new ArrayList<>();
		for (Point p : list) {
			if (b.checkBounds(p))
				if (checkers[(int) p.getX()][(int) p.getY()] == null) // check if valid location in board and space already occupied
					temp.add(p);
		}
		return temp;
	}

	/**
	 * Returns true if the checker trying to be selected is valid for selection and owned by the current player.
	 * 
	 * @param c
	 * @return
	 */
	public boolean canBeSelected(Checker c) {
		if (c == null)
			return false;
		if (turn == Player.BLACK && (c.getType() != CheckerType.BLACK && c.getType() != CheckerType.BLACKKING))
			return false;
		if (turn == Player.WHITE && (c.getType() != CheckerType.WHITE && c.getType() != CheckerType.WHITEKING))
			return false;
		return true;
	}
	
	public void remove(int x, int y) {
		if (!b.checkBounds(new Point(x,y)))
			return;
		if (checkers[x][y] == null)
			return;
		Checker remove = checkers[x][y];
		if (remove.isBlack())
			blackCheckers--;
		else if (remove.isWhite())
			whiteCheckers--;
		
		checkers[x][y] = null; 
	}
	
	public Game copy(Board b) {
		Game copy = new Game(b);
		copy.checkers = checkers.clone();
		copy.turn = turn;
		return copy;
	}

	public enum Player {
		BLACK, WHITE;
	}
	
	public enum WinReason{
		BLACK,WHITE,DRAW;
	}
}
