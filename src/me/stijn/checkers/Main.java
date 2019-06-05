package me.stijn.checkers;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFrame;

import javafx.application.Application;
import javafx.stage.Stage;
import me.stijn.checkers.objects.Checker;

public class Main extends Application {
	
	public static boolean showFPS = false;
	public static boolean debug = false;

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
		panel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
	
	/**
	 * Complete reset the game
	 * @param g Game to be set
	 */
	public static void resetGame() {
		System.out.println("Game reset");
		panel.remove(board);
		panel.removeKeyListener(board);
		board = new Board();
		panel.addKeyListener(board);
		panel.add(board);
		panel.setVisible(true);
	}
	
	/**
	 * Save the game
	 */
	public static void saveGame() {
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("/save.checkers"));
			outputStream.writeObject(board.game);
			outputStream.close();
		} catch (IOException e) {
			System.out.println("Error occured when trying to save the game");
			e.printStackTrace();
		}
	}
	
	/**
	 * Try to load saved game
	 */
	public static void loadGame() {
		File save = new File("/save.checkers");
		if (!save.exists()) {
			System.out.println("Save does not exist");
			resetGame();
			return;
		}
		try {
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(save));
			Game g = (Game)inputStream.readObject();
			inputStream.close();
			g.b = board;
			board.game = g;
			g.calcPossible();
			board.repaint();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Error occured while loading save");
			e.printStackTrace();
		}

	}



}