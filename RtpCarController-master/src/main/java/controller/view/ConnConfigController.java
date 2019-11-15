package controller.view;

import controller.connection.ConnController;
import exception.NetworkConnectionException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnConfigController implements Initializable {
    @FXML
    private AnchorPane rootAP;
    @FXML
    private TextField addressField;
    @FXML
    private TextField portField;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    private Scene scene;
    private Stage stage;
    private ConnController connController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logger logger = Logger.getLogger(ConnConfigController.class.getName());
        logger.log(Level.INFO, "Initializing ConnConfigController");

        connController = ConnController.getInstance();

        Platform.runLater(() -> {
            scene = rootAP.getScene();
            stage = (Stage) scene.getWindow();
        });

        setInputValidation();
    }


    @FXML
    private void cancel(ActionEvent actionEvent) {
        stage.close();
    }

    /**
     * Validates the entered server address and port and connect to the server if valid.
     * @param actionEvent The ActionEvent provided by JavaFX.
     */
    @FXML
    private void confirm(ActionEvent actionEvent) {
        String address = this.addressField.getText();
        String port = this.portField.getText();

        boolean addressValid = ConnController.isValidServerAddress(address);
        boolean portValid = ConnController.isValidPortNumber(port);

        if (addressValid && portValid) {
            try {
                connController.connect(address, port);
                stage.close();
            } catch (NetworkConnectionException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error while trying to connect: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * Initialises change listeners that validate the user input once the text field loses focus, for the address and
     * port text fields.
     */
    private void setInputValidation() {
        setAddressValidation();
        setPortValidation();
    }

    /**
     * Initialises a change listener that validates the address input once the associated text field loses focus.
     */
    private void setAddressValidation() {
        addressField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !addressField.getText().isEmpty()) {
                boolean validAddress = ConnController.isValidServerAddress(addressField.getText());
                if (!validAddress) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Entered server address is invalid.");
                    alert.showAndWait();
                }
            }
        });
    }

    /**
     * Initialises a change listener that validates the port input once the associated text field loses focus.
     */
    private void setPortValidation() {
        portField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !portField.getText().isEmpty()) {
                boolean validPort = ConnController.isValidPortNumber(portField.getText());

                if (!validPort) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Entered port number is invalid.");
                    alert.showAndWait();
                }
            }
        });
    }

}
