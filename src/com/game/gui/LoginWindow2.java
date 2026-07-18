package com.game.gui;

import com.game.auth.AuthManager;
import com.game.auth.DatabaseStub;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

/**
 * Login/Registration window for OMG Platform.
 * Handles user authentication and account creation flows.
 *
 * <p>Implements the following features from GUI specification:
 * <ul>
 *   <li>Login screen for existing users</li>
 *   <li>Registration screen for new users</li>
 *   <li>Password visibility toggle</li>
 *   <li>Form validation and error messaging</li>
 *   <li>Tab-based navigation between forms</li>
 * </ul>
 *
 * @see AuthManager For authentication business logic
 */
public class LoginWindow2 extends Application {
    private AuthManager authManager;
    private VBox loginContent;
    private VBox registerContent;

    /**
     * Main entry point for the JavaFX application.
     * Initializes the login/registration window and its components.
     *
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        authManager = new AuthManager(new DatabaseStub());

        // Main Window
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: black;");

        // Content box
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(20));
        contentBox.setStyle("-fx-background-color: #2C0016; -fx-background-radius: 10;");
        contentBox.setMaxWidth(800);
        contentBox.setMaxHeight(400);

        // Initialize and configure all UI components
        initializeLogo(contentBox);
        initializeTabs(contentBox, primaryStage);
        initializeFooter(contentBox);

        root.getChildren().add(contentBox);
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    /**
     * Initializes and configures the OMG platform logo.
     * Falls back to text title if image cannot be loaded.
     *
     * @param contentBox The parent container for the logo
     */
    private void initializeLogo(VBox contentBox) {
        ImageView logo = new ImageView();
        try {
            Image logoImage = new Image(getClass().getResourceAsStream("/OMG_Full_Logo.png"));
            logo.setImage(logoImage);
            logo.setPreserveRatio(true);
            logo.setFitWidth(200);  // Logo size
        } catch (Exception e) {
            System.err.println("Could not load logo image");
            // Fallback to text title if image missing
            Label fallbackTitle = new Label("OMG Platform");
            fallbackTitle.setFont(Font.font("Arial", 36));
            fallbackTitle.setTextFill(Color.WHITE);
            contentBox.getChildren().add(fallbackTitle);
        }

        Label subtitle = new Label("Out of This World Multiplayer Gaming");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.WHITE);
    }

    /**
     * Initializes the login/register tabs and their switching behavior.
     *
     * @param contentBox The parent container for the tabs
     */
    private void initializeTabs(VBox contentBox, Stage primaryStage) {
        // Tab selector
        HBox tabSelector = new HBox(30);
        tabSelector.setAlignment(Pos.CENTER);

        // Tab style
        String baseTabStyle = "-fx-background-color: transparent; -fx-font-size: 16; "
                + "-fx-text-fill: #a0a0a0; -fx-padding: 5 15;"; // Normal state (gray)
        String selectedTabStyle = "-fx-background-color: transparent; -fx-font-size: 16; "
                + "-fx-text-fill: white; -fx-padding: 5 15; -fx-font-weight: bold;"; // Selected state

        // Create tabs
        ToggleButton loginTab = new ToggleButton("Login");
        ToggleButton registerTab = new ToggleButton("Register");

        // Set initial styles (Login selected by default)
        loginTab.setStyle(selectedTabStyle);
        registerTab.setStyle(baseTabStyle);

        tabSelector.getChildren().addAll(loginTab, registerTab);

        // Form Container
        StackPane formContainer = new StackPane();
        formContainer.setPadding(new Insets(20));
        formContainer.setPadding(new Insets(15));

        // Initialize forms
        loginContent = createLoginForm(primaryStage);
        registerContent = createRegisterForm(primaryStage);
        registerContent.setVisible(false);

        formContainer.getChildren().addAll(loginContent, registerContent);

        // Tab switching
        loginTab.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                loginTab.setStyle(selectedTabStyle);
                registerTab.setStyle(baseTabStyle);
                loginContent.setVisible(true);
                registerContent.setVisible(false);
            }
        });

        registerTab.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                registerTab.setStyle(selectedTabStyle);
                loginTab.setStyle(baseTabStyle);
                loginContent.setVisible(false);
                registerContent.setVisible(true);
            }
        });
        contentBox.getChildren().addAll(tabSelector, formContainer);
    }

    /**
     * Initializes the footer with copyright information.
     *
     * @param contentBox The parent container for the footer
     */
    private void initializeFooter(VBox contentBox) {
        Label footer = new Label("© 2025 OMG Platform. All rights reserved.");
        footer.setFont(Font.font("Arial", 12));
        footer.setTextFill(Color.WHITE);
        contentBox.getChildren().add(footer);
    }

    /**
     * Creates the login form with username/email and password fields.
     * Implements validation and authentication flow.
     *
     * @param stage The parent stage for dialog positioning
     * @return Configured VBox containing all login form elements
     */
    private VBox createLoginForm(Stage stage) {
        VBox form = new VBox(10);
        form.setMaxWidth(300);
        form.setAlignment(Pos.TOP_LEFT);
        form.setSpacing(10);  // Reduced vertical spacing

        // Input field styling
        String inputStyle = "-fx-background-color: #453c3c; -fx-text-fill: white; -fx-padding: 8; "
                            + "-fx-border-color: #a0a0a0; -fx-border-radius: 3;";
        String labelStyle = "-fx-text-fill: white; -fx-font-size: 12; -fx-padding: 0 0 3 5;";

        // Username/Email field
        Label userLabel = new Label("Username/Email");
        userLabel.setStyle(labelStyle);
        TextField userField = new TextField();
        userField.setPromptText("Enter your username or email");
        userField.setStyle(inputStyle);
        userField.setMaxWidth(300);

        // Password field
        Label passLabel = new Label("Password");
        passLabel.setStyle(labelStyle);
        PasswordFieldWithToggle passField = new PasswordFieldWithToggle();

        // Remember me and forgot password
        HBox options = new HBox(20);
        options.setAlignment(Pos.CENTER_LEFT);
        // TODO: Implement session persistence logic when checked
        // TODO: Integrate with 2FA system to skip verification when checked

        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setTextFill(Color.WHITE);

        Button forgotPass = new Button("Forgot password?");
        forgotPass.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0a0a0;");
        forgotPass.setOnAction(e -> showForgotPasswordDialog());
        //TODO: Add forgot password functionality

        options.getChildren().addAll(rememberMe, forgotPass);

        // Login button
        Button loginBtn = new Button("LOGIN");
        HBox loginBtnContainer = new HBox(loginBtn);
        loginBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #403c3c; -fx-font-weight: bold; -fx-padding: 8 25;");
        loginBtnContainer.setAlignment(Pos.CENTER); // Center just the button
        loginBtnContainer.setPadding(new Insets(10, 0, 0, 0));
        loginBtn.setPrefWidth(200);

        // Login action with proper validation
        loginBtn.setOnAction(e -> {handleLogin(stage, userField, passField);});

        form.getChildren().addAll(
                userLabel, userField,
                passLabel, passField,
                options, loginBtnContainer
        );

        return form;
    }

    /**
     * Handles login form submission and authentication.
     *
     * @param stage The parent stage for dialog positioning
     * @param userField The username/email input field
     * @param passField The password input field
     */
    private void handleLogin(Stage stage, TextField userField, PasswordFieldWithToggle passField) {
        String username = userField.getText().trim();
        String password = passField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Please enter both username/email and password");
            return;
        }

        if (authManager.login(username, password)) {
            // TODO: Check if 2FA is required based on "Remember me" status
            launchMainMenu(stage);
        } else {
            // TODO: Implement login attempt tracking for lockout mechanism
            showAlert("Invalid credentials. Please check your username and password.");
        }
    }

    /**
     * Creates the registration form for new users.
     * Includes validation and terms acceptance flow.
     *
     * @param stage The parent stage for dialog positioning
     * @return Configured VBox containing all registration form elements
     */
    private VBox createRegisterForm(Stage stage) {
        VBox form = new VBox(10); // Same spacing as login form
        form.setMaxWidth(300);
        form.setAlignment(Pos.TOP_LEFT); // Changed to TOP_LEFT like login form
        form.setSpacing(10);

        // Use same styles as login form
        String inputStyle = "-fx-background-color: #453c3c; -fx-text-fill: white; -fx-padding: 8; "
                + "-fx-border-color: #a0a0a0; -fx-border-radius: 3;";
        String labelStyle = "-fx-text-fill: white; -fx-font-size: 12; -fx-padding: 0 0 3 5;"; // Same as login

        // Username field - left aligned
        Label userLabel = new Label("Username");
        userLabel.setStyle(labelStyle);
        TextField userField = new TextField();
        userField.setPromptText("Choose a username");
        userField.setStyle(inputStyle);
        userField.setMaxWidth(300);

        // Email field - left aligned
        Label emailLabel = new Label("Email");
        emailLabel.setStyle(labelStyle); // Removed duplicate style setting
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle(inputStyle);
        emailField.setMaxWidth(300);

        // Password field
        Label passLabel = new Label("Password");
        passLabel.setStyle(labelStyle);
        PasswordFieldWithToggle passField = new PasswordFieldWithToggle();

        // Confirm Password field
        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.setStyle(labelStyle);
        PasswordFieldWithToggle confirmField = new PasswordFieldWithToggle();

        // Terms checkbox -
        CheckBox termsCheck = new CheckBox("I agree to the Terms and Conditions");
        termsCheck.setStyle("-fx-text-fill: white; -fx-font-size: 12;");
        // TODO: Implement terms viewing functionality
        // TODO: Require scrolling to bottom before enabling acceptance

        // Register button
        Button registerBtn = new Button("CONTINUE");
        HBox registerBtnContainer = new HBox(registerBtn);
        registerBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #403c3c; "
                + "-fx-font-weight: bold; -fx-padding: 8 25;"); // Same as login
        registerBtnContainer.setAlignment(Pos.CENTER);
        registerBtnContainer.setPadding(new Insets(10, 0, 0, 0));
        registerBtn.setPrefWidth(300);
        registerBtn.setDisable(true); // Disabled until terms are accepted

        // Enable register button only when terms are accepted
        termsCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            registerBtn.setDisable(!newVal);
        });

        // Registration action with validation
        registerBtn.setOnAction(e -> handleRegistration(stage, userField, emailField, passField, confirmField));

        form.getChildren().addAll(
                userLabel, userField,
                emailLabel, emailField,
                passLabel, passField,
                confirmLabel, confirmField,
                termsCheck, registerBtnContainer
        );

        return form;
    }

    /**
     * Handles registration form submission and account creation.
     *
     * @param stage The parent stage
     * @param userField The username input
     * @param emailField The email input
     * @param passField The password input
     * @param confirmField The password confirmation input
     */
    private void handleRegistration(Stage stage, TextField userField, TextField emailField,
                                    PasswordFieldWithToggle passField, PasswordFieldWithToggle confirmField) {
        String username = userField.getText().trim();
        String email = emailField.getText().trim();
        String password = passField.getText().trim();
        String confirmPass = confirmField.getText().trim();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Please complete all required fields");
            return;
        }

        if (!password.equals(confirmPass)) {
            showAlert("Passwords do not match");
            return;
        }

        if (password.length() < 8) {
            showAlert("Password must be at least 8 characters");
            return;
        }

        if (authManager.register(username, password, email)) {
            showAlert("Registration successful! Please check your email for verification.");
            // TODO: Transition to 2FA verification screen
        } else {
            showAlert("Registration failed. Username or email may already be in use.");
        }
    }

    /**
     * Custom password field with visibility toggle functionality.
     * Shows/hides password text when eye icon is clicked.
     */
    public class PasswordFieldWithToggle extends StackPane {
        private PasswordField passwordField = new PasswordField();
        private TextField visibleField = new TextField();
        private Button toggleButton = new Button();
        private boolean isVisible = false;

        public PasswordFieldWithToggle() {
            // Configure fields
            passwordField = new PasswordField();
            passwordField.setStyle("-fx-background-color: #453c3c; -fx-text-fill: white; "
                    + "-fx-padding: 8; -fx-border-color: #a0a0a0; -fx-border-radius: 3;");
            passwordField.setPromptText("Enter your password");
            passwordField.setPrefWidth(300);

            visibleField = new TextField();
            visibleField.setStyle(passwordField.getStyle());
            visibleField.setPrefWidth(300);
            visibleField.setVisible(false);

            // Configure eye icon button
            toggleButton = new Button();
            ImageView eyeIcon = new ImageView("/login_registration_assets/Eye_Hide.png");
            eyeIcon.setFitWidth(16);
            eyeIcon.setFitHeight(16);
            toggleButton.setGraphic(eyeIcon);
            toggleButton.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            toggleButton.setCursor(Cursor.HAND);

            // Layout setup
            this.setAlignment(Pos.CENTER_LEFT);
            this.getChildren().addAll(passwordField, visibleField, toggleButton);

            // Perfectly position eye icon
            StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
            StackPane.setMargin(toggleButton, new Insets(0, 8, 0, 0)); // Right margin

            // Bind fields
            passwordField.textProperty().bindBidirectional(visibleField.textProperty());
            toggleButton.setOnAction(e -> toggleVisibility());
        }

        /**
         * Toggles between visible and hidden password states.
         */
        private void toggleVisibility() {
            boolean visible = !passwordField.isVisible();
            passwordField.setVisible(!visible);
            visibleField.setVisible(visible);

            ImageView icon = new ImageView(visible
                    ? "/login_registration_assets/Eye_Open.png"
                    : "/login_registration_assets/Eye_Hide.png");
            icon.setFitWidth(16);
            icon.setFitHeight(16);
            toggleButton.setGraphic(icon);
        }

        /**
         * Gets the current text content of the password field.
         * @return The entered password text
         */
        public String getText() {
            return isVisible ? visibleField.getText() : passwordField.getText();
        }
    }


    /**
     * Shows the forgot password dialog (placeholder implementation).
     */
    private void showForgotPasswordDialog() {
        // TODO: Implement complete password recovery flow
        // TODO: Should trigger email with reset link
        // TODO: Need lockout check before allowing reset
        new Alert(AlertType.INFORMATION, "Password reset feature is coming soon!").showAndWait();
    }

    /**
     * Displays an alert dialog with the given message.
     * @param message The message to display
     */
    private void showAlert(String message) {
        new Alert(AlertType.INFORMATION, message).showAndWait();
    }

    /**
     * Transitions to the main menu after successful authentication.
     * @param stage The current stage to transition from
     */
    private void launchMainMenu(Stage stage) {
        // TODO: Check if 2FA verification is needed before proceeding
        GameMenu_2 gameMenu = new GameMenu_2();
        Scene menuScene = gameMenu.createScene(stage);
        stage.setScene(menuScene);
        stage.setTitle("Board Game Hub");
        Main.StageManager.configureStage(stage);
    }
}