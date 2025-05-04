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

public class SellController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button endButton;

    @FXML
    private Label menuLabel;

    @FXML
    private Button toMenuButton;

    @FXML
    void onAddClicked(ActionEvent event) {

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

    public void onDeleteClicked(ActionEvent event) {
    }

    public void onEndClicked(ActionEvent event) {


    }
    @FXML
    void initialize() {
        assert addButton != null : "fx:id=\"addButton\" was not injected: check your FXML file 'sell-view.fxml'.";
        assert deleteButton != null : "fx:id=\"deleteButton\" was not injected: check your FXML file 'sell-view.fxml'.";
        assert endButton != null : "fx:id=\"endButton\" was not injected: check your FXML file 'sell-view.fxml'.";
        assert menuLabel != null : "fx:id=\"menuLabel\" was not injected: check your FXML file 'sell-view.fxml'.";
        assert toMenuButton != null : "fx:id=\"toMenuButton\" was not injected: check your FXML file 'sell-view.fxml'.";

    }


}
