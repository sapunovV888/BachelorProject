package project.bachelor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class ShopApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        try (Connection conn = DatabaseConnector.connect()) {
            if (conn != null && !conn.isClosed()) {
                showInfo("З'єднання з базою даних встановлено успішно.");
            }
        } catch (SQLException e) {
            showDatabaseError(e.getMessage());
            return; // Не запускаємо програму далі
        }

        FXMLLoader fxmlLoader = new FXMLLoader(ShopApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 1000);
        stage.setScene(scene);
        stage.show();
    }

    private void showDatabaseError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка підключення до бази");
        alert.setHeaderText("Неможливо з'єднатися з базою даних");
        alert.setContentText("Перевірте налаштування з'єднання.\n\nПовідомлення: " + errorMessage);
        alert.showAndWait();
    }

    // Alert у разі успішного з'єднання
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("База даних");
        alert.setHeaderText("Успішне підключення");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}