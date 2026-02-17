package controller.demandes;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;

import java.io.IOException;

public class NavigationHelper {

    private static StackPane contentArea;

    public static void setContentArea(StackPane area) {
        contentArea = area;
    }

    public static StackPane getContentArea() {
        return contentArea;
    }

    public static StackPane findContentArea(Node anyNode) {
        if (contentArea != null) return contentArea;

        Parent root = anyNode.getScene().getRoot();
        if (root instanceof javafx.scene.layout.BorderPane) {
            javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) root;
            if (borderPane.getCenter() instanceof StackPane) {
                contentArea = (StackPane) borderPane.getCenter();
                return contentArea;
            }
        }
        return null;
    }

    public static FXMLLoader loadView(Node anyNode, String fxmlFile) throws IOException {
        StackPane area = findContentArea(anyNode);
        if (area == null) {
            throw new IOException("Cannot find contentArea!");
        }

        FXMLLoader loader = new FXMLLoader(
                NavigationHelper.class.getResource("/" + fxmlFile));
        Parent view = loader.load();
        area.getChildren().setAll(view);
        return loader;
    }
}