package controller.employers.RHetAdminE;

import entities.employers.employe;
import entities.employers.session;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RHetAdminE implements Initializable {

    @FXML private VBox sidebar;
    @FXML private StackPane avatarSidebar;
    @FXML private Label lblEmployeCourant;
    @FXML private Button btnDeconnexion;
    @FXML private StackPane contentArea;
    @FXML private Button btnHome, btnFormation, btnDemande, btnEmployer, btnProjet, btnOffre;
    @FXML private VBox userCard;

    // Top Bar — PAS de lblPageTitle
    @FXML private HBox topBar;
    @FXML private ImageView imgProfilTopBar;
    @FXML private StackPane notifContainer;
    @FXML private Label lblNotifBadge;

    private boolean isExpanded = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadView("annonce/annonces");
        chargerInfoUtilisateur();
        chargerImageTopBar();
    }
    private void chargerImageTopBar() {
        employe emp = session.getEmploye();
        if (emp == null || imgProfilTopBar == null) return;

        Image image = null;
        if (emp.hasCustomImage()) {
            try {
                File imgFile = new File(emp.getImageProfil());
                if (imgFile.exists()) {
                    image = new Image(imgFile.toURI().toString(), 68, 68, true, true);
                }
            } catch (Exception ignored) {}
        }
        if (image == null) {
            try {
                URL resource = getClass().getResource(employe.DEFAULT_IMAGE);
                if (resource != null) {
                    image = new Image(resource.toExternalForm(), 68, 68, true, true);
                }
            } catch (Exception ignored) {}
        }

        if (image != null) {
            imgProfilTopBar.setImage(image);
            Circle clip = new Circle(17, 17, 17);
            imgProfilTopBar.setClip(clip);
        }
    }

    public void chargerInfoUtilisateur() {
        employe emp = session.getEmploye();
        if (emp == null) return;
        lblEmployeCourant.setText(emp.getPrenom() + " " + emp.getNom());
        chargerAvatarSidebar(emp);
    }

    public void chargerAvatarSidebar(employe emp) {
        if (avatarSidebar == null) return;
        avatarSidebar.getChildren().clear();
        double size = 30;

        Image image = chargerImageProfil(emp, size);

        if (image != null) {
            ImageView iv = creerImageViewRond(image, size);
            avatarSidebar.setStyle("-fx-border-color: rgba(255,255,255,0.5);-fx-border-width: 2;-fx-border-radius: 20;-fx-background-radius: 20;");
            avatarSidebar.getChildren().add(iv);
            return;
        }

        String initials = getInitiales(emp);
        avatarSidebar.setStyle("-fx-background-color: rgba(255,255,255,0.25);-fx-background-radius: 20;");
        Label lbl = new Label(initials);
        lbl.setStyle("-fx-text-fill: white;-fx-font-weight: bold;-fx-font-size: 15px;");
        avatarSidebar.getChildren().add(lbl);
    }

    public Image chargerImageProfil(employe emp, double size) {
        double loadSize = size * 2;
        Image image = null;

        if (emp.hasCustomImage()) {
            try {
                File imgFile = new File(emp.getImageProfil());
                if (imgFile.exists()) {
                    image = new Image(imgFile.toURI().toString(),
                            loadSize, loadSize, true, true);
                }
            } catch (Exception ignored) {}
        }

        if (image == null) {
            try {
                URL resource = getClass().getResource(employe.DEFAULT_IMAGE);
                if (resource != null) {
                    image = new Image(resource.toExternalForm(),
                            loadSize, loadSize, true, true);
                }
            } catch (Exception ignored) {}
        }

        return image;
    }

    private ImageView creerImageViewRond(Image image, double size) {
        ImageView iv = new ImageView(image);
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setSmooth(true);

        double w = image.getWidth();
        double h = image.getHeight();
        double side = Math.min(w, h);
        double x = (w - side) / 2;
        double y = (h - side) / 2;
        iv.setViewport(new javafx.geometry.Rectangle2D(x, y, side, side));
        iv.setPreserveRatio(false);

        Circle clip = new Circle(size / 2, size / 2, size / 2);
        iv.setClip(clip);
        return iv;
    }

    private String getInitiales(employe emp) {
        String initials = "";
        if (emp.getPrenom() != null && !emp.getPrenom().isEmpty())
            initials += emp.getPrenom().charAt(0);
        if (emp.getNom() != null && !emp.getNom().isEmpty())
            initials += emp.getNom().charAt(0);
        return initials.toUpperCase();
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
            userCard.setVisible(true);
            userCard.setManaged(true);
        } else {
            sidebar.getStyleClass().remove("expanded");
            userCard.setVisible(false);
            userCard.setManaged(false);
        }

        timeline.play();
        isExpanded = !isExpanded;
    }

    @FXML
    private void handleDeconnexion() {
        try {
            session.logout();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Déconnexion");
            alert.setHeaderText(null);
            alert.setContentText("Vous voulez déconnecter?");
            alert.showAndWait();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/emp/Login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Connexion");
            loginStage.setScene(new Scene(root));
            loginStage.show();

            Stage currentStage = (Stage) sidebar.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ═══ Navigation (SANS lblPageTitle) ═══

    private void loadView(String fxmlFileName) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/" + fxmlFileName + ".fxml"));
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
        loadView("annonce/annonces");
        updateActiveButton(btnHome);
    }

    @FXML
    private void showFormation(ActionEvent event) {
        loadView("formations");
        updateActiveButton(btnFormation);
    }

    @FXML
    private void showDemande(ActionEvent event) {
        loadView("demandes");
        updateActiveButton(btnDemande);
    }

    @FXML
    private void showEmployer(ActionEvent event) {
        loadView("emp/RHetAdminE/employers");
        updateActiveButton(btnEmployer);
    }

    @FXML
    private void showProjet(ActionEvent event) {
        loadView("projets");
        updateActiveButton(btnProjet);
    }

    @FXML
    private void showOffres(ActionEvent event) {
        loadView("offres/recrutement");
        updateActiveButton(btnOffre);
    }

    @FXML
    private void showProfil() {
        loadView("emp/profil_employe");

        btnHome.getStyleClass().remove("nav-active");
        btnFormation.getStyleClass().remove("nav-active");
        btnDemande.getStyleClass().remove("nav-active");
        btnEmployer.getStyleClass().remove("nav-active");
        btnProjet.getStyleClass().remove("nav-active");
        btnOffre.getStyleClass().remove("nav-active");
    }
}