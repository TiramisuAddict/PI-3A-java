package controller.employers.RHetAdminE;

import controller.demandes.DemandeFormHelper;
import controller.demandes.NavigationHelper;
import entities.demande.Demande;
import entities.demande.DemandeDetails;
import service.api.MapPickerDialog;  // ← Correct import
import service.demande.DemandeCRUD;
import service.demande.DemandeDetailsCRUD;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.ResourceBundle;

public class ModifierDemandeController implements Initializable {

    // ═══════════════════════════════════════════════════════════════════════════
    // FXML FIELDS
    // ═══════════════════════════════════════════════════════════════════════════

    @FXML private Label headerLabel;
    @FXML private TextField titreField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private ComboBox<String> typeDemandeCombo;
    @FXML private ComboBox<String> prioriteCombo;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> statusCombo;
    @FXML private DatePicker dateCreationPicker;

    @FXML private Label titreError;
    @FXML private Label categorieError;
    @FXML private Label typeError;
    @FXML private Label prioriteError;
    @FXML private Label descriptionError;
    @FXML private Label statusError;
    @FXML private Label dateError;

    @FXML private TitledPane detailsPane;
    @FXML private VBox dynamicFieldsContainer;

    // MAP / DESTINATION FIELDS
    @FXML private HBox destinationContainer;
    @FXML private TextField destinationField;
    @FXML private Button mapButton;
    @FXML private Label destinationError;
    @FXML private Label destinationLabel;

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
    private DemandesController parentController;
    private Demande currentDemande;

    // Types de demandes qui nécessitent une destination
    private static final String[] DESTINATION_TYPES = {
            "Mission", "Déplacement", "Formation externe", "Voyage d'affaires",
            "Conférence", "Séminaire", "Visite client", "Voyage", "Transport"
    };

    // ═══════════════════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════

    public void setParentController(DemandesController parentController) {
        this.parentController = parentController;
    }

    public void setDemande(Demande demande) {
        this.currentDemande = demande;
        fillFormWithDemande(demande);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        demandeCRUD = new DemandeCRUD();
        detailsCRUD = new DemandeDetailsCRUD();
        formHelper = new DemandeFormHelper();

        formHelper.initializeComboBoxes(categorieCombo, typeDemandeCombo, prioriteCombo, statusCombo);
        setupDatePicker();
        setupDestinationField();

        typeDemandeCombo.setOnAction(e -> {
            String type = typeDemandeCombo.getValue();
            formHelper.updateDynamicFields(type, dynamicFieldsContainer, detailsPane);
            updateDestinationVisibility(type);
        });

        setupRealtimeValidation();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DATE PICKER SETUP
    // ═══════════════════════════════════════════════════════════════════════════

    private void setupDatePicker() {
        if (dateCreationPicker == null) return;

        // RH/Admin can select past dates (for corrections)
        // But show visual indication
        Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item.isBefore(LocalDate.now())) {
                            setStyle("-fx-background-color: #fff3cd;"); // Warning yellow
                            setTooltip(new Tooltip("Date dans le passé"));
                        } else if (item.equals(LocalDate.now())) {
                            setStyle("-fx-background-color: #90EE90; -fx-font-weight: bold;");
                            setTooltip(new Tooltip("Aujourd'hui"));
                        }
                    }
                };
            }
        };

        dateCreationPicker.setDayCellFactory(dayCellFactory);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DESTINATION / MAP METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    private void setupDestinationField() {
        if (destinationContainer != null) {
            destinationContainer.setVisible(false);
            destinationContainer.setManaged(false);
        }
        if (destinationLabel != null) {
            destinationLabel.setVisible(false);
            destinationLabel.setManaged(false);
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
        if (destinationLabel != null) {
            destinationLabel.setVisible(needsDestination);
            destinationLabel.setManaged(needsDestination);
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

                    System.out.println("📍 Destination modifiée: " + result.cityName +
                            " (Lat: " + result.lat + ", Lon: " + result.lon + ")");
                });
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // REALTIME VALIDATION
    // ═══════════════════════════════════════════════════════════════════════════

    private void setupRealtimeValidation() {
        if (titreField != null) {
            titreField.textProperty().addListener((obs, o, n) -> {
                if (n != null && !n.trim().isEmpty()) formHelper.clearFieldError(titreField, titreError);
            });
        }
        if (descriptionArea != null) {
            descriptionArea.textProperty().addListener((obs, o, n) -> {
                if (n != null && !n.trim().isEmpty()) formHelper.clearFieldError(descriptionArea, descriptionError);
            });
        }
        if (categorieCombo != null) {
            categorieCombo.valueProperty().addListener((obs, o, n) -> {
                if (n != null) formHelper.clearFieldError(categorieCombo, categorieError);
            });
        }
        if (typeDemandeCombo != null) {
            typeDemandeCombo.valueProperty().addListener((obs, o, n) -> {
                if (n != null) formHelper.clearFieldError(typeDemandeCombo, typeError);
            });
        }
        if (prioriteCombo != null) {
            prioriteCombo.valueProperty().addListener((obs, o, n) -> {
                if (n != null) formHelper.clearFieldError(prioriteCombo, prioriteError);
            });
        }
        if (statusCombo != null) {
            statusCombo.valueProperty().addListener((obs, o, n) -> {
                if (n != null) formHelper.clearFieldError(statusCombo, statusError);
            });
        }
        if (dateCreationPicker != null) {
            dateCreationPicker.valueProperty().addListener((obs, o, n) -> {
                if (n != null) formHelper.clearFieldError(dateCreationPicker, dateError);
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
    // FILL FORM WITH EXISTING DATA
    // ═══════════════════════════════════════════════════════════════════════════

    private void fillFormWithDemande(Demande demande) {
        if (headerLabel != null) {
            headerLabel.setText("✏️ Modifier: " + demande.getTitre());
        }
        if (titreField != null) titreField.setText(demande.getTitre());
        if (descriptionArea != null) descriptionArea.setText(demande.getDescription());
        if (categorieCombo != null) categorieCombo.setValue(demande.getCategorie());
        if (prioriteCombo != null) prioriteCombo.setValue(demande.getPriorite());
        if (statusCombo != null) statusCombo.setValue(demande.getStatus());

        if (demande.getDateCreation() != null && dateCreationPicker != null) {
            java.sql.Date sqlDate = new java.sql.Date(demande.getDateCreation().getTime());
            dateCreationPicker.setValue(sqlDate.toLocalDate());
        }

        if (typeDemandeCombo != null) {
            typeDemandeCombo.setValue(demande.getTypeDemande());
        }

        formHelper.updateDynamicFields(demande.getTypeDemande(), dynamicFieldsContainer, detailsPane);
        updateDestinationVisibility(demande.getTypeDemande());

        // Load existing details including destination
        try {
            DemandeDetails details = detailsCRUD.getByDemande(demande.getIdDemande());
            if (details != null && details.getDetails() != null) {
                String detailsJson = details.getDetails();
                Map<String, String> parsed = formHelper.parseDetailsJson(detailsJson);

                // Fill dynamic fields
                for (Map.Entry<String, String> entry : parsed.entrySet()) {
                    Control field = formHelper.getDynamicFields().get(entry.getKey());
                    if (field != null) {
                        formHelper.setFieldValue(field, entry.getValue());
                    }
                }

                // Load destination if exists
                if (parsed.containsKey("destination")) {
                    selectedDestination = parsed.get("destination");
                    if (destinationField != null) {
                        destinationField.setText(selectedDestination);
                        destinationField.setStyle("-fx-border-color: #27ae60;");
                    }
                }
                if (parsed.containsKey("destinationLat")) {
                    try {
                        selectedLat = Double.parseDouble(parsed.get("destinationLat"));
                    } catch (NumberFormatException ignored) {}
                }
                if (parsed.containsKey("destinationLon")) {
                    try {
                        selectedLon = Double.parseDouble(parsed.get("destinationLon"));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading details: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FORM SUBMISSION
    // ═══════════════════════════════════════════════════════════════════════════

    @FXML
    private void modifierDemande() {
        if (!validateForm()) {
            return;
        }

        try {
            currentDemande.setTitre(titreField.getText().trim());
            currentDemande.setDescription(descriptionArea.getText().trim());
            currentDemande.setCategorie(categorieCombo.getValue());
            currentDemande.setTypeDemande(typeDemandeCombo.getValue());
            currentDemande.setPriorite(prioriteCombo.getValue());
            currentDemande.setStatus(statusCombo.getValue());
            currentDemande.setDateCreation(java.sql.Date.valueOf(dateCreationPicker.getValue()));

            demandeCRUD.modifier(currentDemande);

            // Build details JSON including destination
            String detailsJson = buildDetailsJsonWithDestination();

            DemandeDetails existing = detailsCRUD.getByDemande(currentDemande.getIdDemande());
            if (existing != null) {
                existing.setDetails(detailsJson);
                detailsCRUD.modifier(existing);
            } else if (!detailsJson.equals("{}")) {
                DemandeDetails newDetails = new DemandeDetails();
                newDetails.setIdDemande(currentDemande.getIdDemande());
                newDetails.setDetails(detailsJson);
                detailsCRUD.ajouter(newDetails);
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès", "✅ Demande modifiée avec succès!");
            retourListe();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════════════════════

    private boolean validateForm() {
        boolean valid = true;

        // Basic validation using form helper
        if (!formHelper.validateForm(titreField, titreError, categorieCombo, categorieError,
                typeDemandeCombo, typeError, prioriteCombo, prioriteError,
                descriptionArea, descriptionError, statusCombo, statusError,
                dateCreationPicker, dateError)) {
            valid = false;
        }

        // Destination validation (only if visible)
        if (destinationContainer != null && destinationContainer.isVisible()) {
            if (selectedDestination == null || selectedDestination.trim().isEmpty()) {
                if (destinationError != null) {
                    destinationError.setText("⚠️ Veuillez sélectionner une destination");
                }
                if (destinationField != null) {
                    destinationField.setStyle("-fx-border-color: #e74c3c;");
                }
                valid = false;
            }
        }

        return valid;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════════════════

    @FXML
    private void retourListe() {
        try {
            NavigationHelper.loadView("/emp/RHetAdminE/demandes.fxml");
        } catch (IOException e) {
            e.printStackTrace();
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