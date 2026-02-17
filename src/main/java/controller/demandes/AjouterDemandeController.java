package controller.demandes;

import entites.Demande;
import entites.DemandeDetails;
import service.demande.DemandeCRUD;
import service.demande.DemandeDetailsCRUD;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AjouterDemandeController implements Initializable {

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

    public void setParentController(DemandesController parentController) {
        this.parentController = parentController;
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

    @FXML
    private void ajouterDemande() {
        if (!formHelper.validateForm(titreField, titreError, categorieCombo, categorieError,
                typeDemandeCombo, typeError, prioriteCombo, prioriteError,
                descriptionArea, descriptionError, statusCombo, statusError,
                dateCreationPicker, dateError)) {
            return;
        }

        try {
            Demande demande = new Demande();
            demande.setTitre(titreField.getText().trim());
            demande.setDescription(descriptionArea.getText().trim());
            demande.setCategorie(categorieCombo.getValue());
            demande.setTypeDemande(typeDemandeCombo.getValue());
            demande.setPriorite(prioriteCombo.getValue());
            demande.setStatus(statusCombo.getValue());
            demande.setDateCreation(java.sql.Date.valueOf(dateCreationPicker.getValue()));

            demandeCRUD.ajouter(demande);

            String detailsJson = formHelper.buildDetailsJson();
            if (!detailsJson.equals("{}")) {
                DemandeDetails details = new DemandeDetails();
                details.setIdDemande(demande.getIdDemande());
                details.setDetails(detailsJson);
                detailsCRUD.ajouter(details);
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande ajoutée avec succès!");
            retourListe();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void reinitialiserFormulaire() {
        titreField.clear();
        descriptionArea.clear();
        categorieCombo.setValue(null);
        typeDemandeCombo.setValue(null);
        prioriteCombo.setValue(null);
        statusCombo.setValue("Nouvelle");
        dateCreationPicker.setValue(LocalDate.now());

        formHelper.getDynamicFields().clear();
        formHelper.getDynamicErrorLabels().clear();
        dynamicFieldsContainer.getChildren().clear();
        Label placeholder = new Label("Sélectionnez un type de demande");
        placeholder.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
        dynamicFieldsContainer.getChildren().add(placeholder);
        detailsPane.setText("Détails Spécifiques");

        formHelper.clearFieldError(titreField, titreError);
        formHelper.clearFieldError(categorieCombo, categorieError);
        formHelper.clearFieldError(typeDemandeCombo, typeError);
        formHelper.clearFieldError(prioriteCombo, prioriteError);
        formHelper.clearFieldError(descriptionArea, descriptionError);
        formHelper.clearFieldError(statusCombo, statusError);
        formHelper.clearFieldError(dateCreationPicker, dateError);
    }

    @FXML
    private void retourListe() {
        try {
            NavigationHelper.loadView(titreField, "demandes.fxml");
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