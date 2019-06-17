package me.stijn.checkers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import javafx.animation.AnimationTimer;
import me.stijn.checkers.Game.GameState;
import me.stijn.checkers.Game.Player;
import me.stijn.checkers.objects.Checker;
import me.stijn.checkers.objects.Checker.CheckerType;

public class Board extends JPanel implements MouseListener, KeyListener {

	public static BufferedImage KINGIMGWHITE, KINGIMGBLACK;
	public final int BOARDSIZE;

	public HashMap<Checker, Point2D.Double> animating = new HashMap<>();
	public HashMap<Point, Integer> removalAnimation = new HashMap<>();

	public Game game;

	public int CHECKERSIZE, XOFFSET, YOFFSET;
	
	/**
	 * Copy board method
	 * @deprecated
	 * @return
	 */
	public Board copy() {
		Board copy = new Board(BOARDSIZE);
		copy.game = game.copy(copy);
		return copy;
	}
	
	/**
	 * Board object where the checker game is playing on
	 */
	public Board(int boardsize) {
		this.BOARDSIZE = boardsize;
		game = new Game(this);
		addMouseListener(this);

		try { // load king image
			KINGIMGWHITE = ImageIO.read(getClass().getResource("/me/stijn/checkers/resources/king.png"));
			KINGIMGBLACK = new BufferedImage(KINGIMGWHITE.getWidth(), KINGIMGWHITE.getHeight(), IndexColorModel.TRANSLUCENT);
			for (int y = 0; y < KINGIMGBLACK.getHeight(); y++) { // invert colors for black king
				for (int x = 0; x < KINGIMGBLACK.getWidth(); x++) {
					int p = KINGIMGWHITE.getRGB(x, y);
					int a = (p >> 24) & 0xff;
					int r = (p >> 16) & 0xff;
					int g = (p >> 8) & 0xff;
					int b = p & 0xff;
					if (r < 255 && g < 255 && b < 255)// check if black
						p = (255 << 24) | (255 << 16) | (255 << 8) | 255;
					else
						p = 0;
					KINGIMGBLACK.setRGB(x, y, p);
				}
			}
		} catch (IllegalArgumentException | IOException e) { // als king image niet kan vinden
			System.out.println("King image not found");
		}

	}

	@Override
	protected void paintComponent(Graphics g) {
		long time = System.currentTimeMillis();
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawBoard(g2d);
		drawInfo(g2d);
		drawCheckers(g2d);
		drawAnimating(g2d);

		System.out.println("Frame took: " + (System.currentTimeMillis() - time) + " ms to render");
		if ((System.currentTimeMillis() - time) > 0 && Main.showFPS) {
			g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 30));
			g2d.drawString("FPS: " + 1000 / (System.currentTimeMillis() - time), 1, 50);
		}
		
		if (Main.debug)
			g2d.drawString("DEBUG ENABLED:", 1, 100);
	}

	private void drawInfo(Graphics2D g) {
		int margin = CHECKERSIZE < 50 ? 3 : 5; // padding arround checker

		int ovalx = (BOARDSIZE * CHECKERSIZE) + XOFFSET + (CHECKERSIZE / 3);
		if (game.turn == Player.WHITE) {//white player's turn indicator
			g.setPaint(new GradientPaint((BOARDSIZE * CHECKERSIZE) + XOFFSET + 4 + (int) ((CHECKERSIZE * BOARDSIZE) - (CHECKERSIZE * 1.5)), (CHECKERSIZE / 2) + YOFFSET + 4,
					new Color(40, 255, 25), (BOARDSIZE * CHECKERSIZE) + XOFFSET + 50, (CHECKERSIZE / 2) + YOFFSET + 50, new Color(10, 150, 10)));
			g.fillOval(ovalx, (int) ((CHECKERSIZE * BOARDSIZE) - (CHECKERSIZE * 1.5)) + YOFFSET + 4, CHECKERSIZE, CHECKERSIZE);
		} else if (game.turn == Player.BLACK) { //black player's turn indicator
			g.setPaint(new GradientPaint((BOARDSIZE * CHECKERSIZE) + XOFFSET + 4 + +(CHECKERSIZE / 2), (CHECKERSIZE / 2) + YOFFSET + 4, new Color(40, 255, 25),
					(BOARDSIZE * CHECKERSIZE) + XOFFSET + 50, (CHECKERSIZE / 2) + YOFFSET + 50, new Color(10, 150, 10)));
			g.fillOval(ovalx, (CHECKERSIZE / 2) + YOFFSET + 4, CHECKERSIZE, CHECKERSIZE);
		}

		Point start = new Point((int) (BOARDSIZE * CHECKERSIZE) + XOFFSET + margin + (CHECKERSIZE / 3), (int) (CHECKERSIZE / 2) + YOFFSET + margin + 4); // black point
		int textx = (BOARDSIZE * CHECKERSIZE) + XOFFSET + margin + (CHECKERSIZE / 3) + ((CHECKERSIZE - (margin * 2)) / 2);//x coord for all text
		for (int i = 0; i < 2; i++) {
			if (i == 1) {
				start = new Point((int) (BOARDSIZE * CHECKERSIZE) + XOFFSET + margin + (CHECKERSIZE / 3), (int) ((CHECKERSIZE * BOARDSIZE) - (CHECKERSIZE * 1.5)) + YOFFSET + margin + 4); // white point
			}
			g.setColor(i == 0 ? Main.BLACKCHECKER : Main.WHITECHECKER);
			g.fillOval(start.x, start.y, CHECKERSIZE - (margin * 2), CHECKERSIZE - (margin * 2));// draw checker
			g.setColor(i == 0 ? Color.WHITE : Color.BLACK);
			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, CHECKERSIZE / 2));
			printCenteredString("" + (i == 0 ? game.blackCheckers : game.whiteCheckers), textx, i == 1 ? (int) ((CHECKERSIZE * BOARDSIZE) - (CHECKERSIZE * 1.5)) + YOFFSET + margin + 4 + ((CHECKERSIZE - (margin * 2)) / 2) : (int)(CHECKERSIZE/2) + YOFFSET + margin + 4 + ((CHECKERSIZE - (margin * 2))/2) , g);
		}

	}
	
	/**
	 * Print centered string
	 * @param s String to print
	 * @param XPos X coord of center
	 * @param YPos Y coord of center
	 * @param g Graphics2D to draw on
	 */
	private void printCenteredString(String s, int XPos, int YPos, Graphics2D g) {
		int startX = (int) g.getFontMetrics().getStringBounds(s, g).getWidth() / 2;
		int startY = (int) g.getFontMetrics().getStringBounds(s, g).getHeight() / 4;
		g.drawString(s, XPos - startX - 1, YPos + startY + 1);
	}

	/**
	 * Draw current animating, checkers and other animations.
	 * 
	 * @param g graphics to be drawn on
	 */
	private void drawAnimating(Graphics2D g) {
		for (Checker c : animating.keySet()) {
			drawChecker(c, animating.get(c), g);
		}
		for (Point p : removalAnimation.keySet()) {
			Integer progress = removalAnimation.get(p);
			g.setColor(Color.RED);
			g.fill(getEllipseFromCenter((p.x * CHECKERSIZE) + XOFFSET + (CHECKERSIZE / 2), (p.y * CHECKERSIZE) + YOFFSET + (CHECKERSIZE / 2), (double) CHECKERSIZE / Main.CHECKER_ANIMATION_TIME * progress,
					(double) CHECKERSIZE / Main.CHECKER_ANIMATION_TIME * progress));
		}
	}

	/**
	 * Draw eclipse fromout a center point
	 * 
	 * @param x center x
	 * @param y center y
	 * @param width
	 * @param height
	 * @return Returns Ellipse2D
	 */
	private Ellipse2D getEllipseFromCenter(double x, double y, double width, double height) {
		double newX = x - width / 2.0;
		double newY = y - height / 2.0;
		Ellipse2D ellipse = new Ellipse2D.Double(newX, newY, width, height);
		return ellipse;
	}

	/**
	 * Draw checkers on the board
	 * 
	 * @param g Graphics to be draw on
	 */
	private void drawCheckers(Graphics2D g) {
		if (game.getState() != GameState.RUNNING)
			return;
		g.setStroke(new BasicStroke(3));

		// draw selected checker and posibilities to move to
		if (game.selectedX != -1 && game.selectedY != -1) {
			g.setColor(Color.RED);
			g.fillRect(XOFFSET + game.selectedX * CHECKERSIZE, YOFFSET + game.selectedY * CHECKERSIZE, CHECKERSIZE, CHECKERSIZE); // draw selected
			if (game.selectedPosibilities != null && !game.selectedPosibilities.isEmpty()) { // draw possible moves
				g.setColor(Color.GREEN);
				for (Point p : game.selectedPosibilities) {
					g.fillRect(XOFFSET + (int) p.getX() * CHECKERSIZE, YOFFSET + (int) p.getY() * CHECKERSIZE, CHECKERSIZE, CHECKERSIZE); // draw selected
				}
			}
		}

		// draw checkers themselves
		for (int x = 0; x < BOARDSIZE; x++) {
			for (int y = 0; y < BOARDSIZE; y++) {
				if (game.checkers[x][y] == null)
					continue;
				Checker c = game.checkers[x][y];
				if (game.possibleSelections.contains(new Point(x, y))) {
					GradientPaint gradient = new GradientPaint((x * CHECKERSIZE) + XOFFSET, (y * CHECKERSIZE) + YOFFSET, new Color(40, 255, 25), (x * CHECKERSIZE) + XOFFSET + 50,
							(y * CHECKERSIZE) + YOFFSET + 50, new Color(10, 150, 10));
					g.setPaint(gradient);
					g.fillOval((x * CHECKERSIZE) + XOFFSET, (y * CHECKERSIZE) + YOFFSET, CHECKERSIZE, CHECKERSIZE);// draw to indicate checker is selectable
				}
				drawChecker(c, new Point2D.Double(x, y), g);
			}
		}
	}

	/**
	 * Draw a single checker
	 * 
	 * @param c Cjhecker
	 * @param point Point where to draw the checker
	 * @param g Graphics to be draw on
	 */
	public void drawChecker(Checker c, Point2D.Double point, Graphics2D g) {
		int margin = CHECKERSIZE < 50 ? 3 : 5; // padding arround checker
		switch (c.getType()) {
		case BLACK:
		case BLACKKING:
			g.setColor(Main.BLACKCHECKER);
			break;
		case WHITE:
		case WHITEKING:
			g.setColor(Color.BLACK);
			g.drawOval((int) (point.x * CHECKERSIZE) + XOFFSET + margin, (int) (point.y * CHECKERSIZE) + YOFFSET + margin, CHECKERSIZE - (margin * 2), CHECKERSIZE - (margin * 2));// draw outline for
																																													// white only
			g.setColor(Main.WHITECHECKER);
			break;
		}
		g.fillOval((int) (point.x * CHECKERSIZE) + XOFFSET + margin, (int) (point.y * CHECKERSIZE) + YOFFSET + margin, CHECKERSIZE - (margin * 2), CHECKERSIZE - (margin * 2));// draw checker
		if (c.isKing()) {
			g.drawImage(c.getType() == CheckerType.BLACKKING ? KINGIMGBLACK : KINGIMGWHITE, (int) (point.x * CHECKERSIZE) + XOFFSET + (CHECKERSIZE / 6),
					(int) (point.y * CHECKERSIZE) + YOFFSET + (CHECKERSIZE / 6), (int) (CHECKERSIZE / 1.5), (int) (CHECKERSIZE / 1.5), null); // draw king on top of checker
		}
	}

	/**
	 * Draw the board itself
	 * 
	 * @param g Graphics to be drawn on
	 */
	private void drawBoard(Graphics2D g) {
		int leadingsize = super.getHeight() < super.getWidth() ? super.getHeight() : super.getWidth(); // check welke dimensie als richtlijn gebruikt moet worden voor grootte
		CHECKERSIZE = (int) ((leadingsize - 8) / (BOARDSIZE + 1.5)); // -8 voor border, +1 boardsize voor stats aan de onderkant
		XOFFSET = (int) (((super.getWidth() - (CHECKERSIZE * BOARDSIZE)) / 2) - CHECKERSIZE / 1.5);
		YOFFSET = ((super.getHeight() - (CHECKERSIZE * BOARDSIZE)) / 2) - CHECKERSIZE / 2;

		g.setStroke(new BasicStroke(4));
		g.setColor(Color.BLACK);
		g.drawRect(XOFFSET, YOFFSET, CHECKERSIZE * BOARDSIZE, CHECKERSIZE * BOARDSIZE); // draw rand
		g.setColor(Main.WHITETILE);
		g.fillRect(XOFFSET, YOFFSET, CHECKERSIZE * BOARDSIZE, CHECKERSIZE * BOARDSIZE); // draw achtergrond / witte tegels
		g.setColor(Main.BLACKTILE);
		for (int y = 0; y < BOARDSIZE; y++) { // draw zwarte tegels
			for (int x = y % 2; x < BOARDSIZE; x += 2) {
				g.fillRect(XOFFSET + x * CHECKERSIZE, YOFFSET + y * CHECKERSIZE, CHECKERSIZE, CHECKERSIZE);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int x, y;
		x = (e.getX() - XOFFSET) / CHECKERSIZE;
		y = (e.getY() - YOFFSET) / CHECKERSIZE;
		if (!checkBounds(new Point(x, y)))
			return;
		Checker selected = game.checkers[x][y];
		//debug move checkers
		if (Main.debug) {
			if (!game.validSelected(new Point(game.selectedX, game.selectedY))) {
				game.selectedX = x;
				game.selectedY = y;
				repaint();
				return;
			}
			Checker temp = game.checkers[game.selectedX][game.selectedY];
			game.checkers[game.selectedX][game.selectedY] = null;
			game.checkers[x][y] = temp;
			game.selectedX = -1;
			game.selectedY = -1;
			repaint();
			return;
		}

		if (selected == null && game.selectedPosibilities.contains(new Point(x, y))) {// check if he is trying to make a move to valid location
			System.out.println("Moved checker: " + game.selectedX + " : " + game.selectedY + " to: " + x + " : " + y);

			Checker temp = game.checkers[game.selectedX][game.selectedY];
			
			move(temp, new Point(game.selectedX, game.selectedY), new Point(x,y), true); //perform the move
			return;
		}

		if (!game.canBeSelected(selected) || !game.possibleSelections.contains(new Point(x, y)))
			return;

		game.setSelected(x, y);
		repaint();
	}
	
	/**
	 * Perform a move
	 * @param c Checker to move
	 * @param from Point from
	 * @param to Point to
	 * @param animate boolean to animate true or false
	 */
	public void move(Checker c, Point from, Point to, boolean animate) {
		game.checkers[to.x][to.y] = null;
		game.checkers[from.x][from.y] = null;

		if (animate)
			animateMovement(c, new Point(from.x, from.y), new Point(to.x,to.y));
		else {
			if (game.hasToStrike)
				handleStrike(c,from,to);
			game.checkers[to.x][to.y] = c;
			//game.changeTurns(to.x, to.y);
		}
	}
	
	/**
	 * Checks and performs strikes if found available
	 * @param c Checker to strike with
	 * @param from Point from where it moved
	 * @param to Point to where it moves
	 * @return point which has been striked (optional)
	 */
	private Point handleStrike(Checker c, Point from, Point to) {
		// get passed checker
		int minx = to.x < from.x ? to.x : from.x;
		int maxx = to.x > from.x ? to.x : from.x;

		int miny = to.x < from.x ? to.y : from.y;
		int maxy = to.x > from.x ? to.y : from.y;

		int tempy = miny;

		int strikeX = 0, strikeY = 0;

		for (int tempx = minx; tempx < maxx; tempx++) {
			// System.out.println("passed: " + tempx + " : " + tempy + " bools: " + (game.checkers[tempx][tempy] != null) + " : " + (game.canBeSelected(game.checkers[tempx][tempy])));
			if (game.checkers[tempx][tempy] != null && !game.canBeSelected(game.checkers[tempx][tempy])) {
				strikeX = tempx;
				strikeY = tempy;
				break;
			}
			if (miny > maxy)
				tempy--;
			else
				tempy++;
		}
		// game.checkers[strikeX][strikeY] = null;
		game.remove(strikeX, strikeY);
		System.out.println("Removed: " + strikeX + " : " + strikeY);
		return new Point(strikeX, strikeY);
	}
	
	/**
	 * Performs the animation of a checker moving
	 * @param c Checker to move
	 * @param from Where to move from
	 * @param to Where to move to
	 */
	private void animateMovement(Checker c, Point from, Point to) {
		AnimationTimer animation = new AnimationTimer() {
			long curtime = System.currentTimeMillis();
			int selectedX = game.selectedX;
			int selectedY = game.selectedY;

			@Override
			public void handle(long now) {
				int frame = 0;
				float xadd = (to.x - from.x) / (float) Main.CHECKER_ANIMATION_TIME;
				float yadd = (to.y - from.y) / (float) Main.CHECKER_ANIMATION_TIME;
				Point strike = null;

				// removes selection
				game.selectedPosibilities.clear();
				game.possibleSelections.clear();
				game.selectedX = -1;
				game.selectedY = -1;

				// handle strike animation and removal
				if (game.hasToStrike) {
					strike = handleStrike(c,from,to);
				}

				while (frame <= Main.CHECKER_ANIMATION_TIME) {
					if (System.currentTimeMillis() - curtime < 2)
						continue;
					curtime = System.currentTimeMillis();
					if (strike != null)
						removalAnimation.put(strike, frame);
					animating.put(c, new Point2D.Double(selectedX + (xadd * frame), selectedY + (yadd * frame)));
					repaint();
					frame++;
				}
				animating.remove(c);
				removalAnimation.remove(strike);
				game.checkers[to.x][to.y] = c;

				game.changeTurns(to.x, to.y);
				stop();
			}
		};
		animation.start();
	}


	/**
	 * Check if given point is within bounds of the board
	 * 
	 * @param p Point to check
	 * @return boolean
	 */
	public boolean checkBounds(Point p) {
		if (p.getX() < 0 || p.getX() > BOARDSIZE - 1) // ceck x out of bounds of board
			return false;
		if (p.getY() < 0 || p.getY() > BOARDSIZE - 1) // check y out of bounds of board
			return false;
		return true;
	}
	
	/**
	 * Optionally for AI implementation
	 * @deprecated
	 * @return
	 */
	public int getScore() {
		//System.out.println("Returning for: " + game.turn);
		int base = game.turn == Player.BLACK ? game.blackCheckers : game.whiteCheckers;
		
		for (int x = 0; x < BOARDSIZE; x++) {
			for (int y = 0; y < BOARDSIZE; y++) {
				if (game.checkers[x][y] == null)
					continue;
				Checker c = game.checkers[x][y];
				if (((c.isBlack() && game.turn == Player.WHITE) || (c.isWhite() && game.turn == Player.BLACK))) 
					base-=2;
				if (c.isKing())
					base+=5;
				base += c.isKing() ? game.calculatePosibilities(new Point(x,y), 1).size() / 4 : game.calculatePosibilities(new Point(x,y), 1).size();
				game.turn = game.turn == Player.BLACK ? Player.WHITE : Player.BLACK;
				if (!game.checkSkips(new Point(x,y),1,true,c.isKing()).isEmpty())
					base-=10;
				game.turn = game.turn == Player.BLACK ? Player.WHITE : Player.BLACK;
			}
		}
		if (game.hasToStrike)
			base+=20;
	
		return base;
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_DELETE:
			Main.resetGame();
			break;
		case KeyEvent.VK_END:
			Main.showFPS = !Main.showFPS;
			break;
		case KeyEvent.VK_PAGE_DOWN:
			game.changeTurns(-1, -1);
			break;
		case KeyEvent.VK_INSERT:
			Main.saveGame();
			break;
		case KeyEvent.VK_HOME:
			Main.loadGame();
			break;
		case KeyEvent.VK_PAGE_UP:
			Main.debug = !Main.debug;
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
