package com.game.gui;

import com.game.auth.*;
import com.game.auth.session.SessionManager;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.Optional;

public class LoginWindow extends Application {

    private Scene menuScene;
    private AuthManager authManager;
    private SessionManager sessionManager;
    private VBox loginContent;
    private VBox registerContent;
    private Line movingBar;
    private TextField regEmailField;
    private TextField regUsernameField;
    private PasswordField regPasswordField;
    private double loginStartX;
    private double loginEndX;
    private double registerStartX;
    private double registerEndX;
    private Stage primaryStage;
    private PasswordField passwordField;

    // Eye icons for login
    private Image eyeOpenImage;
    private Image eyeHideImage;
    private ImageView eyeView;
    private boolean isPasswordVisible = false;

    // Eye icons for registration
    private Image regEyeOpenImage;
    private Image regEyeHideImage;
    private ImageView regEyeView;
    private boolean isRegPasswordVisible = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Ensures all parts of the application use the same AuthManager instance
        authManager = AuthManager.getInstance();
        sessionManager = SessionManager.getInstance();

        StackPane root = new StackPane();
        root.setAlignment(Pos.CENTER);

        Image backgroundImage = new Image(GameMenu.class.getResource("/Login_bg.png").toExternalForm());
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setSmooth(false);
        backgroundImageView.fitWidthProperty().bind(root.widthProperty());
        backgroundImageView.fitHeightProperty().bind(root.heightProperty());

        root.getChildren().add(backgroundImageView);

        VBox formPane = new VBox(20);
        formPane.setAlignment(Pos.CENTER);
        formPane.setPadding(new Insets(20));
        formPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 10;");

        Label title = new Label("OMG Platform");
        title.setFont(Font.font("Arial", 44));
        title.setTextFill(Color.WHITE);

        Label subTitle = new Label("Out of This World Multiplayer Gaming");
        subTitle.setFont(Font.font("Arial", 14));
        subTitle.setTextFill(Color.WHITE);

        Image GamePad = new Image(GameMenu.class.getResource("/OMG_logo.png").toExternalForm());
        ImageView GamePadImageView = new ImageView(GamePad);
        GamePadImageView.setPreserveRatio(true);
        GamePadImageView.setFitHeight(60);
        GamePadImageView.setFitWidth(60);

        HBox tabBar = new HBox(40);
        tabBar.setAlignment(Pos.CENTER);

        Label loginTab = new Label("Login");
        loginTab.setTextFill(Color.WHITE);
        loginTab.setFont(Font.font("Arial", 16));

        Label registerTab = new Label("Register");
        registerTab.setTextFill(Color.WHITE);
        registerTab.setFont(Font.font("Arial", 16));

        Line baseLine = new Line();
        baseLine.setStroke(Color.GRAY);
        baseLine.setStrokeWidth(3);

        movingBar = new Line();
        movingBar.setStroke(Color.WHITE);
        movingBar.setStrokeWidth(4);

        StackPane lineStack = new StackPane();
        lineStack.setAlignment(Pos.TOP_LEFT);
        lineStack.getChildren().addAll(baseLine, movingBar);

        VBox loginTabBox = new VBox(5);
        loginTabBox.setAlignment(Pos.CENTER);
        loginTabBox.getChildren().add(loginTab);

        VBox registerTabBox = new VBox(5);
        registerTabBox.setAlignment(Pos.CENTER);
        registerTabBox.getChildren().add(registerTab);

        VBox tabContainer = new VBox(5);
        tabContainer.setAlignment(Pos.CENTER);
        HBox tabLabels = new HBox(40);
        tabLabels.setAlignment(Pos.CENTER);
        tabLabels.getChildren().addAll(loginTabBox, registerTabBox);
        tabContainer.getChildren().addAll(tabLabels, lineStack);

        tabBar.getChildren().add(tabContainer);

        StackPane contentArea = new StackPane();
        contentArea.setAlignment(Pos.CENTER);

        // Login Section
        loginContent = new VBox(20);
        loginContent.setAlignment(Pos.CENTER);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setTextFill(Color.WHITE);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username or email");
        usernameField.setMaxWidth(400);
        usernameField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        usernameField.setAlignment(Pos.CENTER_LEFT);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setTextFill(Color.WHITE);
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setMaxWidth(400);
        passwordField.setPrefWidth(400);
        passwordField.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        eyeOpenImage = new Image(getClass().getResourceAsStream("/login_registration_assets/Eye_Open.png"));
        eyeHideImage = new Image(getClass().getResourceAsStream("/login_registration_assets/Eye_Hide.png"));
        eyeView = new ImageView(eyeOpenImage);
        eyeView.setFitHeight(20);
        eyeView.setFitWidth(20);
        Button togglePasswordButton = new Button();
        togglePasswordButton.setGraphic(eyeView);
        togglePasswordButton.setStyle("-fx-background-color: transparent;");

        HBox passwordBox = new HBox(5);
        passwordField.setAlignment(Pos.CENTER);
        passwordBox.getChildren().addAll(passwordField, togglePasswordButton);
        passwordBox.setPrefWidth(450);
        passwordBox.setMaxWidth(450);
        HBox.setHgrow(passwordField, Priority.ALWAYS);

        togglePasswordButton.setOnAction(e -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                TextField tempTextField = new TextField(passwordField.getText());
                tempTextField.setPromptText("Enter password");
                tempTextField.setMaxWidth(400);
                tempTextField.setPrefWidth(passwordField.getWidth());
                tempTextField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                passwordBox.getChildren().set(0, tempTextField);
                HBox.setHgrow(tempTextField, Priority.ALWAYS);
                eyeView.setImage(eyeHideImage);
            } else {
                PasswordField newPasswordField = new PasswordField();
                newPasswordField.setText(((TextField)passwordBox.getChildren().get(0)).getText());
                newPasswordField.setPromptText("Enter password");
                newPasswordField.setMaxWidth(400);
                newPasswordField.setPrefWidth(((TextField)passwordBox.getChildren().get(0)).getWidth());
                newPasswordField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                passwordBox.getChildren().set(0, newPasswordField);
                HBox.setHgrow(newPasswordField, Priority.ALWAYS);
                passwordField = newPasswordField;
                eyeView.setImage(eyeOpenImage);
            }
        });

        HBox optionsBox = new HBox(20);
        optionsBox.setAlignment(Pos.CENTER);

        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setTextFill(Color.WHITE);
        // Load initial state from config
        rememberMe.setSelected(Boolean.parseBoolean(ConfigManager.loadPreference("rememberMe", "false")));

        Button forgotPasswordButton = new Button("Forgot Password");
        forgotPasswordButton.setPrefSize(200, 40);
        forgotPasswordButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 15px;");
        forgotPasswordButton.setOnMouseEntered(e -> forgotPasswordButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-font-size: 25px;"));
        forgotPasswordButton.setOnMouseExited(e -> forgotPasswordButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 25px;"));

        optionsBox.getChildren().addAll(rememberMe, forgotPasswordButton);

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 25px;");
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-font-size: 25px;"));
        loginButton.setPrefSize(200, 40);
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 25px;"));

        Label errorMessage = new Label("");
        errorMessage.setTextFill(Color.RED);

        loginContent.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordBox, optionsBox, loginButton, errorMessage);

        // Registration Section (unchanged for brevity, same as original)
        registerContent = new VBox(20);
        registerContent.setAlignment(Pos.CENTER);

        Label regEmailLabel = new Label("Email:");
        regEmailLabel.setTextFill(Color.WHITE);
        regEmailField = new TextField();
        regEmailField.setPromptText("Enter email");
        regEmailField.setMaxWidth(400);
        regEmailField.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        Label regUsernameLabel = new Label("Username:");
        regUsernameLabel.setTextFill(Color.WHITE);
        regUsernameField = new TextField();
        regUsernameField.setPromptText("Enter username");
        regUsernameField.setMaxWidth(400);
        regUsernameField.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        Label regPasswordLabel = new Label("Password:");
        regPasswordLabel.setTextFill(Color.WHITE);
        regPasswordField = new PasswordField();
        regPasswordField.setPromptText("Enter password");
        regPasswordField.setMaxWidth(400);
        regPasswordField.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        regEyeOpenImage = new Image(getClass().getResourceAsStream("/login_registration_assets/Eye_Open.png"));
        regEyeHideImage = new Image(getClass().getResourceAsStream("/login_registration_assets/Eye_Hide.png"));
        regEyeView = new ImageView(regEyeOpenImage);
        regEyeView.setFitHeight(20);
        regEyeView.setFitWidth(20);
        Button regTogglePasswordButton = new Button();
        regTogglePasswordButton.setGraphic(regEyeView);
        regTogglePasswordButton.setStyle("-fx-background-color: transparent;");

        HBox regPasswordBox = new HBox(5);
        regPasswordBox.setAlignment(Pos.CENTER);
        regPasswordBox.getChildren().addAll(regPasswordField, regTogglePasswordButton);
        regPasswordBox.setPrefWidth(450);
        regPasswordBox.setMaxWidth(450);
        HBox.setHgrow(regPasswordField, Priority.ALWAYS);

        regTogglePasswordButton.setOnAction(e -> {
            isRegPasswordVisible = !isRegPasswordVisible;
            if (isRegPasswordVisible) {
                TextField tempTextField = new TextField(regPasswordField.getText());
                tempTextField.setPromptText("Enter password");
                tempTextField.setMaxWidth(400);
                tempTextField.setPrefWidth(regPasswordField.getWidth());
                tempTextField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                regPasswordBox.getChildren().set(0, tempTextField);
                HBox.setHgrow(tempTextField, Priority.ALWAYS);
                regEyeView.setImage(regEyeHideImage);
            } else {
                PasswordField newPasswordField = new PasswordField();
                newPasswordField.setText(((TextField)regPasswordBox.getChildren().get(0)).getText());
                newPasswordField.setPromptText("Enter password");
                newPasswordField.setMaxWidth(400);
                newPasswordField.setPrefWidth(((TextField)regPasswordBox.getChildren().get(0)).getWidth());
                newPasswordField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                regPasswordBox.getChildren().set(0, newPasswordField);
                HBox.setHgrow(newPasswordField, Priority.ALWAYS);
                regPasswordField = newPasswordField;
                regEyeView.setImage(regEyeOpenImage);
            }
        });

        Button createAccountButton = new Button("Create Account");
        createAccountButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 25px;");
        createAccountButton.setOnMouseEntered(e -> createAccountButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-font-size: 25px;"));
        createAccountButton.setPrefSize(200, 40);
        createAccountButton.setOnMouseExited(e -> createAccountButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 25px;"));

        registerContent.getChildren().addAll(regEmailLabel, regEmailField, regUsernameLabel, regUsernameField, regPasswordLabel, regPasswordBox, createAccountButton);

        contentArea.getChildren().addAll(loginContent, registerContent);
        registerContent.setVisible(false);

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().addAll(GamePadImageView, title);

        formPane.getChildren().addAll(titleBox, subTitle, tabBar, contentArea);
        root.getChildren().addAll(formPane);

        Scene loginScene = new Scene(root, 950, 620);
        primaryStage.setTitle("Login");
        primaryStage.setScene(loginScene);
        Main.StageManager.configureStage(primaryStage);
        primaryStage.show();

        loginTab.applyCss();
        loginTab.layout();
        registerTab.applyCss();
        registerTab.layout();

        double loginWidth = loginTab.getBoundsInParent().getWidth();
        double registerWidth = registerTab.getBoundsInParent().getWidth();

        loginStartX = loginTab.localToParent(loginTab.getBoundsInLocal().getMinX(), 0).getX();
        loginEndX = loginStartX + loginWidth;
        registerStartX = registerTab.localToParent(registerTab.getBoundsInLocal().getMinX(), 0).getX();
        registerEndX = registerStartX + registerWidth;

        final double barYPosition = 5;
        movingBar.setStartY(barYPosition);
        movingBar.setEndY(barYPosition);
        baseLine.setStartY(barYPosition);
        baseLine.setEndY(barYPosition);

        baseLine.setStartX(0);
        baseLine.setEndX(tabLabels.getWidth());

        movingBar.setStartX(loginStartX);
        movingBar.setEndX(loginEndX);

        loginTab.setOnMouseClicked(e -> {
            loginContent.setVisible(true);
            registerContent.setVisible(false);
            animateMovingBar(registerStartX, loginStartX, loginEndX);
        });

        registerTab.setOnMouseClicked(e -> {
            loginContent.setVisible(false);
            registerContent.setVisible(true);
            animateMovingBar(loginStartX, registerStartX, registerEndX);
        });

        createAccountButton.setOnAction(e -> {
            String email = regEmailField.getText();
            String username = regUsernameField.getText();
            String password = regPasswordField.getText();

            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                new Alert(AlertType.ERROR, "Please fill in all fields.").showAndWait();
            } else {
                boolean success = authManager.register(username, password, email);
                if (success) {
                    showOTPVerificationInterface(username, email, true);
                } else {
                    new Alert(AlertType.ERROR, "Registration failed. Username or email may already exist.").showAndWait();
                }
            }
        });

        loginButton.setOnAction(e -> {
            String id = usernameField.getText();
            String password = passwordField.getText();

            boolean loginSuccess = authManager.login(id, password);
            if (loginSuccess) {
                // Determines username after successful login
                User user = authManager.getDatabase().getUser(id);
                if (user == null) {
                    user = authManager.getDatabase().getUserByEmail(id);
                }
                String username = user != null ? user.getUsername() : id;

                boolean skipOTP = Boolean.parseBoolean(ConfigManager.loadPreference("skipOTP", "false"));
                String storedToken = null;

                if (skipOTP) {
                    try {
                        if (StoreToken.doesKeystoreExist()) {
                            storedToken = StoreToken.retrieveJWT();
                            if (!JWTTOKEN.validateToken(storedToken) ||
                                    !JWTTOKEN.getUsernameFromToken(storedToken).equals(username)) {
                                skipOTP = false;
                                System.out.println("Token validation failed or username mismatch");
                            }
                        } else {
                            skipOTP = false;
                        }
                    } catch (Exception ex) {
                        System.out.println("Failed to retrieve/validate token: " + ex.getMessage());
                        skipOTP = false;
                    }
                }

                boolean rememberMeSelected = rememberMe.isSelected();
                ConfigManager.savePreference("rememberMe", String.valueOf(rememberMeSelected));

                if (skipOTP) {
                    sessionManager.loginUser(username, storedToken);
                    proceedToGameMenu(username);
                } else {
                    String token = JWTTOKEN.generateToken(username);
                    if (rememberMeSelected) {
                        try {
                            StoreToken.storeJWT(token);
                            ConfigManager.savePreference("skipOTP", "true");
                        } catch (Exception ex) {
                            System.out.println("Failed to store JWT token: " + ex.getMessage());
                        }
                    }
                    showOTPVerificationInterface(username, user.getEmail(), false);
                }
            } else {
                errorMessage.setText("Invalid username, email, or password");
            }
        });

        forgotPasswordButton.setOnAction(e -> showForgotPasswordDialog(primaryStage));
    }

    private void animateMovingBar(double fromStartX, double toStartX, double toEndX) {
        Timeline timeline = new Timeline();
        if (movingBar.getStartX() == fromStartX) {
            KeyFrame frame1 = new KeyFrame(Duration.millis(150),
                    new KeyValue(movingBar.startXProperty(), toStartX));
            KeyFrame frame2 = new KeyFrame(Duration.millis(300),
                    new KeyValue(movingBar.endXProperty(), toEndX));
            timeline.getKeyFrames().addAll(frame1, frame2);
        } else {
            movingBar.setStartX(toStartX);
            movingBar.setEndX(toEndX);
        }
        timeline.play();
    }

    private void showOTPVerificationInterface(String username, String email, boolean isRegistration) {
        VBox currentTabContent = loginContent.isVisible() ? loginContent : registerContent;
        currentTabContent.getChildren().clear();

        Label otpTitle = new Label("Two-Factor Authentication");
        otpTitle.setTextFill(Color.WHITE);
        otpTitle.setFont(Font.font("Arial", 20));

        Label otpMessage = new Label("A verification code has been sent to your email.\nPlease enter it below.");
        otpMessage.setTextFill(Color.WHITE);
        otpMessage.setAlignment(Pos.CENTER);
        otpMessage.setStyle("-fx-background-color: #555; -fx-padding: 10; -fx-background-radius: 5;");

        HBox otpFields = new HBox(5);
        otpFields.setAlignment(Pos.CENTER);
        TextField[] otpDigits = new TextField[6];
        for (int i = 0; i < 6; i++) {
            otpDigits[i] = new TextField();
            otpDigits[i].setPrefWidth(40);
            otpDigits[i].setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-alignment: center;");
            final int index = i;
            otpDigits[i].textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue.length() > 1) {
                    otpDigits[index].setText(newValue.substring(0, 1));
                }
                if (newValue.length() == 1 && index < 5) {
                    otpDigits[index + 1].requestFocus();
                } else if (newValue.length() == 0 && index > 0) {
                    otpDigits[index - 1].requestFocus();
                }
            });
            otpDigits[i].setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.BACK_SPACE && otpDigits[index].getText().isEmpty() && index > 0) {
                    otpDigits[index - 1].requestFocus();
                }
            });
        }
        otpFields.getChildren().addAll(otpDigits);

        Button verifyButton = new Button("Verify & Complete " + (isRegistration ? "Registration" : "Login"));
        verifyButton.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-size: 16px;");
        verifyButton.setPrefSize(250, 40);
        verifyButton.setOnAction(e -> {
            StringBuilder enteredOtp = new StringBuilder();
            for (TextField digit : otpDigits) {
                enteredOtp.append(digit.getText());
            }
            String enteredOtpStr = enteredOtp.toString();

            boolean success;
            if (isRegistration) {
                success = authManager.verify2FARegistration(username, enteredOtpStr);
                if (success) {
                    new Alert(AlertType.INFORMATION, "Registration completed! Please sign in.").showAndWait();
                    loginContent.setVisible(true);
                    registerContent.setVisible(false);
                    animateMovingBar(registerStartX, loginStartX, loginEndX);
                    resetRegisterContent();
                } else {
                    new Alert(AlertType.ERROR, "Invalid OTP. Please try again.").showAndWait();
                }
            } else {
                success = authManager.verify2FALogin(username, enteredOtpStr);
                if (success) {
                    String token = JWTTOKEN.generateToken(username);
                    boolean rememberMeSelected = Boolean.parseBoolean(ConfigManager.loadPreference("rememberMe", "false"));
                    if (rememberMeSelected) {
                        try {
                            StoreToken.storeJWT(token);
                            ConfigManager.savePreference("skipOTP", "true");
                        } catch (Exception ex) {
                            System.out.println("Failed to store JWT token: " + ex.getMessage());
                        }
                    }
                    sessionManager.loginUser(username, token);
                    proceedToGameMenu(username);
                } else {
                    new Alert(AlertType.ERROR, "Invalid OTP. Please try again.").showAndWait();
                }
            }
        });

        Label resendLink = new Label("Didn't receive the code? Resend");
        resendLink.setTextFill(Color.WHITE);
        resendLink.setStyle("-fx-underline: true;");
        resendLink.setOnMouseClicked(e -> {
            if (isRegistration) {
                authManager.register(username, regPasswordField.getText(), email);
            } else {
                authManager.login(username, passwordField.getText());
            }
            new Alert(AlertType.INFORMATION, "OTP resent successfully!").showAndWait();
        });

        Label signInLink = new Label("Already have an account? Sign in");
        signInLink.setTextFill(Color.WHITE);
        signInLink.setStyle("-fx-underline: true;");
        signInLink.setOnMouseClicked(e -> {
            loginContent.setVisible(true);
            registerContent.setVisible(false);
            animateMovingBar(registerStartX, loginStartX, loginEndX);
        });

        currentTabContent.getChildren().addAll(otpTitle, otpMessage, otpFields, verifyButton, resendLink, signInLink);
    }

    private void resetRegisterContent() {
        registerContent.getChildren().clear();

        Label regEmailLabel = new Label("Email:");
        regEmailLabel.setTextFill(Color.WHITE);
        regEmailField.setText("");
        regEmailField.setPromptText("Enter email");
        regEmailField.setMaxWidth(400);
        regEmailField.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        Label regUsernameLabel = new Label("Username:");
        regUsernameLabel.setTextFill(Color.WHITE);
        regUsernameField.setText("");
        regUsernameField.setPromptText("Enter username");
        regUsernameField.setMaxWidth(400);
        regUsernameField.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        Label regPasswordLabel = new Label("Password:");
        regPasswordLabel.setTextFill(Color.WHITE);
        regPasswordField.setText("");
        regPasswordField.setPromptText("Enter password");
        regPasswordField.setMaxWidth(400);
        regPasswordField.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        regEyeOpenImage = new Image(getClass().getResourceAsStream("/login_registration_assets/Eye_Open.png"));
        regEyeHideImage = new Image(getClass().getResourceAsStream("/login_registration_assets/Eye_Hide.png"));
        regEyeView = new ImageView(regEyeOpenImage);
        regEyeView.setFitHeight(20);
        regEyeView.setFitWidth(20);
        Button regTogglePasswordButton = new Button();
        regTogglePasswordButton.setGraphic(regEyeView);
        regTogglePasswordButton.setStyle("-fx-background-color: transparent;");

        HBox regPasswordBox = new HBox(5);
        regPasswordBox.setAlignment(Pos.CENTER);
        regPasswordBox.getChildren().addAll(regPasswordField, regTogglePasswordButton);
        HBox.setHgrow(regPasswordField, Priority.ALWAYS);

        regTogglePasswordButton.setOnAction(e -> {
            isRegPasswordVisible = !isRegPasswordVisible;
            if (isRegPasswordVisible) {
                TextField tempTextField = new TextField(regPasswordField.getText());
                tempTextField.setPromptText("Enter password");
                tempTextField.setMaxWidth(400);
                tempTextField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                regPasswordBox.getChildren().set(0, tempTextField);
                regEyeView.setImage(regEyeHideImage);
            } else {
                PasswordField newPasswordField = new PasswordField();
                newPasswordField.setText(regPasswordField.getText());
                newPasswordField.setPromptText("Enter password");
                newPasswordField.setMaxWidth(400);
                newPasswordField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                regPasswordBox.getChildren().set(0, newPasswordField);
                regPasswordField = newPasswordField;
                regEyeView.setImage(regEyeOpenImage);
            }
        });

        Button createAccountButton = new Button("Create Account");
        createAccountButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 25px;");
        createAccountButton.setOnMouseEntered(e -> createAccountButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-font-size: 25px;"));
        createAccountButton.setPrefSize(200, 40);
        createAccountButton.setOnMouseExited(e -> createAccountButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 25px;"));

        registerContent.getChildren().addAll(regEmailLabel, regEmailField, regUsernameLabel, regUsernameField, regPasswordLabel, regPasswordBox, createAccountButton);

        createAccountButton.setOnAction(e -> {
            String email = regEmailField.getText();
            String username = regUsernameField.getText();
            String password = regPasswordField.getText();

            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                new Alert(AlertType.ERROR, "Please fill in all fields.").showAndWait();
            } else {
                boolean success = authManager.register(username, password, email);
                if (success) {
                    showOTPVerificationInterface(username, email, true);
                } else {
                    new Alert(AlertType.ERROR, "Registration failed. Username or email may already exist.").showAndWait();
                }
            }
        });
    }

    private void showForgotPasswordDialog(Stage ownerStage) {
        Dialog<String> dialog = new Dialog<>();
        dialog.initOwner(ownerStage);
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Enter your username or email to reset your password");

        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 10;");

        Label promptLabel = new Label("Username/Email:");
        promptLabel.setTextFill(Color.WHITE);
        TextField idField = new TextField();
        idField.setPromptText("Username or Email");
        idField.setMaxWidth(300);
        idField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        dialogContent.getChildren().addAll(promptLabel, idField);

        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        okButton.setOnMouseEntered(e -> okButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-font-size: 14px;"));
        okButton.setOnMouseExited(e -> okButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;"));
        okButton.setDisable(true);

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-font-size: 14px;"));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;"));

        idField.textProperty().addListener((obs, oldVal, newVal) ->
                okButton.setDisable(newVal.trim().isEmpty()));

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return idField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String id = result.get().toLowerCase();
            System.out.println("Attempting forgot password for ID: " + id); // Debug
            boolean initiated = authManager.forgetPassword(id);
            if (initiated) {
                showOTPVerificationForReset(ownerStage, id);
            } else {
                new Alert(AlertType.ERROR, "User not found. Please check your username or email.").showAndWait();
            }
        }
    }

    private void showOTPVerificationForReset(Stage ownerStage, String id) {
        Dialog<String> otpDialog = new Dialog<>();
        otpDialog.initOwner(ownerStage);
        otpDialog.setTitle("Verify OTP");
        otpDialog.setHeaderText("A verification code has been sent to your email.");

        VBox otpContent = new VBox(15);
        otpContent.setPadding(new Insets(20));
        otpContent.setAlignment(Pos.CENTER);
        otpContent.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 10;");

        Label instruction = new Label("Enter the 6-digit code sent to your email:");
        instruction.setTextFill(Color.WHITE);

        HBox otpFields = new HBox(5);
        otpFields.setAlignment(Pos.CENTER);
        TextField[] otpDigits = new TextField[6];
        for (int i = 0; i < 6; i++) {
            otpDigits[i] = new TextField();
            otpDigits[i].setPrefWidth(40);
            otpDigits[i].setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-alignment: center;"); // White field, black text
            final int index = i;
            otpDigits[i].textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.length() > 1) {
                    otpDigits[index].setText(newVal.substring(0, 1));
                }
                if (newVal.length() == 1 && index < 5) {
                    otpDigits[index + 1].requestFocus();
                }
            });
            otpDigits[i].setOnKeyTyped(e -> {
                if (!Character.isDigit(e.getCharacter().charAt(0))) {
                    e.consume();
                }
            });
        }
        otpFields.getChildren().addAll(otpDigits);

        Label resendLink = new Label("Didn't receive the code? Resend");
        resendLink.setTextFill(Color.WHITE);
        resendLink.setStyle("-fx-underline: true;");
        resendLink.setOnMouseClicked(e -> {
            authManager.resend2FA(id);
            new Alert(AlertType.INFORMATION, "OTP resent successfully!").showAndWait();
        });

        otpContent.getChildren().addAll(instruction, otpFields, resendLink);
        otpDialog.getDialogPane().setContent(otpContent);
        otpDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        otpDialog.getDialogPane().setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        Button okButton = (Button) otpDialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        okButton.setOnMouseEntered(e -> okButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-font-size: 14px;"));
        okButton.setOnMouseExited(e -> okButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;"));
        okButton.setDisable(true);

        Button cancelButton = (Button) otpDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-font-size: 14px;"));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;"));

        javafx.beans.value.ChangeListener<String> otpListener = (obs, oldVal, newVal) -> {
            boolean allFilled = Arrays.stream(otpDigits).allMatch(tf -> !tf.getText().isEmpty());
            okButton.setDisable(!allFilled);
        };
        for (TextField digit : otpDigits) {
            digit.textProperty().addListener(otpListener);
        }

        otpDialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                StringBuilder enteredOtp = new StringBuilder();
                for (TextField digit : otpDigits) {
                    enteredOtp.append(digit.getText());
                }
                return enteredOtp.toString();
            }
            return null;
        });

        Optional<String> otpResult = otpDialog.showAndWait();
        if (otpResult.isPresent()) {
            String enteredOtp = otpResult.get();
            boolean verified = authManager.verify2ForgetPassword(id, enteredOtp);
            if (verified) {
                showNewPasswordDialog(ownerStage, id);
            } else {
                new Alert(AlertType.ERROR, "Invalid OTP. Please try again.").showAndWait();
            }
        }
    }

    private void showNewPasswordDialog(Stage ownerStage, String id) {
        Dialog<String> passwordDialog = new Dialog<>();
        passwordDialog.initOwner(ownerStage);
        passwordDialog.setTitle("Reset Password");
        passwordDialog.setHeaderText("Enter your new password");

        VBox passwordContent = new VBox(15);
        passwordContent.setPadding(new Insets(20));
        passwordContent.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 10;");

        Label newPasswordLabel = new Label("New Password:");
        newPasswordLabel.setTextFill(Color.WHITE);
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        newPasswordField.setMaxWidth(300);
        newPasswordField.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.setTextFill(Color.WHITE);
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        confirmPasswordField.setMaxWidth(300);
        confirmPasswordField.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.RED);

        passwordContent.getChildren().addAll(
                newPasswordLabel, newPasswordField,
                confirmPasswordLabel, confirmPasswordField,
                errorLabel
        );

        passwordDialog.getDialogPane().setContent(passwordContent);
        passwordDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        passwordDialog.getDialogPane().setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        Button okButton = (Button) passwordDialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        okButton.setOnMouseEntered(e -> okButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-font-size: 14px;"));
        okButton.setOnMouseExited(e -> okButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;"));
        okButton.setDisable(true);

        Button cancelButton = (Button) passwordDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-font-size: 14px;"));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;"));

        ChangeListener<String> passwordListener = (obs, oldVal, newVal) -> {
            String newPass = newPasswordField.getText();
            String confirmPass = confirmPasswordField.getText();
            boolean isValid = !newPass.isEmpty() && newPass.equals(confirmPass) && authManager.passwordValidation(newPass);
            okButton.setDisable(!isValid);
            errorLabel.setText(isValid ? "" : "Passwords must match and meet requirements (6+ chars, upper, lower, digit)");
        };
        newPasswordField.textProperty().addListener(passwordListener);
        confirmPasswordField.textProperty().addListener(passwordListener);

        passwordDialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return newPasswordField.getText();
            }
            return null;
        });

        Optional<String> passwordResult = passwordDialog.showAndWait();
        if (passwordResult.isPresent()) {
            String newPassword = passwordResult.get();
            boolean resetSuccess = authManager.forgetPasswordReset(id, newPassword);
            if (resetSuccess) {
                new Alert(AlertType.INFORMATION, "Password reset successfully! Please log in with your new password.").showAndWait();
                loginContent.getChildren().stream()
                        .filter(node -> node instanceof TextField || node instanceof PasswordField)
                        .forEach(node -> {
                            if (node instanceof TextField) ((TextField) node).clear();
                            if (node instanceof PasswordField) ((PasswordField) node).clear();
                        });
                loginContent.setVisible(true);
                registerContent.setVisible(false);
                animateMovingBar(registerStartX, loginStartX, loginEndX);
            } else {
                new Alert(AlertType.ERROR, "Password reset failed. Ensure it's different from your old password.").showAndWait();
            }
        }
    }

    private void proceedToGameMenu(String username) {
        String menuPref = ConfigManager.loadMenuPreference();
        if ("GameMenu_2".equals(menuPref)) {
            GameMenu_2 gameMenu = new GameMenu_2();
            menuScene = gameMenu.createScene(primaryStage);
        } else {
            GameMenu gameMenu = new GameMenu();
            menuScene = gameMenu.createScene(primaryStage);
        }
        primaryStage.setScene(menuScene);
        Main.StageManager.showStage(primaryStage, menuScene);
        primaryStage.setTitle("Board Game Hub");
    }
}