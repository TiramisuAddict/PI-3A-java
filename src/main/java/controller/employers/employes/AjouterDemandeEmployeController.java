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
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
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

            // Setup date picker with validation (future dates only)
            setupDatePickerWithValidation();

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

    private void setupDatePickerWithValidation() {
        if (dateCreationPicker == null) return;

        // Date par défaut = aujourd'hui
        dateCreationPicker.setValue(LocalDate.now());

        Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb; -fx-opacity: 0.6;"); // Rose clair pour dates passées

                            setTooltip(new Tooltip("Les dates passées ne sont pas autorisées"));
                        } else if (item.equals(LocalDate.now())) {
                            setStyle("-fx-background-color: #90EE90; -fx-font-weight: bold;"); // Vert clair
                            setTooltip(new Tooltip("Aujourd'hui"));
                        }
                    }
                };
            }
        };

        dateCreationPicker.setDayCellFactory(dayCellFactory);

        dateCreationPicker.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            validateDateInput();
        });

        dateCreationPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.isBefore(LocalDate.now())) {
                Platform.runLater(() -> {
                    dateCreationPicker.setValue(LocalDate.now());
                    showDateError("La date ne peut pas être dans le passé. Date réinitialisée à aujourd'hui.");
                });
            } else {
                clearDateError();
            }
        });

        dateCreationPicker.setOnAction(event -> {
            validateDateInput();
        });
    }

    private void validateDateInput() {
        if (dateCreationPicker == null) return;

        LocalDate selectedDate = dateCreationPicker.getValue();

        if (selectedDate == null) {
            return; // Sera validé dans validateForm()
        }

        if (selectedDate.isBefore(LocalDate.now())) {
            showDateError("La date doit être aujourd'hui ou une date future");
            dateCreationPicker.setValue(LocalDate.now());
        } else {
            clearDateError();
        }
    }

    private void showDateError(String message) {
        if (dateError != null) {
            dateError.setText(message);
            dateError.setStyle("-fx-text-fill: #e74c3c;");
        }
        if (dateCreationPicker != null) {
            dateCreationPicker.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
        }
    }

    private void clearDateError() {
        if (dateError != null) {
            dateError.setText("");
        }
        if (dateCreationPicker != null) {
            dateCreationPicker.setStyle("");
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
                if (nv != null && !nv.isBefore(LocalDate.now())) {
                    formHelper.clearFieldError(dateCreationPicker, dateError);
                }
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

        // Validation du titre
        if (titreField == null || titreField.getText() == null || titreField.getText().trim().isEmpty()) {
            if (titreError != null) titreError.setText("Le titre est obligatoire");
            if (titreField != null) titreField.setStyle("-fx-border-color: #e74c3c;");
            valid = false;
        } else {
            // Validation de la longueur du titre
            String titre = titreField.getText().trim();
            if (titre.length() < 3) {
                if (titreError != null) titreError.setText("Le titre doit contenir au moins 3 caractères");
                titreField.setStyle("-fx-border-color: #e74c3c;");
                valid = false;
            } else if (titre.length() > 100) {
                if (titreError != null) titreError.setText("Le titre ne peut pas dépasser 100 caractères");
                titreField.setStyle("-fx-border-color: #e74c3c;");
                valid = false;
            }
        }

        // Validation de la catégorie
        if (categorieCombo == null || categorieCombo.getValue() == null) {
            if (categorieError != null) categorieError.setText("La catégorie est obligatoire");
            if (categorieCombo != null) categorieCombo.setStyle("-fx-border-color: #e74c3c;");
            valid = false;
        }

        // Validation du type
        if (typeDemandeCombo == null || typeDemandeCombo.getValue() == null) {
            if (typeError != null) typeError.setText("Le type est obligatoire");
            if (typeDemandeCombo != null) typeDemandeCombo.setStyle("-fx-border-color: #e74c3c;");
            valid = false;
        }

        // Validation de la priorité
        if (prioriteCombo == null || prioriteCombo.getValue() == null) {
            if (prioriteError != null) prioriteError.setText("La priorité est obligatoire");
            if (prioriteCombo != null) prioriteCombo.setStyle("-fx-border-color: #e74c3c;");
            valid = false;
        }

        // Validation de la description
        if (descriptionArea == null || descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            if (descriptionError != null) descriptionError.setText("La description est obligatoire");
            if (descriptionArea != null) descriptionArea.setStyle("-fx-border-color: #e74c3c;");
            valid = false;
        } else {
            // Validation de la longueur de la description
            String description = descriptionArea.getText().trim();
            if (description.length() < 10) {
                if (descriptionError != null) descriptionError.setText("La description doit contenir au moins 10 caractères");
                descriptionArea.setStyle("-fx-border-color: #e74c3c;");
                valid = false;
            } else if (description.length() > 2000) {
                if (descriptionError != null) descriptionError.setText("La description ne peut pas dépasser 2000 caractères");
                descriptionArea.setStyle("-fx-border-color: #e74c3c;");
                valid = false;
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // VALIDATION DE LA DATE - DOIT ÊTRE >= AUJOURD'HUI
        // ═══════════════════════════════════════════════════════════════════════════
        if (dateCreationPicker == null || dateCreationPicker.getValue() == null) {
            if (dateError != null) dateError.setText("La date est obligatoire");
            if (dateCreationPicker != null) dateCreationPicker.setStyle("-fx-border-color: #e74c3c;");
            valid = false;
        } else {
            LocalDate selectedDate = dateCreationPicker.getValue();
            LocalDate today = LocalDate.now();

            if (selectedDate.isBefore(today)) {
                if (dateError != null) {
                    dateError.setText("⚠️ La date ne peut pas être dans le passé");
                }
                dateCreationPicker.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                valid = false;
            } else {
                // Optionnel: Vérifier que la date n'est pas trop loin dans le futur (ex: max 1 an)
                LocalDate maxDate = today.plusYears(1);
                if (selectedDate.isAfter(maxDate)) {
                    if (dateError != null) {
                        dateError.setText("⚠️ La date ne peut pas dépasser 1 an dans le futur");
                    }
                    dateCreationPicker.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                    valid = false;
                }
            }
        }

        if (formHelper != null && !formHelper.validateDynamicFields()) {
            valid = false;
        }

        return valid;
    }

    @FXML
    private void reinitialiserFormulaire() {
        if (titreField != null) {
            titreField.clear();
            titreField.setStyle("");
        }
        if (descriptionArea != null) {
            descriptionArea.clear();
            descriptionArea.setStyle("");
        }
        if (categorieCombo != null) {
            categorieCombo.setValue(null);
            categorieCombo.setStyle("");
        }
        if (typeDemandeCombo != null) {
            typeDemandeCombo.setValue(null);
            typeDemandeCombo.getItems().clear();
            typeDemandeCombo.setStyle("");
        }
        if (prioriteCombo != null) {
            prioriteCombo.setValue("NORMALE");
            prioriteCombo.setStyle("");
        }
        if (dateCreationPicker != null) {
            dateCreationPicker.setValue(LocalDate.now());
            dateCreationPicker.setStyle("");
        }

        clearAllErrors();

        if (formHelper != null) {
            formHelper.updateDynamicFields(null, dynamicFieldsContainer, detailsPane);
        }
    }

    private void clearAllErrors() {
        if (titreError != null) titreError.setText("");
        if (categorieError != null) categorieError.setText("");
        if (typeError != null) typeError.setText("");
        if (prioriteError != null) prioriteError.setText("");
        if (descriptionError != null) descriptionError.setText("");
        if (dateError != null) dateError.setText("");
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