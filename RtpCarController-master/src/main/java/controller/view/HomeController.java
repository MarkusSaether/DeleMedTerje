package controller.view;

import controller.DrivingController;
import controller.connection.ConnController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Car;
import model.Steer;
import model.Throttle;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomeController implements Initializable {
    @FXML
    private AnchorPane rootAP;
    @FXML
    private Button leftButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Button rightButton;
    @FXML
    private Button reverseButton;

    private Scene scene;
    private Stage stage;
    private Logger logger;
    private ConnController connController;
    private DrivingController drivingController;
    private HashMap<KeyCode, Boolean> keyPressed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger = Logger.getLogger(HomeController.class.getName());
        logger.log(Level.INFO, "Initializing MainController");

        Car car = new Car();
        connController = ConnController.getInstance();
        connController.registerCar(car);
        drivingController = DrivingController.getInstance();
        drivingController.registerCar(car);

        keyPressed = new HashMap<>();
        keyPressed.put(KeyCode.UP, false);
        keyPressed.put(KeyCode.DOWN, false);
        keyPressed.put(KeyCode.LEFT, false);
        keyPressed.put(KeyCode.RIGHT, false);

        Platform.runLater(() -> {
            scene = rootAP.getScene();
            stage = (Stage) scene.getWindow();
            setEventHandlers();

            configureConnection();

            if (connController.isActive()) {
                setKeyboardHandlers();
            } else {
                stage.close();
            }
        });
    }

    /**
     * Creates a new Stage in which the user can configure the server address and port to connect to.
     */
    private void configureConnection() {
        logger.log(Level.INFO, "Setting up connection configuration stage");
        try {
            FXMLLoader connConfigLoader = new FXMLLoader(getClass().getResource("/view/connectionconfig.fxml"));
            Parent connConfigFXML = connConfigLoader.load();
            Scene connConfigScene = new Scene(connConfigFXML);
            Stage connConfigStage = new Stage();
            connConfigStage.setScene(connConfigScene);
            connConfigStage.initModality(Modality.APPLICATION_MODAL);
            connConfigStage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns whether the given KeyCode is a key that belongs to a steer function of the Car.
     * @param key The KeyCode to check.
     * @return True if the given KeyCode belongs to a steer function of the Car.
     */
    private boolean isSteerKey(KeyCode key) {
        return key == KeyCode.LEFT || key == KeyCode.RIGHT;
    }

    /**
     * Returns whether the given KeyCode is a key that belongs to a throttle function of the Car.
     * @param key The KeyCode to check.
     * @return True if the given KeyCode belongs to a throttle function of the Car.
     */
    private boolean isThrottleKey(KeyCode key) {
        return key == KeyCode.UP || key == KeyCode.DOWN;
    }

    /**
     * Returns the Throttle that corresponds to the given KeyCode.
     * @param key The KeyCode for which the associated Throttle needs to be returned.
     * @return The Throttle that corresponds to the given KeyCode, or null if none exists.
     */
    private Throttle getThrottleFromKey(KeyCode key) {
        switch (key) {
            case UP:
                return Throttle.FORWARD;
            case DOWN:
                return Throttle.REVERSE;
        }
        return null;
    }

    /**
     * Returns the Steer that corresponds to the given KeyCode.
     * @param key The KeyCode for which the associated Steer needs to be returned.
     * @return The Steer that corresponds to the given KeyCode, or null if none exists.
     */
    private Steer getSteerFromKey(KeyCode key) {
        switch (key) {
            case RIGHT:
                return Steer.RIGHT;
            case LEFT:
                return Steer.LEFT;
        }
        return null;
    }

    /**
     * Returns the KeyCode that is associated with the action that is opposite to the action associated with the given
     * KeyCode.
     * @param key The key for which the opposite key needs to be returned.
     * @return The KeyCode that is associated with the action that is opposite to the action associated with the given
     * KeyCode, or null if the given KeyCode has no opposite in this piece of software.
     */
    private KeyCode getOppositeKey(KeyCode key) {
        KeyCode opposite = null;
        switch (key) {
            case UP:
                opposite = KeyCode.DOWN;
                break;
            case DOWN:
                opposite = KeyCode.UP;
                break;
            case LEFT:
                opposite = KeyCode.RIGHT;
                break;
            case RIGHT:
                opposite = KeyCode.LEFT;
                break;
        }
        return opposite;
    }

    /**
     * Tells the DrivingController to throttle.
     * @param dir The direction to Throttle in.
     */
    private void throttle(Throttle dir) {
        drivingController.throttle(dir);
    }

    /**
     * Tells the DrivingController to steer.
     * @param dir The direction to steer in.
     */
    private void steer(Steer dir) {
        drivingController.steer(dir);
    }

    /**
     * Sets up the keyboard event listeners related to driving the car.
     */
    private void setKeyboardHandlers() {
        logger.log(Level.INFO, "Initialising keyboard handlers");

        scene.setOnKeyPressed(e -> {
            KeyCode key = e.getCode();
            if (keyPressed.containsKey(key)) {
                keyPressed.put(key, true);
                if (isThrottleKey(key)) {
                    throttle(getThrottleFromKey(key));
                } else if (isSteerKey(key)) {
                    steer(getSteerFromKey(key));
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            KeyCode key = e.getCode();
            if (keyPressed.containsKey(key)) {
                keyPressed.put(key, false);
                KeyCode opposite = getOppositeKey(key);
                boolean oppositePressed = keyPressed.get(opposite);

                if (isThrottleKey(key)) {
                    throttle(oppositePressed ? getThrottleFromKey(opposite) : Throttle.NEUTRAL);
                } else if (isSteerKey(key)) {
                    steer(oppositePressed ? getSteerFromKey(opposite) : Steer.NEUTRAL);
                }
            }
        });
    }

    /**
     * Sets up the keyboard event listeners and shutdown event listeners.
     */
    private void setEventHandlers() {
        setKeyboardHandlers();
        stage.setOnCloseRequest(e -> {
            if (connController.isActive()) {
                connController.disconnect();
            }
            stage.close();
        });
    }
}
