package me.stijn.checkers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.stijn.checkers.objects.Checker;
import me.stijn.checkers.objects.Checker.CheckerType;
import me.stijn.checkers.objects.MoveAnimation;

public class Game {

	private static final int CHECKERS = 20;

	Board b;
	public Checker[][] checkers;
	int selectedX, selectedY;
	
	Set<Point> possibleSelections = new HashSet<>();
	Set<Point> selectedPosibilities = new HashSet<>();
	
	boolean hasToStrike;

	GameState state;
	Player turn;

	public Game(Board b) {
		this.b = b;
		checkers = new Checker[b.BOARDSIZE][b.BOARDSIZE];

		state = GameState.RUNNING;
		turn = Player.BLACK;
		selectedX = -1;
		selectedY = -1;
		initCheckers();
	}

	private void initCheckers() {
		int temp = 0;
		for (int y = 0; y < b.BOARDSIZE; y++) { // fill black checkers
			for (int x = y % 2; x < b.BOARDSIZE; x += 2) {
				if (temp >= CHECKERS)
					break;
				temp++;
				checkers[x][y] = new Checker(CheckerType.BLACK);
			}
		}
		temp = 0;
		for (int y = b.BOARDSIZE - 1; y > 0; y--) { // fill white checkers
			for (int x = y % 2; x < b.BOARDSIZE; x += 2) {
				if (temp >= CHECKERS)
					break;
				temp++;
				checkers[x][y] = new Checker(CheckerType.WHITE);
			}
		}
		calcPossible();
	}

	public GameState getState() {
		return state;
	}

	public void setSelected(int x, int y) {
		selectedX = x;
		selectedY = y;
		selectedPosibilities.clear();
		selectedPosibilities.addAll(calculatePosibilities(new Point(x, y)));
	}

	public void changeTurns() {
		selectedPosibilities.clear();
		possibleSelections.clear();
		selectedX = -1;
		selectedY = -1;
		hasToStrike = false;
		if (turn == Player.BLACK)
			turn = Player.WHITE;
		else
			turn = Player.BLACK;
		calcPossible();
	}

	private void calcPossible() {
		possibleSelections.clear();
		for (int x = 0; x < b.BOARDSIZE; x++) {
			for (int y = 0; y < b.BOARDSIZE; y++) {
				Checker c = checkers[x][y];
				if (c == null)
					continue;
				if (!canBeSelected(c))
					continue;
				if (!checkSkips(new Point(x,y), 1).isEmpty()) {
					System.out.println("Skip available");
					if (!hasToStrike) {
						possibleSelections.clear();
						hasToStrike = true;
					}
					possibleSelections.add(new Point(x, y));
					continue;
				}
				if (!calculatePosibilities(new Point(x, y)).isEmpty() && !hasToStrike) {
					possibleSelections.add(new Point(x, y));
				}
			}
		}
		b.repaint();
	}

	public List<Point> calculatePosibilities(Point p) {
		int direction = getDirection();
		if (checkers[p.x][p.y] == null) // check if checker selected is null and if so stop checking posibilities
			return new ArrayList<>();
		Checker c = checkers[p.x][p.y];
		boolean king = isKing(c);
		if (king) {
			
		} else if (!checkSkips(p, 1).isEmpty()) {// skips available
			if (!hasToStrike) {
				selectedPosibilities.clear();
				hasToStrike = true;
			}
			System.out.println("Skip calced");
			selectedPosibilities.addAll(checkSkips(p, 1));

			return checkSkips(p, 1);
		}
		List<Point> temp = new ArrayList<>();

		temp.add(new Point(p.x + 1, p.y + direction));
		temp.add(new Point(p.x - 1, p.y + direction));
		if (king) {
			temp.add(new Point(p.x + 1, p.y + direction == -1 ? +1 : -1));
			temp.add(new Point(p.x - 1, p.y + direction == -1 ? +1 : -1));

		}
		return checkValidLandingLocations(temp);
	}

	private List<Point> checkSkips(Point p, int delta) {
		List<Point> temp = new ArrayList<>();
		int direction = getDirection();

		if (b.checkBounds(new Point(p.x + (delta), p.y + (direction * (delta)))) && checkers[p.x + delta][p.y + (direction * delta)] != null && !canBeSelected(checkers[p.x + delta][p.y + (direction * delta)]) 
		 && b.checkBounds(new Point(p.x + (delta + 1), p.y + (direction * (delta + 1)))) && checkers[p.x + (delta + 1)][p.y + (direction * (delta + 1))] == null) {
			temp.add(new Point(p.x + (delta + 1), p.y + (direction * (delta + 1))));
		}
		if (b.checkBounds(new Point(p.x - (delta), p.y + (direction * (delta)))) && checkers[p.x - delta][p.y + (direction * delta)] != null && !canBeSelected(checkers[p.x - delta][p.y + (direction * delta)]) 
		 && b.checkBounds(new Point(p.x - (delta + 1), p.y + (direction * (delta + 1)))) && checkers[p.x - (delta + 1)][p.y + (direction * (delta + 1))] == null) {
			temp.add(new Point(p.x - (delta + 1), p.y + (direction * (delta + 1))));
		}

		// temp.add(new Point(p.x + delta, p.y + (direction * delta)));
		// temp.add(new Point(p.x - delta, p.y + (direction * delta)));
		return temp;
	}

	private boolean validSelected() {
		if (selectedX == -1 || selectedY == -1)
			return false;
		return true;
	}

	private int getDirection() {
		return turn == Player.BLACK ? +1 : -1;
	}

	public boolean isKing(Checker c) {
		return (c.getType() == CheckerType.BLACKKING || c.getType() == CheckerType.WHITEKING);
	}

	private List<Point> checkValidLandingLocations(List<Point> list) {
		List<Point> temp = new ArrayList<>();
		for (Point p : list) {
			if (b.checkBounds(p))
				if (b.checkBounds(p) && checkers[(int) p.getX()][(int) p.getY()] == null) // check if valid location in board and space already occupied
					temp.add(p);
		}
		return temp;
	}

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
