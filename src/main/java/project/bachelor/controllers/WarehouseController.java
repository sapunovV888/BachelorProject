package project.bachelor.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import project.bachelor.DatabaseConnector;
import project.bachelor.models.WarehouseModel;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class WarehouseController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private Button addButton;
    @FXML private Button addFewButton;
    @FXML private Button createRequestButton;
    @FXML private Button deleteButton;
    @FXML private Button toMenuButton;
    @FXML private Label menuLabel;
    @FXML private CheckBox requestCheckBox;
    @FXML private TableView<WarehouseModel> productTable;
    @FXML private TableColumn<WarehouseModel, Integer> colId;
    @FXML private TableColumn<WarehouseModel, String> colCategory;
    @FXML private TableColumn<WarehouseModel, String> colName;
    @FXML private TableColumn<WarehouseModel, Double> colPrice;
    @FXML private TableColumn<WarehouseModel, Integer> colStock;
    @FXML private TableColumn<WarehouseModel, Integer> colRequest;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField productNameField;
    @FXML private TextField quantityField;

    private final ObservableList<WarehouseModel> productList = FXCollections.observableArrayList();
    private final Map<String, Integer> categoryMap = new HashMap<>();

    @FXML
    void initialize() {
        setupTableColumns();
        loadCategories();
        loadAllProducts();

        requestCheckBox.setOnAction(event -> {
            if (requestCheckBox.isSelected()) loadRequestProducts();
            else loadAllProducts();
        });

        categoryComboBox.setOnAction(event -> {
            if (requestCheckBox.isSelected()) loadRequestProducts();
            else loadAllProducts();
        });

        productTable.setOnMouseClicked(event -> {
            WarehouseModel selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                productNameField.setText(selected.getName());
            }
        });
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colCategory.setCellValueFactory(data -> data.getValue().categoryProperty());
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colPrice.setCellValueFactory(data -> data.getValue().priceProperty().asObject());
        colStock.setCellValueFactory(data -> data.getValue().stockQuantityProperty().asObject());
        colRequest.setCellValueFactory(data -> data.getValue().inRequestProperty().asObject());
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
            showAlert("Помилка при завантаженні категорій: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAllProducts() {
        productList.clear();
        String selectedCategory = categoryComboBox.getValue();
        boolean filter = selectedCategory != null && !selectedCategory.equals("Усі категорії");

        String query = """
            SELECT p.id, c.name AS category, p.name, p.price, p.stock_quantity,
                   COALESCE(r.quantity, 0) AS inRequest
            FROM products p
            JOIN categories c ON p.category_id = c.id
            LEFT JOIN request_items r ON p.id = r.product_id
            """ + (filter ? "WHERE c.name = ?" : "");

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (filter) stmt.setString(1, selectedCategory);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                productList.add(new WarehouseModel(
                        rs.getInt("id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("inRequest")
                ));
            }

        } catch (SQLException e) {
            showAlert("Помилка при завантаженні товарів: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadRequestProducts() {
        productList.clear();
        String query = """
            SELECT p.id, c.name AS category, p.name, p.price, p.stock_quantity, r.quantity AS inRequest
            FROM request_items r
            JOIN products p ON r.product_id = p.id
            JOIN categories c ON p.category_id = c.id
        """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productList.add(new WarehouseModel(
                        rs.getInt("id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("inRequest")
                ));
            }
        } catch (SQLException e) {
            showAlert("Помилка при завантаженні запиту: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onAddClicked() {
        WarehouseModel selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null && !productNameField.getText().isEmpty()) {
            selected = productList.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(productNameField.getText().trim()))
                    .findFirst().orElse(null);
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
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT quantity FROM request_items WHERE product_id = ?");
            checkStmt.setInt(1, selected.getId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int existing = rs.getInt("quantity");
                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE request_items SET quantity = ? WHERE product_id = ?");
                updateStmt.setInt(1, existing + quantity);
                updateStmt.setInt(2, selected.getId());
                updateStmt.executeUpdate();
            } else {
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO request_items (product_id, quantity) VALUES (?, ?)");
                insertStmt.setInt(1, selected.getId());
                insertStmt.setInt(2, quantity);
                insertStmt.executeUpdate();
            }

            showAlert("Товар додано до запиту!", Alert.AlertType.INFORMATION);
            loadAllProducts();

        } catch (SQLException e) {
            showAlert("Помилка при додаванні: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onAddFewClicked() {
        int minStock;
        try {
            minStock = Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Некоректна кількість для авто-запиту!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DatabaseConnector.connect()) {
            for (WarehouseModel p : productList) {
                if (p.getStockQuantity() < 5) {
                    PreparedStatement check = conn.prepareStatement("SELECT quantity FROM request_items WHERE product_id = ?");
                    check.setInt(1, p.getId());
                    ResultSet rs = check.executeQuery();
                    if (rs.next()) {
                        int existing = rs.getInt("quantity");
                        PreparedStatement update = conn.prepareStatement("UPDATE request_items SET quantity = ? WHERE product_id = ?");
                        update.setInt(1, existing + minStock);
                        update.setInt(2, p.getId());
                        update.executeUpdate();
                    } else {
                        PreparedStatement insert = conn.prepareStatement("INSERT INTO request_items (product_id, quantity) VALUES (?, ?)");
                        insert.setInt(1, p.getId());
                        insert.setInt(2, minStock);
                        insert.executeUpdate();
                    }
                }
            }
            showAlert("Автоматично додано товари до запиту", Alert.AlertType.INFORMATION);
            loadAllProducts();
        } catch (SQLException e) {
            showAlert("Помилка: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onDeleteClicked() {
        WarehouseModel selected = productTable.getSelectionModel().getSelectedItem();

        // Якщо нічого не вибрано в таблиці — пробуємо знайти за ім’ям з поля
        if (selected == null && !productNameField.getText().isBlank()) {
            selected = productList.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(productNameField.getText().trim()))
                    .findFirst()
                    .orElse(null);
        }

        if (selected == null || selected.getInRequest() <= 0) {
            showAlert("Оберіть товар або введіть його назву, і переконайтесь, що він є у запиті!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM request_items WHERE product_id = ?")) {

            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();

            showAlert("Товар видалено з запиту.", Alert.AlertType.INFORMATION);

            // Після видалення — оновлюємо список
            if (requestCheckBox.isSelected()) {
                loadRequestProducts();  // ✅ показуємо товари тільки з запиту
            } else {
                loadAllProducts();     // якщо не активовано — показуємо всі
            }

            productTable.refresh();

        } catch (SQLException e) {
            showAlert("Помилка при видаленні товару з запиту: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void onCreateRequestClicked() {
        String dateStr = java.time.LocalDate.now().toString();
        String filePath = "Запит_" + dateStr + ".csv";

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            // Запис UTF-8 BOM (EF BB BF)
            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);

            try (OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                // Нова шапка без "Ціна", але з "Ціна закупки"
                writer.write("Назва;Категорія;Залишок;У запиті;Ціна закупки\n");

                for (WarehouseModel product : productList) {
                    if (product.getInRequest() > 0) {
                        writer.write(String.format("%s;%s;%d;%d;\n",
                                product.getName(),
                                product.getCategory(),
                                product.getStockQuantity(),
                                product.getInRequest()
                        ));
                    }
                }
            }

            showAlert("Файл запиту успішно збережено!", Alert.AlertType.INFORMATION);

            // Очищаємо таблицю запиту в БД
            try (Connection conn = DatabaseConnector.connect();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM request_items")) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                showAlert("Помилка при очищенні запиту: " + e.getMessage(), Alert.AlertType.ERROR);
                return;
            }

            // Обнуляємо дані в UI
            for (WarehouseModel item : productList) {
                item.setInRequest(0);
            }
            productTable.refresh();

        } catch (IOException e) {
            showAlert("Помилка при збереженні файлу: " + e.getMessage(), Alert.AlertType.ERROR);
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
        } catch (IOException e) {
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