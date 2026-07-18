package com.game.gui;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main {
    public static class StageManager {
        private static StageManager instance;
        private Stage currentStage;

        private StageManager() {}

        public static StageManager getInstance() {
            if (instance == null) {
                instance = new StageManager();
            }
            return instance;
        }

        public void setStage(Stage stage) {
            this.currentStage = stage;
            configureStage(currentStage);
        }

        public Stage getStage() {
            if (currentStage == null) {
                currentStage = new Stage();
                configureStage(currentStage);
            }
            return currentStage;
        }

        public static void configureStage(Stage stage) {
            stage.setFullScreen(true);  // Maximize the window
            stage.setMaximized(true);
        }

        public static void showStage(Stage stage, Scene scene) {
            stage.setScene(scene);
            configureStage(stage);
            stage.show();
        }
    }






    public static void main(String[] args) {
        javafx.application.Application.launch(LoginWindow.class, args);
    }
}