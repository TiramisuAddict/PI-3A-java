package test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestMapTiles extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20;");

        Label statusLabel = new Label("Testing map tile loading...");
        root.getChildren().add(statusLabel);

        // Test loading a single tile
        new Thread(() -> {
            try {
                String urlStr = "https://a.tile.openstreetmap.org/13/4251/3015.png";

                System.out.println("Attempting to load: " + urlStr);

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                conn.setRequestProperty("Accept", "image/png,image/*,*/*");
                conn.setRequestProperty("Referer", "https://www.openstreetmap.org/");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                System.out.println("Response code: " + responseCode);

                if (responseCode == 200) {
                    InputStream is = conn.getInputStream();
                    Image img = new Image(is);
                    is.close();

                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("✅ Tile loaded successfully!");
                        statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 14;");

                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(256);
                        iv.setFitHeight(256);
                        root.getChildren().add(iv);

                        root.getChildren().add(new Label("Tile: 13/4251/3015 (Tunis area)"));
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("❌ Failed: HTTP " + responseCode);
                        statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14;");
                    });
                }

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("❌ Error: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14;");
                });
            }
        }).start();

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("Map Tile Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}