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
import project.bachelor.models.SellModel;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SellController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button endButton;
    @FXML private Button toMenuButton;

    @FXML private Label menuLabel;
    @FXML private TableView<SellModel> productTable;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField productNameField;
    @FXML private TextField quantityField;
    @FXML private CheckBox showCartOnlyCheckBox;
    @FXML private Label totalLabel;

    private final ObservableList<SellModel> productList = FXCollections.observableArrayList();
    private final Map<String, Integer> categoryMap = new HashMap<>();

    @FXML
    void initialize() {
        assertElements();

        setupTableColumns();
        loadCategories();
        loadAllProducts();


        productTable.setOnMouseClicked(event -> {
            SellModel selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                productNameField.setText(selected.getName());
            }
        });

        addButton.setOnAction(event -> onAddClicked());
        deleteButton.setOnAction(event -> onDeleteClicked());
        endButton.setOnAction(event -> onEndClicked());
        toMenuButton.setOnAction(event -> onToMenuClicked(event));
        showCartOnlyCheckBox.setOnAction(event -> {
            if (showCartOnlyCheckBox.isSelected()) loadCartProducts();
            else loadAllProducts();
        });
        categoryComboBox.setOnAction(event -> loadAllProducts());
    }

    private void setupTableColumns() {
        productTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        productTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("category"));
        productTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));
        productTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("price"));
        productTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("inCart"));
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTable.setFixedCellSize(40);
    }

    private void loadCategories() {
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM categories")) {

            categoryComboBox.getItems().clear();
            categoryMap.clear();
            categoryComboBox.getItems().add("Усі категорії");

            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                categoryComboBox.getItems().add(name);
                categoryMap.put(name, id);
            }

            categoryComboBox.getSelectionModel().selectFirst();

        } catch (SQLException e) {
            showAlert("Помилка при завантаженні категорій: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAllProducts() {
        productList.clear();

        String selectedCategory = categoryComboBox.getValue();
        boolean filterByCategory = selectedCategory != null && !selectedCategory.equals("Усі категорії");

        String query = """
            SELECT p.id, c.name AS category, p.name, p.price, 
                COALESCE(ci.quantity, 0) AS inCart
            FROM products p
            JOIN categories c ON p.category_id = c.id
            LEFT JOIN cart_items ci ON p.id = ci.product_id
            """ + (filterByCategory ? "WHERE c.name = ?" : "");

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (filterByCategory) {
                stmt.setString(1, selectedCategory);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                productList.add(new SellModel(
                        rs.getInt("id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("inCart")
                ));
            }

            productTable.setItems(productList);
            updateTotalLabel();

        } catch (SQLException e) {
            showAlert("Помилка при завантаженні товарів: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private SellModel findProductByName(String name) {
        return productList.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
    }

    private void loadCartProducts() {
        productList.clear();

        String query = """
            SELECT p.id, c.name AS category, p.name, p.price, ci.quantity AS inCart
            FROM cart_items ci
            JOIN products p ON ci.product_id = p.id
            JOIN categories c ON p.category_id = c.id
        """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productList.add(new SellModel(
                        rs.getInt("id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("inCart")
                ));
            }

            productTable.setItems(productList);
            updateTotalLabel();

        } catch (SQLException e) {
            showAlert("Помилка при завантаженні кошика: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onAddClicked() {
        SellModel selected = productTable.getSelectionModel().getSelectedItem();

        if (selected == null && !productNameField.getText().isEmpty()) {
            selected = findProductByName(productNameField.getText());
        }

        if (selected == null) {
            showAlert("Товар не знайдено!", Alert.AlertType.WARNING);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Некоректна кількість!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DatabaseConnector.connect()) {
            // Отримуємо залишок на складі
            PreparedStatement stockStmt = conn.prepareStatement(
                    "SELECT stock_quantity FROM products WHERE id = ?"
            );
            stockStmt.setInt(1, selected.getId());
            ResultSet stockRs = stockStmt.executeQuery();

            if (stockRs.next()) {
                int available = stockRs.getInt("stock_quantity");

                // Перевіряємо, скільки вже є в кошику
                PreparedStatement cartStmt = conn.prepareStatement(
                        "SELECT quantity FROM cart_items WHERE product_id = ?"
                );
                cartStmt.setInt(1, selected.getId());
                ResultSet cartRs = cartStmt.executeQuery();

                int alreadyInCart = 0;
                if (cartRs.next()) {
                    alreadyInCart = cartRs.getInt("quantity");
                }

                if (quantity + alreadyInCart > available) {
                    showAlert("На складі залишилось лише " + available +
                            " од. товару \"" + selected.getName() + "\"!", Alert.AlertType.WARNING);
                    return;
                }

                // Додаємо або оновлюємо запис у кошику
                if (alreadyInCart > 0) {
                    PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE cart_items SET quantity = ? WHERE product_id = ?"
                    );
                    updateStmt.setInt(1, alreadyInCart + quantity);
                    updateStmt.setInt(2, selected.getId());
                    updateStmt.executeUpdate();
                } else {
                    PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO cart_items (product_id, quantity) VALUES (?, ?)"
                    );
                    insertStmt.setInt(1, selected.getId());
                    insertStmt.setInt(2, quantity);
                    insertStmt.executeUpdate();
                }

                showAlert("Товар додано до кошика!", Alert.AlertType.INFORMATION);
                loadAllProducts();
                productTable.refresh();
                updateTotalLabel();
            }

        } catch (SQLException e) {
            showAlert("Помилка при додаванні товару: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }



    @FXML
    private void onDeleteClicked() {
        SellModel selected = productTable.getSelectionModel().getSelectedItem();

        if (selected == null && !productNameField.getText().isEmpty()) {
            selected = findProductByName(productNameField.getText());
        }

        if (selected == null) {
            showAlert("Товар не знайдено!", Alert.AlertType.WARNING);
            return;
        }

        if (selected.getInCart() <= 0) {
            showAlert("Цього товару немає у кошику!", Alert.AlertType.INFORMATION);
            return;
        }

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM cart_items WHERE product_id = ?")
        ) {
            stmt.setInt(1, selected.getId());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Товар \"" + selected.getName() + "\" видалено з кошика!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Товар не був знайдений у кошику.", Alert.AlertType.WARNING);
            }

            loadAllProducts();
            updateTotalLabel();
            productNameField.clear();
            quantityField.clear();

        } catch (SQLException e) {
            showAlert("Помилка видалення: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void onEndClicked() {
        try (Connection conn = DatabaseConnector.connect()) {
            conn.setAutoCommit(false);

            // Списуємо товари зі складу
            PreparedStatement selectCart = conn.prepareStatement(
                    "SELECT product_id, quantity FROM cart_items"
            );
            ResultSet rs = selectCart.executeQuery();

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int cartQty = rs.getInt("quantity");

                PreparedStatement updateStock = conn.prepareStatement(
                        "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?"
                );
                updateStock.setInt(1, cartQty);
                updateStock.setInt(2, productId);
                updateStock.executeUpdate();
            }

            // Очищення кошика
            conn.prepareStatement("DELETE FROM cart_items").executeUpdate();
            conn.commit();

            // Очищення полів
            productNameField.clear();
            quantityField.clear();

            showAlert("Операцію завершено. Товари списано, кошик очищено.", Alert.AlertType.INFORMATION);
            loadAllProducts();
            updateTotalLabel();

        } catch (SQLException e) {
            showAlert("Помилка завершення операції: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void onToMenuClicked(javafx.event.ActionEvent event) {
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

    private void updateTotalLabel() {
        double total = productList.stream()
                .mapToDouble(p -> p.getPrice() * p.getInCart())
                .sum();
        totalLabel.setText(String.format("Сума: %.2f грн", total));
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Інформація");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void assertElements() {
        assert addButton != null;
        assert deleteButton != null;
        assert endButton != null;
        assert toMenuButton != null;
        assert menuLabel != null;
        assert productTable != null;
        assert categoryComboBox != null;
        assert productNameField != null;
        assert quantityField != null;
        assert showCartOnlyCheckBox != null;
    }
}
