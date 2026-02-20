package controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import models.employe.session;
import models.employe.employe;

public class MainViewController {

    // Logique Sidebar Toggle
    @FXML private VBox sidebar;
    @FXML private BorderPane rootPane;
    private boolean isExpanded = false;

    // Label pour afficher l'employé courant
    @FXML private Label lblEmployeCourant;
    @FXML private Button btnDeconnexion;

    @FXML
    private void handleToggleSidebar() {
        double endWidth = isExpanded ? 68 : 200;

        Timeline timeline = new Timeline();

        // Animation Sidebar
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

    // Initialisation : récupérer l'employé depuis la session et afficher le menu selon le rôle
    @FXML
    private void initialize() {
        updateEmployeLabel();
        configureMenuByRole();
    }

    // Configurer le menu selon le rôle de l'utilisateur
    private void configureMenuByRole() {
        employe emp = session.getEmploye();

        if (emp != null) {
            String role = emp.getRole();

            // Normaliser le rôle pour comparaison
            if (role != null) {
                role = role.toLowerCase().trim();

                // EMPLOYE : ne voit que Home, Formations (inscription), Demandes, Projets
                if (role.equals("employé") || role.equals("employe")) {
                    btnEmployer.setVisible(false);
                    btnOffre.setVisible(false);
                }
                // RH : voit tout
                else if (role.equals("rh")) {
                    btnEmployer.setVisible(true);
                    btnOffre.setVisible(true);
                }
                // ADMINISTRATEUR_ENTREPRISE : voit tout
                else if (role.equals("administrateur entreprise")) {
                    btnEmployer.setVisible(true);
                    btnOffre.setVisible(true);
                }
                // CHEF_PROJET : peut voir tout sauf peut-être employés (à ajuster selon vos besoins)
                else if (role.equals("chef projet")) {
                    btnEmployer.setVisible(true);
                    btnOffre.setVisible(true);
                }
                else {
                    // Par défaut, afficher tous les boutons
                    btnEmployer.setVisible(true);
                    btnOffre.setVisible(true);
                }
            }
        }
    }

    // Logique de Navigation
    @FXML private StackPane contentArea;
    @FXML private Button btnHome, btnFormation, btnDemande, btnEmployer, btnProjet, btnOffre;

    // Tracker la vue actuelle pour pouvoir la rafraîchir après changement d'employé
    private String currentView = null;
    private Button currentButton = null;

    // Charger FXML dans StackPane
    private void loadView(String fxmlFileName) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/" + fxmlFileName + ".fxml"));
            contentArea.getChildren().setAll(view);
            currentView = fxmlFileName; // Sauvegarder la vue actuelle
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Rafraîchir la vue actuellement affichée
    private void refreshCurrentView() {
        if (currentView != null) {
            loadView(currentView);
            if (currentButton != null) {
                updateActiveButton(currentButton);
            }
        }
    }

    // Logique pour mettre à jour le bouton actif
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
        currentButton = btnHome;
        updateActiveButton(btnHome);
    }

    @FXML private void showFormation(ActionEvent event) {
        loadView("formations");
        currentButton = btnFormation;
        updateActiveButton(btnFormation);
    }

    @FXML private void showDemande(ActionEvent event) {
        loadView("demandes");
        currentButton = btnDemande;
        updateActiveButton(btnDemande);
    }

    @FXML private void showEmployer(ActionEvent event) {
        loadView("emp/RHetAdminE/employers");
        currentButton = btnEmployer;
        updateActiveButton(btnEmployer);
    }

    @FXML private void showProjet(ActionEvent event) {
        loadView("projets");
        currentButton = btnProjet;
        updateActiveButton(btnProjet);
    }

    @FXML private void showOffres(ActionEvent event) {
        loadView("offres");
        currentButton = btnOffre;
        updateActiveButton(btnOffre);
    }

    // Bouton de déconnexion : retourner au login
    @FXML
    private void handleDeconnexion(ActionEvent event) {
        session.logout();

        try {
            // Retourner à la page de login
            Parent loginView = FXMLLoader.load(getClass().getResource("/emp/Login.fxml"));
            rootPane.getScene().setRoot(loginView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Mettre à jour le label affichant l'employé courant
    private void updateEmployeLabel() {
        employe emp = session.getEmploye();

        if (emp != null) {
            String employeInfo = emp.getPrenom() + " " + emp.getNom();
            lblEmployeCourant.setText(employeInfo);
            lblEmployeCourant.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: white;");
        } else {
            lblEmployeCourant.setText("Non connecté");
            lblEmployeCourant.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8);");
        }
    }
}