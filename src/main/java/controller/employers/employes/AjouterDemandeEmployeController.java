package controller.employers.employes;

import controller.demandes.DemandeFormHelper;
import controller.demandes.NavigationHelper;
import entities.demande.Demande;
import entities.demande.DemandeDetails;
import entities.employe.session;
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

    private DemandeCRUD demandeCRUD;
    private DemandeDetailsCRUD detailsCRUD;
    private DemandeFormHelper formHelper;
    private DemandesEmployeController parentController;

    public void setParentController(DemandesEmployeController p) {
        this.parentController = p;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        demandeCRUD = new DemandeCRUD();
        detailsCRUD = new DemandeDetailsCRUD();
        formHelper = new DemandeFormHelper();

        formHelper.initializeEmployeeComboBoxes(categorieCombo, typeDemandeCombo, prioriteCombo);
        formHelper.setupDatePicker(dateCreationPicker);

        typeDemandeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("=== Type changed: " + oldValue + " -> " + newValue + " ===");
            if (newValue != null && !newValue.isEmpty()) {
                System.out.println("Calling updateDynamicFields for: " + newValue);
                formHelper.updateDynamicFields(newValue, dynamicFieldsContainer, detailsPane);
            } else {
                // Clear dynamic fields when no type selected
                formHelper.updateDynamicFields(null, dynamicFieldsContainer, detailsPane);
            }
        });

        setupRealtimeValidation();
    }

    private void setupRealtimeValidation() {
        titreField.textProperty().addListener((o, ov, nv) -> {
            if (!nv.trim().isEmpty()) formHelper.clearFieldError(titreField, titreError);
        });
        descriptionArea.textProperty().addListener((o, ov, nv) -> {
            if (!nv.trim().isEmpty()) formHelper.clearFieldError(descriptionArea, descriptionError);
        });
        categorieCombo.valueProperty().addListener((o, ov, nv) -> {
            if (nv != null) formHelper.clearFieldError(categorieCombo, categorieError);
        });
        typeDemandeCombo.valueProperty().addListener((o, ov, nv) -> {
            if (nv != null) formHelper.clearFieldError(typeDemandeCombo, typeError);
        });
        prioriteCombo.valueProperty().addListener((o, ov, nv) -> {
            if (nv != null) formHelper.clearFieldError(prioriteCombo, prioriteError);
        });
        dateCreationPicker.valueProperty().addListener((o, ov, nv) -> {
            if (nv != null) formHelper.clearFieldError(dateCreationPicker, dateError);
        });
    }

    @FXML
    private void ajouterDemande() {
        if (!validateForm()) {
            return;
        }

        try {
            Demande demande = new Demande();
            demande.setTitre(titreField.getText().trim());
            demande.setDescription(descriptionArea.getText().trim());
            demande.setCategorie(categorieCombo.getValue());
            demande.setTypeDemande(typeDemandeCombo.getValue());
            demande.setPriorite(prioriteCombo.getValue());
            demande.setStatus("Nouvelle");
            demande.setDateCreation(java.sql.Date.valueOf(dateCreationPicker.getValue()));
            demande.setIdEmploye(session.getEmploye().getId_employé());

            demandeCRUD.ajouter(demande);

            String json = formHelper.buildDetailsJson();
            System.out.println("Details JSON: " + json);
            if (!json.equals("{}")) {
                DemandeDetails details = new DemandeDetails();
                details.setIdDemande(demande.getIdDemande());
                details.setDetails(json);
                detailsCRUD.ajouter(details);
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande soumise avec succès!");
            retourListe();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la soumission: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        if (titreField.getText().trim().isEmpty()) {
            titreError.setText("Le titre est obligatoire");
            titreField.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (categorieCombo.getValue() == null) {
            categorieError.setText("La catégorie est obligatoire");
            categorieCombo.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (typeDemandeCombo.getValue() == null) {
            typeError.setText("Le type est obligatoire");
            typeDemandeCombo.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (prioriteCombo.getValue() == null) {
            prioriteError.setText("La priorité est obligatoire");
            prioriteCombo.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            descriptionError.setText("La description est obligatoire");
            descriptionArea.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (dateCreationPicker.getValue() == null) {
            dateError.setText("La date est obligatoire");
            dateCreationPicker.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (!formHelper.validateDynamicFields()) {
            valid = false;
        }

        return valid;
    }

    @FXML
    private void reinitialiserFormulaire() {
        titreField.clear();
        descriptionArea.clear();
        categorieCombo.setValue(null);
        typeDemandeCombo.setValue(null);
        typeDemandeCombo.getItems().clear();
        prioriteCombo.setValue("NORMALE");
        dateCreationPicker.setValue(LocalDate.now());

        formHelper.updateDynamicFields(null, dynamicFieldsContainer, detailsPane);

        formHelper.clearFieldError(titreField, titreError);
        formHelper.clearFieldError(categorieCombo, categorieError);
        formHelper.clearFieldError(typeDemandeCombo, typeError);
        formHelper.clearFieldError(prioriteCombo, prioriteError);
        formHelper.clearFieldError(descriptionArea, descriptionError);
        formHelper.clearFieldError(dateCreationPicker, dateError);
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