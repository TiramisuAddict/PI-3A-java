package controller.employers.RHetAdminE;

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
    private boolean isExpanded = false;

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


    @FXML private StackPane contentArea;
    @FXML private Button btnHome, btnFormation, btnDemande, btnEmployer, btnProjet, btnOffre;

    private void loadView(String fxmlFileName) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/emp/RHetAdminE/" + fxmlFileName + ".fxml"));
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
        loadView("evenements");
        updateActiveButton(btnHome);
    }

    @FXML private void showFormation(ActionEvent event) {
        loadView("formations");
        updateActiveButton(btnFormation);
    }

    @FXML private void showDemande(ActionEvent event) {
        loadView("demandes");
        updateActiveButton(btnDemande);
    }

    @FXML private void showEmployer(ActionEvent event) {
        loadView("employers");
        updateActiveButton(btnEmployer);
    }

    @FXML private void showProjet(ActionEvent event) {
        loadView("projets");
        updateActiveButton(btnProjet);
    }

    @FXML private void showOffres(ActionEvent event) {
        loadView("offres");
        updateActiveButton(btnOffre);
    }
}