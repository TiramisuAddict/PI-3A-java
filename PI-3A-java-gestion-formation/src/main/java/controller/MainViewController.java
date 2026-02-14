package controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

// Ajouts pour la sélection d'employé
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import utils.MyDB;
import utils.SessionManager;

public class MainViewController {

    // Logique Sidebar Toggle
    @FXML private VBox sidebar;
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

    // Initialisation : demander la sélection de l'employé au démarrage
    @FXML
    private void initialize() {
        updateEmployeLabel();

        // Forcer la sélection d'un employé si aucun n'est connecté
        if (!SessionManager.isEmployeSelected()) {
            // Utiliser Platform.runLater pour attendre que la fenêtre soit affichée
            javafx.application.Platform.runLater(this::promptEmployeSelection);
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
        loadView("employers");
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

    // Demander la sélection d'un employé
    private void promptEmployeSelection() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Connexion Employé");
        dialog.setHeaderText("Bienvenue ! Veuillez vous identifier");
        dialog.setContentText("Entrez votre ID employé :");

        boolean validSelection = false;

        while (!validSelection) {
            dialog.showAndWait().ifPresent(input -> {
                try {
                    int id = Integer.parseInt(input.trim());

                    if (employeExiste(id)) {
                        SessionManager.setCurrentEmployeId(id);
                        updateEmployeLabel();
                    } else {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("ID invalide");
                        alert.setHeaderText(null);
                        alert.setContentText("Aucun employé trouvé avec cet ID.");
                        alert.showAndWait();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Format invalide");
                    alert.setHeaderText(null);
                    alert.setContentText("Veuillez saisir un nombre entier valide.");
                    alert.showAndWait();
                }
            });

            // Si un employé a été sélectionné, sortir de la boucle
            if (SessionManager.isEmployeSelected()) {
                validSelection = true;
            } else {
                // Si l'utilisateur annule sans sélectionner, afficher un avertissement
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Sélection requise");
                alert.setHeaderText(null);
                alert.setContentText("Vous devez sélectionner un employé pour utiliser l'application.");
                alert.showAndWait();
            }
        }
    }

    // Bouton de déconnexion : changer d'employé
    @FXML
    private void handleDeconnexion(ActionEvent event) {
        SessionManager.clear();
        updateEmployeLabel();
        promptEmployeSelection();

        // Rafraîchir la vue actuelle pour afficher les données du nouvel employé
        refreshCurrentView();
    }

    // Mettre à jour le label affichant l'employé courant
    private void updateEmployeLabel() {
        if (SessionManager.isEmployeSelected()) {
            Integer id = SessionManager.getCurrentEmployeId();
            // Optionnel : récupérer le nom/prénom depuis la BDD
            String employeInfo = getEmployeInfo(id);
            lblEmployeCourant.setText(employeInfo != null ? employeInfo : "Employé ID: " + id);
            // Toujours en blanc sur le gradient
            lblEmployeCourant.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: white;");
        } else {
            lblEmployeCourant.setText("Non connecté");
            // En blanc aussi pour rester cohérent avec le gradient
            lblEmployeCourant.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8);");
        }
    }

    // Récupérer le nom et prénom de l'employé
    private String getEmployeInfo(int idEmploye) {
        String sql = "SELECT nom, prenom FROM employé WHERE id_employe = ?";

        try {
            Connection conn = MyDB.getInstance().getConn();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idEmploye);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                return prenom + " " + nom;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean employeExiste(int idEmploye) {
        String sql = "SELECT 1 FROM employé WHERE id_employe = ?";

        try {
            Connection conn = MyDB.getInstance().getConn();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idEmploye);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur base de données");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de vérifier l'existence de l'employé.");
            alert.showAndWait();
            return false;
        }
    }
}