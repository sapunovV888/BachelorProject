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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class WarehouseController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button addButton;

    @FXML
    private Button addFewButton;

    @FXML
    private Button createRequestButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Label menuLabel;

    @FXML
    private CheckBox requestCheckBox;

    @FXML
    private Button toMenuButton;

    @FXML
    void onAddClicked(ActionEvent event) {

    }

    @FXML
    void onAddFewClicked(ActionEvent event) {

    }

    @FXML
    void onCreateRequestClicked(ActionEvent event) {

    }

    @FXML
    void onDeleteClicked(ActionEvent event) {

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
        assert addButton != null : "fx:id=\"addButton\" was not injected: check your FXML file 'warehouse-view.fxml'.";
        assert addFewButton != null : "fx:id=\"addFewButton\" was not injected: check your FXML file 'warehouse-view.fxml'.";
        assert createRequestButton != null : "fx:id=\"createRequestButton\" was not injected: check your FXML file 'warehouse-view.fxml'.";
        assert deleteButton != null : "fx:id=\"deleteButton\" was not injected: check your FXML file 'warehouse-view.fxml'.";
        assert menuLabel != null : "fx:id=\"menuLabel\" was not injected: check your FXML file 'warehouse-view.fxml'.";
        assert requestCheckBox != null : "fx:id=\"requestCheckBox\" was not injected: check your FXML file 'warehouse-view.fxml'.";
        assert toMenuButton != null : "fx:id=\"toMenuButton\" was not injected: check your FXML file 'warehouse-view.fxml'.";

    }

}
