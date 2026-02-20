package controller.employers.employes;

import controller.demandes.NavigationHelper;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class employes {

    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private Button btnHome, btnFormation, btnDemande,
            btnProjet, btnOffre;

    private boolean isExpanded = false;

    @FXML
    public void initialize() {
        NavigationHelper.setContentArea(contentArea);
        System.out.println("employes.initialize: contentArea set to NavigationHelper");
        showHome(null);
    }

    @FXML
    private void handleToggleSidebar() {
        double endWidth = isExpanded ? 68 : 200;
        Timeline timeline = new Timeline();
        KeyValue widthValue = new KeyValue(sidebar.prefWidthProperty(), endWidth);
        KeyFrame widthFrame = new KeyFrame(Duration.millis(150), widthValue);
        timeline.getKeyFrames().add(widthFrame);

        if (!isExpanded) {
            sidebar.getStyleClass().add("expanded");
        } else {
            sidebar.getStyleClass().remove("expanded");
        }
        timeline.play();
        isExpanded = !isExpanded;
    }

    private void loadView(String fxmlPath) {
        try {
            System.out.println("employes.loadView: Loading " + fxmlPath);
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.out.println("ERROR loading view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateActiveButton(Button activeBtn) {
        btnHome.getStyleClass().remove("nav-active");
        btnFormation.getStyleClass().remove("nav-active");
        btnDemande.getStyleClass().remove("nav-active");
        btnProjet.getStyleClass().remove("nav-active");
        btnOffre.getStyleClass().remove("nav-active");
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("nav-active");
        }
    }

    @FXML
    private void showHome(ActionEvent event) {
        loadView("/evenements.fxml");
        updateActiveButton(btnHome);
    }

    @FXML
    private void showFormation(ActionEvent event) {
        loadView("/formations.fxml");
        updateActiveButton(btnFormation);
    }

    @FXML
    private void showDemande(ActionEvent event) {
        loadView("/emp/employes/demandes-employe.fxml");
        updateActiveButton(btnDemande);
    }

    @FXML
    private void showProjet(ActionEvent event) {
        loadView("/projets.fxml");
        updateActiveButton(btnProjet);
    }

    @FXML
    private void showOffres(ActionEvent event) {
        loadView("/offres.fxml");
        updateActiveButton(btnOffre);
    }
}