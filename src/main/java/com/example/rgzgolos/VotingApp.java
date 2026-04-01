package com.example.rgzgolos;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;


 //Основной класс JavaFX приложения для голосования с сохранением данных.

public class VotingApp extends Application {

    private List<Flower> flowers;
    private FlowPane flowerCardsPane;

    // Данные пользователей
    private Map<String, String> registeredUsers = new HashMap<>(); // username -> password
    private String loggedInUser = null;

    // Данные голосования
    private Map<String, Integer> flowerVoteCounts = new HashMap<>(); // flowerName -> count
    private Map<String, String> userVotes = new HashMap<>(); // username -> flowerNameVotedFor

    // UI элементы для обновления
    private Map<String, VBox> flowerButtonContainers = new HashMap<>();
    private Map<String, Label> flowerVoteCountLabels = new HashMap<>();
    private Label userInfoLabel;
    private Button loginButton, registerButton, logoutButton, statsButton, saveButton;
    private Stage primaryStage;

    // Имена файлов для сохранения данных
    private static final String USERS_FILE = "users.dat";
    private static final String VOTES_FILE = "votes.dat";
    private static final String DATA_SEPARATOR = "---DATA_SEPARATOR---";


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Голосование за лучший цветок НГТУ");

        // Загрузка данных перед инициализацией UI, зависящего от этих данных
        loadUsers();

        // Инициализация данных о цветах (определяет список доступных цветов)
        initializeFlowers();

        // Загрузка голосов (должна быть после initializeFlowers, чтобы знать о всех цветах)
        loadVotes();


        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        //Верхняя панель (Аутентификация и Заголовок)
        HBox topPanel = new HBox(10);
        topPanel.setAlignment(Pos.CENTER_LEFT);
        topPanel.setPadding(new Insets(10, 0, 10, 0));

        Label titleLabel = new Label("Голосование за лучший цветок НГТУ");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        userInfoLabel = new Label("Вы не вошли в систему.");
        loginButton = new Button("Войти");
        registerButton = new Button("Регистрация");
        logoutButton = new Button("Выйти");

        loginButton.setOnAction(e -> showLoginDialog());
        registerButton.setOnAction(e -> showRegisterDialog());
        logoutButton.setOnAction(e -> logoutUser());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topPanel.getChildren().addAll(titleLabel, spacer, userInfoLabel, loginButton, registerButton, logoutButton);
        mainLayout.setTop(topPanel);


        //Центральная панель (Карточки цветов)
        flowerCardsPane = new FlowPane();
        flowerCardsPane.setPadding(new Insets(10));
        flowerCardsPane.setHgap(20);
        flowerCardsPane.setVgap(20);
        flowerCardsPane.setAlignment(Pos.CENTER);

        for (Flower flower : flowers) {
            flowerCardsPane.getChildren().add(createFlowerCard(flower, primaryStage));
        }

        ScrollPane scrollPane = new ScrollPane(flowerCardsPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        mainLayout.setCenter(scrollPane);

        //Нижняя панель (Статистика и Сохранение)
        HBox bottomPanel = new HBox(10);
        bottomPanel.setAlignment(Pos.CENTER_RIGHT);
        bottomPanel.setPadding(new Insets(10, 0, 0, 0));
        statsButton = new Button("Показать статистику");
        saveButton = new Button("Сохранить результаты (в TXT)");

        statsButton.setOnAction(e -> showStatisticsWindow());
        saveButton.setOnAction(e -> saveResultsToTxtFile()); // Переименовал для ясности

        bottomPanel.getChildren().addAll(statsButton, saveButton);
        mainLayout.setBottom(bottomPanel);

        updateAuthStateUI(); // Обновляем UI в зависимости от состояния аутентификации
        updateAllButtonAndVoteStates(); // Первоначальное обновление счетчиков и кнопок

        Scene scene = new Scene(mainLayout, 950, 750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        saveUsers();
        saveVotes();
        super.stop();
        System.out.println("Приложение остановлено, данные сохранены.");
    }

    private void initializeFlowers() {
        flowers = new ArrayList<>();
        flowers.add(new Flower("Герань", "Герань.jpg", "Неприхотливое растение с яркими соцветиями, часто используется для украшения балконов и садов."));
        flowers.add(new Flower("Циния", "Циния.jpg", "Разноцветная циния с крупными, похожими на астры, цветами. Цветет обильно все лето."));
        flowers.add(new Flower("Бархатцы", "Бархатцы.jpg", "Яркие оранжевые или желтые цветы с характерным ароматом. Считаются отпугивающими вредителей."));
        flowers.add(new Flower("Гиацинт", "Гиацинт.jpg", "Весенний цветок с плотными ароматными соцветиями разнообразных оттенков."));
        flowers.add(new Flower("Альстромерия", "Альстромерия.jpg", "Экзотический цветок с пятнистыми лепестками, долго сохраняет свежесть в срезке."));
        flowers.add(new Flower("Люпин", "Люпин.jpg", "Высокое растение с эффектными колосовидными соцветиями синих, фиолетовых, розовых и белых оттенков."));

        // Инициализация счетчиков голосов нулями для всех определенных цветов.
        // Если данные загрузятся из файла, они перезапишут эти нули.
        for (Flower flower : flowers) {
            flowerVoteCounts.putIfAbsent(flower.getName(), 0);
        }
    }

    private VBox createFlowerCard(Flower flower, Stage ownerStage) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 8px; " +
                "-fx-background-color: #ffffff; -fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.3, 2, 2);");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(220);
        card.setMinWidth(200);

        Node imageNode;
        try {
            String fullImagePath = "/images/" + flower.getImagePath();
            InputStream imageStream = getClass().getResourceAsStream(fullImagePath);
            if (imageStream == null) throw new FileNotFoundException("Ресурс: " + fullImagePath);
            Image image = new Image(imageStream);
            if (image.isError()) throw new Exception("Ошибка загрузки: " + flower.getImagePath());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(160);
            imageView.setFitHeight(160);
            imageView.setPreserveRatio(true);
            imageView.setOnMouseClicked(e -> showFlowerDetails(flower, ownerStage));
            imageNode = imageView;
        } catch (Exception e) {
            System.err.println("Не удалось загрузить: " + flower.getImagePath() + ". Ошибка: " + e.getMessage());
            Rectangle placeholderRect = new Rectangle(160, 160, Color.LIGHTSLATEGRAY);
            placeholderRect.setArcWidth(10); placeholderRect.setArcHeight(10);
            Label placeholderText = new Label("Фото\n" + flower.getName());
            placeholderText.setTextAlignment(TextAlignment.CENTER);
            placeholderText.setTextFill(Color.WHITE);
            StackPane placeholderPane = new StackPane(placeholderRect, placeholderText);
            placeholderPane.setOnMouseClicked(event -> showFlowerDetails(flower, ownerStage));
            imageNode = placeholderPane;
        }
        card.getChildren().add(imageNode);

        Label nameLabel = new Label(flower.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setWrapText(true);

        Label votesLabel = new Label("Голосов: " + flowerVoteCounts.getOrDefault(flower.getName(), 0));
        votesLabel.setFont(Font.font("Arial", 12));
        flowerVoteCountLabels.put(flower.getName(), votesLabel);

        VBox buttonContainer = new VBox(5);
        buttonContainer.setAlignment(Pos.CENTER);
        Button voteButton = new Button("Проголосовать");
        voteButton.setPrefWidth(150);
        Button cancelVoteButton = new Button("Отменить голос");
        cancelVoteButton.setPrefWidth(150);

        flowerButtonContainers.put(flower.getName(), buttonContainer);

        voteButton.setOnAction(e -> handleVote(flower.getName()));
        cancelVoteButton.setOnAction(e -> handleCancelVote(flower.getName()));

        buttonContainer.getChildren().addAll(voteButton, cancelVoteButton);
        card.getChildren().addAll(nameLabel, votesLabel, buttonContainer);
        // updateButtonStatesForCard(flower.getName()); // Будет вызвано в updateAllButtonAndVoteStates
        return card;
    }

    private void handleVote(String flowerName) {
        if (loggedInUser == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Пожалуйста, войдите или зарегистрируйтесь, чтобы голосовать.");
            return;
        }

        String previouslyVotedFlower = userVotes.get(loggedInUser);

        if (previouslyVotedFlower != null && !previouslyVotedFlower.equals(flowerName)) {
            flowerVoteCounts.computeIfPresent(previouslyVotedFlower, (k, v) -> v > 0 ? v - 1 : 0);
        }

        if (previouslyVotedFlower == null || !previouslyVotedFlower.equals(flowerName)) {
            flowerVoteCounts.compute(flowerName, (k, v) -> (v == null) ? 1 : v + 1);
        }

        userVotes.put(loggedInUser, flowerName);
        updateAllButtonAndVoteStates();
        // Немедленное сохранение для большей надежности (опционально, но рекомендуется)
        saveVotes();
        System.out.println(loggedInUser + " проголосовал за: " + flowerName);
    }

    private void handleCancelVote(String flowerName) {
        if (loggedInUser == null || !flowerName.equals(userVotes.get(loggedInUser))) {
            return;
        }
        flowerVoteCounts.computeIfPresent(flowerName, (k, v) -> v > 0 ? v - 1 : 0);
        userVotes.remove(loggedInUser);
        updateAllButtonAndVoteStates();
        // Немедленное сохранение
        saveVotes();
        System.out.println(loggedInUser + " отменил голос за: " + flowerName);
    }

    private void updateAllButtonAndVoteStates() {
        flowers.forEach(f -> {
            updateButtonStatesForCard(f.getName());
            Label countLabel = flowerVoteCountLabels.get(f.getName());
            if (countLabel != null) {
                countLabel.setText("Голосов: " + flowerVoteCounts.getOrDefault(f.getName(), 0));
            }
        });
    }

    private void updateButtonStatesForCard(String flowerName) {
        VBox btnContainer = flowerButtonContainers.get(flowerName);
        if (btnContainer == null || btnContainer.getChildren().size() < 2) return;

        Button voteBtn = (Button) btnContainer.getChildren().get(0);
        Button cancelBtn = (Button) btnContainer.getChildren().get(1);

        boolean isLoggedIn = loggedInUser != null;
        String currentVoteForUser = isLoggedIn ? userVotes.get(loggedInUser) : null;

        voteBtn.setDisable(!isLoggedIn);

        if (isLoggedIn && flowerName.equals(currentVoteForUser)) {
            voteBtn.setText("Голос отдан!");
            voteBtn.setStyle("-fx-font-size: 14px; -fx-base: #9E9E9E; -fx-text-fill: white;");
            voteBtn.setDisable(true);
            cancelBtn.setVisible(true);
            cancelBtn.setManaged(true);
            cancelBtn.setStyle("-fx-font-size: 14px; -fx-base: #f44336; -fx-text-fill: white;");
        } else {
            voteBtn.setText("Проголосовать");
            voteBtn.setStyle("-fx-font-size: 14px; -fx-base: #4CAF50; -fx-text-fill: white;");
            if(isLoggedIn) voteBtn.setDisable(false);
            cancelBtn.setVisible(false);
            cancelBtn.setManaged(false);
        }
    }

    private void showLoginDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Вход в систему");
        dialog.setHeaderText("Введите ваши имя пользователя и пароль.");

        ButtonType loginButtonType = new ButtonType("Войти", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Имя пользователя");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");

        grid.add(new Label("Имя пользователя:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Пароль:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        usernameField.requestFocus();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(credentials -> {
            if (registeredUsers.containsKey(credentials.getKey()) &&
                    registeredUsers.get(credentials.getKey()).equals(credentials.getValue())) { // ПРОВЕРКА ПАРОЛЯ (НЕБЕЗОПАСНО)
                loggedInUser = credentials.getKey();
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Вы успешно вошли как " + loggedInUser);
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка входа", "Неверное имя пользователя или пароль.");
            }
            updateAuthStateUI();
            updateAllButtonAndVoteStates();
        });
    }

    private void showRegisterDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Регистрация");
        dialog.setHeaderText("Создайте новую учетную запись.");
        ButtonType registerButtonType = new ButtonType("Зарегистрироваться", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Имя пользователя");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");

        grid.add(new Label("Имя пользователя:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Пароль:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        usernameField.requestFocus();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(credentials -> {
            String username = credentials.getKey();
            String password = credentials.getValue(); // ПАРОЛЬ В ОТКРЫТОМ ВИДЕ
            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка регистрации", "Имя пользователя и пароль не могут быть пустыми.");
                return;
            }
            if (registeredUsers.containsKey(username)) {
                showAlert(Alert.AlertType.ERROR, "Ошибка регистрации", "Пользователь с таким именем уже существует.");
            } else {
                registeredUsers.put(username, password);
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Пользователь " + username + " успешно зарегистрирован. Теперь вы можете войти.");
                // Немедленное сохранение пользователей после регистрации
                saveUsers();
            }
        });
    }

    private void logoutUser() {
        if (loggedInUser != null) {
            System.out.println("Пользователь " + loggedInUser + " вышел из системы.");
            loggedInUser = null;
            updateAuthStateUI();
            updateAllButtonAndVoteStates();
        }
    }

    private void updateAuthStateUI() {
        boolean isLoggedIn = loggedInUser != null;
        loginButton.setVisible(!isLoggedIn);
        loginButton.setManaged(!isLoggedIn);
        registerButton.setVisible(!isLoggedIn);
        registerButton.setManaged(!isLoggedIn);
        logoutButton.setVisible(isLoggedIn);
        logoutButton.setManaged(isLoggedIn);
        statsButton.setDisable(flowers.isEmpty());
        saveButton.setDisable(flowers.isEmpty());

        if (isLoggedIn) {
            userInfoLabel.setText("Привет, " + loggedInUser + "!");
        } else {
            userInfoLabel.setText("Вы не вошли в систему.");
        }
        updateAllButtonAndVoteStates();
    }

    private void showStatisticsWindow() {
        Stage statsStage = new Stage();
        statsStage.initModality(Modality.APPLICATION_MODAL);
        statsStage.initOwner(primaryStage); // Связываем с основным окном
        statsStage.setTitle("Статистика голосования");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Цветок");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Количество голосов");
        yAxis.setTickUnit(1); // Шаг оси Y = 1
        yAxis.setMinorTickCount(0); // Убираем промежуточные деления
        yAxis.setForceZeroInRange(true); // Начинать ось Y с нуля

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Результаты голосования");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        // series.setName("Голоса"); // Можно убрать, если только одна серия

        ObservableList<XYChart.Data<String, Number>> chartData = FXCollections.observableArrayList();
        for (Flower flower : flowers) { // Используем порядок из flowers для консистентности
            chartData.add(new XYChart.Data<>(flower.getName(), flowerVoteCounts.getOrDefault(flower.getName(), 0)));
        }
        series.setData(chartData);
        barChart.getData().add(series);
        barChart.setLegendVisible(false);


        VBox layout = new VBox(barChart);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 800, 600);
        statsStage.setScene(scene);
        statsStage.show();
    }

    private void saveResultsToTxtFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить результаты голосования в TXT");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовый файл (*.txt)", "*.txt"));
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) { // FileWriter для корректной кодировки
                writer.println("Результаты голосования за лучший цветок НГТУ:");
                writer.println("============================================");
                for (Flower flower : flowers) {
                    writer.println(flower.getName() + ": " + flowerVoteCounts.getOrDefault(flower.getName(), 0) + " голосов");
                }
                writer.println("============================================");
                if (loggedInUser != null) {
                    writer.println("\nТекущий пользователь: " + loggedInUser);
                    String votedFor = userVotes.get(loggedInUser);
                    if (votedFor != null) {
                        writer.println("Ваш голос отдан за: " + votedFor);
                    } else {
                        writer.println("Вы еще не проголосовали.");
                    }
                } else {
                    writer.println("\nПользователь не вошел в систему.");
                }
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Результаты успешно сохранены в файл: " + file.getAbsolutePath());
            } catch (IOException e) { // IOException вместо FileNotFoundException для FileWriter
                showAlert(Alert.AlertType.ERROR, "Ошибка сохранения", "Не удалось сохранить файл: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showFlowerDetails(Flower flower, Stage ownerStage) {
        Stage detailStage = new Stage();
        detailStage.initModality(Modality.WINDOW_MODAL);
        detailStage.initOwner(ownerStage);
        detailStage.setTitle("Детали: " + flower.getName());

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f4f4f4;");

        Node largeImageNode;
        try {
            String fullImagePath = "/images/" + flower.getImagePath();
            InputStream imageStream = getClass().getResourceAsStream(fullImagePath);
            if (imageStream == null) throw new FileNotFoundException("Ресурс: " + fullImagePath);
            Image image = new Image(imageStream);
            if (image.isError()) throw new Exception("Ошибка загрузки: " + flower.getImagePath());
            ImageView largeImageView = new ImageView(image);
            largeImageView.setFitWidth(350);
            largeImageView.setFitHeight(350);
            largeImageView.setPreserveRatio(true);
            largeImageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0.5, 0, 0);");
            largeImageNode = largeImageView;
        } catch (Exception e) {
            System.err.println("Детальное изображение: " + flower.getImagePath() + ". Ошибка: " + e.getMessage());
            Rectangle placeholderRect = new Rectangle(350, 350, Color.DARKGRAY);
            placeholderRect.setArcWidth(15); placeholderRect.setArcHeight(15);
            Label placeholderText = new Label("Фото не найдено\n" + flower.getName());
            placeholderText.setTextAlignment(TextAlignment.CENTER);
            placeholderText.setTextFill(Color.WHITE);
            placeholderText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            StackPane placeholderPane = new StackPane(placeholderRect, placeholderText);
            largeImageNode = placeholderPane;
        }
        layout.getChildren().add(largeImageNode);

        Label nameLabel = new Label(flower.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        nameLabel.setTextAlignment(TextAlignment.CENTER);

        Label descriptionLabel = new Label(flower.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setTextAlignment(TextAlignment.JUSTIFY);
        descriptionLabel.setMaxWidth(350);
        descriptionLabel.setFont(Font.font("Arial", 14));

        layout.getChildren().addAll(nameLabel, descriptionLabel);

        ScrollPane detailScrollPane = new ScrollPane(layout);
        detailScrollPane.setFitToWidth(true);

        Scene detailScene = new Scene(detailScrollPane, 450, 600);
        detailStage.setScene(detailScene);
        detailStage.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage); // Указываем родительское окно для Alert
        alert.showAndWait();
    }

    // Методы сохранения и загрузки данных

    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            System.out.println("Файл пользователей " + USERS_FILE + " не найден, загрузка пропущена.");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                if (tokenizer.countTokens() == 2) {
                    String username = tokenizer.nextToken();
                    String password = tokenizer.nextToken(); // ПАРОЛЬ В ОТКРЫТОМ ВИДЕ
                    registeredUsers.put(username, password);
                }
            }
            System.out.println("Данные пользователей загружены из " + USERS_FILE);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки данных пользователей: " + e.getMessage());
            // showAlert(Alert.AlertType.ERROR, "Ошибка загрузки", "Не удалось загрузить данные пользователей.");
        }
    }

    private void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, String> entry : registeredUsers.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
            System.out.println("Данные пользователей сохранены в " + USERS_FILE);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения данных пользователей: " + e.getMessage());
            // showAlert(Alert.AlertType.ERROR, "Ошибка сохранения", "Не удалось сохранить данные пользователей.");
        }
    }

    private void loadVotes() {
        File file = new File(VOTES_FILE);
        if (!file.exists()) {
            System.out.println("Файл голосования " + VOTES_FILE + " не найден, загрузка пропущена.");
            // Убедимся, что для всех определенных цветов есть запись в flowerVoteCounts (обычно 0)
            for (Flower flower : flowers) {
                flowerVoteCounts.putIfAbsent(flower.getName(), 0);
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean readingCounts = false;
            boolean readingUserVotes = false;

            while ((line = reader.readLine()) != null) {
                if (line.equals("[COUNTS]")) {
                    readingCounts = true;
                    readingUserVotes = false;
                    continue;
                } else if (line.equals("[USER_VOTES]")) {
                    readingCounts = false;
                    readingUserVotes = true;
                    continue;
                }

                if (readingCounts) {
                    StringTokenizer tokenizer = new StringTokenizer(line, ":");
                    if (tokenizer.countTokens() == 2) {
                        String flowerName = tokenizer.nextToken();
                        try {
                            int count = Integer.parseInt(tokenizer.nextToken());
                            // Загружаем голоса только для тех цветов, которые определены в initializeFlowers
                            if (flowers.stream().anyMatch(f -> f.getName().equals(flowerName))) {
                                flowerVoteCounts.put(flowerName, count);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Ошибка формата числа голосов для " + flowerName + ": " + line);
                        }
                    }
                } else if (readingUserVotes) {
                    StringTokenizer tokenizer = new StringTokenizer(line, ":");
                    if (tokenizer.countTokens() == 2) {
                        String username = tokenizer.nextToken();
                        String flowerName = tokenizer.nextToken();
                        // Загружаем голос пользователя только если такой пользователь и цветок существуют
                        if (registeredUsers.containsKey(username) && flowers.stream().anyMatch(f -> f.getName().equals(flowerName))) {
                            userVotes.put(username, flowerName);
                        }
                    }
                }
            }
            // Убедимся, что для всех определенных цветов есть запись в flowerVoteCounts после загрузки
            for (Flower flower : flowers) {
                flowerVoteCounts.putIfAbsent(flower.getName(), 0);
            }
            System.out.println("Данные голосования загружены из " + VOTES_FILE);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки данных голосования: " + e.getMessage());
        }
    }

    private void saveVotes() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(VOTES_FILE))) {
            writer.println("[COUNTS]");
            for (Map.Entry<String, Integer> entry : flowerVoteCounts.entrySet()) {
                // Сохраняем голоса только для тех цветов, которые определены в initializeFlowers
                if (flowers.stream().anyMatch(f -> f.getName().equals(entry.getKey()))) {
                    writer.println(entry.getKey() + ":" + entry.getValue());
                }
            }
            writer.println("[USER_VOTES]");
            for (Map.Entry<String, String> entry : userVotes.entrySet()) {
                // Сохраняем голос пользователя только если такой пользователь и цветок существуют
                if (registeredUsers.containsKey(entry.getKey()) && flowers.stream().anyMatch(f -> f.getName().equals(entry.getValue()))) {
                    writer.println(entry.getKey() + ":" + entry.getValue());
                }
            }
            System.out.println("Данные голосования сохранены в " + VOTES_FILE);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения данных голосования: " + e.getMessage());
        }
    }

    // Вспомогательный класс Pair
    private static class Pair<K, V> {
        private K key;
        private V value;
        public Pair(K key, V value) { this.key = key; this.value = value; }
        public K getKey() { return key; }
        public V getValue() { return value; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
