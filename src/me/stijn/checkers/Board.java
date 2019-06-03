package me.stijn.checkers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import javafx.scene.paint.Paint;
import me.stijn.checkers.objects.Checker;
import me.stijn.checkers.objects.Checker.CheckerType;

public class Board extends JPanel implements MouseListener{

	public static final int BOARDSIZE = 10;
	public static final Color BLACKTILE = Color.GRAY, WHITETILE = Color.WHITE;
	public static final Color BLACKCHECKER = Color.BLACK, WHITECHECKER = Color.LIGHT_GRAY;
	private Game game;

	private int CHECKERSIZE, XOFFSET, YOFFSET;

	public Board() {
		game = new Game(this);
		addMouseListener(this);

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawBoard(g2d);
		drawCheckers(g2d);

		//System.out.println("repaint");
	}

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
		
			
		
		
		for (int x = 0; x < BOARDSIZE; x++) {
			for (int y = 0; y < BOARDSIZE; y++) {
				if (game.checkers[x][y] == null)
					continue;
				Checker c = game.checkers[x][y];
				if (game.possibleSelections.contains(new Point(x,y))) {
					//System.out.println("Possible: " + x + " : " + y);
					GradientPaint gradient = new GradientPaint((x * CHECKERSIZE) + XOFFSET, (y * CHECKERSIZE) + YOFFSET, Color.BLACK, (x * CHECKERSIZE) + XOFFSET + 50, (y * CHECKERSIZE) + YOFFSET + 50, Color.WHITE);
					g.setPaint(gradient);
					g.fillOval((x * CHECKERSIZE) + XOFFSET, (y * CHECKERSIZE) + YOFFSET, CHECKERSIZE, CHECKERSIZE);//draw checker
				}
				int margin = CHECKERSIZE < 50 ? 3 : 5; // padding arround checker
				switch (c.getType()) {
				case BLACK:
					g.setColor(BLACKCHECKER);
					break;
				case WHITE:
					g.setColor(Color.BLACK);
					g.drawOval((x * CHECKERSIZE) + XOFFSET + margin, (y * CHECKERSIZE) + YOFFSET + margin, CHECKERSIZE - (margin * 2), CHECKERSIZE - (margin * 2));// draw outline for white only
					g.setColor(WHITECHECKER);
					break;
				case BLACKKING:
					break;
				case WHITEKING:
					break;
				}
				g.fillOval((x * CHECKERSIZE) + XOFFSET + margin, (y * CHECKERSIZE) + YOFFSET + margin, CHECKERSIZE - (margin * 2), CHECKERSIZE - (margin * 2));//draw checker
			}
		}
		


	}

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
	public void mouseClicked(MouseEvent e) {
		int x,y;
		x = (e.getX() - XOFFSET) / CHECKERSIZE;
		y = (e.getY() - YOFFSET) / CHECKERSIZE;
		if (!checkBounds(new Point(x,y)))
			return;
		Checker selected = game.checkers[x][y];
		if (selected == null && game.selectedPosibilities.contains(new Point(x,y))) {//check if he is trying to make a move to valid location
			System.out.println("moved");
			//TODO ANIMATE
			game.checkers[x][y] = game.checkers[game.selectedX][game.selectedY];
			game.checkers[game.selectedX][game.selectedY] = null;
			
			
			
			
			
			game.changeTurns();
			return;
		}
		
		if (!game.canBeSelected(selected))
			return;
		
		game.setSelected(x, y);
		repaint();
	}
	
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
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}


}
