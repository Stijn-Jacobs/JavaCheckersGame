package me.stijn.checkers;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class CheckersGame {
	
	private GameState state;
	private Board board;
	private JFrame panel;
	
	public CheckersGame() {
		panel = new JFrame();
		panel.setPreferredSize(new Dimension(100,800));
		panel.add(new Board());
		panel.setResizable(false);
		panel.setVisible(true);		
	}
	
	
}
