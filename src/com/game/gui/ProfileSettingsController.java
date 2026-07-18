package com.game.gui;

import com.game.auth.AuthManager;
import com.game.auth.User;
import com.game.auth.session.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProfileSettingsController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private Button saveChangesButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button changePasswordBtn; // Button for Change Password action

    // ImageViews for the platform themes.
    @FXML
    private ImageView moonImageView;
    @FXML
    private ImageView earthImageView;
    @FXML
    private ImageView marsImageView;
    @FXML
    private ImageView venusImageView;

    // Variable to keep track of the selected theme.
    private String selectedTheme = "Moon";  // default theme

    /**
     * Initializes the form with the current user's data.
     * Disables editing for the username, sets up the email field,
     * and attaches click handlers for theme selection and the change password button.
     */
    public void initializeData(User currentUser) {
        usernameField.setText(currentUser.getUsername());
        usernameField.setEditable(false);
        emailField.setText(currentUser.getEmail());

        // If our User model implements getTheme(), we can use this:
        // if (currentUser.getTheme() != null) {
        //     selectedTheme = currentUser.getTheme();
        // } else {
        //     selectedTheme = "Moon";
        // }
        // For now, we'll default to "Moon"
        selectedTheme = "Moon";

        selectTheme(selectedTheme);

        // Attach click listeners to theme ImageViews.
        moonImageView.setOnMouseClicked(e -> selectTheme("Moon"));
        earthImageView.setOnMouseClicked(e -> selectTheme("Earth"));
        marsImageView.setOnMouseClicked(e -> selectTheme("Mars"));
        venusImageView.setOnMouseClicked(e -> selectTheme("Venus"));

        // Attach listener for the Change Password button.
        changePasswordBtn.setOnAction(e -> handleChangePassword());
    }

    /**
     * Highlights the selected theme and removes highlight from others.
     */
    private void selectTheme(String theme) {
        // Clear any existing effects.
        moonImageView.setEffect(null);
        earthImageView.setEffect(null);
        marsImageView.setEffect(null);
        venusImageView.setEffect(null);

        selectedTheme = theme;  // Update the selected theme

        // Create a drop shadow effect for visual indication.
        DropShadow highlight = new DropShadow();
        highlight.setRadius(10);
        highlight.setSpread(0.7);
        highlight.setColor(Color.DODGERBLUE);

        if ("Moon".equals(theme)) {
            moonImageView.setEffect(highlight);
        } else if ("Earth".equals(theme)) {
            earthImageView.setEffect(highlight);
        } else if ("Mars".equals(theme)) {
            marsImageView.setEffect(highlight);
        } else if ("Venus".equals(theme)) {
            venusImageView.setEffect(highlight);
        }
    }

    /**
     * Handles the Save Changes action.
     * Updates the email (and, optionally, the selected theme) and saves the changes.
     */
    @FXML
    private void handleSaveChanges() {
        // Retrieve the current user by username.
        String currentUsername = usernameField.getText();
        User currentUser = AuthManager.getInstance().getDatabase().getUser(currentUsername);

        // Update the email address.
        String updatedEmail = emailField.getText();
        currentUser.setEmail(updatedEmail);

        // currentUser.setTheme(selectedTheme);

        AuthManager.getInstance().getDatabase().updateUser(currentUser);
        AuthManager.getInstance().updateUsersFile();

        // Close the settings window.
        Stage stage = (Stage) saveChangesButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles Cancel/Exit actions.
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the Change Password action by showing a popup (without using FXML).
     * Placeholder code is provided for password validation and update.
     */
    private void handleChangePassword() {
        // Create a new stage for the Change Password dialog.
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(changePasswordBtn.getScene().getWindow());
        dialog.setTitle("Change Password");

        // Build the layout.
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        // Title label.
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Change Password");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Password fields.
        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current Password");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");

        // Button bar.
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        Button cancelBtn = new Button("Cancel");
        Button saveBtn = new Button("Save Changes");
        buttonBar.getChildren().addAll(cancelBtn, saveBtn);

        // Assemble the layout.
        vbox.getChildren().addAll(titleLabel, currentPasswordField, newPasswordField, confirmPasswordField, buttonBar);

        Scene dialogScene = new Scene(vbox, 400, 280);
        dialog.setScene(dialogScene);

        // Cancel action.
        cancelBtn.setOnAction(e -> dialog.close());

        // Save action: validate input and update password.
        saveBtn.setOnAction(e -> {
            String currentPass = currentPasswordField.getText();
            String newPass = newPasswordField.getText();
            String confirmPass = confirmPasswordField.getText();

            // Validate non-empty current password.
            if (currentPass == null || currentPass.trim().isEmpty()) {
                showAlert("Error", "Current password cannot be empty.");
                return;
            }

            // Validate new password match.
            if (!newPass.equals(confirmPass)) {
                showAlert("Error", "New password and confirm password do not match.");
                return;
            }

            // Check new password length (example validation).
            if (newPass.length() < 4) {
                showAlert("Error", "New password must be at least 4 characters.");
                return;
            }

            // --------------------------
            // PLACEHOLDER: Validate current password.
            // Replace with actual authentication logic.
            boolean valid = false;
            try {
                // For example, the Auth team might implement:
                // valid = AuthManager.getInstance().validatePassword(usernameField.getText(), currentPass);
                valid = true; // Temporary placeholder: assume it's valid.
            } catch (Exception ex) {
                valid = false;
            }
            if (!valid) {
                showAlert("Error", "Current password is incorrect.");
                return;
            }
            // --------------------------

            // --------------------------
            // PLACEHOLDER: Update the user's password.
            // Replace with secure password update logic as integrated by the Auth team.
            User currentUser = AuthManager.getInstance().getDatabase().getUser(usernameField.getText());
         //   currentUser.setPassword(newPass);
            AuthManager.getInstance().getDatabase().updateUser(currentUser);
            AuthManager.getInstance().updateUsersFile();
            // --------------------------

            showAlert("Success", "Password changed successfully!");
            dialog.close();
        });

        dialog.showAndWait();
    }

    /**
     * Utility method to show an alert.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}