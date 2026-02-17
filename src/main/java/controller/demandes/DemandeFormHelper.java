package controller.demandes;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class DemandeFormHelper {

    private Map<String, Control> dynamicFields = new LinkedHashMap<>();
    private Map<String, Label> dynamicErrorLabels = new LinkedHashMap<>();

    public Map<String, Control> getDynamicFields() { return dynamicFields; }
    public Map<String, Label> getDynamicErrorLabels() { return dynamicErrorLabels; }

    // ============ COMBOBOX INITIALIZATION ============

    public void initializeComboBoxes(ComboBox<String> categorieCombo,
                                     ComboBox<String> typeDemandeCombo,
                                     ComboBox<String> prioriteCombo,
                                     ComboBox<String> statusCombo) {
        categorieCombo.setItems(FXCollections.observableArrayList(
                "Technique", "Administrative", "Commerciale", "Support", "Autre"
        ));
        typeDemandeCombo.setItems(FXCollections.observableArrayList(
                "Incident", "Service", "Information", "Réclamation", "Suggestion"
        ));
        prioriteCombo.setItems(FXCollections.observableArrayList(
                "BASSE", "NORMALE", "HAUTE"
        ));
        statusCombo.setItems(FXCollections.observableArrayList(
                "Nouvelle", "En cours", "En attente", "Résolue", "Fermée"
        ));
        statusCombo.setValue("Nouvelle");
    }

    // ============ DATE PICKER ============

    public void setupDatePicker(DatePicker picker) {
        picker.setValue(LocalDate.now());
        picker.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #999999;");
                }
            }
        });
    }

    // ============ DYNAMIC FIELDS ============

    public void updateDynamicFields(String selectedType, VBox container, TitledPane detailsPane) {
        container.getChildren().clear();
        dynamicFields.clear();
        dynamicErrorLabels.clear();

        if (selectedType == null) {
            Label placeholder = new Label("Sélectionnez un type de demande");
            placeholder.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            container.getChildren().add(placeholder);
            return;
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));

        switch (selectedType) {
            case "Incident":
                addTextField(grid, 0, "Localisation", "Lieu de l'incident");
                addDateField(grid, 1, "Date Incident");
                addComboField(grid, 2, "Urgence", "Critique", "Majeure", "Mineure");
                addComboField(grid, 3, "Impact", "Élevé", "Moyen", "Faible");
                addTextField(grid, 4, "Système Affecté", "Nom du système");
                detailsPane.setText("Détails Incident");
                break;
            case "Service":
                addTextField(grid, 0, "Service Demandé", "Décrivez le service");
                addTextField(grid, 1, "Département", "Nom du département");
                addDateField(grid, 2, "Délai Souhaité");
                addComboField(grid, 3, "Type Service", "Installation", "Configuration", "Maintenance", "Formation");
                addTextField(grid, 4, "Nombre Utilisateurs", "Ex: 5");
                detailsPane.setText("Détails Service");
                break;
            case "Information":
                addTextField(grid, 0, "Sujet", "Sujet de la demande");
                addComboField(grid, 1, "Source", "Interne", "Externe", "Partenaire");
                addComboField(grid, 2, "Format Souhaité", "PDF", "Excel", "Word", "Présentation");
                addTextField(grid, 3, "Destinataire", "Nom du destinataire");
                addDateField(grid, 4, "Date Limite");
                detailsPane.setText("Détails Information");
                break;
            case "Réclamation":
                addTextField(grid, 0, "Référence", "Numéro de référence");
                addDateField(grid, 1, "Date Événement");
                addTextField(grid, 2, "Montant", "Montant en DT");
                addComboField(grid, 3, "Type Réclamation", "Qualité", "Facturation", "Délai", "Autre");
                addTextAreaField(grid, 4, "Détail Réclamation", "Décrivez votre réclamation");
                detailsPane.setText("Détails Réclamation");
                break;
            case "Suggestion":
                addComboField(grid, 0, "Domaine", "Processus", "Technologie", "Organisation", "Communication");
                addTextAreaField(grid, 1, "Bénéfice Attendu", "Décrivez les bénéfices");
                addTextField(grid, 2, "Coût Estimé", "Estimation en DT");
                addComboField(grid, 3, "Priorité Suggestion", "Court terme", "Moyen terme", "Long terme");
                addTextField(grid, 4, "Ressources Nécessaires", "Ressources requises");
                detailsPane.setText("Détails Suggestion");
                break;
        }

        container.getChildren().add(grid);
    }

    private void addTextField(GridPane grid, int row, String label, String prompt) {
        int actualRow = row * 2;
        Label lbl = new Label(label + ": *");
        lbl.setStyle("-fx-font-weight: bold;");
        lbl.setMinWidth(150);

        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(300);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11;");

        field.textProperty().addListener((obs, o, n) -> {
            if (!n.trim().isEmpty()) clearFieldError(field, errorLabel);
        });

        grid.add(lbl, 0, actualRow);
        grid.add(field, 1, actualRow);
        grid.add(errorLabel, 1, actualRow + 1);

        dynamicFields.put(label, field);
        dynamicErrorLabels.put(label, errorLabel);
    }

    private void addDateField(GridPane grid, int row, String label) {
        int actualRow = row * 2;
        Label lbl = new Label(label + ": *");
        lbl.setStyle("-fx-font-weight: bold;");
        lbl.setMinWidth(150);

        DatePicker picker = new DatePicker();
        picker.setPrefWidth(300);
        setupDatePicker(picker);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11;");

        picker.valueProperty().addListener((obs, o, n) -> {
            if (n != null) clearFieldError(picker, errorLabel);
        });

        grid.add(lbl, 0, actualRow);
        grid.add(picker, 1, actualRow);
        grid.add(errorLabel, 1, actualRow + 1);

        dynamicFields.put(label, picker);
        dynamicErrorLabels.put(label, errorLabel);
    }

    private void addComboField(GridPane grid, int row, String label, String... options) {
        int actualRow = row * 2;
        Label lbl = new Label(label + ": *");
        lbl.setStyle("-fx-font-weight: bold;");
        lbl.setMinWidth(150);

        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(options));
        combo.setPrefWidth(300);
        combo.setPromptText("Sélectionnez");

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11;");

        combo.valueProperty().addListener((obs, o, n) -> {
            if (n != null) clearFieldError(combo, errorLabel);
        });

        grid.add(lbl, 0, actualRow);
        grid.add(combo, 1, actualRow);
        grid.add(errorLabel, 1, actualRow + 1);

        dynamicFields.put(label, combo);
        dynamicErrorLabels.put(label, errorLabel);
    }

    private void addTextAreaField(GridPane grid, int row, String label, String prompt) {
        int actualRow = row * 2;
        Label lbl = new Label(label + ": *");
        lbl.setStyle("-fx-font-weight: bold;");
        lbl.setMinWidth(150);

        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefWidth(300);
        area.setPrefRowCount(2);
        area.setWrapText(true);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11;");

        area.textProperty().addListener((obs, o, n) -> {
            if (!n.trim().isEmpty()) clearFieldError(area, errorLabel);
        });

        grid.add(lbl, 0, actualRow);
        grid.add(area, 1, actualRow);
        grid.add(errorLabel, 1, actualRow + 1);

        dynamicFields.put(label, area);
        dynamicErrorLabels.put(label, errorLabel);
    }

    // ============ VALIDATION ============

    public boolean validateForm(TextField titreField, Label titreError,
                                ComboBox<String> categorieCombo, Label categorieError,
                                ComboBox<String> typeDemandeCombo, Label typeError,
                                ComboBox<String> prioriteCombo, Label prioriteError,
                                TextArea descriptionArea, Label descriptionError,
                                ComboBox<String> statusCombo, Label statusError,
                                DatePicker dateCreationPicker, Label dateError) {
        boolean isValid = true;

        // Titre
        if (titreField.getText().trim().isEmpty()) {
            setFieldError(titreField, titreError, "Le titre est obligatoire");
            isValid = false;
        } else if (titreField.getText().trim().length() < 3) {
            setFieldError(titreField, titreError, "Minimum 3 caractères");
            isValid = false;
        } else if (titreField.getText().trim().length() > 50) {
            setFieldError(titreField, titreError, "Maximum 50 caractères");
            isValid = false;
        } else {
            clearFieldError(titreField, titreError);
        }

        if (categorieCombo.getValue() == null) {
            setFieldError(categorieCombo, categorieError, "La catégorie est obligatoire");
            isValid = false;
        } else {
            clearFieldError(categorieCombo, categorieError);
        }

        // Type
        if (typeDemandeCombo.getValue() == null) {
            setFieldError(typeDemandeCombo, typeError, "Le type est obligatoire");
            isValid = false;
        } else {
            clearFieldError(typeDemandeCombo, typeError);
        }

        if (prioriteCombo.getValue() == null) {
            setFieldError(prioriteCombo, prioriteError, "La priorité est obligatoire");
            isValid = false;
        } else {
            clearFieldError(prioriteCombo, prioriteError);
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            setFieldError(descriptionArea, descriptionError, "La description est obligatoire");
            isValid = false;
        } else if (descriptionArea.getText().trim().length() < 10) {
            setFieldError(descriptionArea, descriptionError, "Minimum 10 caractères");
            isValid = false;
        } else if (descriptionArea.getText().trim().length() > 50) {
            setFieldError(descriptionArea, descriptionError, "Maximum 50 caractères");
            isValid = false;
        } else {
            clearFieldError(descriptionArea, descriptionError);
        }

        if (statusCombo.getValue() == null) {
            setFieldError(statusCombo, statusError, "Le statut est obligatoire");
            isValid = false;
        } else {
            clearFieldError(statusCombo, statusError);
        }

        if (dateCreationPicker.getValue() == null) {
            setFieldError(dateCreationPicker, dateError, "La date est obligatoire");
            isValid = false;
        } else if (dateCreationPicker.getValue().isBefore(LocalDate.now())) {
            setFieldError(dateCreationPicker, dateError, "La date doit être aujourd'hui ou ultérieure");
            isValid = false;
        } else {
            clearFieldError(dateCreationPicker, dateError);
        }

        if (!validateDynamicFields()) {
            isValid = false;
        }

        return isValid;
    }

    public boolean validateDynamicFields() {
        boolean isValid = true;
        for (Map.Entry<String, Control> entry : dynamicFields.entrySet()) {
            String fieldName = entry.getKey();
            Control field = entry.getValue();
            Label errorLabel = dynamicErrorLabels.get(fieldName);
            if (errorLabel == null) continue;

            String value = getFieldValue(field);
            if (value == null || value.trim().isEmpty()) {
                setFieldError(field, errorLabel, fieldName + " est obligatoire");
                isValid = false;
            } else {
                clearFieldError(field, errorLabel);
            }
        }
        return isValid;
    }

    // ============ FIELD ERROR HELPERS ============

    public void setFieldError(Control field, Label errorLabel, String message) {
        errorLabel.setText(message);
        field.setStyle("-fx-border-color: red; -fx-border-width: 1.5;");
    }

    public void clearFieldError(Control field, Label errorLabel) {
        errorLabel.setText("");
        field.setStyle("");
    }

    // ============ JSON HELPERS ============

    public String buildDetailsJson() {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Control> entry : dynamicFields.entrySet()) {
            String key = entry.getKey();
            String value = getFieldValue(entry.getValue());
            if (value != null && !value.isEmpty()) {
                if (!first) json.append(",");
                json.append("\"").append(escapeJson(key)).append("\":\"").append(escapeJson(value)).append("\"");
                first = false;
            }
        }
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    public String getFieldValue(Control control) {
        if (control instanceof TextField) return ((TextField) control).getText();
        if (control instanceof TextArea) return ((TextArea) control).getText();
        if (control instanceof ComboBox) {
            Object val = ((ComboBox<?>) control).getValue();
            return val != null ? val.toString() : "";
        }
        if (control instanceof DatePicker) {
            LocalDate date = ((DatePicker) control).getValue();
            return date != null ? date.toString() : "";
        }
        return "";
    }

    public void setFieldValue(Control control, String value) {
        if (value == null || value.isEmpty()) return;
        if (control instanceof TextField) ((TextField) control).setText(value);
        else if (control instanceof TextArea) ((TextArea) control).setText(value);
        else if (control instanceof ComboBox) ((ComboBox<String>) control).setValue(value);
        else if (control instanceof DatePicker) {
            try { ((DatePicker) control).setValue(LocalDate.parse(value)); }
            catch (Exception e) { System.out.println("Invalid date: " + value); }
        }
    }

    public Map<String, String> parseDetailsJson(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null || json.isEmpty() || json.equals("{}")) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");
                value = value.replace("\\n", "\n").replace("\\r", "\r");
                map.put(key, value);
            }
        }
        return map;
    }
}