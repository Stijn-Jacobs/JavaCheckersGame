package me.stijn.checkers;

import java.awt.Dimension;

import javax.swing.JFrame;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	
	public static boolean showFPS = false;

	private static Board board;
	private static JFrame panel;

	public static void main(String args[]) {
		launch();
	}

	public void start(Stage stage) throws Exception {
		panel = new JFrame();
		panel.setTitle("Java eindopdracht Stijn Jacobs - Dammen");
		panel.setSize(new Dimension(1000, 800));
		panel.setMinimumSize(new Dimension(300,300));
		board = new Board();
		panel.addKeyListener(board);
		panel.add(board);
		panel.setVisible(true);
	}
	
	public static void resetGame() {
		System.out.println("Game reset");
		panel.remove(board);
		panel.removeKeyListener(board);
		board = new Board();
		panel.addKeyListener(board);
		panel.add(board);
		panel.setVisible(true);
	}

}