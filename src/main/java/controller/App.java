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

    public static final String ANNONCES_FXML = "/annonces.fxml";
    public static final String ICONS_LOGO_PNG = "/icons/logo.png";
    public static final String CUSTOM_THEME_CSS = "/customTheme.css";
    public static final String MOMENTUM_TITLE = "Momentum";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

            //Parent root = FXMLLoader.load(getClass().getResource("/annonces-display.fxml"));
            Parent root = FXMLLoader.load(getClass().getResource(ANNONCES_FXML));

            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                    getClass().getResource(CUSTOM_THEME_CSS).toExternalForm()
            );

            Image appIcon = new Image(getClass().getResourceAsStream(ICONS_LOGO_PNG));
            stage.getIcons().add(appIcon);

            //Minimum size of the window
            stage.setMinWidth(1160);
            stage.setMinHeight(700);

            //Default size of the window when launched
            stage.setWidth(1160);
            stage.setHeight(700);

            stage.setTitle(MOMENTUM_TITLE);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.out.println(e.getMessage() );
            e.printStackTrace();
        }
    }
}
