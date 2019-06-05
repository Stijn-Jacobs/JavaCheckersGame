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
import me.stijn.checkers.objects.Checker;
import me.stijn.checkers.objects.Checker.CheckerType;

public class Board extends JPanel implements MouseListener,KeyListener{

	public static final int BOARDSIZE = 10, CHECKER_ANIMATION_TIME = 100;
	public static final Color BLACKTILE = Color.GRAY, WHITETILE = Color.WHITE;
	public static final Color BLACKCHECKER = Color.BLACK, WHITECHECKER = Color.LIGHT_GRAY;
	public static BufferedImage KINGIMGWHITE,KINGIMGBLACK;
	
	public HashMap<Checker, Point2D.Double> animating = new HashMap<>();
	public HashMap<Point, Integer> removalAnimation = new HashMap<>();
	
	public Game game;

	public int CHECKERSIZE, XOFFSET, YOFFSET;
	
	/**
	 * Board object where the checker game is playing on
	 */
	public Board() {
		game = new Game(this);
		addMouseListener(this);
		
		try { //load king image
			KINGIMGWHITE = ImageIO.read(getClass().getResource("/me/stijn/checkers/resources/king.png"));
			KINGIMGBLACK = new BufferedImage(KINGIMGWHITE.getWidth(), KINGIMGWHITE.getHeight(), IndexColorModel.TRANSLUCENT);
			for (int y = 0; y < KINGIMGBLACK.getHeight(); y++) { //invert colors for black king
				for (int x = 0; x < KINGIMGBLACK.getWidth(); x++) {
					int p = KINGIMGWHITE.getRGB(x, y);
					int a = (p>>24)&0xff;
			        int r = (p>>16)&0xff;
			        int g = (p>>8)&0xff;
			        int b = p&0xff;
			        if (r < 255 && g < 255  && b < 255)//check if black
			        	p = (255 << 24) | (255 << 16) | (255 << 8) | 255;
			        else
			        	p = 0;
					KINGIMGBLACK.setRGB(x, y, p);
				}
			}
		} catch (IllegalArgumentException | IOException e) { //als king image niet kan vinden
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
		drawCheckers(g2d);
		drawAnimating(g2d);

		System.out.println("Frame took: " + (System.currentTimeMillis() - time) + " ms to render");
		if ((System.currentTimeMillis() - time) > 0 && Main.showFPS) {
			g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN,30));
			g2d.drawString("FPS: " + 1000 / (System.currentTimeMillis() - time), 1, 50);
		}
		if (Main.debug)
			g2d.drawString("DEBUG ENABLED:",1,100);
	}
	
	/**
	 * Draw current animating, checkers and other animations.
	 * @param g graphics to be drawn on
	 */
	private void drawAnimating(Graphics2D g) {
		for (Checker c : animating.keySet()) {
			drawChecker(c, animating.get(c), g);
		}
		for (Point p : removalAnimation.keySet()) {
			Integer progress = removalAnimation.get(p);
			g.setColor(Color.RED);
			g.fill(getEllipseFromCenter((p.x * CHECKERSIZE) + XOFFSET + (CHECKERSIZE / 2), (p.y * CHECKERSIZE) + YOFFSET + (CHECKERSIZE / 2), (double)CHECKERSIZE / CHECKER_ANIMATION_TIME * progress, (double)CHECKERSIZE / CHECKER_ANIMATION_TIME * progress));
		}
	}
	
	/**
	 * Draw eclipse fromout a center point
	 * @param x center x
	 * @param y center y
	 * @param width 
	 * @param height
	 * @return Returns Ellipse2D
	 */
	private Ellipse2D getEllipseFromCenter(double x, double y, double width, double height){
	    double newX = x - width / 2.0;
	    double newY = y - height / 2.0;
	    Ellipse2D ellipse = new Ellipse2D.Double(newX, newY, width, height);
	    return ellipse;
	}

	/**
	 * Draw checkers on the board
	 * @param g Graphics to be draw on
	 */
	private void drawCheckers(Graphics2D g) {
		if (game.getState() != GameState.RUNNING)
			return;
		g.setStroke(new BasicStroke(3));
		
		//draw selected checker and posibilities to move to
		if (game.selectedX != -1 && game.selectedY != -1) { 
			g.setColor(Color.RED);
			g.fillRect(XOFFSET + game.selectedX * CHECKERSIZE, YOFFSET + game.selectedY * CHECKERSIZE, CHECKERSIZE, CHECKERSIZE); //draw selected
			if (game.selectedPosibilities != null && !game.selectedPosibilities.isEmpty()) { //draw possible moves
				g.setColor(Color.GREEN);
				for (Point p : game.selectedPosibilities) {
					g.fillRect(XOFFSET + (int) p.getX() * CHECKERSIZE, YOFFSET + (int) p.getY() * CHECKERSIZE, CHECKERSIZE, CHECKERSIZE); //draw selected
				}
			}
		}
		
		//draw checkers themselves
		for (int x = 0; x < BOARDSIZE; x++) {
			for (int y = 0; y < BOARDSIZE; y++) {
				if (game.checkers[x][y] == null)
					continue;
				Checker c = game.checkers[x][y];
				if (game.possibleSelections.contains(new Point(x,y))) {
					GradientPaint gradient = new GradientPaint((x * CHECKERSIZE) + XOFFSET, (y * CHECKERSIZE) + YOFFSET, new Color(40,255,25), (x * CHECKERSIZE) + XOFFSET + 50, (y * CHECKERSIZE) + YOFFSET + 50, new Color(10, 150, 10));
					g.setPaint(gradient);
					g.fillOval((x * CHECKERSIZE) + XOFFSET, (y * CHECKERSIZE) + YOFFSET, CHECKERSIZE, CHECKERSIZE);//draw to indicate checker is selectable
				}
				drawChecker(c,new Point2D.Double(x, y),g);
			}
		}
	}
	
	/**
	 * Draw a single checker 
	 * @param c Cjhecker
	 * @param point Point where to draw the checker
	 * @param g Graphics to be draw on
	 */
	public void drawChecker(Checker c, Point2D.Double point, Graphics2D g) {
		int margin = CHECKERSIZE < 50 ? 3 : 5; // padding arround checker
		switch (c.getType()) {
		case BLACK:
		case BLACKKING:
			g.setColor(BLACKCHECKER);
			break;
		case WHITE:
		case WHITEKING:
			g.setColor(Color.BLACK);
			g.drawOval((int)(point.x * CHECKERSIZE) + XOFFSET + margin, (int)(point.y * CHECKERSIZE) + YOFFSET + margin, CHECKERSIZE - (margin * 2), CHECKERSIZE - (margin * 2));// draw outline for white only
			g.setColor(WHITECHECKER);
			break;
		}
		g.fillOval((int)(point.x * CHECKERSIZE) + XOFFSET + margin, (int)(point.y * CHECKERSIZE) + YOFFSET + margin, CHECKERSIZE - (margin * 2), CHECKERSIZE - (margin * 2));//draw checker
		if (c.isKing()) {
			g.drawImage(c.getType() == CheckerType.BLACKKING ? KINGIMGBLACK : KINGIMGWHITE, (int)(point.x * CHECKERSIZE) + XOFFSET + (CHECKERSIZE / 6), (int)(point.y * CHECKERSIZE) + YOFFSET + (CHECKERSIZE / 6), (int)(CHECKERSIZE/1.5), (int)(CHECKERSIZE/1.5), null); //draw king on top of checker
		}
	}

	/**
	 * Draw the board itself
	 * @param g Graphics to be drawn on
	 */
	private void drawBoard(Graphics2D g) {
		final int LEADINGSIZE = super.getHeight() < super.getWidth() ? super.getHeight() : super.getWidth(); // check welke dimensie als richtlijn gebruikt moet worden voor grootte
		CHECKERSIZE = (LEADINGSIZE - 8) / BOARDSIZE; // -8 voor border
		XOFFSET = (super.getWidth() - (CHECKERSIZE * BOARDSIZE)) / 2;
		YOFFSET = (super.getHeight() - (CHECKERSIZE * BOARDSIZE)) / 2;

		g.setStroke(new BasicStroke(4));
		g.setColor(Color.BLACK);
		g.drawRect(XOFFSET, YOFFSET, CHECKERSIZE * BOARDSIZE, CHECKERSIZE * BOARDSIZE); // draw rand
		g.setColor(WHITETILE);
		g.fillRect(XOFFSET, YOFFSET, CHECKERSIZE * BOARDSIZE, CHECKERSIZE * BOARDSIZE); // draw achtergrond / witte tegels
		g.setColor(BLACKTILE);
		for (int y = 0; y < BOARDSIZE; y++) { // draw zwarte tegels
			for (int x = y % 2; x < BOARDSIZE; x += 2) {
				g.fillRect(XOFFSET + x * CHECKERSIZE, YOFFSET + y * CHECKERSIZE, CHECKERSIZE, CHECKERSIZE);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int x,y;
		x = (e.getX() - XOFFSET) / CHECKERSIZE;
		y = (e.getY() - YOFFSET) / CHECKERSIZE;
		if (!checkBounds(new Point(x,y)))
			return;
		Checker selected = game.checkers[x][y];
		if (Main.debug ) {
			if (!game.validSelected(new Point(game.selectedX,game.selectedY))) {
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
		
		if (selected == null && game.selectedPosibilities.contains(new Point(x,y))) {//check if he is trying to make a move to valid location
			System.out.println("Moved checker: " + game.selectedX + " : " + game.selectedY + " to: " + x + " : " + y);
			
			Checker temp = game.checkers[game.selectedX][game.selectedY];
			game.checkers[x][y] = null;
			game.checkers[game.selectedX][game.selectedY] = null;
			
			AnimationTimer animation = new AnimationTimer() {
				long curtime = System.currentTimeMillis();
				int selectedX = game.selectedX;
				int selectedY = game.selectedY;
				@Override
				public void handle(long now) {
					int frame = 0;
					float xadd = (x - selectedX) / (float)CHECKER_ANIMATION_TIME;
					float yadd = (y - selectedY) / (float)CHECKER_ANIMATION_TIME;
					Point strike = null;
					
					//removes selection 
					game.selectedPosibilities.clear();
					game.possibleSelections.clear();
					game.selectedX = -1;
					game.selectedY = -1;
					
					//handle strike animation and removal
					if (game.hasToStrike) {
						//get passed checker
						int minx = x < selectedX ? x : selectedX;
						int maxx = x > selectedX ? x : selectedX;
						
						int miny = x < selectedX ? y : selectedY;
						int maxy = x > selectedX ? y : selectedY;
						
						System.out.println("Start: " + minx + " : " + miny + " | " + maxx + " : " + maxy);
						
						int tempy = miny;

						int strikeX = 0, strikeY = 0;
						
						for (int tempx = minx; tempx < maxx; tempx++) {
							//System.out.println("passed: " + tempx + " : " + tempy + " bools: " + (game.checkers[tempx][tempy] != null)  + " : " + (game.canBeSelected(game.checkers[tempx][tempy])));
							if (game.checkers[tempx][tempy] != null && !game.canBeSelected(game.checkers[tempx][tempy])) {
								strikeX = tempx;
								strikeY = tempy;
								break;
							}
							if (miny > maxy)
								tempy--;
							else tempy++;
						}
						game.checkers[strikeX][strikeY] = null;
						strike = new Point(strikeX,strikeY);
						System.out.println("Removed: " + strikeX + " : " + strikeY);
					}
					
					while (frame <= CHECKER_ANIMATION_TIME) {
						if (System.currentTimeMillis() - curtime < 2)
							continue;
						curtime = System.currentTimeMillis();
						if (strike != null)
							removalAnimation.put(strike, frame);
						animating.put(temp, new Point2D.Double(selectedX + (xadd * frame), selectedY + (yadd * frame)));
						repaint();
						frame++;
					}
					animating.remove(temp);
					removalAnimation.remove(strike);
					game.checkers[x][y] = temp;
					
					game.changeTurns(x,y); 
					stop();
				}
			};
			
			animation.start();
			return;
		}
		
		if (!game.canBeSelected(selected) || !game.possibleSelections.contains(new Point(x,y)))
			return;
		
		game.setSelected(x, y);
		repaint();
	}
	
	/**
	 * Check if given point is within bounds of the board
	 * @param p Point to check
	 * @return boolean
	 */
	public boolean checkBounds(Point p) {
		if (p.getX() < 0 || p.getX() > BOARDSIZE - 1) //ceck x out of bounds of board
			return false;
		if (p.getY() < 0 || p.getY() > BOARDSIZE - 1) //check y out of bounds of board
			return false;
		return true;
	}

	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_DELETE:
			Main.resetGame();
			break;
		case KeyEvent.VK_END:
			Main.showFPS = !Main.showFPS;
			break;
		case KeyEvent.VK_PAGE_DOWN:
			game.changeTurns(-1,-1);
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
	public void keyReleased(KeyEvent e) {}


}
