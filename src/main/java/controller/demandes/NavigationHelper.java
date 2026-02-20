package controller.demandes;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class NavigationHelper {

    private static Pane contentArea;

    public static void setContentArea(Pane container) {
        contentArea = container;
        System.out.println("NavigationHelper: contentArea set to " + container);
    }

    public static Pane getContentArea() {
        return contentArea;
    }

    public static FXMLLoader loadView(String fxmlPath) throws IOException {
        System.out.println("=== NavigationHelper.loadView(String) ===");
        System.out.println("Requested path: " + fxmlPath);

        if (contentArea == null) {
            throw new IOException("Content area not set. Call setContentArea() first.");
        }

        String normalizedPath = fxmlPath.startsWith("/") ? fxmlPath : "/" + fxmlPath;
        System.out.println("Normalized path: " + normalizedPath);

        URL resourceUrl = NavigationHelper.class.getResource(normalizedPath);
        System.out.println("Resource URL: " + resourceUrl);

        if (resourceUrl == null) {
            throw new IOException("Cannot find FXML file: " + normalizedPath);
        }

        FXMLLoader loader = new FXMLLoader(resourceUrl);
        Parent view = loader.load();

        replaceContent(view);

        System.out.println("View loaded successfully!");
        return loader;
    }

    public static FXMLLoader loadView(Node currentNode, String fxmlPath) throws IOException {
        System.out.println("=== NavigationHelper.loadView(Node, String) ===");
        System.out.println("Requested path: " + fxmlPath);

        return loadView(fxmlPath);
    }

    private static void replaceContent(Parent view) {
        if (contentArea instanceof StackPane) {
            ((StackPane) contentArea).getChildren().setAll(view);
        } else if (contentArea instanceof AnchorPane) {
            AnchorPane anchorPane = (AnchorPane) contentArea;
            anchorPane.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } else {
            contentArea.getChildren().setAll(view);
        }
    }
}