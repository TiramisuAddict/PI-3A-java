package controller.employers.RHetAdminE;

import controller.demandes.NavigationHelper;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class RHetAdminE {

    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private Button btnHome, btnFormation, btnDemande,
            btnEmployer, btnProjet, btnOffre;

    private boolean isExpanded = false;

    @FXML
    public void initialize() {
        // Set the contentArea for NavigationHelper
        // so demande sub-views can navigate back
        NavigationHelper.setContentArea(contentArea);
    }

    @FXML
    private void handleToggleSidebar() {
        double endWidth = isExpanded ? 68 : 200;
        Timeline timeline = new Timeline();
        KeyValue widthValue = new KeyValue(
                sidebar.prefWidthProperty(), endWidth);
        KeyFrame widthFrame = new KeyFrame(
                Duration.millis(150), widthValue);
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
            Parent view = FXMLLoader.load(
                    getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateActiveButton(Button activeBtn) {
        btnHome.getStyleClass().remove("nav-active");
        btnFormation.getStyleClass().remove("nav-active");
        btnDemande.getStyleClass().remove("nav-active");
        btnEmployer.getStyleClass().remove("nav-active");
        btnProjet.getStyleClass().remove("nav-active");
        btnOffre.getStyleClass().remove("nav-active");
        activeBtn.getStyleClass().add("nav-active");
    }

    @FXML
    private void showHome(ActionEvent event) {
        loadView("/emp/RHetAdminE/evenements.fxml");
        updateActiveButton(btnHome);
    }

    @FXML
    private void showFormation(ActionEvent event) {
        loadView("/emp/RHetAdminE/formations.fxml");
        updateActiveButton(btnFormation);
    }

    @FXML
    private void showDemande(ActionEvent event) {
        // RH/Admin loads the admin demandes view (no add button)
        loadView("/emp/RHetAdminE/demandes.fxml");
        updateActiveButton(btnDemande);
    }

    @FXML
    private void showEmployer(ActionEvent event) {
        loadView("/emp/RHetAdminE/employers.fxml");
        updateActiveButton(btnEmployer);
    }

    @FXML
    private void showProjet(ActionEvent event) {
        loadView("/emp/RHetAdminE/projets.fxml");
        updateActiveButton(btnProjet);
    }

    @FXML
    private void showOffres(ActionEvent event) {
        loadView("/emp/RHetAdminE/offres.fxml");
        updateActiveButton(btnOffre);
    }
}