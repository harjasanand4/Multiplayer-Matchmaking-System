package com.game.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class AboutUs extends Application {

    public static void open() {
        AboutUs aboutUs = new AboutUs();
        Stage stage = new Stage();
        try {
            aboutUs.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F5F5;");

        // Top bar
        root.setTop(UIUtils.createTopBar(stage));

        VBox content = new VBox(20);
        content.setPadding(new Insets(40, 80, 30, 80));
        content.setAlignment(Pos.TOP_LEFT);

        Label header = new Label("About Us");
        header.setFont(Font.font("Arial", 28));
        header.setStyle("-fx-font-weight: bold;");

        Label mission = new Label("Welcome to OMG Platform — an online multiplayer board game experience where strategy meets style. " +
                "Whether you're challenging a friend or climbing the ranked ladder, our platform brings classic games like Checkers, Connect Four, and Tic Tac Toe to life with sleek design and seamless gameplay.\n\n" +
                "Our mission is to create an inclusive, competitive, and visually engaging environment where casual players and hardcore strategists alike can enjoy timeless games — anywhere, anytime.");
        mission.setWrapText(true);
        mission.setFont(Font.font("Arial", 14));

        TitledPane gameLogicPane = createTeamPane("Game Logic Team", "Anthony", "Natasha", "Croy", "Oliver", "Victor");
        TitledPane guiPane = createTeamPane("GUI Team", "Anica", "Sam", "Supan", "Sourav", "Eric");
        TitledPane netPane = createTeamPane("Networking Team", "Jed", "JJ", "Mikeljan", "Kevin", "Akshin");
        TitledPane authPane = createTeamPane("Authentication & Profile Team", "Mahir", "Boya", "Michael", "Maneet", "Thomas");
        TitledPane lbPane = createTeamPane("Leaderboard & Matchmaking Team", "Himanshu", "Harjas", "Tanishk", "Surkhab", "Punar");
        TitledPane integrationPane = createTeamPane("Integration Team", "Anthony", "Supan", "Kevin", "Maneet", "Himanshu");

        VBox teamBox = new VBox(10, gameLogicPane, guiPane, netPane, authPane, lbPane, integrationPane);

        content.getChildren().addAll(header, mission, teamBox);
        root.setCenter(content);

        // Footer
        root.setBottom(UIUtils.createFooter());

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("About Us");
        stage.show();
    }

    private TitledPane createTeamPane(String teamName, String... members) {
        VBox memberBox = new VBox(5);
        for (String name : members) {
            Label label = new Label("• " + name);
            label.setFont(Font.font("Arial", 13));
            memberBox.getChildren().add(label);
        }
        TitledPane pane = new TitledPane(teamName, memberBox);
        pane.setExpanded(false);
        return pane;
    }
}
