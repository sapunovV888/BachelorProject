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
import project.bachelor.models.WarehouseModel;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ReceiptController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button toMenuButton;

    @FXML private ComboBox<String> categoryField;
    @FXML private TextField nameField;
    @FXML private TextField numberField;
    @FXML private TextField priceField;
    @FXML private TextField marginField;

    @FXML private TableView<WarehouseModel> productTable;
    @FXML private TableColumn<WarehouseModel, Integer> colId;
    @FXML private TableColumn<WarehouseModel, String> colCategory;
    @FXML private TableColumn<WarehouseModel, String> colName;
    @FXML private TableColumn<WarehouseModel, Integer> colStock;
    @FXML private TableColumn<WarehouseModel, Double> colPrice;


    private final ObservableList<WarehouseModel> productList = FXCollections.observableArrayList();
    private final Map<String, Integer> categoryMap = new HashMap<>();

    @FXML
    void initialize() {
        setupTable();
        categoryField.setOnAction(e -> loadProducts());
        loadCategories();
        loadProducts();

        productTable.setOnMouseClicked(event -> {
            WarehouseModel selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                nameField.setText(selected.getName());
            }
        });

    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

    }

    private void loadCategories() {
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM categories")) {

            categoryMap.clear();
            categoryField.getItems().clear();
            categoryField.getItems().add("Усі категорії");

            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                categoryField.getItems().add(name);
                categoryMap.put(name, id);
            }

            categoryField.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            showAlert("Помилка завантаження категорій: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private void loadProducts() {
        productList.clear();

        String selectedCategory = categoryField.getValue();
        boolean filter = selectedCategory != null && !selectedCategory.equals("Усі категорії");

        String query = """
        SELECT p.id, c.name AS category, p.name, p.price, p.stock_quantity
        FROM products p
        JOIN categories c ON p.category_id = c.id
        """ + (filter ? "WHERE c.name = ?" : "");

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (filter) {
                stmt.setString(1, selectedCategory);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                productList.add(new WarehouseModel(
                        rs.getInt("id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        0
                ));
            }

            productTable.setItems(productList);
        } catch (SQLException e) {
            showAlert("Помилка завантаження товарів: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    @FXML
    void onAddClicked() {
        String name = nameField.getText().trim();
        String category = categoryField.getValue();

        if (name.isEmpty() || category == null) {
            showAlert("Заповніть назву та категорію!", Alert.AlertType.WARNING);
            return;
        }

        int quantity, margin;
        double price;
        try {
            quantity = Integer.parseInt(numberField.getText().trim());
            price = Double.parseDouble(priceField.getText().trim());
            margin = Integer.parseInt(marginField.getText().trim());

            if (quantity <= 0 || price < 0 || margin < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Перевірте правильність введених чисел!", Alert.AlertType.WARNING);
            return;
        }

        double finalPrice = price + (price * margin / 100.0);

        try (Connection conn = DatabaseConnector.connect()) {
            PreparedStatement check = conn.prepareStatement("SELECT id, stock_quantity FROM products WHERE name = ?");
            check.setString(1, name);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                int existing = rs.getInt("stock_quantity");
                PreparedStatement update = conn.prepareStatement("UPDATE products SET stock_quantity = ?, price = ? WHERE id = ?");
                update.setInt(1, existing + quantity);
                update.setDouble(2, finalPrice);
                update.setInt(3, id);
                update.executeUpdate();
            } else {
                int categoryId = categoryMap.get(category);
                PreparedStatement insert = conn.prepareStatement("INSERT INTO products (category_id, name, price, stock_quantity) VALUES (?, ?, ?, ?)");
                insert.setInt(1, categoryId);
                insert.setString(2, name);
                insert.setDouble(3, finalPrice);
                insert.setInt(4, quantity);
                insert.executeUpdate();
            }

            showAlert("Товар додано або оновлено успішно!", Alert.AlertType.INFORMATION);
            loadProducts();

        } catch (SQLException e) {
            showAlert("Помилка при оновленні бази: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onDeleteClicked() {
        String name = nameField.getText().trim();
        int quantity;
        try {
            quantity = Integer.parseInt(numberField.getText().trim());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Некоректна кількість!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DatabaseConnector.connect()) {
            PreparedStatement check = conn.prepareStatement("SELECT stock_quantity FROM products WHERE name = ?");
            check.setString(1, name);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                int stock = rs.getInt("stock_quantity");
                if (stock < quantity) {
                    showAlert("На складі лише " + stock + " од. товару.", Alert.AlertType.WARNING);
                    return;
                }

                PreparedStatement update = conn.prepareStatement("UPDATE products SET stock_quantity = ? WHERE name = ?");
                update.setInt(1, stock - quantity);
                update.setString(2, name);
                update.executeUpdate();

                showAlert("Товар успішно видалено зі складу!", Alert.AlertType.INFORMATION);
                loadProducts();

            } else {
                showAlert("Товар не знайдено!", Alert.AlertType.WARNING);
            }

        } catch (SQLException e) {
            showAlert("Помилка при видаленні: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onToMenuClicked(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/bachelor/main-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Головне меню");
            stage.setScene(new Scene(root));
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
