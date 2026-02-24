package controller.employers.RHetAdminE;

import controller.demandes.NavigationHelper;
import entities.demande.Demande;
import entities.demande.HistoriqueDemande;
import entities.employe.employe;
import entities.employe.session;
import service.api.EmailService;
import service.demande.DemandeCRUD;
import service.demande.HistoriqueDemandeCRUD;
import service.employe.employeCRUD;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class AvancerDemandeController implements Initializable {

    @FXML private Label infoTitreLabel;
    @FXML private Label infoTypeLabel;
    @FXML private Label infoPrioriteLabel;
    @FXML private Label infoStatusLabel;
    @FXML private HBox statusFlowContainer;

    @FXML private ComboBox<String> nouveauStatutCombo;
    @FXML private Label acteurLabel;
    @FXML private TextArea commentaireArea;
    @FXML private Label statutError;
    @FXML private Label commentaireError;

    @FXML private TableView<HistoriqueDemande> historiqueTable;
    @FXML private TableColumn<HistoriqueDemande, String> ancienStatutCol;
    @FXML private TableColumn<HistoriqueDemande, String> nouveauStatutCol;
    @FXML private TableColumn<HistoriqueDemande, String> acteurCol;
    @FXML private TableColumn<HistoriqueDemande, Date> dateActionCol;
    @FXML private TableColumn<HistoriqueDemande, String> commentaireCol;

    private DemandeCRUD demandeCRUD;
    private HistoriqueDemandeCRUD historiqueCRUD;
    private employeCRUD employeCrud;
    private EmailService emailService;
    private DemandesController parentController;
    private Demande currentDemande;

    private String connectedActeurRole;
    private String connectedActeurFullName;

    private static final List<String> STATUS_ORDER = Arrays.asList(
            "Nouvelle", "En cours", "En attente", "Résolue", "Fermée"
    );

    public void setParentController(DemandesController parentController) {
        this.parentController = parentController;
    }

    public void setDemande(Demande demande) {
        this.currentDemande = demande;
        if (demande != null) {
            fillDemandeInfo(demande);
            buildStatusFlow(demande.getStatus());
            setupAllowedStatuses(demande.getStatus());
            loadHistorique(demande.getIdDemande());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        demandeCRUD = new DemandeCRUD();
        historiqueCRUD = new HistoriqueDemandeCRUD();
        employeCrud = new employeCRUD();
        emailService = new EmailService();

        detectConnectedUser();
        initializeHistoriqueTable();
        setupRealtimeValidation();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("📧 Email Service Status: " + (emailService.isEnabled() ? "✅ ENABLED" : "❌ DISABLED"));
        System.out.println("═══════════════════════════════════════════════");
    }

    private void detectConnectedUser() {
        connectedActeurRole = "RH";
        connectedActeurFullName = "Utilisateur";

        try {
            employe emp = session.getEmploye();
            if (emp != null) {
                String nom = emp.getNom() != null ? emp.getNom() : "";
                String prenom = emp.getPrenom() != null ? emp.getPrenom() : "";
                connectedActeurFullName = (nom + " " + prenom).trim();

                if (connectedActeurFullName.isEmpty()) {
                    connectedActeurFullName = "Utilisateur #" + emp.getId_employé();
                }

                if (emp.getRole() != null) {
                    String roleStr = emp.getRole().toString().toUpperCase();
                    if (roleStr.contains("RH")) {
                        connectedActeurRole = "RH";
                    } else if (roleStr.contains("ADMIN")) {
                        connectedActeurRole = "ADMIN";
                    } else if (roleStr.contains("CHEF")) {
                        connectedActeurRole = "CHEF PROJET";
                    } else {
                        connectedActeurRole = emp.getRole().getLibelle();
                    }
                }
            } else if (session.getCompte() != null) {
                String email = session.getCompte().getE_mail();
                if (email != null && !email.isEmpty()) {
                    connectedActeurFullName = email;
                }
            }
        } catch (Exception e) {
            System.out.println("Role detection: " + e.getMessage());
        }

        updateActeurLabel();
    }

    private void updateActeurLabel() {
        if (acteurLabel == null) return;

        String roleIcon = "👤";
        String roleColor = "#8e44ad";
        String bgColor = "#f4ecf7";

        if (connectedActeurRole.contains("ADMIN")) {
            roleIcon = "🔧";
            roleColor = "#c0392b";
            bgColor = "#fadbd8";
        } else if (connectedActeurRole.contains("CHEF")) {
            roleIcon = "👔";
            roleColor = "#2980b9";
            bgColor = "#d4e6f1";
        }

        acteurLabel.setText(roleIcon + " " + connectedActeurFullName + "  —  " + connectedActeurRole);
        acteurLabel.setStyle(
                "-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: " + roleColor + ";"
                        + " -fx-background-color: " + bgColor + ";"
                        + " -fx-padding: 10 15; -fx-background-radius: 8;"
        );
    }

    private void initializeHistoriqueTable() {
        if (ancienStatutCol != null) ancienStatutCol.setCellValueFactory(new PropertyValueFactory<>("ancienStatut"));
        if (nouveauStatutCol != null) nouveauStatutCol.setCellValueFactory(new PropertyValueFactory<>("nouveauStatut"));
        if (acteurCol != null) acteurCol.setCellValueFactory(new PropertyValueFactory<>("acteur"));
        if (dateActionCol != null) dateActionCol.setCellValueFactory(new PropertyValueFactory<>("dateAction"));
        if (commentaireCol != null) commentaireCol.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
    }

    private void fillDemandeInfo(Demande demande) {
        if (infoTitreLabel != null) infoTitreLabel.setText(demande.getTitre());
        if (infoTypeLabel != null) infoTypeLabel.setText(demande.getTypeDemande());

        if (infoPrioriteLabel != null) {
            String priorite = demande.getPriorite() != null ? demande.getPriorite() : "N/A";
            infoPrioriteLabel.setText(priorite);
            switch (priorite.toUpperCase()) {
                case "HAUTE":
                    infoPrioriteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    break;
                case "NORMALE":
                    infoPrioriteLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                    break;
                case "BASSE":
                    infoPrioriteLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    break;
            }
        }

        if (infoStatusLabel != null) {
            infoStatusLabel.setText(demande.getStatus());
        }
    }

    private void buildStatusFlow(String currentStatus) {
        if (statusFlowContainer == null) return;
        statusFlowContainer.getChildren().clear();

        int currentIndex = STATUS_ORDER.indexOf(currentStatus);

        for (int i = 0; i < STATUS_ORDER.size(); i++) {
            String status = STATUS_ORDER.get(i);
            Label statusLabel = new Label(status);
            statusLabel.setMinWidth(80);
            statusLabel.setAlignment(Pos.CENTER);

            if (i < currentIndex) {
                statusLabel.setStyle("-fx-background-color: #d5f5e3; -fx-text-fill: #27ae60; -fx-padding: 8 12; -fx-background-radius: 15; -fx-font-weight: bold;");
            } else if (i == currentIndex) {
                statusLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 15; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #bdc3c7; -fx-padding: 8 12; -fx-background-radius: 15;");
            }

            statusFlowContainer.getChildren().add(statusLabel);

            if (i < STATUS_ORDER.size() - 1) {
                Label arrow = new Label("→");
                arrow.setStyle("-fx-font-size: 16; -fx-text-fill: #bdc3c7;");
                statusFlowContainer.getChildren().add(arrow);
            }
        }
    }

    private void setupAllowedStatuses(String currentStatus) {
        List<String> allowed = new ArrayList<>();
        switch (currentStatus) {
            case "Nouvelle": allowed.addAll(Arrays.asList("En cours", "En attente")); break;
            case "En cours": allowed.addAll(Arrays.asList("En attente", "Résolue")); break;
            case "En attente": allowed.addAll(Arrays.asList("En cours", "Résolue")); break;
            case "Résolue": allowed.addAll(Arrays.asList("Fermée", "En cours")); break;
        }
        nouveauStatutCombo.setItems(FXCollections.observableArrayList(allowed));
        if (allowed.isEmpty()) {
            nouveauStatutCombo.setDisable(true);
            nouveauStatutCombo.setPromptText("Aucune transition possible");
        }
    }

    private void loadHistorique(int idDemande) {
        try {
            List<HistoriqueDemande> historiques = historiqueCRUD.getByDemande(idDemande);
            if (historiqueTable != null) {
                historiqueTable.setItems(FXCollections.observableArrayList(historiques));
            }
        } catch (SQLException e) {
            System.err.println("Error loading historique: " + e.getMessage());
        }
    }

    private void setupRealtimeValidation() {
        if (nouveauStatutCombo != null) {
            nouveauStatutCombo.valueProperty().addListener((obs, o, n) -> {
                if (n != null && statutError != null) {
                    statutError.setText("");
                    nouveauStatutCombo.setStyle("");
                }
            });
        }
        if (commentaireArea != null) {
            commentaireArea.textProperty().addListener((obs, o, n) -> {
                if (n != null && !n.trim().isEmpty() && commentaireError != null) {
                    commentaireError.setText("");
                    commentaireArea.setStyle("");
                }
            });
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        if (nouveauStatutCombo.getValue() == null) {
            if (statutError != null) statutError.setText("Sélectionnez un nouveau statut");
            nouveauStatutCombo.setStyle("-fx-border-color: red; -fx-border-width: 1.5;");
            valid = false;
        }

        String comment = commentaireArea.getText();
        if (comment == null || comment.trim().isEmpty()) {
            if (commentaireError != null) commentaireError.setText("Le commentaire est obligatoire");
            commentaireArea.setStyle("-fx-border-color: red; -fx-border-width: 1.5;");
            valid = false;
        } else if (comment.trim().length() < 5) {
            if (commentaireError != null) commentaireError.setText("Minimum 5 caractères");
            commentaireArea.setStyle("-fx-border-color: red; -fx-border-width: 1.5;");
            valid = false;
        }

        return valid;
    }

    @FXML
    private void avancerStatut() {
        if (!validateForm()) return;

        String ancienStatut = currentDemande.getStatus();
        String nouveauStatut = nouveauStatutCombo.getValue();
        String acteur = connectedActeurFullName + " (" + connectedActeurRole + ")";
        String commentaire = commentaireArea.getText().trim();

        try {
            // Update demande
            currentDemande.setStatus(nouveauStatut);
            demandeCRUD.modifier(currentDemande);

            // Add historique
            HistoriqueDemande historique = new HistoriqueDemande();
            historique.setIdDemande(currentDemande.getIdDemande());
            historique.setAncienStatut(ancienStatut);
            historique.setNouveauStatut(nouveauStatut);
            historique.setDateAction(new java.util.Date());
            historique.setActeur(acteur);
            historique.setCommentaire(commentaire);
            historiqueCRUD.ajouter(historique);

            // Send email
            sendEmailToEmployee(ancienStatut, nouveauStatut, acteur, commentaire);

            showAlert(Alert.AlertType.INFORMATION, "Succès ✅",
                    "Statut changé: " + ancienStatut + " → " + nouveauStatut +
                            "\nPar: " + acteur +
                            "\n\n📧 Notification envoyée.");

            retourListe();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EMAIL - SIMPLIFIED - USING ONLY employe.getE_mail()
    // ═══════════════════════════════════════════════════════════════════════

    private void sendEmailToEmployee(String ancienStatut, String nouveauStatut,
                                     String acteur, String commentaire) {
        try {
            System.out.println("═══════════════════════════════════════════════");
            System.out.println("📧 SENDING EMAIL NOTIFICATION");
            System.out.println("   Demande ID: " + currentDemande.getIdDemande());
            System.out.println("   Employee ID: " + currentDemande.getIdEmploye());

            // Get employee using existing method
            employe emp = employeCrud.getById(currentDemande.getIdEmploye());

            if (emp == null) {
                System.err.println("❌ Employee not found for ID: " + currentDemande.getIdEmploye());
                return;
            }

            // Get name
            String nom = emp.getNom() != null ? emp.getNom() : "";
            String prenom = emp.getPrenom() != null ? emp.getPrenom() : "";
            String empName = (prenom + " " + nom).trim();
            if (empName.isEmpty()) {
                empName = "Employé #" + emp.getId_employé();
            }

            // Get email directly from employe entity
            String empEmail = emp.getE_mail();

            System.out.println("   Employee Name: " + empName);
            System.out.println("   Employee Email: " + empEmail);
            System.out.println("═══════════════════════════════════════════════");

            // Validate email
            if (empEmail == null || empEmail.isEmpty() || !empEmail.contains("@")) {
                System.err.println("❌ Invalid email for employee: " + empEmail);
                return;
            }

            // Send email asynchronously
            emailService.sendStatusChangeToEmployee(
                    empName,
                    empEmail,
                    currentDemande.getTitre(),
                    ancienStatut,
                    nouveauStatut,
                    acteur,
                    commentaire
            ).thenAccept(success -> {
                Platform.runLater(() -> {
                    System.out.println(success ?
                            "✅ Email sent to: " + empEmail :
                            "❌ Email failed to: " + empEmail);
                });
            }).exceptionally(ex -> {
                System.err.println("❌ Email error: " + ex.getMessage());
                return null;
            });

        } catch (Exception e) {
            System.err.println("❌ Email error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void retourListe() {
        try {
            NavigationHelper.loadView("/emp/RHetAdminE/demandes.fxml");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}