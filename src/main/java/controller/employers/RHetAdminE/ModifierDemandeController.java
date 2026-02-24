package controller.employers.RHetAdminE;

import controller.demandes.DemandeFormHelper;
import controller.demandes.NavigationHelper;
import entities.demande.Demande;
import entities.demande.DemandeDetails;
import service.demande.DemandeCRUD;
import service.demande.DemandeDetailsCRUD;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class ModifierDemandeController implements Initializable {

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

    private DemandeCRUD demandeCRUD;
    private DemandeDetailsCRUD detailsCRUD;
    private DemandeFormHelper formHelper;
    private DemandesController parentController;
    private Demande currentDemande;

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
        formHelper.setupDatePicker(dateCreationPicker);

        typeDemandeCombo.setOnAction(e ->
                formHelper.updateDynamicFields(typeDemandeCombo.getValue(), dynamicFieldsContainer, detailsPane));

        setupRealtimeValidation();
    }

    private void setupRealtimeValidation() {
        titreField.textProperty().addListener((obs, o, n) -> {
            if (!n.trim().isEmpty()) formHelper.clearFieldError(titreField, titreError);
        });
        descriptionArea.textProperty().addListener((obs, o, n) -> {
            if (!n.trim().isEmpty()) formHelper.clearFieldError(descriptionArea, descriptionError);
        });
        categorieCombo.valueProperty().addListener((obs, o, n) -> {
            if (n != null) formHelper.clearFieldError(categorieCombo, categorieError);
        });
        typeDemandeCombo.valueProperty().addListener((obs, o, n) -> {
            if (n != null) formHelper.clearFieldError(typeDemandeCombo, typeError);
        });
        prioriteCombo.valueProperty().addListener((obs, o, n) -> {
            if (n != null) formHelper.clearFieldError(prioriteCombo, prioriteError);
        });
        statusCombo.valueProperty().addListener((obs, o, n) -> {
            if (n != null) formHelper.clearFieldError(statusCombo, statusError);
        });
        dateCreationPicker.valueProperty().addListener((obs, o, n) -> {
            if (n != null) formHelper.clearFieldError(dateCreationPicker, dateError);
        });
    }

    private void fillFormWithDemande(Demande demande) {
        headerLabel.setText("Modifier: " + demande.getTitre());
        titreField.setText(demande.getTitre());
        descriptionArea.setText(demande.getDescription());
        categorieCombo.setValue(demande.getCategorie());
        prioriteCombo.setValue(demande.getPriorite());
        statusCombo.setValue(demande.getStatus());

        if (demande.getDateCreation() != null) {
            java.sql.Date sqlDate = new java.sql.Date(demande.getDateCreation().getTime());
            dateCreationPicker.setValue(sqlDate.toLocalDate());
        }

        typeDemandeCombo.setValue(demande.getTypeDemande());
        formHelper.updateDynamicFields(demande.getTypeDemande(), dynamicFieldsContainer, detailsPane);

        try {
            DemandeDetails details = detailsCRUD.getByDemande(demande.getIdDemande());
            if (details != null) {
                Map<String, String> parsed = formHelper.parseDetailsJson(details.getDetails());
                for (Map.Entry<String, String> entry : parsed.entrySet()) {
                    Control field = formHelper.getDynamicFields().get(entry.getKey());
                    if (field != null) {
                        formHelper.setFieldValue(field, entry.getValue());
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading details: " + e.getMessage());
        }
    }

    @FXML
    private void modifierDemande() {
        if (!formHelper.validateForm(titreField, titreError, categorieCombo, categorieError,
                typeDemandeCombo, typeError, prioriteCombo, prioriteError,
                descriptionArea, descriptionError, statusCombo, statusError,
                dateCreationPicker, dateError)) {
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

            String detailsJson = formHelper.buildDetailsJson();
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

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande modifiée avec succès!");
            retourListe();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
        }
    }

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