package com.game.gui;

import com.game.auth.AuthManager;
import com.game.auth.User;
import com.game.auth.session.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Settings extends Application {

    // Define the blur effect to apply on the parent stage.
    private static final GaussianBlur blurEffect = new GaussianBlur(10);

    @Override
    public void start(Stage settingsStage) throws Exception {
        // Get the current user info from the session.
        String currentUsername = SessionManager.getInstance().getCurrentUsername();
        User currentUser = AuthManager.getInstance().getDatabase().getUser(currentUsername);

        // If no user is found, create a placeholder user.
        if (currentUser == null) {
            System.out.println("No user found for username: " + currentUsername + ". Using placeholder user.");
            currentUser = new User();
            currentUser.setUsername("user");
            currentUser.setEmail("user@gmail.com");
        }

        // Locate the parent stage (the one that launched settings).
        Stage parentStage = (Stage) Stage.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);

        // Apply a blur effect to the parent stage.
        if (parentStage != null) {
            parentStage.getScene().getRoot().setEffect(blurEffect);
        }

        // Load the ProfileSettings FXML.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile_settings_assets/ProfileSettings.fxml"));
        Parent root = loader.load();

        // Get the controller and initialize the form fields.
        ProfileSettingsController controller = loader.getController();
        controller.initializeData(currentUser);

        // Set up the settings stage.
        Scene scene = new Scene(root);
        settingsStage.setScene(scene);
        settingsStage.setTitle("Profile Settings");
        settingsStage.initModality(Modality.WINDOW_MODAL);
        if (parentStage != null) {
            settingsStage.initOwner(parentStage);
        }
        settingsStage.setResizable(false);

        // When the settings window is hidden, remove the blur from the parent.
        settingsStage.setOnHidden(e -> {
            if (parentStage != null) {
                parentStage.getScene().getRoot().setEffect(null);
            }
        });

        settingsStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}