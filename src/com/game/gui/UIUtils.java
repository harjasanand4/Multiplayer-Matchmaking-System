package com.game.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.io.File;
import javafx.stage.Stage;

import com.game.auth.*;
import com.game.auth.session.SessionManager;

import java.util.Optional;

public final class UIUtils {
    private static String username;
    private static Stage stage;

    private UIUtils() {}

    // <=========== Basic Layouts ===============>
    static HBox createTopBar(Stage stage) {
        HBox topBar = new HBox();
        topBar.setStyle("-fx-background-color: #111111;");
        topBar.setPrefHeight(60);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 20, 0, 20));

        // Logo and platform name
        Button logoButton = new Button();
        logoButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;" +
                        "-fx-cursor: hand;"
        );
        HBox brandBox = new HBox(10);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        // Logo and Platform name
        ImageView logo = new ImageView(new Image("/OMG_Logo.png"));
        logo.setFitHeight(30);
        logo.setFitWidth(30);

        Label platformLabel = new Label("OMG Platform");
        platformLabel.setFont(Font.font("Arial Black", 18));
        platformLabel.setTextFill(Color.WHITE);
        brandBox.getChildren().addAll(logo, platformLabel);
        logoButton.setGraphic(brandBox);

        // Logo button action - returns to main menu
        logoButton.setOnAction(e -> {
            returnToMainMenu(stage);
        });

        // Create the network button using your new utility method
        Button networkButton = createNetworkButton(stage);


        // // Spacer to push right-side controls to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.SOMETIMES);

        // Right side controls
        HBox rightControls = new HBox(15);
        rightControls.setAlignment(Pos.CENTER_RIGHT);

        // Friends Button
        Button friendsButton = createMenuButton(
                "/friends_menu/friends_online_large.png",
                "Friends",
                "Friends",
                stage
        );

        // Leaderboard button
        Button leaderboardButton = createMenuButton(
                "/menu_buttons/leaderboard_button_icon_large.png",
                "Leaderboard",
                "Leaderboard",
                stage
        );

        // My Stats button
        Button myStatsButton = createMenuButton(
                "/menu_buttons/my_stats_button_icon_large.png",
                "My Stats",
                "User Profile",
                stage
        );

        // Settings button
        Button settingsButton = createIconButton("/menu_buttons/settings.png", "Settings", stage);

        // Get current user info
        String currentUsername = SessionManager.getInstance().getCurrentUsername();
        User currentUser = AuthManager.getInstance().getDatabase().getUser(currentUsername);

        // User profile icon
        VBox userContainer = createUserProfileIcon(currentUser);

        rightControls.getChildren().addAll(friendsButton, leaderboardButton, myStatsButton, settingsButton, userContainer);
        // Add the logo, network button, spacer, and right controls to the top bar.
        topBar.getChildren().addAll(logoButton, networkButton, spacer, rightControls);

        return topBar;
    }

    /**
     * Creates the application footer.
     * @return Configured HBox with copyright text.
     */
    static HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: #F8F8F8; -fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");

        Label copyright = new Label("© 2025 OMG Platform. All rights reserved.");
        copyright.setFont(Font.font("Arial", 12));
        copyright.setStyle("-fx-text-fill: #888888;");

        Hyperlink aboutLink = new Hyperlink("About Us");
        aboutLink.setFont(Font.font("Arial", 12));
        aboutLink.setStyle("-fx-text-fill: #888888;");
        aboutLink.setOnAction(e -> AboutUs.open());

        footer.getChildren().addAll(copyright, aboutLink);
        return footer;
    }

    // <=========== Utility ===============>
    // Helper Function to create user icon in the top right corner (avatar icon, username, status)
    private static VBox createUserProfileIcon(User currentUser) {
        // Container for the user profile icon and details.
        VBox userContainer = new VBox();
        userContainer.setAlignment(Pos.CENTER);
        // Optionally adjust the padding if needed.
        userContainer.setPadding(new Insets(0, 0, 5, 0));

        // HBox that holds the profile picture (as a MenuButton) and the user info (username and status).
        HBox userSection = new HBox();
        // Align all elements to the left vertically.
        userSection.setAlignment(Pos.CENTER_LEFT);
        // Reduce spacing so that the avatar and user info appear closer (adjust as needed).
        userSection.setSpacing(5);

        // Create the user profile MenuButton for the avatar.
        MenuButton userMenu = new MenuButton();
        // Load the user's avatar; use a default if none is provided.
        ImageView userAvatar = new ImageView();
        try {
            Image avatarImage = new Image(currentUser.getAvatarUrl());
            userAvatar.setImage(avatarImage);
        } catch (Exception e) {
            // Fallback to a default avatar image.
            Image defaultAvatar = new Image("/profile_pictures/avatar_0.png");
            userAvatar.setImage(defaultAvatar);
        }
        userAvatar.setFitHeight(40);
        userAvatar.setFitWidth(40);
        // Ensure the avatar is displayed as a circle by applying a rounded background style.
        userAvatar.setStyle("-fx-background-radius: 50%;");

        // Set the avatar inside a MenuButton to allow for profile actions.
        userMenu.setGraphic(userAvatar);
        MenuItem changePfpItem = new MenuItem("Change Profile Picture");
        // Action for changing the profile picture here.
        changePfpItem.setOnAction(e -> {
            // Get the current stage from the menu item
            MenuItem item = (MenuItem) e.getSource();
            ContextMenu menu = item.getParentPopup();
            Node node = menu.getOwnerNode();
            Stage currentStage = (Stage) node.getScene().getWindow();
            System.out.println("Current stage:" + currentStage);
            handleProfilePictureChange(currentUser, currentStage);
        });

        MenuItem signOutItem = new MenuItem("Sign Out");
        // Action for signing out here.
        signOutItem.setOnAction(e -> {
            SessionManager.getInstance().forceLogoutUser(username);
            try {
                if (StoreToken.doesKeystoreExist()) {
                    File tokenFile = new File("ACCESS-TOKEN.p12");
                    if (tokenFile.exists() && tokenFile.delete()) {
                        System.out.println("ACCESS-TOKEN.p12 deleted successfully.");
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error deleting ACCESS-TOKEN.p12: " + ex.getMessage());
            }
            showLoginWindow(stage);
        });

        userMenu.getItems().addAll(changePfpItem, signOutItem);
        userMenu.setStyle("-fx-background-color: transparent;");

        // Create a VBox for the username and online status.
        VBox userInfo = new VBox();
        // Align the text labels to the left.
        userInfo.setAlignment(Pos.CENTER_LEFT);
        // Set a small vertical spacing between the username and status.
        userInfo.setSpacing(2);

        String username = currentUser != null ? currentUser.getUsername() : "user";
        Label userNameLabel = new Label(username);
        userNameLabel.setTextFill(Color.WHITE);
        userNameLabel.setFont(Font.font("Arial", 15));

        Label userStatus = new Label("Online");
        userStatus.setTextFill(Color.LIGHTGREEN);
        userStatus.setFont(Font.font("Arial", 10));

        // Add the username and status to the VBox.
        userInfo.getChildren().addAll(userNameLabel, userStatus);

        // Add both the avatar (MenuButton) and the user info VBox to the HBox.
        userSection.getChildren().addAll(userMenu, userInfo);
        // Add the userSection HBox to the outer VBox.
        userContainer.getChildren().add(userSection);

        return userContainer;
    }

    /**
     * Allows the user to change their profile picture by inserting the image URL (Must be in resources folder)
     * @param currentUser
     * @param currentStage
     */
    private static void handleProfilePictureChange(User currentUser, Stage currentStage) {
        TextInputDialog dialog = new TextInputDialog(currentUser.getAvatarUrl());
        dialog.setTitle("Change Profile Picture");
        dialog.setHeaderText("Enter image URL");
        dialog.setContentText("URL:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(url -> {
            try {
                // Update the user's avatar URL
                currentUser.setAvatarUrl(url);
                AuthManager.getInstance().getDatabase().updateUser(currentUser);
                AuthManager.getInstance().updateUsersFile();

                // Refresh the current screen instead of going to profile
                refreshCurrentScreen(currentStage);

                // Show success message
                showAlert("Success", "Profile picture updated!");
            } catch (Exception e) {
                showAlert("Error", "Failed to update profile picture: " + e.getMessage());
            }
        });
    }

    private static void refreshCurrentScreen(Stage currentStage) {
        // Get the current scene's class to know which screen to recreate
        Scene currentScene = currentStage.getScene();

//        if (currentScene instanceof GameMenu_2) {
//            // If we're on the main menu
//            GameMenu_2 menu = new GameMenu_2();
//            currentStage.setScene(menu.createScene(currentStage));
//        }
//        else if (currentScene.getRoot() instanceof UserProfile) {
//            // If we're on the user profile (though you said you don't want this)
//            UserProfile profile = new UserProfile();
//            currentStage.setScene(profile.createScene(currentStage));
//        }
        // Add more else-if blocks for other screens as needed

        // Keep the stage configuration
        Main.StageManager.configureStage(currentStage);
    }

    // Initialize LoginWindow
    private static void showLoginWindow(Stage currentStage) {
        Platform.runLater(() -> {
            try {
                LoginWindow loginWindow = new LoginWindow();
                Stage loginStage = new Stage();
                loginWindow.start(loginStage);
                currentStage.close();
            } catch (Exception e) {
                System.out.println("Error launching LoginWindow: " + e.getMessage());
            }
        });
    }


    static void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Creates an icon button with hover effects.
     * @param iconPath Path to the icon image.
     * @param featureName The name of the feature to launch.
     * @param stage The parent stage.
     * @return A configured Button.
     */
    private static Button createIconButton(String iconPath, String featureName, Stage stage) {
        Image icon = new Image(iconPath);
        ImageView iconView = new ImageView(icon);
        iconView.setFitWidth(30);
        iconView.setFitHeight(30);
        iconView.setPreserveRatio(true);

        Button button = new Button();
        button.setGraphic(iconView);
        button.setStyle("-fx-background-color: transparent;");
        button.setEffect(new DropShadow(3, Color.GRAY));

        // Hover effects
        button.setOnMouseEntered(e -> {
            iconView.setScaleX(1.1);
            iconView.setScaleY(1.1);
        });
        button.setOnMouseExited(e -> {
            iconView.setScaleX(1.0);
            iconView.setScaleY(1.0);
        });

        button.setOnAction(e -> launchFeature(featureName, stage));
        return button;
    }

    // Helper function to create a menu button
    static Button createMenuButton(String iconPath, String buttonLabel, String featureName, Stage stage) {
        Button button = new Button();

        button.setStyle(
                "-fx-background-color: #333333, #444444;" + // Normal, hover
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 5 10;" +
                        "-fx-border-insets: 5 0;" + // Space from black bar
                        "-fx-background-insets: 0, 0;" +
                        "-fx-text-fill: white;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);" +
                        "-fx-border-color: transparent;"
        );

        // Makes the button pop on hover
        button.setMaxHeight(40);
        button.setOnMouseEntered(e -> {
            button.setScaleX(1.1);
            button.setScaleY(1.1);
        });
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        button.setOnAction(e -> launchFeature(featureName, stage));

        // Button content
        HBox buttonContent = new HBox(5);
        buttonContent.setAlignment(Pos.CENTER);

        ImageView icon = new ImageView(new Image(iconPath));
        icon.setFitHeight(16);
        icon.setFitWidth(16);
        icon.setStyle("-fx-background-color: transparent;");

        Label leaderboardLabel = new Label(buttonLabel);
        leaderboardLabel.setTextFill(Color.WHITE);
        leaderboardLabel.setFont(Font.font("Arial", 15));

        buttonContent.getChildren().addAll(icon, leaderboardLabel);
        button.setGraphic(buttonContent);

        return button;
    }

    // Network Icon
    public static Button createNetworkButton(Stage stage) {
        // Create an HBox that will hold the label and icon
        HBox networkBox = new HBox(10);
        networkBox.setAlignment(Pos.CENTER);

        // Create the network label with the desired style
        Label networkLabel = new Label("Network");
        networkLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-family: 'Inter';");

        // Create the network icon ImageView
        ImageView networkIcon = new ImageView(new Image(UIUtils.class.getResourceAsStream("/game_images/network icon.png")));
        networkIcon.setFitHeight(16);
        networkIcon.setFitWidth(20);
        networkIcon.setPreserveRatio(true);

        // Add both to the HBox
        networkBox.getChildren().addAll(networkLabel, networkIcon);

        // Create a transparent button and set its graphic to our HBox
        Button networkButton = new Button();
        networkButton.setGraphic(networkBox);
        networkButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        // Set the action to display network details
        networkButton.setOnAction(e -> showNetworkDetails());

        return networkButton;
    }

    /**
     * Helper method that displays network details in an alert.
     */
    private static void showNetworkDetails() {
        String details = NetworkUtils.getNetworkDetails();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Network Details");
        alert.setHeaderText("Your Network Information");
        alert.setContentText(details);
        alert.showAndWait();
    }



    // <=========== Common Navigation Actions ===============>
    /**
     * Returns to the main menu (closes profile view).
     * @param stage Current stage to close.
     */
    protected static void returnToMainMenu(Stage stage) {
        Stage menuStage = new Stage();
        GameMenu_2 gameMenu = new GameMenu_2();
        menuStage.setScene(gameMenu.createScene(menuStage));
        menuStage.setTitle("Board Game Hub");
        Main.StageManager.configureStage(menuStage);
        menuStage.show();
        stage.close();
    }

    /**
     * Launches a feature (Settings, Leaderboard, etc.) in a new window.
     * @param featureName The name of the feature to launch.
     * @param stage The parent stage (will be closed for some features).
     */
    static void launchFeature(String featureName, Stage stage) {
        try {
            switch (featureName) {
                case "Settings":
                    new Settings().start(new Stage());
                    break;
                case "User Profile":
                    UserProfile profile = new UserProfile();
                    Stage profileStage = new Stage();
                    profile.start(profileStage);
                    stage.close();
                    break;
                case "Leaderboard":
                    new Leaderboard().start(new Stage());
                    stage.close();
                    break;
                case "Friends":
                    new FriendList().start(new Stage());
                    stage.close();
                case "Chats":
                    new ChatBox().start(new Stage());
                    stage.close();
                    break;
            }
        } catch (Exception e) {
            System.out.println("Error launching " + featureName + ": " + e.getMessage());
        }
    }
}

