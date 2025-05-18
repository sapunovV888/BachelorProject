package project.bachelor.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import project.bachelor.DatabaseConnector;
import project.bachelor.models.CashDeskModel;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CashDeskController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private Button openCashDeskButton, closeCashDeskButton, toMenuButton;
    @FXML private TableView<CashDeskModel> cashTable;
    @FXML private TableColumn<CashDeskModel, String> colDate, colCashier;
    @FXML private TableColumn<CashDeskModel, Double> colRevenue;
    @FXML private TextField dateField, cashierField;

    private final ObservableList<CashDeskModel> cashList = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        setupTableColumns();
        loadCashData();

        // Встановлення сьогоднішньої дати та блокування поля
        dateField.setText(LocalDate.now().toString());
        dateField.setEditable(false);
    }

    private void setupTableColumns() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colCashier.setCellValueFactory(new PropertyValueFactory<>("cashierName"));
        colRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        cashTable.setItems(cashList);
    }

    private void loadCashData() {
        cashList.clear();

        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cash_register")) {

            while (rs.next()) {
                cashList.add(new CashDeskModel(
                        rs.getString("date"),
                        rs.getString("cashier_name"),
                        rs.getDouble("revenue")
                ));
            }

        } catch (SQLException e) {
            showAlert("Помилка завантаження даних: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onOpenCashDeskClicked() {
        String date = dateField.getText().trim();
        String cashier = cashierField.getText().trim();

        if (cashier.isEmpty()) {
            showAlert("Введіть ПІБ касира!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DatabaseConnector.connect()) {
            // Перевірка: чи вже відкрита каса з таким ПІБ на цю ж дату
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT * FROM cash_register WHERE date = ? AND cashier_name = ?"
            );
            checkStmt.setString(1, date);
            checkStmt.setString(2, cashier);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                showAlert("Цей касир вже відкривав касу на цю дату!", Alert.AlertType.WARNING);
                return;
            }

            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO cash_register (date, cashier_name, revenue, is_open) VALUES (?, ?, 0.0, TRUE)"
            );
            stmt.setString(1, date);
            stmt.setString(2, cashier);
            stmt.executeUpdate();

            showAlert("Касу відкрито!", Alert.AlertType.INFORMATION);
            loadCashData();
        } catch (SQLException e) {
            showAlert("Помилка відкриття каси: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }



    @FXML
    void onCloseCashDeskClicked() {
        String date = dateField.getText().trim();

        try (Connection conn = DatabaseConnector.connect()) {
            // Оновити is_open на FALSE
            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE cash_register SET is_open = FALSE WHERE date = ? AND is_open = TRUE"
            );
            updateStmt.setString(1, date);
            int affectedRows = updateStmt.executeUpdate();

            if (affectedRows > 0) {
                showAlert("Касу закрито!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Каса вже була закрита або не існує!", Alert.AlertType.WARNING);
            }

            loadCashData();

        } catch (SQLException e) {
            showAlert("Помилка при закритті каси: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onToMenuClicked(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/bachelor/main-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Головне меню");
            stage.show();
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Інформація");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
