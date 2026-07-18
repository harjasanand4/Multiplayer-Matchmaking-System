package com.game.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public class TicTacToe1 extends Application {

    private Stage primaryStage;
    private Stage gameStage;

    /**
     * Constructor for TicTacToe1.
     * Initializes the stages for the game.
     */
    public TicTacToe1() {
        this.primaryStage = new Stage();  // Primary stage initialized here
        this.gameStage = new Stage();
    }

    /**
     * Loads the FXML file and displays the TicTacToe game UI.
     */
    public void showGame() {
        try {
            // Define the FXML file path.
            String fxmlPath = "/tic_tac_toe_assets/TicTacToeScreen.fxml";
            System.out.println("Attempting to load FXML from: " + fxmlPath);

            // Debug: Attempt to get the resource URL.
            java.net.URL resourceURL = getClass().getResource(fxmlPath);
            System.out.println("Resource URL: " + resourceURL);
            if (resourceURL == null) {
                System.out.println("FXML file not found at: " + fxmlPath);
                throw new IOException("FXML file not found at " + fxmlPath);
            }

            // Create the FXMLLoader with the validated URL.
            FXMLLoader loader = new FXMLLoader(resourceURL);

            // Load the FXML file and create the scene.
            System.out.println("Loading FXML file...");
            Scene scene = new Scene(loader.load(), 1440, 1024);
            System.out.println("FXML file loaded successfully.");

            // Set the scene on the game stage.
            gameStage.setScene(scene);
            gameStage.setTitle("Tic Tac Toe");
            gameStage.setResizable(false);

            // Show the game stage and hide the primary stage.
            System.out.println("Showing game stage and hiding primary stage...");
            gameStage.show();
            primaryStage.hide();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Load Game");
            alert.setContentText("Could not load TicTacToeScreen.fxml. Check file path: " + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Unexpected error during game setup: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Unexpected Error");
            alert.setContentText("An unexpected error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * The start method required by JavaFX Application.
     * @param stage The primary stage provided by JavaFX.
     */
    @Override
    public void start(Stage stage) {
        System.out.println("Starting TicTacToe1 application..."); // Debug: Log application start
        this.primaryStage = stage;  // Use the stage provided by JavaFX
        this.gameStage = new Stage();
        showGame();  // Launch the game UI
    }

    /**
     * Main method to launch the JavaFX application.
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        System.out.println("Launching JavaFX application..."); // Debug: Log launch
        launch(args);  // Launch the JavaFX application
    }
}