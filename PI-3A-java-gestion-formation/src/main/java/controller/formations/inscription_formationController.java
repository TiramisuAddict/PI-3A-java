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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
    private ComboBox<EmployeItem> comboEmploye;
    @FXML
    private ComboBox<StatutInscription> comboStatut;

    @FXML
    private Button btnInscrire;
    @FXML
    private Button btnAnnuler;

    private formation currentFormation;
    private final inscription_formationCRUD inscriptionService = new inscription_formationCRUD();
    private Runnable onSuccessCallback;

    /**
     * Classe interne pour représenter un employé dans le ComboBox
     */
    private static class EmployeItem {
        private final int id;
        private final String nom;
        private final String prenom;

        public EmployeItem(int id, String nom, String prenom) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return id + " - " + prenom + " " + nom;
        }
    }

    @FXML
    private void initialize() {
        // Initialiser les valeurs du statut
        comboStatut.getItems().addAll(StatutInscription.values());
        comboStatut.setValue(StatutInscription.EN_ATTENTE);

        // Charger les IDs d'employés réels depuis la base de données
        loadEmployeIds();
    }

    /**
     * Charger les IDs d'employés disponibles depuis la table employé
     */
    private void loadEmployeIds() {
        Connection conn = MyDB.getInstance().getConn();
        try {
            String query = "SELECT id_employe, nom, prenom FROM employé ORDER BY id_employe";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("id_employe");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");

                // Ajouter l'employé avec toutes ses informations
                comboEmploye.getItems().add(new EmployeItem(id, nom, prenom));
            }

            if (comboEmploye.getItems().isEmpty()) {
                showAlert(AlertType.WARNING, "Attention",
                    "Aucun employé trouvé dans la base de données.\nVeuillez d'abord ajouter des employés.");
            }

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur",
                "Impossible de charger les employés: " + e.getMessage());
            System.err.println("Erreur lors du chargement des employés: " + e.getMessage());
        }
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
        if (!isFormValid()) {
            showAlert(AlertType.WARNING, "Champs requis", "Veuillez sélectionner un employé.");
            return;
        }

        int employeId = comboEmploye.getValue().getId();

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
                    message = "Cet employé est déjà inscrit et son inscription a été ACCEPTÉE.\n" +
                             "Vous ne pouvez pas vous réinscrire.";
                } else if (existingInscription.getStatut() == StatutInscription.EN_ATTENTE) {
                    message = "Cet employé est déjà inscrit à cette formation.\n" +
                             "Son inscription est EN ATTENTE de validation.";
                } else {
                    message = "Cet employé est déjà inscrit à cette formation.\n" +
                             "Statut: " + existingInscription.getStatut();
                }

                showAlert(AlertType.WARNING, "Inscription existante", message);
                return;
            }

            // Créer l'inscription avec l'ID de l'employé sélectionné
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
        return comboEmploye.getValue() != null && currentFormation != null;
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
