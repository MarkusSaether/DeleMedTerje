import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("view/home.fxml"));
        Parent homeFXML = homeLoader.load();
        Scene homeScene = new Scene(homeFXML);
        primaryStage.setScene(homeScene);
        primaryStage.show();
    }
}
