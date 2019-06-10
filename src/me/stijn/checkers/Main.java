package me.stijn.checkers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import me.stijn.checkers.objects.Checker;
import me.stijn.checkers.objects.FinishScreen;

public class Main extends Application {
	
	public static boolean showFPS = false;
	public static boolean debug = false;
	public static boolean AI = false;
	
	/**
	 * Settings
	 */
	public static final int CHECKERS = 12; //default 20
	public static final int BOARDSIZE = 8, CHECKER_ANIMATION_TIME = 100; //default 10 and 100
	public static final Color BLACKTILE = Color.GRAY, WHITETILE = Color.WHITE; //default GRAY and WHITE
	public static final Color BLACKCHECKER = Color.BLACK, WHITECHECKER = Color.LIGHT_GRAY; //default BLACK and LIGHT_GRAY
	
	//fields
	public static Board board;
	public static JFrame frame;
	private static FinishScreen screen;
	
	public static void main(String args[]) throws Exception {
		launch();
	}

	@Override
	public void start(Stage stage) throws Exception {
		frame = new JFrame();
		frame.setTitle("Java eindopdracht Stijn Jacobs - Dammen");
		frame.setSize(new Dimension(1000, 800));
		frame.setMinimumSize(new Dimension(300,300));
		frame.setLocationRelativeTo(null);
		board = new Board(BOARDSIZE);
		frame.add(board);
		
		frame.addKeyListener(board);//key listener 
		frame.setVisible(true);	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * Complete reset the game
	 * @param g Game to be set
	 */
	public static void resetGame() {
		if (screen != null)
			frame.remove(screen);
		System.out.println("Game reset");
		frame.removeKeyListener(board);
		frame.remove(board);
		board = new Board(BOARDSIZE);
		frame.add(board);
		frame.addKeyListener(board);
		frame.setVisible(true);
	}
	
	public static void displayFinishScreen(FinishScreen scrn) {
		frame.remove(board);
		frame.add(scrn);
		frame.setVisible(true);
		screen = scrn;
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
			g.calcPossibleSelectable();
			board.repaint();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Error occured while loading save");
			e.printStackTrace();
		}

	}




}