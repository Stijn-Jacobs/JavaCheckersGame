package me.stijn.checkers.objects;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import me.stijn.checkers.Board;

public class MoveAnimation extends Thread {
	
	private static final int ANIMATIONTIME = 100;
	
	private Point from, to;
	private Checker c;
	private Board b;
	
	public MoveAnimation(Point from, Point to, Checker c, Board b) {
		this.from = from;
		this.to = to;
		this.c = c;
		this.b = b;
	}
	
	@Override
	public void run() {
		int frame = 0;
		System.out.println("Locationchange: " + Math.abs(from.x - to.x) + " : " + Math.abs(from.y - to.y));
		float xadd = (to.x - from.x) / (float)ANIMATIONTIME;
		float yadd = (to.y - from.y) / (float)ANIMATIONTIME;
		while (frame <= 100) {
			System.out.println("frmae " + b.animating);
			//b.drawChecker(c,new Point2D.Double(from.x + (xadd * frame), from.y + (yadd * frame)),(Graphics2D)b.getGraphics());
			b.animating.put(c, new Point2D.Double(from.x + (xadd * frame), from.y + (yadd * frame)));
			b.repaint();
			frame++;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		b.animating.remove(c);
		b.game.checkers[to.x][to.y] = c;
		b.repaint();
	}

	
	
	

}
