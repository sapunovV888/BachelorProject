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
import project.bachelor.models.ProductsModel;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ProductsController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private TableView<ProductsModel> productTable;
    @FXML private TableColumn<ProductsModel, Integer> colId;
    @FXML private TableColumn<ProductsModel, String> colCategory;
    @FXML private TableColumn<ProductsModel, String> colName;
    @FXML private TableColumn<ProductsModel, Integer> colStock;
    @FXML private TableColumn<ProductsModel, Double> colPrice;

    @FXML private TextField categoryField, nameField, priceField;
    @FXML private ComboBox<String> categoryComboBox;

    @FXML private Button addButton, deleteButton, editButton, toMenuButton;
    @FXML private RadioButton categoryRadio, productRadio;

    private final ObservableList<ProductsModel> productList = FXCollections.observableArrayList();
    private final Map<String, Integer> categoryMap = new HashMap<>();

    @FXML
    void initialize() {
        ToggleGroup toggleGroup = new ToggleGroup();
        categoryRadio.setToggleGroup(toggleGroup);
        productRadio.setToggleGroup(toggleGroup);
        productRadio.setSelected(true);

        setupTableColumns();
        loadCategories();
        loadProducts();
        adjustFieldAccessibility();

        toggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> adjustFieldAccessibility());

        productTable.setOnMouseClicked(event -> {
            ProductsModel selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                nameField.setText(selected.getName());
                priceField.setText(String.valueOf(selected.getPrice()));
              //  categoryComboBox.getSelectionModel().select("Усі категорії");
            }
        });

        categoryComboBox.setOnAction(e -> loadProducts());
    }

    private void adjustFieldAccessibility() {
        boolean isCategory = categoryRadio.isSelected();
        categoryField.setDisable(!isCategory);
        categoryComboBox.setDisable(false);
        nameField.setDisable(isCategory);
        priceField.setDisable(isCategory);
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        productTable.setItems(productList);
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
            showAlert("Помилка завантаження категорій: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadProducts() {
        productList.clear();
        String selectedCategory = categoryComboBox.getValue();
        boolean filter = selectedCategory != null && !selectedCategory.equals("Усі категорії");

        String query = """
        SELECT p.id, c.name AS category, p.name, p.price, p.stock_quantity
        FROM products p
        JOIN categories c ON p.category_id = c.id
        """ + (filter ? "WHERE c.name = ?" : "");

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (filter) stmt.setString(1, selectedCategory);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                productList.add(new ProductsModel(
                        rs.getInt("id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getInt("stock_quantity"),
                        rs.getDouble("price")
                ));
            }

        } catch (SQLException e) {
            showAlert("Помилка завантаження товарів: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onAddClicked() {
        String category = categoryField.getText().trim();
        String name = nameField.getText().trim();
        String selectedCategory = categoryComboBox.getValue();
        String priceText = priceField.getText().trim();

        try (Connection conn = DatabaseConnector.connect()) {
            if (categoryRadio.isSelected()) {
                if (category.isEmpty()) {
                    showAlert("Введіть назву категорії!", Alert.AlertType.WARNING);
                    return;
                }
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO categories(name) VALUES (?)");
                stmt.setString(1, category);
                stmt.executeUpdate();
                showAlert("Категорію додано!", Alert.AlertType.INFORMATION);
                loadCategories();

            } else if (productRadio.isSelected()) {
                if (name.isEmpty() || selectedCategory == null || priceText.isEmpty()) {
                    showAlert("Заповніть усі поля для товару!", Alert.AlertType.WARNING);
                    return;
                }
                if (!categoryMap.containsKey(selectedCategory)) {
                    showAlert("Оберіть дійсну категорію!", Alert.AlertType.WARNING);
                    return;
                }
                double price = Double.parseDouble(priceText);
                int categoryId = categoryMap.get(selectedCategory);

                PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO products(name, price, stock_quantity, category_id)
                    VALUES (?, ?, 0, ?)""");
                stmt.setString(1, name);
                stmt.setDouble(2, price);
                stmt.setInt(3, categoryId);
                stmt.executeUpdate();

                showAlert("Товар додано!", Alert.AlertType.INFORMATION);
                loadProducts();
            }
        } catch (SQLException | NumberFormatException e) {
            showAlert("Помилка: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onDeleteClicked() {
        try (Connection conn = DatabaseConnector.connect()) {
            if (categoryRadio.isSelected()) {
                String selectedCategory = categoryComboBox.getValue();
                if (selectedCategory == null || !categoryMap.containsKey(selectedCategory)) {
                    showAlert("Оберіть дійсну категорію!", Alert.AlertType.WARNING);
                    return;
                }
                int categoryId = categoryMap.get(selectedCategory);
                PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM products WHERE category_id = ? AND stock_quantity > 0");
                checkStmt.setInt(1, categoryId);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    showAlert("Неможливо видалити категорію — на складі ще є товари!", Alert.AlertType.WARNING);
                    return;
                }
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM categories WHERE id = ?");
                stmt.setInt(1, categoryId);
                stmt.executeUpdate();
                showAlert("Категорію видалено!", Alert.AlertType.INFORMATION);
                loadCategories();

            } else if (productRadio.isSelected()) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showAlert("Введіть назву товару!", Alert.AlertType.WARNING);
                    return;
                }
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE name = ?");
                stmt.setString(1, name);
                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    showAlert("Товар видалено!", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Товар не знайдено!", Alert.AlertType.WARNING);
                }
                loadProducts();
            }
        } catch (SQLException e) {
            showAlert("Помилка видалення: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onEditClicked() {
        try (Connection conn = DatabaseConnector.connect()) {
            if (categoryRadio.isSelected()) {
                String newName = categoryField.getText().trim();
                String selectedCategory = categoryComboBox.getValue();
                if (newName.isEmpty() || selectedCategory == null || !categoryMap.containsKey(selectedCategory)) {
                    showAlert("Оберіть та введіть нову назву для категорії!", Alert.AlertType.WARNING);
                    return;
                }
                PreparedStatement stmt = conn.prepareStatement("UPDATE categories SET name = ? WHERE id = ?");
                stmt.setString(1, newName);
                stmt.setInt(2, categoryMap.get(selectedCategory));
                stmt.executeUpdate();
                showAlert("Категорію оновлено!", Alert.AlertType.INFORMATION);
                loadCategories();

            } else if (productRadio.isSelected()) {
                ProductsModel selected = productTable.getSelectionModel().getSelectedItem();
                String name = nameField.getText().trim();
                String priceText = priceField.getText().trim();
                String selectedCategory = categoryComboBox.getValue();
                if (selected == null || name.isEmpty() || priceText.isEmpty() || selectedCategory == null || !categoryMap.containsKey(selectedCategory)) {
                    showAlert("Заповніть усі поля для оновлення!", Alert.AlertType.WARNING);
                    return;
                }
                double price = Double.parseDouble(priceText);
                
                Integer categoryId = categoryMap.get(selectedCategory);
                if (categoryId == null) {
                    showAlert("Оберіть конкретну категорію для редагування!", Alert.AlertType.WARNING);
                    return;
                }

                PreparedStatement stmt = conn.prepareStatement("UPDATE products SET name = ?, price = ?, category_id = ? WHERE id = ?");
                stmt.setString(1, name);
                stmt.setDouble(2, price);
                stmt.setInt(3, categoryId);
                stmt.setInt(4, selected.getId());
                stmt.executeUpdate();
                showAlert("Товар оновлено!", Alert.AlertType.INFORMATION);
                loadProducts();
            }
        } catch (Exception e) {
            showAlert("Помилка редагування: " + e.getMessage(), Alert.AlertType.ERROR);
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
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
