package me.stijn.checkers;

import java.awt.Dimension;

import javax.swing.JFrame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	private Board board;
	private JFrame panel;

	public static void main(String args[]) {
		launch();
	}

	public void start(Stage stage) throws Exception {
		panel = new JFrame();
		panel.setTitle("Java eindopdracht Stijn Jacobs - Dammen");
		panel.setSize(new Dimension(1000, 800));
		board = new Board();
		panel.add(board);
		panel.setVisible(true);
	}

}