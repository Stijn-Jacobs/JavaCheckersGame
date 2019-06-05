package me.stijn.checkers;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.stijn.checkers.objects.Checker;
import me.stijn.checkers.objects.Checker.CheckerType;

public class Game implements Serializable {

	private static final int CHECKERS = 10;

	Board b;
	public Checker[][] checkers;
	int selectedX, selectedY;
	
	Set<Point> possibleSelections = new HashSet<>();
	Set<Point> selectedPosibilities = new HashSet<>();
	
	boolean hasToStrike = false;

	GameState state;
	Player turn;
	
	/**
	 * Create a new game object, which holds the current game status
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
			}
		}
		temp = 0;
		for (int y = Board.BOARDSIZE - 1; y > 0; y--) { // fill white checkers
			for (int x = y % 2; x < Board.BOARDSIZE; x += 2) {
				if (temp >= CHECKERS)
					break;
				temp++;
				checkers[x][y] = new Checker(CheckerType.WHITE);
			}
		}
		
		checkers[3][5] = new Checker(CheckerType.BLACK);
		checkers[7][5] = new Checker(CheckerType.BLACK);
		checkers[2][6] = new Checker(CheckerType.WHITEKING);
		
		//checkers[3][5] = new Checker(CheckerType.BLACK);
		//checkers[3][3] = new Checker(CheckerType.BLACK);
		calcPossible();
	}

	/**
	 * Get the current state of the game
	 * @return game state
	 */
	public GameState getState() {
		return state;
	}

	/**
	 * Set selected checker field, and automatically calculate next valid moves
	 * @param x coord of to be selected checker field
	 * @param y coord of to be selected checker field
	 */
	public void setSelected(int x, int y) {
		selectedX = x;
		selectedY = y;
		hasToStrike = false; //TODO VERIFY
		selectedPosibilities.clear();
		selectedPosibilities.addAll(calculatePosibilities(new Point(x, y),1));
	}

	/**
	 * Change turns 
	 * @param x coord of selected piece 
	 * @param y coord of selected piece 
	 */
	public void changeTurns(int x, int y) { 
		selectedPosibilities.clear();
		possibleSelections.clear();
		selectedX = -1;
		selectedY = -1;
		if (hasToStrike && validSelected(new Point(x,y))) { //has striked previous turn check if he can strike again
			hasToStrike = false;
			System.out.println("IMPORTANT: " + calculatePosibilities(new Point(x,y),1));
			calculatePosibilities(new Point(x,y),1); //check if he can strike again
			if (hasToStrike) { //can strike again
				System.out.println("Strike again");
				selectedX = x;
				selectedY = y;
				selectedPosibilities.addAll(calculatePosibilities(new Point(x, y),1));
				b.repaint();
				return;
			}
		}
		
		hasToStrike = false;
		
		//handle king when reaching edge
		if (validSelected(new Point(x,y))) { //only when real move and not shortcut
			Checker selected = checkers[x][y];
			if (y == 0 && selected.getType() == CheckerType.WHITE)
				selected.setType(CheckerType.WHITEKING);
			if (y == b.BOARDSIZE - 1 && selected.getType() == CheckerType.BLACK)
				selected.setType(CheckerType.BLACKKING);
		}

		if (turn == Player.BLACK)
			turn = Player.WHITE;
		else
			turn = Player.BLACK;
		
		calcPossible();
		b.repaint();
	}

	/**
	 * Calculate all possible moves across the board for the current player
	 */
	public void calcPossible() {
		possibleSelections.clear();
		for (int x = 0; x < Board.BOARDSIZE; x++) {
			for (int y = 0; y < Board.BOARDSIZE; y++) {
				Checker c = checkers[x][y];
				if (c == null)
					continue;
				if (!canBeSelected(c))
					continue;
				if (!checkSkips(new Point(x,y), 1, false, c.isKing()).isEmpty()) {
					System.out.println("Skip available");
					if (!hasToStrike) {
						possibleSelections.clear();
						hasToStrike = true;
					}
					possibleSelections.add(new Point(x, y));
					continue;
				}
				if (!calculatePosibilities(new Point(x, y),1).isEmpty() && !hasToStrike) {
					possibleSelections.add(new Point(x, y));
				}
			}
		}
		//b.repaint();
	}

	/**
	 * Calculate possible moves for point p
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
		System.out.println("bool: " + !checkSkips(p, delta, false, king).isEmpty());
		if (!checkSkips(p, delta, false, king).isEmpty()) {// skips available
			if (!hasToStrike) {
				selectedPosibilities.clear();
				hasToStrike = true;
			}
			System.out.println("Skip calced delta: ");
			//selectedPosibilities.addAll(checkSkips(p, delta)); //TODO TEST IF UNNESSESARY
			return checkSkips(p, delta, true, king);
		}
		List<Point> temp = new ArrayList<>();
		
		temp.add(new Point(p.x + delta, p.y + (direction * (delta))));
		temp.add(new Point(p.x - delta, p.y + (direction * (delta))));
		if (king) {
			temp.add(new Point(p.x + delta, p.y + ((direction == -1 ? +1 : -1) * (delta))));
			temp.add(new Point(p.x - delta, p.y + ((direction == -1 ? +1 : -1) * (delta))));
			if (delta == 1) {
				int dtemp = 2;
				List<Point> templist = new ArrayList<>();
				while(!calculatePosibilities(p, dtemp).isEmpty()){ //checkSkips(p,dtemp, true)
					if (hasToStrike) { //check if he is calculating strike
						if (!temp.isEmpty()) { //check if this is first available tile after strike has been spottet
							possibleSelections.clear();
							possibleSelections.add(p);
							selectedPosibilities.clear();
							temp.clear();
							templist.clear();
						}
						if (checkSkips(p,dtemp, false, king).isEmpty())
							break;
						templist.addAll(checkSkips(p,dtemp, true, king)); //TODO REMOVE LOOP
					} else {
						templist.addAll(calculatePosibilities(p, dtemp));
					}
					dtemp++;
				} 
				templist.addAll(checkValidLandingLocations(temp));
				return templist;
			}
		}
		return checkValidLandingLocations(temp);
	}

	/**
	 * Check if there are skips available for point p
	 * @param p Point where to check from
	 * @param delta depth delta
	 * @param extend boolean to extend the selection until it can't no more
	 * @return List of points where you can skip to
	 */
	private List<Point> checkSkips(Point p, int delta, boolean extend, boolean king) {
		//System.out.println("CheckSkips: " + delta + " : " + p);
		List<Point> temp = new ArrayList<>();
		int direction = getDirection();
		for (int i = 0; i < 2; i++) {
			if (i == 1)//reverse direction
				direction = (direction == -1 ? +1 : -1);
			if (b.checkBounds(new Point(p.x + (delta), p.y + (direction * (delta)))) && checkers[p.x + delta][p.y + (direction * delta)] != null
					&& !canBeSelected(checkers[p.x + delta][p.y + (direction * delta)]) && b.checkBounds(new Point(p.x + (delta + 1), p.y + (direction * (delta + 1))))
					&& checkers[p.x + (delta + 1)][p.y + (direction * (delta + 1))] == null) {
				temp.add(new Point(p.x + (delta + 1), p.y + (direction * (delta + 1))));
				if (extend && king)
					temp.addAll(addAfter(new Point(p.x + (delta + 1), p.y + (direction * (delta + 1))), i == 1 ? Direction.RIGHTDOWN : Direction.RIGHTUP));
			}
			if (b.checkBounds(new Point(p.x - (delta), p.y + (direction * (delta)))) && checkers[p.x - delta][p.y + (direction * delta)] != null
					&& !canBeSelected(checkers[p.x - delta][p.y + (direction * delta)]) && b.checkBounds(new Point(p.x - (delta + 1), p.y + (direction * (delta + 1))))
					&& checkers[p.x - (delta + 1)][p.y + (direction * (delta + 1))] == null) {
				temp.add(new Point(p.x - (delta + 1), p.y + (direction * (delta + 1))));
				if (extend && king)
					temp.addAll(addAfter(new Point(p.x - (delta + 1), p.y + (direction * (delta + 1))), i == 1 ? Direction.LEFTDOWN : Direction.LEFTUP));
			}
		}
		return temp;
	}
	
	private List<Point> addAfter(Point start, Direction dir){
		List<Point> list = new ArrayList<>();
		int delta = 1; 
		System.out.println("Started: " + start + " : " + dir);
		while (b.checkBounds(new Point(start.x + (dir.x * delta), start.y + (dir.y * delta))) && checkers[start.x + (dir.x * delta)][start.y + (dir.y * delta)] == null) {
			System.out.println("Added: " + new Point(start.x + (dir.x * delta), start.y + (dir.y * delta)));
			list.add(new Point(start.x + (dir.x * delta), start.y + (dir.y * delta)));
			delta++;
		}
		return list;
	}

	/**
	 * Returns true if given checker is valid
	 * @return 
	 */
	public boolean validSelected(Point p) {
		if (p.x == -1 || p.y == -1)
			return false;
		return true;
	}

	/**
	 * Get the direction of current team
	 * @return 
	 */
	private int getDirection() {
		return turn == Player.BLACK ? +1 : -1;
	}

//	/**
//	 * Returns true if checker is king
//	 * @param c Checker to be checked
//	 * @return 
//	 */
//	public boolean isKing(Checker c) {
//		return (c.getType() == CheckerType.BLACKKING || c.getType() == CheckerType.WHITEKING);
//	}

	/**
	 * Check if given list contains valid, empty locations. And return that.
	 * @param list
	 * @return
	 */
	private List<Point> checkValidLandingLocations(List<Point> list) {
		List<Point> temp = new ArrayList<>();
		for (Point p : list) {
			if (b.checkBounds(p))
				if (b.checkBounds(p) && checkers[(int) p.getX()][(int) p.getY()] == null) // check if valid location in board and space already occupied
					temp.add(p);
		}
		return temp;
	}

	/**
	 * Returns true if the checker trying to be selected is valid for selection and owned by the current player.
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

	public enum Player {
		BLACK, WHITE;
	}
}
