package me.stijn.checkers.objects;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import me.stijn.checkers.Game;
import me.stijn.checkers.Main;
import me.stijn.checkers.Game.WinReason;

public class FinishScreen extends JPanel implements ActionListener {
	
	WinReason win;
	
	public FinishScreen(WinReason win) {
		this.win = win;
		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(Box.createVerticalGlue());
		String msg = "Player: " + win + " has won the game";
		if (win == WinReason.DRAW)
			msg = "Nobody won the game";
		JLabel lbl = new JLabel();
		lbl.setText(msg);
		lbl.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,50));
		lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(lbl);
		
		add(Box.createRigidArea(new Dimension(50,50)));
		
		JButton btn = new JButton();
		btn.setText("Restart the game");
		btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		btn.setPreferredSize(new Dimension(350,50));
		add(btn);
		btn.addActionListener(this);
		btn.setFocusable(false);
		add(Box.createVerticalGlue());
	}

//	@Override
//	protected void paintComponent(Graphics g) {
//		System.out.println("Drew finish screen");
//		super.paintComponent(g);
//		Graphics2D g2d = (Graphics2D)g;
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		String msg = "Player: " + win + " has won the game";
//		if (win == WinReason.DRAW)
//			msg = "Nobody won the game";
//		drawCenteredString(g, msg, new Rectangle(this.getSize()), new Font(Font.SANS_SERIF,Font.PLAIN,50));
//		
//	}
//	public void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
//	    FontMetrics metrics = g.getFontMetrics(font);
//	    int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
//	    int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
//	    g.setFont(font);
//	    g.drawString(text, x, y);
//	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Main.resetGame();
	}
	
	
	

}
