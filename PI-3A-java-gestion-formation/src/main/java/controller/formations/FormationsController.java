package controller.formations;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import models.formation;
import models.inscription_formation;
import models.StatutInscription;
import service.formationCRUD;
import service.inscription_formationCRUD;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.LocalDate;
import java.util.List;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class FormationsController {

    @FXML
    private FlowPane statisticsPane;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblUpcoming;
    @FXML
    private Label lblOngoing;
    @FXML
    private Label lblCompleted;

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> filterLieu;
    @FXML
    private VBox formationsContainer;

    // Onglet Liste des Formations (pour employés)
    @FXML
    private TextField txtSearchEmploye;
    @FXML
    private ComboBox<String> filterLieuEmploye;
    @FXML
    private VBox availableFormationsContainer;

    @FXML
    private TextField txtTitre;
    @FXML
    private TextField txtOrganisme;
    @FXML
    private DatePicker dpDateDebut;
    @FXML
    private DatePicker dpDateFin;
    @FXML
    private TextField txtLieu;
    @FXML
    private TextField txtCapacite;

    @FXML
    private Button btnSave;

    private final formationCRUD formationService = new formationCRUD();
    private final inscription_formationCRUD inscriptionService = new inscription_formationCRUD();

    // ID employé temporaire (sera remplacé par l'authentification)
    private final int CURRENT_EMPLOYE_ID = 19; // TODO: Remplacer par l'ID de l'utilisateur connecté

    @FXML
    private void initialize() {
        refreshFormations();
        refreshAvailableFormations();
    }

    @FXML
    private void handleNewFormation() {
        clearForm();
    }

    @FXML
    private void handleSave() {
        if (!isFormValid()) {
            showAlert(AlertType.WARNING, "Champs requis", "Veuillez remplir le titre, l'organisme et les dates.");
            return;
        }

        formation f = new formation(
                txtTitre.getText().trim(),
                txtOrganisme.getText().trim(),
                dpDateDebut.getValue(),
                dpDateFin.getValue(),
                txtLieu.getText().trim(),
                txtCapacite.getText().trim()
        );

        try {
            formationService.ajouter(f);
            showAlert(AlertType.INFORMATION, "Succes", "Formation ajoutee avec succes.");
            clearForm();
            refreshFormations();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Echec de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        // TODO: implement update logic
    }

    @FXML
    private void handleDelete() {
        // TODO: implement delete logic
    }

    private void clearForm() {
        txtTitre.clear();
        txtOrganisme.clear();
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
        txtLieu.clear();
        txtCapacite.clear();
    }

    private boolean isFormValid() {
        return txtTitre != null && !txtTitre.getText().trim().isEmpty()
                && txtOrganisme != null && !txtOrganisme.getText().trim().isEmpty()
                && dpDateDebut != null && dpDateDebut.getValue() != null
                && dpDateFin != null && dpDateFin.getValue() != null;
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void refreshFormations() {
        try {
            List<formation> formations = formationService.afficher();
            renderFormations(formations);
            updateStats(formations);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Echec du chargement: " + e.getMessage());
        }
    }

    private void renderFormations(List<formation> formations) {
        formationsContainer.getChildren().clear();

        for (formation f : formations) {
            VBox card = new VBox(8);
            card.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 6; " +
                    "-fx-padding: 12; -fx-border-color: -color-border-muted; -fx-border-radius: 6;");

            // Header avec titre et badge
            HBox headerRow = new HBox(10);
            headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label title = new Label(f.getTitre());
            title.setFont(Font.font(title.getFont().getFamily(), FontWeight.BOLD, 14));

            // Badge pour les inscriptions en attente
            try {
                int pendingCount = inscriptionService.countPendingInscriptions(f.getId_formation());
                if (pendingCount > 0) {
                    Label badge = new Label(pendingCount + " en attente");
                    badge.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; " +
                            "-fx-padding: 2 8 2 8; -fx-background-radius: 10; -fx-font-size: 11px;");
                    headerRow.getChildren().addAll(title, badge);
                } else {
                    headerRow.getChildren().add(title);
                }
            } catch (SQLException e) {
                headerRow.getChildren().add(title);
            }

            Label organisme = new Label("Organisme: " + safeText(f.getOrganisme()));

            String dates = formatDateRange(f.getDate_debut(), f.getDate_fin());
            Label dateRange = new Label("Dates: " + dates);

            Label lieu = new Label("Lieu: " + safeText(f.getLieu()));
            Label capacite = new Label("Capacite: " + safeText(f.getCapacite()));

            HBox metaRow = new HBox(12, lieu, capacite);
            metaRow.setFillHeight(true);

            Button btnEdit = new Button("Modifier");
            btnEdit.getStyleClass().add("accent");
            btnEdit.setOnAction(event -> openEditWindow(event, f));

            Button btnDeleteCard = new Button("Supprimer");
            btnDeleteCard.getStyleClass().addAll("danger", "outline");
            btnDeleteCard.setOnAction(event -> handleDeleteFromCard(f));

            Button btnAccept = new Button("Accepter Inscriptions");
            btnAccept.getStyleClass().addAll("accent", "outline");
            btnAccept.setOnAction(event -> openAcceptInscriptionsWindow(event, f));

            HBox actionsRow = new HBox(10, btnEdit, btnDeleteCard, btnAccept);

            card.getChildren().addAll(headerRow, organisme, dateRange, metaRow, actionsRow);
            formationsContainer.getChildren().add(card);
        }
    }

    private void openEditWindow(javafx.event.ActionEvent event, formation f) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit-formation.fxml"));
            Parent root = loader.load();

            EditFormationController controller = loader.getController();
            controller.setFormation(f, this::refreshFormations);

            Stage stage = new Stage();
            stage.setTitle("Modifier une formation");
            stage.setScene(new Scene(root));
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur", "Echec d'ouverture de l'edition: " + e.getMessage());
        }
    }

    private void openInscriptionWindow(javafx.event.ActionEvent event, formation f) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/inscription-formation.fxml"));
            Parent root = loader.load();

            inscription_formationController controller = loader.getController();
            controller.setFormation(f, () -> {
                refreshFormations();
                refreshAvailableFormations();
            });

            Stage stage = new Stage();
            stage.setTitle("Inscription à la formation");
            stage.setScene(new Scene(root));
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur", "Echec d'ouverture de l'inscription: " + e.getMessage());
        }
    }

    private void handleDeleteFromCard(formation f) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText("Supprimer la formation ?");
        confirm.setContentText("Cette action est irreversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                formationService.supprimer(f.getId_formation());
                refreshFormations();
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur", "Echec de la suppression: " + e.getMessage());
            }
        }
    }


    private void updateStats(List<formation> formations) {
        int total = formations.size();
        int upcoming = 0;
        int ongoing = 0;
        int completed = 0;
        LocalDate today = LocalDate.now();

        for (formation f : formations) {
            LocalDate start = f.getDate_debut();
            LocalDate end = f.getDate_fin();

            if (start != null && start.isAfter(today)) {
                upcoming++;
            } else if (start != null && end != null && !start.isAfter(today) && !end.isBefore(today)) {
                ongoing++;
            } else if (end != null && end.isBefore(today)) {
                completed++;
            }
        }

        lblTotal.setText(String.valueOf(total));
        lblUpcoming.setText(String.valueOf(upcoming));
        lblOngoing.setText(String.valueOf(ongoing));
        lblCompleted.setText(String.valueOf(completed));
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return "N/A";
        }
        if (start != null && end == null) {
            return start.toString() + " -";
        }
        if (start == null) {
            return "- " + end;
        }
        return start + " - " + end;
    }

    /**
     * Rafraîchir la liste des formations disponibles (pour l'onglet employé)
     */
    private void refreshAvailableFormations() {
        try {
            List<formation> formations = formationService.afficher();
            renderAvailableFormations(formations);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Echec du chargement: " + e.getMessage());
        }
    }

    /**
     * Afficher les formations disponibles avec bouton Inscription ou Annulation
     */
    private void renderAvailableFormations(List<formation> formations) {
        if (availableFormationsContainer == null) return;

        availableFormationsContainer.getChildren().clear();

        for (formation f : formations) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 8; " +
                    "-fx-padding: 18; -fx-border-color: -color-border-muted; -fx-border-radius: 8;");

            Label title = new Label(f.getTitre());
            title.setFont(Font.font(title.getFont().getFamily(), FontWeight.BOLD, 16));
            title.setStyle("-fx-text-fill: -color-fg-default;");

            Label organisme = new Label("📍 Organisme: " + safeText(f.getOrganisme()));
            organisme.setStyle("-fx-text-fill: -color-fg-muted;");

            String dates = formatDateRange(f.getDate_debut(), f.getDate_fin());
            Label dateRange = new Label("📅 Dates: " + dates);
            dateRange.setStyle("-fx-text-fill: -color-fg-muted;");

            Label lieu = new Label("🏢 Lieu: " + safeText(f.getLieu()));
            lieu.setStyle("-fx-text-fill: -color-fg-muted;");

            Label capacite = new Label("👥 Capacité: " + safeText(f.getCapacite()) + " places");
            capacite.setStyle("-fx-text-fill: -color-fg-muted;");

            // Vérifier si l'employé est déjà inscrit
            try {
                inscription_formation existingInscription = inscriptionService.getInscriptionByFormationAndEmploye(
                    f.getId_formation(), CURRENT_EMPLOYE_ID
                );

                if (existingInscription != null) {
                    // Déjà inscrit - Vérifier le statut
                    StatutInscription statut = existingInscription.getStatut();

                    Label statutLabel = new Label("Statut: " + getStatutText(statut));
                    statutLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
                        getStatutColor(statut) + "; -fx-font-size: 14px;");

                    if (statut == StatutInscription.ACCEPTEE) {
                        // Inscription acceptée - Ne pas permettre l'annulation
                        Label infoLabel = new Label("✓ Vous êtes inscrit(e) à cette formation");
                        infoLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold; -fx-font-size: 13px;");

                        card.getChildren().addAll(title, organisme, dateRange, lieu, capacite, statutLabel, infoLabel);

                    } else if (statut == StatutInscription.REFUSEE) {
                        // Inscription refusée - Permettre de se réinscrire
                        Label infoLabel = new Label("Votre inscription a été refusée. Vous pouvez réessayer.");
                        infoLabel.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");

                        Button btnReinscrire = new Button("↻ Réessayer l'inscription");
                        btnReinscrire.getStyleClass().add("accent");
                        btnReinscrire.setPrefHeight(40);
                        btnReinscrire.setMaxWidth(Double.MAX_VALUE);
                        btnReinscrire.setOnAction(event -> handleReinscrire(existingInscription, f));

                        card.getChildren().addAll(title, organisme, dateRange, lieu, capacite, statutLabel, infoLabel, btnReinscrire);

                    } else {
                        // EN_ATTENTE - Permettre l'annulation
                        Button btnAnnuler = new Button("✗ Annuler mon inscription");
                        btnAnnuler.getStyleClass().addAll("danger");
                        btnAnnuler.setPrefHeight(40);
                        btnAnnuler.setMaxWidth(Double.MAX_VALUE);
                        btnAnnuler.setOnAction(event -> handleAnnulerInscription(existingInscription));

                        card.getChildren().addAll(title, organisme, dateRange, lieu, capacite, statutLabel, btnAnnuler);
                    }
                } else {
                    // Pas encore inscrit - Afficher bouton S'inscrire
                    Button btnInscription = new Button("✓ S'inscrire à cette formation");
                    btnInscription.getStyleClass().add("accent");
                    btnInscription.setPrefHeight(40);
                    btnInscription.setMaxWidth(Double.MAX_VALUE);
                    btnInscription.setOnAction(event -> openInscriptionWindow(event, f));

                    card.getChildren().addAll(title, organisme, dateRange, lieu, capacite, btnInscription);
                }
            } catch (SQLException e) {
                // En cas d'erreur, afficher le bouton S'inscrire par défaut
                Button btnInscription = new Button("✓ S'inscrire à cette formation");
                btnInscription.getStyleClass().add("accent");
                btnInscription.setPrefHeight(40);
                btnInscription.setMaxWidth(Double.MAX_VALUE);
                btnInscription.setOnAction(event -> openInscriptionWindow(event, f));

                card.getChildren().addAll(title, organisme, dateRange, lieu, capacite, btnInscription);
            }

            availableFormationsContainer.getChildren().add(card);
        }
    }

    /**
     * Réinscrire après un refus
     */
    private void handleReinscrire(inscription_formation oldInscription, formation f) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la réinscription");
        confirm.setHeaderText("Nouvelle inscription");
        confirm.setContentText("Votre précédente inscription a été refusée. Voulez-vous soumettre une nouvelle demande ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Mettre à jour le statut à EN_ATTENTE
                oldInscription.setStatut(StatutInscription.EN_ATTENTE);
                inscriptionService.modifier(oldInscription);

                showAlert(AlertType.INFORMATION, "Succès", "Votre nouvelle demande d'inscription a été soumise !");
                refreshAvailableFormations();
                refreshFormations();
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Erreur", "Échec de la réinscription: " + e.getMessage());
            }
        }
    }

    /**
     * Obtenir le texte du statut en français
     */
    private String getStatutText(StatutInscription statut) {
        switch (statut) {
            case EN_ATTENTE: return "En attente de validation";
            case ACCEPTEE: return "Inscription acceptée ✓";
            case REFUSEE: return "Inscription refusée";
            default: return statut.name();
        }
    }

    /**
     * Obtenir la couleur du statut
     */
    private String getStatutColor(StatutInscription statut) {
        switch (statut) {
            case EN_ATTENTE: return "#ff9800";
            case ACCEPTEE: return "#4caf50";
            case REFUSEE: return "#f44336";
            default: return "#666666";
        }
    }

    /**
     * Annuler l'inscription d'un employé
     */
    private void handleAnnulerInscription(inscription_formation inscription) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer l'annulation");
        confirm.setHeaderText("Annuler votre inscription ?");
        confirm.setContentText("Êtes-vous sûr de vouloir annuler votre inscription à cette formation ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                inscriptionService.supprimer(inscription.getId_inscription());
                showAlert(AlertType.INFORMATION, "Succès", "Votre inscription a été annulée.");
                refreshAvailableFormations();
                refreshFormations(); // Rafraîchir aussi la vue RH
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Erreur", "Échec de l'annulation: " + e.getMessage());
            }
        }
    }

    /**
     * Ouvrir la fenêtre pour gérer les inscriptions (RH)
     */
    private void openAcceptInscriptionsWindow(javafx.event.ActionEvent event, formation f) {
        try {
            List<inscription_formation> inscriptions = inscriptionService.getInscriptionsByFormation(f.getId_formation());

            if (inscriptions.isEmpty()) {
                showAlert(AlertType.INFORMATION, "Information", "Aucune inscription pour cette formation.");
                return;
            }

            // Ouvrir la nouvelle fenêtre de gestion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion-inscriptions.fxml"));
            Parent root = loader.load();

            GestionInscriptionsController controller = loader.getController();
            controller.setFormation(f, () -> {
                refreshFormations();
                refreshAvailableFormations();
            });

            Stage stage = new Stage();
            stage.setTitle("Gestion des Inscriptions");
            stage.setScene(new Scene(root));
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.show();

        } catch (IOException | SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Échec d'ouverture: " + e.getMessage());
        }
    }
}
