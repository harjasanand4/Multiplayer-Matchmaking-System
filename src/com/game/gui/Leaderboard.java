package com.game.gui;

import com.game.leaderboard.Player;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

import static com.game.gui.UIUtils.createTopBar;

public class Leaderboard extends Application {

    private TableView<Player> leaderboardTable;
    private final com.game.leaderboard.Leaderboard logic = new com.game.leaderboard.Leaderboard();
    private String selectedGame = "ticTacToe"; // default

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F5F5;");

        HBox topBar = createTopBar(stage);
        root.setTop(topBar);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Current Leaderboard");
        titleLabel.setFont(Font.font("Arial", 32));
        titleLabel.setTextFill(Color.BLACK);

        HBox filterButtons = createFilterButtons();
        leaderboardTable = createLeaderboardTable();
        updateLeaderboardData(selectedGame);

        content.getChildren().addAll(titleLabel, filterButtons, leaderboardTable);
        root.setCenter(content);

        VBox bottomContainer = new VBox(10);
        bottomContainer.setPadding(new Insets(10, 20, 20, 20));
        bottomContainer.setAlignment(Pos.CENTER_RIGHT);
        bottomContainer.getChildren().addAll(createReturnButton(stage), UIUtils.createFooter());
        root.setBottom(bottomContainer);

        Scene scene = new Scene(root, 950, 620);
        stage.setTitle("Leaderboard");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setFullScreen(false);
        Main.StageManager.configureStage(stage);
        stage.show();
    }

    private HBox createFilterButtons() {
        Button ticTacToeBtn = new Button("Tic Tac Toe");
        Button checkersBtn = new Button("Checkers");
        Button connectFourBtn = new Button("Connect Four");

        ticTacToeBtn.setOnAction(e -> updateLeaderboardData("tictactoe"));
        checkersBtn.setOnAction(e -> updateLeaderboardData("checkers"));
        connectFourBtn.setOnAction(e -> updateLeaderboardData("connect4"));

        HBox buttonBox = new HBox(10, ticTacToeBtn, checkersBtn, connectFourBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        for (Button btn : new Button[]{ticTacToeBtn, checkersBtn, connectFourBtn}) {
            styleFilterButton(btn);
        }

        return buttonBox;
    }

    private void styleFilterButton(Button button) {
        button.setStyle(
                "-fx-background-color: #D3D3D3;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 14px;"
        );

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #A9A9A9; -fx-background-radius: 10;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #D3D3D3; -fx-background-radius: 10;"));
    }

    private TableView<Player> createLeaderboardTable() {
        TableView<Player> table = new TableView<>();

        TableColumn<Player, String> rankColumn = new TableColumn<>("Rank");
        rankColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper("#" + (table.getItems().indexOf(data.getValue()) + 1)));
        rankColumn.setSortable(false);

        TableColumn<Player, String> playerColumn = new TableColumn<>("Player");
        playerColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
        playerColumn.setSortable(false);

        TableColumn<Player, Integer> eloColumn = new TableColumn<>();
        eloColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getElo(selectedGame)));
        eloColumn.setSortable(true);
        Label eloHeader = new Label("ELO ✦");
        eloColumn.setGraphic(eloHeader);
        eloHeader.setOnMouseClicked(e -> toggleSort(table, eloColumn));

        TableColumn<Player, Integer> winsColumn = new TableColumn<>();
        winsColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getWins(selectedGame)));
        winsColumn.setSortable(true);
        Label winsHeader = new Label("Wins ✦");
        winsColumn.setGraphic(winsHeader);
        winsHeader.setOnMouseClicked(e -> toggleSort(table, winsColumn));

        table.getColumns().addAll(rankColumn, playerColumn, eloColumn, winsColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    private void updateLeaderboardData(String game) {
        this.selectedGame = game;
        List<Player> players = logic.getAllPlayers(); // Ensure this returns the full unsorted list
        leaderboardTable.setItems(FXCollections.observableArrayList(players));

        // Default sort by ELO DESC
        for (TableColumn<Player, ?> col : leaderboardTable.getColumns()) {
            if (col.getText().contains("ELO")) {
                col.setSortType(TableColumn.SortType.DESCENDING);
                leaderboardTable.getSortOrder().clear();
                leaderboardTable.getSortOrder().add(col);
                break;
            }
        }
    }

    private <T> void toggleSort(TableView<Player> table, TableColumn<Player, T> column) {
        TableColumn.SortType current = column.getSortType();
        if (current == null || current == TableColumn.SortType.DESCENDING) {
            column.setSortType(TableColumn.SortType.ASCENDING);
        } else {
            column.setSortType(TableColumn.SortType.DESCENDING);
        }
        table.getSortOrder().clear();
        table.getSortOrder().add(column);
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
        returnButton.setOnAction(e -> UIUtils.returnToMainMenu(stage));
        return returnButton;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
