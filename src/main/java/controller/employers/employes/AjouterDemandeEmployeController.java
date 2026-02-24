package controller.employers.employes;

import controller.demandes.DemandeFormHelper;
import controller.demandes.NavigationHelper;
import entities.demande.Demande;
import entities.demande.DemandeDetails;
import entities.employe.employe;
import entities.employe.session;
import service.api.EmailService;
import service.demande.DemandeCRUD;
import service.demande.DemandeDetailsCRUD;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AjouterDemandeEmployeController implements Initializable {

    @FXML private TextField titreField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private ComboBox<String> typeDemandeCombo;
    @FXML private ComboBox<String> prioriteCombo;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker dateCreationPicker;
    @FXML private Label titreError, categorieError, typeError,
            prioriteError, descriptionError, dateError;
    @FXML private TitledPane detailsPane;
    @FXML private VBox dynamicFieldsContainer;
    @FXML private Button submitButton;
    @FXML private Button resetButton;

    private DemandeCRUD demandeCRUD;
    private DemandeDetailsCRUD detailsCRUD;
    private DemandeFormHelper formHelper;
    private EmailService emailService;
    private DemandesEmployeController parentController;
    private volatile boolean isSubmitting = false;

    public void setParentController(DemandesEmployeController p) {
        this.parentController = p;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        demandeCRUD = new DemandeCRUD();
        detailsCRUD = new DemandeDetailsCRUD();
        formHelper = new DemandeFormHelper();
        emailService = new EmailService();

        initializeUI();
    }

    private void initializeUI() {
        try {
            formHelper.initializeEmployeeComboBoxes(categorieCombo, typeDemandeCombo, prioriteCombo);
            formHelper.setupDatePicker(dateCreationPicker);

            typeDemandeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    formHelper.updateDynamicFields(newValue, dynamicFieldsContainer, detailsPane);
                } else {
                    formHelper.updateDynamicFields(null, dynamicFieldsContainer, detailsPane);
                }
            });

            setupRealtimeValidation();
        } catch (Exception e) {
            System.err.println("Error initializing UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupRealtimeValidation() {
        if (titreField != null) {
            titreField.textProperty().addListener((o, ov, nv) -> {
                if (nv != null && !nv.trim().isEmpty()) {
                    formHelper.clearFieldError(titreField, titreError);
                }
            });
        }
        if (descriptionArea != null) {
            descriptionArea.textProperty().addListener((o, ov, nv) -> {
                if (nv != null && !nv.trim().isEmpty()) {
                    formHelper.clearFieldError(descriptionArea, descriptionError);
                }
            });
        }
        if (categorieCombo != null) {
            categorieCombo.valueProperty().addListener((o, ov, nv) -> {
                if (nv != null) formHelper.clearFieldError(categorieCombo, categorieError);
            });
        }
        if (typeDemandeCombo != null) {
            typeDemandeCombo.valueProperty().addListener((o, ov, nv) -> {
                if (nv != null) formHelper.clearFieldError(typeDemandeCombo, typeError);
            });
        }
        if (prioriteCombo != null) {
            prioriteCombo.valueProperty().addListener((o, ov, nv) -> {
                if (nv != null) formHelper.clearFieldError(prioriteCombo, prioriteError);
            });
        }
        if (dateCreationPicker != null) {
            dateCreationPicker.valueProperty().addListener((o, ov, nv) -> {
                if (nv != null) formHelper.clearFieldError(dateCreationPicker, dateError);
            });
        }
    }

    @FXML
    private void ajouterDemande() {
        if (isSubmitting) {
            return;
        }

        if (!validateForm()) {
            return;
        }

        isSubmitting = true;
        setFormEnabled(false);

        final String titre = titreField.getText().trim();
        final String description = descriptionArea.getText().trim();
        final String categorie = categorieCombo.getValue();
        final String typeDemande = typeDemandeCombo.getValue();
        final String priorite = prioriteCombo.getValue();
        final LocalDate dateCreation = dateCreationPicker.getValue();
        final String detailsJson = formHelper.buildDetailsJson();

        employe currentEmployee = session.getEmploye();
        if (currentEmployee == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Session expirée. Veuillez vous reconnecter.");
            isSubmitting = false;
            setFormEnabled(true);
            return;
        }

        final int employeId = currentEmployee.getId_employé();
        final String empName = getEmployeeName(currentEmployee);
        final String empEmail = getEmployeeEmail(currentEmployee);

        Task<Boolean> submitTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Demande demande = new Demande();
                demande.setTitre(titre);
                demande.setDescription(description);
                demande.setCategorie(categorie);
                demande.setTypeDemande(typeDemande);
                demande.setPriorite(priorite);
                demande.setStatus("Nouvelle");
                demande.setDateCreation(java.sql.Date.valueOf(dateCreation));
                demande.setIdEmploye(employeId);

                demandeCRUD.ajouter(demande);

                if (!detailsJson.equals("{}")) {
                    DemandeDetails details = new DemandeDetails();
                    details.setIdDemande(demande.getIdDemande());
                    details.setDetails(detailsJson);
                    detailsCRUD.ajouter(details);
                }

                emailService.sendNewDemandeToRH(
                        empName,
                        empEmail,
                        demande.getTitre(),
                        demande.getTypeDemande(),
                        demande.getPriorite()
                ).thenAccept(success -> {
                    System.out.println("Email notification: " + (success ? "sent" : "failed"));
                });

                return true;
            }
        };

        submitTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                isSubmitting = false;
                setFormEnabled(true);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Demande soumise avec succès!\nNotification envoyée au service RH.");
                retourListe();
            });
        });

        submitTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                isSubmitting = false;
                setFormEnabled(true);
                Throwable ex = submitTask.getException();
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de la soumission: " + (ex != null ? ex.getMessage() : "Erreur inconnue"));
                if (ex != null) ex.printStackTrace();
            });
        });

        Thread submitThread = new Thread(submitTask);
        submitThread.setDaemon(true);
        submitThread.start();
    }

    private void setFormEnabled(boolean enabled) {
        if (titreField != null) titreField.setDisable(!enabled);
        if (descriptionArea != null) descriptionArea.setDisable(!enabled);
        if (categorieCombo != null) categorieCombo.setDisable(!enabled);
        if (typeDemandeCombo != null) typeDemandeCombo.setDisable(!enabled);
        if (prioriteCombo != null) prioriteCombo.setDisable(!enabled);
        if (dateCreationPicker != null) dateCreationPicker.setDisable(!enabled);
        if (submitButton != null) {
            submitButton.setDisable(!enabled);
            submitButton.setText(enabled ? "✅ Soumettre" : "⏳ Envoi en cours...");
        }
        if (resetButton != null) resetButton.setDisable(!enabled);
        if (dynamicFieldsContainer != null) dynamicFieldsContainer.setDisable(!enabled);
    }

    private String getEmployeeName(employe emp) {
        if (emp == null) return "Employe";
        String nom = emp.getNom() != null ? emp.getNom() : "";
        String prenom = emp.getPrenom() != null ? emp.getPrenom() : "";
        String fullName = (nom + " " + prenom).trim();
        return fullName.isEmpty() ? "Employe #" + emp.getId_employé() : fullName;
    }

    private String getEmployeeEmail(employe emp) {
        if (emp == null) return "unknown@company.com";
        String email = emp.getE_mail();
        return (email != null && !email.isEmpty()) ? email : "employe" + emp.getId_employé() + "@company.com";
    }

    private boolean validateForm() {
        boolean valid = true;

        if (titreField == null || titreField.getText() == null || titreField.getText().trim().isEmpty()) {
            if (titreError != null) titreError.setText("Le titre est obligatoire");
            if (titreField != null) titreField.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (categorieCombo == null || categorieCombo.getValue() == null) {
            if (categorieError != null) categorieError.setText("La catégorie est obligatoire");
            if (categorieCombo != null) categorieCombo.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (typeDemandeCombo == null || typeDemandeCombo.getValue() == null) {
            if (typeError != null) typeError.setText("Le type est obligatoire");
            if (typeDemandeCombo != null) typeDemandeCombo.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (prioriteCombo == null || prioriteCombo.getValue() == null) {
            if (prioriteError != null) prioriteError.setText("La priorité est obligatoire");
            if (prioriteCombo != null) prioriteCombo.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (descriptionArea == null || descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            if (descriptionError != null) descriptionError.setText("La description est obligatoire");
            if (descriptionArea != null) descriptionArea.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (dateCreationPicker == null || dateCreationPicker.getValue() == null) {
            if (dateError != null) dateError.setText("La date est obligatoire");
            if (dateCreationPicker != null) dateCreationPicker.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (formHelper != null && !formHelper.validateDynamicFields()) {
            valid = false;
        }

        return valid;
    }

    @FXML
    private void reinitialiserFormulaire() {
        if (titreField != null) titreField.clear();
        if (descriptionArea != null) descriptionArea.clear();
        if (categorieCombo != null) categorieCombo.setValue(null);
        if (typeDemandeCombo != null) {
            typeDemandeCombo.setValue(null);
            typeDemandeCombo.getItems().clear();
        }
        if (prioriteCombo != null) prioriteCombo.setValue("NORMALE");
        if (dateCreationPicker != null) dateCreationPicker.setValue(LocalDate.now());

        if (formHelper != null) {
            formHelper.updateDynamicFields(null, dynamicFieldsContainer, detailsPane);
            formHelper.clearFieldError(titreField, titreError);
            formHelper.clearFieldError(categorieCombo, categorieError);
            formHelper.clearFieldError(typeDemandeCombo, typeError);
            formHelper.clearFieldError(prioriteCombo, prioriteError);
            formHelper.clearFieldError(descriptionArea, descriptionError);
            formHelper.clearFieldError(dateCreationPicker, dateError);
        }
    }

    @FXML
    private void retourListe() {
        try {
            NavigationHelper.loadView("/emp/employes/demandes-employe.fxml");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de retourner à la liste: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}