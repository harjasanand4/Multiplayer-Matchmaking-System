package com.game.gui;

import com.game.auth.*;
import com.game.auth.session.SessionManager;
import com.game.auth.AuthManager;
import com.game.leaderboard.*;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.File;

import static com.game.leaderboard.GameResult.WIN;

/**
 *  The UserProfile class represents a user profile screen displaying user info, stats, and recent matches
 */
public class UserProfile extends Application {
    private User getCurrentUser() {
        // Get the current username from session
        String username = SessionManager.getInstance().getCurrentUsername();
        System.out.println("Debug - Current username from session: " + username);
        if (username != null) {
            User user = AuthManager.getInstance().getDatabase().getUser(username);
            System.out.println("Debug - User object from database: " + (user != null ? user.getUsername() : "null"));
            return user;
        }
        // Debug
        System.out.println("username is null");
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        Scene scene = createScene(stage);
        stage.setTitle("User Profile");
        stage.setScene(scene);
        Main.StageManager.configureStage(stage);
        stage.show();
    }

    /**
     * Creates the main scene layout for the user profile.
     * @param stage The stage to which this scene belongs.
     * @return A configured Scene with all components.
     */
    public Scene createScene(Stage stage) {
        // Main container using BorderPane for top, center, bottom sections
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F5F5;");

        // Add all components
        root.setTop(UIUtils.createTopBar(stage)); // Navigation bar at top
        root.setCenter(createCenterContent(stage)); // Main content area
        root.setBottom(UIUtils.createFooter()); // Footer at bottom

        return new Scene(root, 1000, 700);
    }

    /**
     * Creates the center content area with profile info, stats and match history.
     * @param stage The parent stage for navigation.
     * @return Configured VBox containing all content sections.
     */
    private VBox createCenterContent(Stage stage) {
        VBox centerContent = new VBox(20);
        centerContent.setPadding(new Insets(40, 80, 30, 80));
        centerContent.setAlignment(Pos.TOP_LEFT);

        // Make content grow with screen size
        VBox.setVgrow(centerContent, Priority.ALWAYS);

        // Profile section
        VBox profileSection = createProfileSection(stage);

        // Game stats sections
        // TODO: Replace stats with real user data
        HBox statsContainer = new HBox(25);
        statsContainer.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(statsContainer, Priority.ALWAYS);
        statsContainer.getChildren().addAll(
                createGameStatsSection("Checkers", "/checkers_assets/checkers_images/checkers_black_large.png", 45, 23, 66.2),
                createGameStatsSection("Tic Tac Toe", "/tic_tac_toe_assets/tictactoe_images/tic_tac_toe.png", 67, 31, 68.4),
                createGameStatsSection("Connect Four", "/connect4_assets/connect4_images/connect_4_large.png", 38, 42, 47.5)
        );

        // Recent matches
        VBox recentMatches = createRecentMatchesSection();
        VBox.setVgrow(recentMatches, Priority.ALWAYS);


        // Return to Main Menu
        HBox returnContainer = new HBox();
        returnContainer.setAlignment(Pos.CENTER_RIGHT);
        returnContainer.getChildren().add(createReturnButton(stage));


        centerContent.getChildren().addAll(profileSection, statsContainer, recentMatches, returnContainer);
        return centerContent;
    }

    /**
     * Creates the profile info section.
     */
    private VBox createProfileSection(Stage stage) {
        VBox profileContainer = new VBox(10);
        profileContainer.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("My Stats");
        titleLabel.setFont(Font.font("Arial", 28));
        titleLabel.setStyle("-fx-font-weight: bold;");

        VBox profileBox = new VBox(15);
        profileBox.setPadding(new Insets(15));
        profileBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        HBox profileContent = new HBox(15);
        profileContent.setAlignment(Pos.CENTER_LEFT);

        User currentUser = getCurrentUser();
        String username = currentUser != null ? currentUser.getUsername() : "Guest";
        System.out.println("currentUser: " + username);
        String avatarPath;
        if (currentUser != null && currentUser.getAvatarUrl() != null) {
            avatarPath = currentUser.getAvatarUrl();
        } else {
            avatarPath = "/profile_pictures/avatar_0.png";
        }

        System.out.printf("Avatar Path: %s\n", avatarPath);

        Image avatarImage;
        try {
            avatarImage = new Image(getClass().getResourceAsStream("/" + avatarPath));
        } catch (Exception e) {
            System.err.println("Error loading avatar, fallback used: " + e.getMessage());
            avatarImage = new Image(getClass().getResourceAsStream("/profile_pictures/avatar_0.png"));
        }

        ImageView avatar = new ImageView(avatarImage);
        avatar.setFitHeight(80);
        avatar.setFitWidth(80);
        avatar.setStyle("-fx-background-radius: 50%;");

        Label usernameLabel = new Label(username);
        usernameLabel.setFont(Font.font("Arial", 18));

        // Add Delete Account button
        Button deleteAccountButton = new Button("Delete Account");
        deleteAccountButton.setStyle(
                "-fx-background-color: #ff4d4d; " +
                        "-fx-text-fill: white; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 14; " +
                        "-fx-padding: 8 20; " +
                        "-fx-background-radius: 5;"
        );
        deleteAccountButton.setOnAction(e -> handleDeleteAccount(stage, username));

        // Layout for profile content
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        profileContent.getChildren().addAll(avatar, usernameLabel, spacer, deleteAccountButton);

        profileBox.getChildren().add(profileContent);
        profileContainer.getChildren().addAll(titleLabel, profileBox);

        return profileContainer;
    }

    // Handle account deletion
    private void handleDeleteAccount(Stage stage, String username) {
        if (username.equals("Guest")) {
            UIUtils.showAlert("Error", "Guest accounts cannot be deleted.");
            return;
        }

        boolean confirmed = showConfirmationDialog();
        if (confirmed) {
            AuthManager authManager = AuthManager.getInstance();
            boolean deleted = authManager.deleteOwnAccount(username);
            if (deleted) {
                // Clear session
                SessionManager.getInstance().forceLogoutUser(username);
                // Delete ACCESS-TOKEN鬆

                try {
                    if (StoreToken.doesKeystoreExist()) {
                        File tokenFile = new File("ACCESS-TOKEN.p12");
                        if (tokenFile.exists() && tokenFile.delete()) {
                            System.out.println("ACCESS-TOKEN.p12 deleted successfully.");
                        } else {
                            System.err.println("Failed to delete ACCESS-TOKEN.p12 or file does not exist.");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting ACCESS-TOKEN.p12: " + e.getMessage());
                    // Continue with deletion flow, as token deletion is secondary
                }
                // Show success message
                UIUtils.showAlert("Success", "Your account has been deleted.");
                // Initialize LoginWindow
                showLoginWindow(stage);
            } else {
                UIUtils.showAlert("Error", "Failed to delete account. Please try again.");
            }
        }
    }

    // Initialize LoginWindow
    private void showLoginWindow(Stage currentStage) {
        try {
            LoginWindow loginWindow = new LoginWindow();
            Stage loginStage = new Stage();
            loginWindow.start(loginStage);
            currentStage.close();
        } catch (Exception e) {
            System.err.println("Error launching LoginWindow: " + e.getMessage());
            UIUtils.showAlert("Error", "Failed to open login window.");
        }
    }

    // Confirmation dialog for deletion
    private boolean showConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Are you sure you want to delete your account?");
        alert.setContentText("This action cannot be undone. All your data will be permanently removed.");

        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }

    /**
     * Creates a game stats card with placeholder data.
     * @param gameName Name of the game.
     * @param wins Number of wins
     * @param losses Number of losses
     * @param winRate Win percentage
     * @return Configured VBox stat card.
     */
    private VBox createGameStatsSection(String gameName, String iconPath, int wins, int losses, double winRate) {
        // TODO: Replace with real game stats from backend
        // Current values are placeholders
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        // Game title with icon
        HBox titleBox = new HBox(10); // Spacing between icon and text
        titleBox.setAlignment(Pos.CENTER_LEFT);

        // Load game icon
        ImageView gameIcon = new ImageView();
        try {
            gameIcon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + iconPath);
        }
        gameIcon.setFitWidth(24);
        gameIcon.setFitHeight(24);

        section.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(section, Priority.ALWAYS);

        Label title = new Label(gameName + " Stats");
        title.setFont(Font.font("Arial", 22));
        title.setStyle("-fx-font-weight: bold;");
        titleBox.getChildren().addAll(gameIcon, title);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(30);
        statsGrid.setVgap(15);

        // Configure columns
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setHgrow(Priority.ALWAYS);
        ColumnConstraints valueCol = new ColumnConstraints();
        valueCol.setHgrow(Priority.NEVER);
        valueCol.setHalignment(HPos.RIGHT);
        statsGrid.getColumnConstraints().addAll(labelCol, valueCol);

        // Add stats rows
        addStatRow(statsGrid, "Wins", wins, 0);  // Placeholder for wins
        addStatRow(statsGrid, "Losses", losses, 1); // Placeholder for losses
        addWinRateRow(statsGrid, "Win Rate", winRate, 2); // Placeholder for Win rate

        section.getChildren().addAll(titleBox, statsGrid);
        return section;
    }

    /**
     * Adds a stat row to a GridPane.
     * @param grid Target GridPane.
     * @param label Stat description.
     * @param value Numeric value.
     * @param row Grid row index.
     */
    private void addStatRow(GridPane grid, String label, int value, int row) {
        Label statLabel = new Label(label);
        statLabel.setFont(Font.font("Arial", 14));

        Label statValue = new Label(String.valueOf(value));
        statValue.setFont(Font.font("Arial", 14));
        statValue.setAlignment(Pos.CENTER_RIGHT);

        // Container to maintain your HBox growth behavior
        HBox valueContainer = new HBox(statValue);
        valueContainer.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(valueContainer, Priority.ALWAYS);

        grid.add(statLabel, 0, row);
        grid.add(valueContainer, 1, row);
    }

    // Another method for WinRate row
    private void addWinRateRow(GridPane grid, String label, double winRate, int row) {
        Label statLabel = new Label(label);
        statLabel.setFont(Font.font("Arial", 14));

        Label statValue = new Label(String.format("%.1f%%", winRate));
        statValue.setFont(Font.font("Arial", 14));
        statValue.setAlignment(Pos.CENTER_RIGHT);

        HBox valueContainer = new HBox(statValue);
        valueContainer.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(valueContainer, Priority.ALWAYS);

        grid.add(statLabel, 0, row);
        grid.add(valueContainer, 1, row);
    }

    /**
     * Creates the "Recent Matches" section with placeholder match data.
     * @return Configured VBox containing match history entries.
     */
    private VBox createRecentMatchesSection() {
        VBox section = new VBox();
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        section.setMaxHeight(220);
        section.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(section, Priority.ALWAYS);

        Label title = new Label("Recent Matches");
        title.setFont(Font.font("Arial", 20));
        title.setStyle("-fx-font-weight: bold;");
        title.setPadding(new Insets(0, 0, 10, 0));

        VBox matchesList = new VBox(8);
        matchesList.setFillWidth(true);

        // Create match items
        // TODO: Replace with real match history data from backend
        Player user = new Player(1, "User", 300, 400, 500, 3, 5, 6);
        //matchesList.getChildren().addAll();
        for (Match match : MatchHistoryManager.loadHistory(user)){
            matchesList.getChildren().add(createMatchItem(match));
        }

        section.getChildren().addAll(title, matchesList);
        return section;
    }

    /**
     * Creates an individual match entity
     * @param match the match data between the users
     * @return a VBox with the match data
     */
    private VBox createMatchItem(Match match) {
        // TODO: Replace with real match history data from backend
        VBox itemContainer = new VBox(5); // Container for the match item
        itemContainer.setPadding(new Insets(10));
        itemContainer.setStyle("-fx-background-color: #F0F0F0; -fx-background-radius: 5;");
        itemContainer.setMaxWidth(Double.MAX_VALUE);

        // Top row: Icon + Game name and result/time
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        String game = null;
        switch (match.getGameType()){
            case CHECKERS -> game = "checkers";
            case CONNECT4 -> game = "connect4";
            case TICTACTOE -> game = "tic tac toe";
            default -> {
                System.err.println("Invalid game type: " + match.getGameType() + ". Reverting to default image");
                game = "default";
            }
        }
        ImageView gameIcon = new ImageView(getGameIcon(game));
        gameIcon.setFitWidth(20);
        gameIcon.setFitHeight(20);

        Label gameLabel = new Label(game);
        gameLabel.setFont(Font.font("Arial", 16));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean won = false;
        switch (match.getResult()){
            case WIN -> won = true;
            case LOSE -> won = false;
            default -> {
                System.err.println("Invalid match result: " + match.getResult() + ". Reverting to default");
                won = false;
            }
        }
        // Result
        HBox resultBox = new HBox();
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(2, 8, 2, 8));
        resultBox.setStyle(won ?
                "-fx-background-color: #E8F5E9; -fx-border-color: #4CAF50; -fx-border-radius: 3; -fx-border-width: 1;" :
                "-fx-background-color: #FFEBEE; -fx-border-color: #F44336; -fx-border-radius: 3; -fx-border-width: 1;");

        Label resultLabel = new Label(won ? "Won" : "Lost");
        resultLabel.setStyle(won ? "-fx-text-fill: #2E7D32;" : "-fx-text-fill: #C62828;");
        resultLabel.setFont(Font.font("Arial", 12));
        resultBox.getChildren().add(resultLabel);
// deprecated feature
//        Label timeLabel = new Label(time);
//        timeLabel.setFont(Font.font("Arial", 12));
//        timeLabel.setTextFill(Color.GRAY);

        topRow.getChildren().addAll(gameIcon, gameLabel, spacer, resultBox, new Label(" "));

        // Bottom row: Opponent
        Label opponentLabel = new Label("vs. " + match.getPlayer2());
        opponentLabel.setFont(Font.font("Arial", 12));
        opponentLabel.setTextFill(Color.GRAY);

        itemContainer.getChildren().addAll(topRow, opponentLabel);
        return itemContainer;
    }

    private Image getGameIcon(String gameName) {
        try {
            String iconPath = switch (gameName.toLowerCase()) {
                case "checkers" -> "/checkers_assets/checkers_images/checkers_icon.png";
                case "tic tac toe" -> "/tic_tac_toe_assets/tictactoe_images/tictactoe_icon.png";
                case "connect four" -> "/connect4_assets/connect4_images/connect4_icon.png";
                default -> "/default_game_icon.png";
            };
            return new Image(getClass().getResourceAsStream(iconPath));
        } catch (Exception e) {
            System.err.println("Error loading icon for " + gameName);
            return new Image(getClass().getResourceAsStream("/default_game_icon.png"));
        }
    }

    private Button createReturnButton(Stage stage) {
        Button returnButton = new Button("Return to Main Menu");
        returnButton.setStyle(
                "-fx-background-color: #595a57;" +
                        "-fx-text-fill: white;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 5;"
        );
        returnButton.setOnAction(e -> {
            UIUtils.returnToMainMenu(stage);
        });

        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.getChildren().add(returnButton);
        return returnButton;
    }
}
