package com.game.gui;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.input.KeyCombination;

public class MatchmakingWindow2 extends BorderPane {

    private boolean isCancelled = false;      // to track Whether the user has cancelled the matchmakind process
    public enum MatchType { RANKED, FRIEND, LOCAL } // types of matchmaking

    // Store the matchtype and game type
    private  MatchType matchType;
    private final String gameName;
    //
    private Label statusLabel;      // message on the screen
    private ProgressIndicator spinner;      // circular loader
    private VBox centerContent;
    private Timeline searchTimeline;  // using for timed animations

    // Constructor initializes the matchmaking window based on game type and selected mode (Local, Friend, Ranked).
    public MatchmakingWindow2(String gameName, MatchType matchType) {
        this.gameName = gameName;
        this.matchType = matchType;

        // Apply global styling
        this.setStyle("-fx-background-color: #f5f5f5;");
        setupHeader();
        initializeUI();
    }

    // Sets up the header area with the game icon, title, and close button.
    private void setupHeader() {
        // Header with game logo and title
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #222; -fx-border-width: 0 0 1 0; -fx-border-color: #444;");

        // Game icon
        ImageView gameIcon = createGameIcon();

        // Game title
        Label titleLabel = new Label(gameName + " - Matchmaking");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        titleLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        // Close button
        Button closeButton = new Button("×");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px;");
        closeButton.setOnAction(e -> ((Stage) this.getScene().getWindow()).close());

        // Add spacer to push close button to right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(gameIcon, titleLabel, spacer, closeButton);
        this.setTop(header);
    }

    // Creates and returns an icon specific to the game selected.
    private ImageView createGameIcon() {
        Circle iconBg = new Circle(15, Color.GRAY);
        ImageView gameIcon = new ImageView();
        gameIcon.setFitHeight(30);
        gameIcon.setFitWidth(30);

        try {
            String imagePath = null;

            if (gameName.contains("Tic Tac Toe")) {
                imagePath = "file:resources/tic_tac_toe_assets/tictactoe_images/tictactoe_icon.png";
            } else if (gameName.contains("Checkers")) {
                imagePath = "file:resources/checkers_assets/checkers_images/checkers_icon.png";
            } else if (gameName.contains("Connect 4")) {
                imagePath = "file:resources/connect4_assets/connect4_images/red_piece.png";
            }

            if (imagePath != null) {
                gameIcon.setImage(new Image(imagePath));
            }
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
        }

        return gameIcon;
    }

    // Initializes main UI elements like status labels, spinner, and bottom bar with cancel button.
    private void initializeUI() {
        // Center content
        centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(30));
        VBox.setVgrow(centerContent, Priority.ALWAYS);
        centerContent.setFillWidth(true);

        // Status message
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 16px;");

        // Progress indicator
        spinner = new ProgressIndicator();
        spinner.setPrefSize(60, 60);

        centerContent.getChildren().addAll(statusLabel, spinner);
        this.setCenter(centerContent);

        // Bottom bar with cancel button
        HBox bottomBar = new HBox();
        bottomBar.setPadding(new Insets(15, 20, 15, 20));
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-width: 1 0 0 0; -fx-border-color: #ddd;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px;");
        cancelButton.setPrefWidth(100);
        cancelButton.setOnAction(e -> {
            isCancelled = true;
            ((Stage) this.getScene().getWindow()).close();
        });

        bottomBar.getChildren().add(cancelButton);
        this.setBottom(bottomBar);
    }

    // Configures and displays the stage, initiating the matchmaking process based on the selected match type.
    public void start(Stage stage) {
        // Configure stage
        stage.setTitle(gameName + " - Matchmaking");
        Scene scene = new Scene(this, 1440, 1024); // increase dimensions
        stage.setScene(scene);
        stage.setMinWidth(1440);
        stage.setMinHeight(1024);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
        stage.setFullScreenExitHint("Press ESC to exit full screen");
        stage.setFullScreenExitKeyCombination(KeyCombination.valueOf("ESCAPE"));
        // Optional: Automatically open in fullscreen
        //stage.setFullScreen(true);

        // Start matchmaking based on selected type
        switch (matchType) {
            case LOCAL -> startLocalGame(stage);
            case FRIEND -> showFriendSelector(stage);
            case RANKED -> startRankedSearch(stage);
        }
    }

    // Handles UI updates and timing for starting a local game.
    private void startLocalGame(Stage stage) {
        statusLabel.setText("Preparing local game...");
        spinner.setProgress(-1);

        PauseTransition wait = new PauseTransition(Duration.seconds(1.5));
        wait.setOnFinished(e -> {
            statusLabel.setText("Game ready! Starting now...");
            spinner.setProgress(1);

            PauseTransition launch = new PauseTransition(Duration.seconds(1));
            launch.setOnFinished(ev -> {
                if (!isCancelled) {
                    launchGame(stage);
                }
            });
            launch.play();
        });
        wait.play();
    }

    // Displays a friend selection interface allowing the user to invite available friends to play.
    private void showFriendSelector(Stage stage) {
        // Clear existing content
        centerContent.getChildren().clear();

        VBox friendSelectorBox = new VBox(15);
        friendSelectorBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Invite a Friend");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Friend search
        TextField searchField = new TextField();
        searchField.setPromptText("Search friends...");
        searchField.setPrefWidth(300);

        // Friend list with status indicators
        ListView<HBox> friendList = new ListView<>();
        friendList.setPrefHeight(200);
        friendList.getStyleClass().add("friend-list");

        // Add mock friends with status indicators
        addFriendToList(friendList, "Sarah123", true, "Playing Connect 4");
        addFriendToList(friendList, "Mike454", true, "Available");
        addFriendToList(friendList, "PlayerX", true, "Available");
        addFriendToList(friendList, "Emma444", false, "Last online 2h ago");
        addFriendToList(friendList, "Tommy78", false, "Last online 5h ago");

        Button inviteButton = new Button("Invite Selected Friend");
        inviteButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px;");
        inviteButton.setPrefWidth(200);
        inviteButton.setDisable(true);

        // Enable button when a friend is selected
        friendList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                HBox selectedItem = friendList.getSelectionModel().getSelectedItem();
                Label nameLabel = (Label) selectedItem.getChildren().get(1);
                String friendName = nameLabel.getText();
                Label statusLabel = (Label) selectedItem.getChildren().get(2);
                String status = statusLabel.getText();

                // Only enable if friend is available
                inviteButton.setDisable(!status.equals("Available"));
            }
        });

        inviteButton.setOnAction(e -> {
            HBox selectedItem = friendList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Label nameLabel = (Label) selectedItem.getChildren().get(1);
                String friendName = nameLabel.getText();
                waitForFriendResponse(stage, friendName);
            }
        });

        friendSelectorBox.getChildren().addAll(titleLabel, searchField, friendList, inviteButton);
        centerContent.getChildren().add(friendSelectorBox);
    }

    // Adds a friend entry to the friend list with their online status and availability.
    private void addFriendToList(ListView<HBox> list, String name, boolean online, String status) {
        HBox friendItem = new HBox(10);
        friendItem.setAlignment(Pos.CENTER_LEFT);
        friendItem.setPadding(new Insets(5, 10, 5, 10));

        // Status indicator
        Circle statusDot = new Circle(6);
        statusDot.setFill(online ? Color.GREEN : Color.GRAY);

        // Friend name
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold;");

        // Status text
        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-text-fill: #666;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Invite button (only for online friends)
        Button quickInvite = new Button("Invite");
        quickInvite.setVisible(online && status.equals("Available"));
        quickInvite.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        friendItem.getChildren().addAll(statusDot, nameLabel, statusLabel, spacer);
        if (online && status.equals("Available")) {
            quickInvite.setOnAction(e -> {
                waitForFriendResponse((Stage) this.getScene().getWindow(), name);
            });
            friendItem.getChildren().add(quickInvite);
        }

        list.getItems().add(friendItem);
    }

    // Manages the waiting UI and timing for friend responses to game invites.
    private void waitForFriendResponse(Stage stage, String friendName) {
        // Clear and reset UI
        centerContent.getChildren().clear();

        VBox waitingBox = new VBox(20);
        waitingBox.setAlignment(Pos.CENTER);

        Label inviteLabel = new Label("Invitation sent to " + friendName);
        inviteLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ProgressIndicator waitSpinner = new ProgressIndicator();
        waitSpinner.setPrefSize(60, 60);

        Label waitingLabel = new Label("Waiting for response...");

        // Countdown indicator
        HBox timeBox = new HBox(5);
        timeBox.setAlignment(Pos.CENTER);
        Label timeLabel = new Label("Expires in: ");
        Label countdownLabel = new Label("60s");
        timeBox.getChildren().addAll(timeLabel, countdownLabel);

        Button cancelInviteButton = new Button("Cancel Invitation");
        cancelInviteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        cancelInviteButton.setOnAction(e -> showFriendSelector(stage));

        waitingBox.getChildren().addAll(inviteLabel, waitSpinner, waitingLabel, timeBox, cancelInviteButton);
        centerContent.getChildren().add(waitingBox);

        // Simulate friend accepting after 4 seconds
        PauseTransition acceptDelay = new PauseTransition(Duration.seconds(4));
        acceptDelay.setOnFinished(e -> {
            inviteLabel.setText(friendName + " accepted your invitation!");
            waitingLabel.setText("Starting game...");
            waitSpinner.setProgress(1);

            PauseTransition startGame = new PauseTransition(Duration.seconds(2));
            startGame.setOnFinished(ev -> {
                if (!isCancelled) {
                    launchGame(stage);
                }
            });
            startGame.play();
        });
        acceptDelay.play();

        // Countdown simulation
        Timeline countdown = new Timeline();
        countdown.setCycleCount(60);
        countdown.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1), ev -> {
                    int currentTime = Integer.parseInt(countdownLabel.getText().replace("s", ""));
                    countdownLabel.setText((currentTime - 1) + "s");
                })
        );
        countdown.play();
    }

    // Starts ranked matchmaking, updates UI to show search range and handles timeout scenarios.
    private void startRankedSearch(Stage stage) {
        statusLabel.setText("Searching for opponents (±200 ELO)...");
        spinner.setProgress(-1);

        // Search range display with fixed placeholder values
        HBox rangeBox = new HBox(10);
        rangeBox.setAlignment(Pos.CENTER);

        Label minEloLabel = new Label("1000");
        minEloLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        ProgressBar rangeBar = new ProgressBar(0.5);
        rangeBar.setPrefWidth(200);

        Label maxEloLabel = new Label("1400");
        maxEloLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        rangeBox.getChildren().addAll(minEloLabel, rangeBar, maxEloLabel);

        if (!centerContent.getChildren().contains(rangeBox)) {
            centerContent.getChildren().add(2, rangeBox);
        }

        // Create timeline for expanding search range
        searchTimeline = new Timeline();

        // Expand search after 10 seconds
        searchTimeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(2), e -> {
                    statusLabel.setText("Expanding search (±400 ELO)...");
                    rangeBar.setProgress(0.7);
                })
        );


        // Timeout after 30 seconds
        searchTimeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(15), e -> {
                    showRankedTimeoutOptions(stage);
                })
        );

        searchTimeline.setCycleCount(1);
        searchTimeline.play();

        // Simulate finding match after 15 seconds
        // was testing if timeout works
//        PauseTransition foundMatch = new PauseTransition(Duration.seconds(15));
//       foundMatch.setOnFinished(e -> {
//           if (searchTimeline != null) {
//               searchTimeline.stop();
//            }
//            showMatchFoundScreen(stage);        });
//        foundMatch.play();
    }

    // Displays details of the opponent found, along with a countdown before the match starts.
    private void showMatchFoundScreen(Stage stage) {
        // Clear existing UI
        centerContent.getChildren().clear();

        VBox matchFoundBox = new VBox(15);
        matchFoundBox.setAlignment(Pos.CENTER);
        matchFoundBox.setPadding(new Insets(20));

        // Animated match found text
        Label matchFoundLabel = new Label("MATCH FOUND!");
        matchFoundLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        // Create opponent card
        VBox opponentCard = new VBox(10);
        opponentCard.setAlignment(Pos.CENTER);
        opponentCard.setPadding(new Insets(15));
        opponentCard.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        opponentCard.setPrefWidth(300);

        // Opponent avatar
        Circle avatarCircle = new Circle(40);
        avatarCircle.setFill(Color.LIGHTGRAY);

        // Opponent details
        Label opponentName = new Label("PlayerX");
        opponentName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label opponentElo = new Label("ELO: 1280");
        opponentElo.setStyle("-fx-font-size: 14px;");

        Label winRate = new Label("Win Rate: 62%");
        winRate.setStyle("-fx-font-size: 14px;");

        // VS indicator
        Label vsLabel = new Label("VS");
        vsLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3498db;");

        // Your details
        Label yourName = new Label("You");
        yourName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Starting countdown
        Label startingLabel = new Label("Starting in 5...");
        startingLabel.setStyle("-fx-font-size: 16px;");

        opponentCard.getChildren().addAll(
                avatarCircle, opponentName, opponentElo, winRate,
                new Separator(), vsLabel, yourName
        );

        matchFoundBox.getChildren().addAll(matchFoundLabel, opponentCard, startingLabel);
        centerContent.getChildren().add(matchFoundBox);

        // Countdown animation
        Timeline countdown = new Timeline();
        countdown.setCycleCount(5);
        countdown.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1), e -> {
                    int currentCount = Integer.parseInt(startingLabel.getText().split("\\.")[0].substring(12));
                    if (currentCount > 1) {
                        startingLabel.setText("Starting in " + (currentCount - 1) + "...");
                    } else {
                        startingLabel.setText("Starting now!");
                    }
                })
        );
        countdown.setOnFinished(e -> {
            if (!isCancelled) {
                launchGame(stage);
            }
        });
        countdown.play();
    }

    // Provides options to the user after a ranked match search times out.
    private void showRankedTimeoutOptions(Stage stage) {
        // Clear existing UI
        centerContent.getChildren().clear();

        VBox timeoutBox = new VBox(15);
        timeoutBox.setAlignment(Pos.CENTER);
        timeoutBox.setPadding(new Insets(20));

        Label timeoutLabel = new Label("Matchmaking Timeout");
        timeoutLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Could not find players in your skill range.");
        infoLabel.setStyle("-fx-font-size: 14px;");

        // Options
        VBox optionsBox = new VBox(10);
        optionsBox.setAlignment(Pos.CENTER);
        optionsBox.setPadding(new Insets(15));
        optionsBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        optionsBox.setPrefWidth(300);

        Button expandedSearchBtn = new Button("Play Random");
        expandedSearchBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        expandedSearchBtn.setPrefWidth(250);
        expandedSearchBtn.setOnAction(e -> showRandomMatchWarning(stage));
        expandedSearchBtn.setOnMouseEntered(e -> expandedSearchBtn.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-size: 14px;"));
        expandedSearchBtn.setOnMouseExited(e -> expandedSearchBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;"));

        Button retryBtn = new Button("Retry Ranked Match");
        retryBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px;");
        retryBtn.setPrefWidth(250);
        retryBtn.setOnAction(e -> startRankedSearch(stage));
        retryBtn.setOnMouseEntered(e -> retryBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14px;"));
        retryBtn.setOnMouseExited(e -> retryBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px;"));

        Button cancelBtn = new Button("Return to Dashboard");
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px;");
        cancelBtn.setPrefWidth(250);
        cancelBtn.setOnAction(e -> {
            isCancelled = true;
            ((Stage) this.getScene().getWindow()).close();
        });
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 14px;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px;"));

        optionsBox.getChildren().addAll(expandedSearchBtn, retryBtn, cancelBtn);

        timeoutBox.getChildren().addAll(timeoutLabel, infoLabel, optionsBox);
        centerContent.getChildren().add(timeoutBox);
    }

    // Warns the user about potential large ELO differences when playing random opponents.
    private void showRandomMatchWarning(Stage stage) {
        // Clear existing UI
        centerContent.getChildren().clear();

        VBox warningBox = new VBox(15);
        warningBox.setAlignment(Pos.CENTER);
        warningBox.setPadding(new Insets(20));

        Label warningTitle = new Label("ELO Warning");
        warningTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        // Warning icon
        Label warningIcon = new Label("⚠");
        warningIcon.setStyle("-fx-font-size: 36px; -fx-text-fill: #e74c3c;");

        // Warning message
        Label warningText = new Label(
                "You may be paired with a player significantly above or below your rank. " +
                        "This may result in larger ELO changes than normal matches."
        );
        warningText.setStyle("-fx-font-size: 14px;");
        warningText.setWrapText(true);
        warningText.setPrefWidth(350);
        warningText.setAlignment(Pos.CENTER);

        CheckBox dontShowAgain = new CheckBox("Don't show this warning again");

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button continueBtn = new Button("Continue Anyway");
        continueBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        continueBtn.setPrefWidth(150);
        continueBtn.setOnAction(e -> startRandomMatch(stage));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px;");
        cancelBtn.setPrefWidth(150);
        cancelBtn.setOnAction(e -> showRankedTimeoutOptions(stage));

        buttonBox.getChildren().addAll(continueBtn, cancelBtn);

        warningBox.getChildren().addAll(warningTitle, warningIcon, warningText, dontShowAgain, buttonBox);
        centerContent.getChildren().add(warningBox);
    }

    // Initiates a quick search for any available opponent, usually used after a ranked timeout.
    private void startRandomMatch(Stage stage) {
        // Clear and reset UI
        centerContent.getChildren().clear();

        statusLabel.setText("Searching for any available opponent...");
        spinner.setProgress(-1);

        centerContent.getChildren().addAll(statusLabel, spinner);

        // Simulate finding a match quickly
        PauseTransition quickMatch = new PauseTransition(Duration.seconds(3));
        quickMatch.setOnFinished(e -> showMatchFoundScreen(stage));
        quickMatch.play();
    }
    private GameEnums.MatchType convertMatchType(MatchType type) {
        return switch (type) {
            case RANKED -> GameEnums.MatchType.RANKED;
            case FRIEND -> GameEnums.MatchType.CASUAL_ONLINE;
            case LOCAL -> GameEnums.MatchType.LOCAL;
        };
    }
    // Launches the game screen based on the selected game type.
    private void launchGame(Stage stage) {
        if (isCancelled) return;
        stage.close();

        try {
            FXMLLoader loader = new FXMLLoader();
            String fxmlPath = switch (gameName) {
                case "Tic Tac Toe" -> "/tic_tac_toe_assets/TicTacToeScreen.fxml";
                case "Connect 4" -> "/connect4_assets/Connect4Screen.fxml";
                case "Checkers" -> "/checkers_assets/CheckersScreen.fxml";
                default -> throw new IllegalArgumentException("Unknown game: " + gameName);
            };

            loader.setLocation(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof TicTacToeController ttt) {
                ttt.setMatchType(convertMatchType(matchType));
            } else if (controller instanceof ConnectFourController c4) {
                c4.setMatchType(convertMatchType(matchType));
            } else if (controller instanceof CheckersController cc) {
                cc.setGame(new Checkers());
                cc.startGame();
                cc.setMatchType(convertMatchType(matchType));

            }

            Stage gameStage = new Stage();
            gameStage.setTitle(gameName);
            gameStage.setScene(new Scene(root, 1440, 1024)); // Set preferred window size
            gameStage.setResizable(true);                  // Allow resizing
            gameStage.setMinWidth(1440);                    // Minimum width
            gameStage.setMinHeight(1024);
            gameStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
