package controller;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.image.Image;

import java.io.IOException;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

            //Parent root = FXMLLoader.load(getClass().getResource("/main-view.fxml"));
            Parent root = FXMLLoader.load(getClass().getResource("/offres/main-front-office.fxml"));

            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                    getClass().getResource("/customTheme.css").toExternalForm()
            );

            Image appIcon = new Image(getClass().getResourceAsStream("/icons/logo.png"));
            stage.getIcons().add(appIcon);

            //Minimum size of the window
            stage.setMinWidth(1160);
            stage.setMinHeight(750);

            //Default size of the window when launched
            stage.setWidth(1160);
            stage.setHeight(750);

            stage.setTitle("Momentum");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.out.println(e.getMessage() );
            e.printStackTrace();
        }
    }
}