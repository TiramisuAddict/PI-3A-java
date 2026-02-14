package controller.formations;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.formation;
import models.inscription_formation;
import models.StatutInscription;
import service.inscription_formationCRUD;
import utils.MyDB;
import utils.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class inscription_formationController {

    @FXML
    private Label lblTitre;
    @FXML
    private Label lblOrganisme;
    @FXML
    private Label lblDates;
    @FXML
    private Label lblLieu;
    @FXML
    private Label lblCapacite;

    @FXML
    private Label lblEmployeConnecte;
    @FXML
    private ComboBox<StatutInscription> comboStatut;

    @FXML
    private Button btnInscrire;
    @FXML
    private Button btnAnnuler;

    private formation currentFormation;
    private final inscription_formationCRUD inscriptionService = new inscription_formationCRUD();
    private Runnable onSuccessCallback;

    @FXML
    private void initialize() {
        // Initialiser les valeurs du statut
        comboStatut.getItems().addAll(StatutInscription.values());
        comboStatut.setValue(StatutInscription.EN_ATTENTE);

        // Afficher l'employé connecté
        updateEmployeConnecteLabel();
    }

    /**
     * Mettre à jour le label affichant l'employé connecté
     */
    private void updateEmployeConnecteLabel() {
        if (!SessionManager.isEmployeSelected()) {
            lblEmployeConnecte.setText("Aucun employé connecté");
            lblEmployeConnecte.setStyle("-fx-text-fill: #c74a4a;");
            return;
        }

        Integer employeId = SessionManager.getCurrentEmployeId();
        String employeInfo = getEmployeInfo(employeId);

        if (employeInfo != null) {
            lblEmployeConnecte.setText(employeInfo);
            lblEmployeConnecte.setStyle("-fx-text-fill: #2d7a3e; -fx-font-weight: bold;");
        } else {
            lblEmployeConnecte.setText("ID: " + employeId);
            lblEmployeConnecte.setStyle("-fx-text-fill: #2d7a3e; -fx-font-weight: bold;");
        }
    }

    /**
     * Récupérer les informations de l'employé depuis la base de données
     */
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
                return prenom + " " + nom + " (ID: " + idEmploye + ")";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Définir la formation pour laquelle l'inscription est faite
     */
    public void setFormation(formation f, Runnable onSuccess) {
        this.currentFormation = f;
        this.onSuccessCallback = onSuccess;
        loadFormationDetails();
    }

    /**
     * Charger les détails de la formation dans l'interface
     */
    private void loadFormationDetails() {
        if (currentFormation != null) {
            lblTitre.setText(currentFormation.getTitre());
            lblOrganisme.setText(safeText(currentFormation.getOrganisme()));

            String dates = formatDateRange(
                currentFormation.getDate_debut() != null ? currentFormation.getDate_debut().toString() : "-",
                currentFormation.getDate_fin() != null ? currentFormation.getDate_fin().toString() : "-"
            );
            lblDates.setText(dates);

            lblLieu.setText(safeText(currentFormation.getLieu()));
            lblCapacite.setText(safeText(currentFormation.getCapacite()));
        }
    }

    /**
     * Gérer l'inscription de l'employé à la formation
     */
    @FXML
    private void handleInscription() {
        // Vérifier qu'un employé est connecté
        if (!SessionManager.isEmployeSelected()) {
            showAlert(AlertType.ERROR, "Aucun employé connecté",
                "Veuillez d'abord vous connecter en tant qu'employé dans l'écran principal.");
            return;
        }

        if (!isFormValid()) {
            showAlert(AlertType.WARNING, "Erreur", "Impossible de procéder à l'inscription.");
            return;
        }

        Integer employeId = SessionManager.getCurrentEmployeId();

        // Vérifier si l'employé n'est pas déjà inscrit
        try {
            inscription_formation existingInscription = inscriptionService.getInscriptionByFormationAndEmploye(
                currentFormation.getId_formation(),
                employeId
            );

            if (existingInscription != null) {
                // Déjà inscrit - Afficher un message approprié selon le statut
                String message;
                if (existingInscription.getStatut() == StatutInscription.ACCEPTEE) {
                    message = "Vous êtes déjà inscrit et votre inscription a été ACCEPTÉE.\n" +
                             "Vous ne pouvez pas vous réinscrire.";
                } else if (existingInscription.getStatut() == StatutInscription.EN_ATTENTE) {
                    message = "Vous êtes déjà inscrit à cette formation.\n" +
                             "Votre inscription est EN ATTENTE de validation.";
                } else {
                    message = "Vous êtes déjà inscrit à cette formation.\n" +
                             "Statut: " + existingInscription.getStatut();
                }

                showAlert(AlertType.WARNING, "Inscription existante", message);
                return;
            }

            // Créer l'inscription avec l'ID de l'employé connecté
            inscription_formation inscription = new inscription_formation(
                currentFormation.getId_formation(),
                employeId,
                comboStatut.getValue()
            );

            inscriptionService.ajouter(inscription);
            showAlert(AlertType.INFORMATION, "Succès", "Inscription effectuée avec succès !");

            // Appeler le callback si défini
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }

            // Fermer la fenêtre
            closeWindow();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Échec de l'inscription: " + e.getMessage());
        }
    }

    /**
     * Annuler et fermer la fenêtre
     */
    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    /**
     * Valider le formulaire
     */
    private boolean isFormValid() {
        return SessionManager.isEmployeSelected() && currentFormation != null;
    }

    /**
     * Afficher une alerte
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Fermer la fenêtre actuelle
     */
    private void closeWindow() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    /**
     * Formater une plage de dates
     */
    private String formatDateRange(String start, String end) {
        if (start.equals("-") && end.equals("-")) {
            return "Non défini";
        }
        return start + " → " + end;
    }

    /**
     * Retourner un texte sûr (éviter null)
     */
    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "Non spécifié" : value;
    }
}
