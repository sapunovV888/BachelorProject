package project.bachelor.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ProductsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button editButton;

    @FXML
    private Label menuLabel;

    @FXML
    private Button toMenuButton;

    @FXML
    void onAddClicked(ActionEvent event) {

    }

    @FXML
    void onDeleteClicked(ActionEvent event) {

    }

    @FXML
    void onEditClicked(ActionEvent event) {

    }

    @FXML
    void onToMenuClicked(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/bachelor/main-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Головне меню");
            stage.setScene(new Scene(root));
            stage.show();

            // Закриваємо поточне вікно
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @FXML
    void initialize() {
        assert addButton != null : "fx:id=\"addButton\" was not injected: check your FXML file 'products-view.fxml'.";
        assert deleteButton != null : "fx:id=\"deleteButton\" was not injected: check your FXML file 'products-view.fxml'.";
        assert editButton != null : "fx:id=\"editButton\" was not injected: check your FXML file 'products-view.fxml'.";
        assert menuLabel != null : "fx:id=\"menuLabel\" was not injected: check your FXML file 'products-view.fxml'.";
        assert toMenuButton != null : "fx:id=\"toMenuButton\" was not injected: check your FXML file 'products-view.fxml'.";

    }

}
