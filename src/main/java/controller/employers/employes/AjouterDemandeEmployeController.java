package controller.employers.employes;

import controller.demandes.DemandeFormHelper;
import controller.demandes.NavigationHelper;
import entities.demande.Demande;
import entities.demande.DemandeDetails;
import entities.employe.employe;
import entities.employe.session;
import service.api.EmailService;
import service.api.MapPickerDialog;  // ← Correct import
import service.demande.DemandeCRUD;
import service.demande.DemandeDetailsCRUD;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AjouterDemandeEmployeController implements Initializable {

    // ═══════════════════════════════════════════════════════════════════════════
    // FXML FIELDS
    // ═══════════════════════════════════════════════════════════════════════════

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

    // MAP / DESTINATION FIELDS
    @FXML private TextField destinationField;
    @FXML private Button mapButton;
    @FXML private Label destinationError;
    @FXML private HBox destinationContainer;

    // ═══════════════════════════════════════════════════════════════════════════
    // INSTANCE VARIABLES
    // ═══════════════════════════════════════════════════════════════════════════

    // Store selected location data
    private double selectedLat = 0;
    private double selectedLon = 0;
    private String selectedDestination = null;

    private DemandeCRUD demandeCRUD;
    private DemandeDetailsCRUD detailsCRUD;
    private DemandeFormHelper formHelper;
    private EmailService emailService;
    private DemandesEmployeController parentController;
    private volatile boolean isSubmitting = false;

    // Types de demandes qui nécessitent une destination
    private static final String[] DESTINATION_TYPES = {
            "Mission", "Déplacement", "Formation externe", "Voyage d'affaires",
            "Conférence", "Séminaire", "Visite client", "Voyage", "Transport"
    };

    // ═══════════════════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════

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
            setupDatePickerWithValidation();
            setupDestinationField();

            typeDemandeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    formHelper.updateDynamicFields(newValue, dynamicFieldsContainer, detailsPane);
                    updateDestinationVisibility(newValue);
                } else {
                    formHelper.updateDynamicFields(null, dynamicFieldsContainer, detailsPane);
                    hideDestinationField();
                }
            });

            setupRealtimeValidation();
        } catch (Exception e) {
            System.err.println("Error initializing UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DESTINATION / MAP METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    private void setupDestinationField() {
        if (destinationContainer != null) {
            destinationContainer.setVisible(false);
            destinationContainer.setManaged(false);
        }

        if (destinationField != null) {
            destinationField.setEditable(false);
            destinationField.setPromptText("Cliquez sur 📍 pour choisir");
        }
    }

    private void updateDestinationVisibility(String type) {
        boolean needsDestination = isDestinationType(type);

        if (destinationContainer != null) {
            destinationContainer.setVisible(needsDestination);
            destinationContainer.setManaged(needsDestination);
        }

        if (!needsDestination) {
            clearDestination();
        }
    }

    private boolean isDestinationType(String type) {
        if (type == null) return false;
        String typeLower = type.toLowerCase();
        for (String destType : DESTINATION_TYPES) {
            if (typeLower.contains(destType.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void hideDestinationField() {
        if (destinationContainer != null) {
            destinationContainer.setVisible(false);
            destinationContainer.setManaged(false);
        }
        clearDestination();
    }

    private void clearDestination() {
        selectedDestination = null;
        selectedLat = 0;
        selectedLon = 0;
        if (destinationField != null) {
            destinationField.clear();
            destinationField.setStyle("");
        }
        if (destinationError != null) {
            destinationError.setText("");
        }
    }

    /**
     * Ouvre le dialogue de sélection de carte
     */
    @FXML
    private void ouvrirCarte() {
        MapPickerDialog mapDialog = new MapPickerDialog();
        mapDialog.show(result -> {
            if (result != null) {
                Platform.runLater(() -> {
                    selectedDestination = result.cityName;
                    selectedLat = result.lat;
                    selectedLon = result.lon;

                    if (destinationField != null) {
                        destinationField.setText(result.cityName);
                        destinationField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                    }
                    if (destinationError != null) {
                        destinationError.setText("");
                    }

                    System.out.println("📍 Destination sélectionnée: " + result.cityName +
                            " (Lat: " + result.lat + ", Lon: " + result.lon + ")");
                });
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DATE VALIDATION
    // ═══════════════════════════════════════════════════════════════════════════

    private void setupDatePickerWithValidation() {
        if (dateCreationPicker == null) return;

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
                            setStyle("-fx-background-color: #ffc0cb; -fx-opacity: 0.6;");
                            setTooltip(new Tooltip("Les dates passées ne sont pas autorisées"));
                        } else if (item.equals(LocalDate.now())) {
                            setStyle("-fx-background-color: #90EE90; -fx-font-weight: bold;");
                            setTooltip(new Tooltip("Aujourd'hui"));
                        }
                    }
                };
            }
        };

        dateCreationPicker.setDayCellFactory(dayCellFactory);

        dateCreationPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.isBefore(LocalDate.now())) {
                Platform.runLater(() -> {
                    dateCreationPicker.setValue(LocalDate.now());
                    showDateError("La date ne peut pas être dans le passé.");
                });
            } else {
                clearDateError();
            }
        });
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

    // ═══════════════════════════════════════════════════════════════════════════
    // REALTIME VALIDATION
    // ═══════════════════════════════════════════════════════════════════════════

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
        if (destinationField != null) {
            destinationField.textProperty().addListener((o, ov, nv) -> {
                if (nv != null && !nv.trim().isEmpty()) {
                    if (destinationError != null) destinationError.setText("");
                    destinationField.setStyle("-fx-border-color: #27ae60;");
                }
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FORM SUBMISSION
    // ═══════════════════════════════════════════════════════════════════════════

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
        final String detailsJson = buildDetailsJsonWithDestination();

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
                        "✅ Demande soumise avec succès!\nNotification envoyée au service RH.");
                retourListe();
            });
        });

        submitTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                isSubmitting = false;
                setFormEnabled(true);
                Throwable ex = submitTask.getException();
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "❌ Erreur lors de la soumission: " + (ex != null ? ex.getMessage() : "Erreur inconnue"));
                if (ex != null) ex.printStackTrace();
            });
        });

        Thread submitThread = new Thread(submitTask);
        submitThread.setDaemon(true);
        submitThread.start();
    }

    /**
     * Build details JSON including destination data
     */
    private String buildDetailsJsonWithDestination() {
        StringBuilder json = new StringBuilder("{");
        boolean hasContent = false;

        // Get form helper's dynamic fields
        String formDetails = formHelper.buildDetailsJson();
        if (!formDetails.equals("{}")) {
            String inner = formDetails.substring(1, formDetails.length() - 1);
            if (!inner.isEmpty()) {
                json.append(inner);
                hasContent = true;
            }
        }

        // Add destination if selected and visible
        if (selectedDestination != null && !selectedDestination.isEmpty() &&
                destinationContainer != null && destinationContainer.isVisible()) {

            if (hasContent) json.append(",");

            json.append("\"destination\":\"").append(escapeJson(selectedDestination)).append("\"");
            json.append(",\"destinationLat\":").append(selectedLat);
            json.append(",\"destinationLon\":").append(selectedLon);
        }

        json.append("}");
        return json.toString();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void setFormEnabled(boolean enabled) {
        if (titreField != null) titreField.setDisable(!enabled);
        if (descriptionArea != null) descriptionArea.setDisable(!enabled);
        if (categorieCombo != null) categorieCombo.setDisable(!enabled);
        if (typeDemandeCombo != null) typeDemandeCombo.setDisable(!enabled);
        if (prioriteCombo != null) prioriteCombo.setDisable(!enabled);
        if (dateCreationPicker != null) dateCreationPicker.setDisable(!enabled);
        if (destinationField != null) destinationField.setDisable(!enabled);
        if (mapButton != null) mapButton.setDisable(!enabled);
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

    // ═══════════════════════════════════════════════════════════════════════════
    // FORM VALIDATION
    // ═══════════════════════════════════════════════════════════════════════════

    private boolean validateForm() {
        boolean valid = true;

        // Titre validation
        if (titreField == null || titreField.getText() == null || titreField.getText().trim().isEmpty()) {
            if (titreError != null) titreError.setText("Le titre est obligatoire");
            if (titreField != null) titreField.setStyle("-fx-border-color: #e74c3c;");
            valid = false;
        } else {
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

        // Catégorie validation
        if (categorieCombo == null || categorieCombo.getValue() == null) {
            if (categorieError != null) categorieError.setText("La catégorie est obligatoire");
            if (categorieCombo != null) categorieCombo.setStyle("-fx-border-color: #e74c3c;");
            valid = false;
        }

        // Type validation
        if (typeDemandeCombo == null || typeDemandeCombo.getValue() == null) {
            if (typeError != null) typeError.setText("Le type est obligatoire");
            if (typeDemandeCombo != null) typeDemandeCombo.setStyle("-fx-border-color: #e74c3c;");
            valid = false;
        }

        // Priorité validation
        if (prioriteCombo == null || prioriteCombo.getValue() == null) {
            if (prioriteError != null) prioriteError.setText("La priorité est obligatoire");
            if (prioriteCombo != null) prioriteCombo.setStyle("-fx-border-color: #e74c3c;");
            valid = false;
        }

        // Description validation
        if (descriptionArea == null || descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            if (descriptionError != null) descriptionError.setText("La description est obligatoire");
            if (descriptionArea != null) descriptionArea.setStyle("-fx-border-color: #e74c3c;");
            valid = false;
        } else {
            String description = descriptionArea.getText().trim();
            if (description.length() < 10) {
                if (descriptionError != null) descriptionError.setText("La description doit contenir au moins 10 caractères");
                descriptionArea.setStyle("-fx-border-color: #e74c3c;");
                valid = false;
            }
        }

        // Date validation
        if (dateCreationPicker == null || dateCreationPicker.getValue() == null) {
            if (dateError != null) dateError.setText("La date est obligatoire");
            if (dateCreationPicker != null) dateCreationPicker.setStyle("-fx-border-color: #e74c3c;");
            valid = false;
        } else {
            LocalDate selectedDate = dateCreationPicker.getValue();
            if (selectedDate.isBefore(LocalDate.now())) {
                if (dateError != null) dateError.setText("⚠️ La date ne peut pas être dans le passé");
                dateCreationPicker.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                valid = false;
            }
        }

        // Destination validation (only if visible)
        if (destinationContainer != null && destinationContainer.isVisible()) {
            if (selectedDestination == null || selectedDestination.trim().isEmpty()) {
                if (destinationError != null) {
                    destinationError.setText("⚠️ Veuillez sélectionner une destination sur la carte");
                }
                if (destinationField != null) {
                    destinationField.setStyle("-fx-border-color: #e74c3c;");
                }
                valid = false;
            }
        }

        // Dynamic fields validation
        if (formHelper != null && !formHelper.validateDynamicFields()) {
            valid = false;
        }

        return valid;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RESET & NAVIGATION
    // ═══════════════════════════════════════════════════════════════════════════

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

        // Clear destination
        clearDestination();
        hideDestinationField();

        // Clear all errors
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
        if (destinationError != null) destinationError.setText("");
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