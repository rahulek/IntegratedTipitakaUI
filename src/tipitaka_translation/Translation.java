package tipitaka_translation;

/**
 * Created by rahul_ekbote on 8/8/2017.
 */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author rahul_ekbote
 */
public class Translation extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Tipiṭaka Marāṭhī Translation Project");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("Buddha.jpg")));
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}