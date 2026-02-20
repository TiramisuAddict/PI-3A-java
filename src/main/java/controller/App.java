package controller;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.image.Image;
import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/emp/login.fxml")));

            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                    getClass().getResource("/customTheme.css").toExternalForm()
            );

            Image appIcon = new Image(getClass().getResourceAsStream("/icons/logo.png"));
            stage.getIcons().add(appIcon);

            stage.setTitle("Momentum");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.out.println(e.getMessage() );
            e.printStackTrace();
        }
    }
}