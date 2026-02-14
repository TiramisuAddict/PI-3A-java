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

            Parent root = FXMLLoader.load(getClass().getResource("/main-view.fxml"));

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
   /*@Override
   public void start(Stage stage) throws Exception {
       Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

       var fxmlUrl = getClass().getResource("/main-view.fxml");
       System.out.println("FXML URL = " + fxmlUrl);

       var cssUrl = getClass().getResource("/customTheme.css");
       System.out.println("CSS URL  = " + cssUrl);

       var iconUrl = getClass().getResource("/icons/logo.png");
       System.out.println("ICON URL = " + iconUrl);

       Parent root = FXMLLoader.load(fxmlUrl);
       Scene scene = new Scene(root);

       if (cssUrl != null) {
           scene.getStylesheets().add(cssUrl.toExternalForm());
       }

       if (iconUrl != null) {
           stage.getIcons().add(new Image(iconUrl.toExternalForm()));
       }

       stage.setTitle("Momentum");
       stage.setScene(scene);
       stage.show();
   }*/

}
