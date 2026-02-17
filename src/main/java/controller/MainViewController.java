package controller;

<<<<<<< HEAD
import controller.demandes.NavigationHelper;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
=======
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import java.io.IOException;
<<<<<<< HEAD
=======

>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class MainViewController {

<<<<<<< HEAD
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private Button btnHome, btnFormation, btnDemande, btnEmployer, btnProjet, btnOffre;

    private boolean isExpanded = false;

    @FXML
    public void initialize() {
        // Share contentArea with NavigationHelper so all controllers can use it
        NavigationHelper.setContentArea(contentArea);
    }

    @FXML
=======
    // Logique Sidebar Toggle
    @FXML private VBox sidebar;
    private boolean isExpanded = false;

    @FXML
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
    private void handleToggleSidebar() {
        double endWidth = isExpanded ? 68 : 200;

        Timeline timeline = new Timeline();
<<<<<<< HEAD
        KeyValue widthValue = new KeyValue(sidebar.prefWidthProperty(), endWidth);
        KeyFrame widthFrame = new KeyFrame(Duration.millis(150), widthValue);
=======

        // Animation Sidebar
        KeyValue widthValue = new KeyValue(sidebar.prefWidthProperty(), endWidth);
        KeyFrame widthFrame = new KeyFrame(Duration.millis(150), widthValue);

>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        timeline.getKeyFrames().add(widthFrame);

        if (!isExpanded) {
            sidebar.getStyleClass().add("expanded");
        } else {
            sidebar.getStyleClass().remove("expanded");
        }

        timeline.play();
        isExpanded = !isExpanded;
    }

<<<<<<< HEAD
=======
    // Logique de Navigation
    @FXML private StackPane contentArea;
    @FXML private Button btnHome, btnFormation, btnDemande, btnEmployer, btnProjet, btnOffre; //

    // Charger FXML dans StackPane
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
    private void loadView(String fxmlFileName) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/" + fxmlFileName + ".fxml"));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

<<<<<<< HEAD
=======
    // Logique pour mettre à jour le bouton actif
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
    private void updateActiveButton(Button activeBtn) {
        btnHome.getStyleClass().remove("nav-active");
        btnFormation.getStyleClass().remove("nav-active");
        btnDemande.getStyleClass().remove("nav-active");
        btnEmployer.getStyleClass().remove("nav-active");
        btnProjet.getStyleClass().remove("nav-active");
        btnOffre.getStyleClass().remove("nav-active");
<<<<<<< HEAD
=======

>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        activeBtn.getStyleClass().add("nav-active");
    }

    @FXML
    private void showHome(ActionEvent event) {
        loadView("evenements");
        updateActiveButton(btnHome);
    }

<<<<<<< HEAD
    @FXML
    private void showFormation(ActionEvent event) {
=======
    @FXML private void showFormation(ActionEvent event) {
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        loadView("formations");
        updateActiveButton(btnFormation);
    }

<<<<<<< HEAD
    @FXML
    private void showDemande(ActionEvent event) {
=======
    @FXML private void showDemande(ActionEvent event) {
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        loadView("demandes");
        updateActiveButton(btnDemande);
    }

<<<<<<< HEAD
    @FXML
    private void showEmployer(ActionEvent event) {
=======
    @FXML private void showEmployer(ActionEvent event) {
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        loadView("employers");
        updateActiveButton(btnEmployer);
    }

<<<<<<< HEAD
    @FXML
    private void showProjet(ActionEvent event) {
=======
    @FXML private void showProjet(ActionEvent event) {
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        loadView("projets");
        updateActiveButton(btnProjet);
    }

<<<<<<< HEAD
    @FXML
    private void showOffres(ActionEvent event) {
=======
    @FXML private void showOffres(ActionEvent event) {
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        loadView("offres");
        updateActiveButton(btnOffre);
    }
}