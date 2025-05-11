package project.bachelor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MainMenuController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button cashdeskButton;

    @FXML
    private Label menuLabel;

    @FXML
    private Button productsButton;

    @FXML
    private Button receiptButton;

    @FXML
    private Button sellButton;

    @FXML
    private Button warehouseButton;

    @FXML
    void initialize() {
        assert cashdeskButton != null : "fx:id=\"cashdeskButton\" was not injected: check your FXML file 'main-view.fxml'.";
        assert menuLabel != null : "fx:id=\"menuLabel\" was not injected: check your FXML file 'main-view.fxml'.";
        assert productsButton != null : "fx:id=\"productsButton\" was not injected: check your FXML file 'main-view.fxml'.";
        assert receiptButton != null : "fx:id=\"receiptButton\" was not injected: check your FXML file 'main-view.fxml'.";
        assert sellButton != null : "fx:id=\"sellButton\" was not injected: check your FXML file 'main-view.fxml'.";
        assert warehouseButton != null : "fx:id=\"warehouseButton\" was not injected: check your FXML file 'main-view.fxml'.";

    }

    private void openScene(String fxmlFile, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();

            // Закриваємо поточне вікно
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSellClicked(ActionEvent event) {
        openScene("sell-view.fxml", "Продаж", event);
    }

    public void onReceiptClicked(ActionEvent event) {
        openScene("receipt-view.fxml", "Надходження", event);
    }

    public void onWarehouseClicked(ActionEvent event) {
        openScene("warehouse-view.fxml", "Склад", event);
    }

    public void onProductsClicked(ActionEvent event) {
        openScene("products-view.fxml", "Меню Товарів", event);
    }

    public void onCashdeskClicked(ActionEvent event) {
        openScene("cashdesk-view.fxml", "Каса", event);
    }


}
