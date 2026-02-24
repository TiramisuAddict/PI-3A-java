package controller.demandes;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import service.api.MapPickerDialog;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DemandeFormHelper {

    private Map<String, Control> dynamicFields = new LinkedHashMap<>();
    private Map<String, Label> dynamicErrorLabels = new LinkedHashMap<>();
    private Set<String> locationFieldKeys = new HashSet<>(); // Track which fields are location fields

    // Define categories and their types
    private static final Map<String, List<String>> CATEGORY_TYPES = new LinkedHashMap<>();

    static {
        // RH Category
        CATEGORY_TYPES.put("Ressources Humaines", Arrays.asList(
                "Congé",
                "Attestation de travail",
                "Attestation de salaire",
                "Certificat de travail",
                "Mutation",
                "Démission"
        ));

        // Administrative Category
        CATEGORY_TYPES.put("Administrative", Arrays.asList(
                "Avance sur salaire",
                "Remboursement",
                "Matériel de bureau",
                "Badge d'accès",
                "Carte de visite"
        ));

        // IT Category
        CATEGORY_TYPES.put("Informatique", Arrays.asList(
                "Matériel informatique",
                "Accès système",
                "Logiciel",
                "Problème technique"
        ));

        // Formation Category
        CATEGORY_TYPES.put("Formation", Arrays.asList(
                "Formation interne",
                "Formation externe",
                "Certification"
        ));

        // Work Organization Category
        CATEGORY_TYPES.put("Organisation du travail", Arrays.asList(
                "Télétravail",
                "Changement d'horaires",
                "Heures supplémentaires"
        ));
    }

    // ==================== INITIALIZATION ====================

    public void initializeComboBoxes(ComboBox<String> categorieCombo,
                                     ComboBox<String> typeDemandeCombo,
                                     ComboBox<String> prioriteCombo,
                                     ComboBox<String> statusCombo) {
        // Categories
        categorieCombo.setItems(FXCollections.observableArrayList(CATEGORY_TYPES.keySet()));

        // Priorities
        prioriteCombo.setItems(FXCollections.observableArrayList("HAUTE", "NORMALE", "BASSE"));

        // Status options
        if (statusCombo != null) {
            statusCombo.setItems(FXCollections.observableArrayList(
                    "Nouvelle", "En cours", "En attente", "Résolue", "Fermée"));
        }

        categorieCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Category changed: " + oldVal + " -> " + newVal);
            if (newVal != null && CATEGORY_TYPES.containsKey(newVal)) {
                List<String> types = CATEGORY_TYPES.get(newVal);
                System.out.println("Setting types: " + types);
                typeDemandeCombo.setItems(FXCollections.observableArrayList(types));
                typeDemandeCombo.setValue(null);
            } else {
                typeDemandeCombo.getItems().clear();
            }
        });
    }

    public void initializeEmployeeComboBoxes(ComboBox<String> categorieCombo,
                                             ComboBox<String> typeDemandeCombo,
                                             ComboBox<String> prioriteCombo) {
        categorieCombo.setItems(FXCollections.observableArrayList(CATEGORY_TYPES.keySet()));

        prioriteCombo.setItems(FXCollections.observableArrayList("HAUTE", "NORMALE", "BASSE"));
        prioriteCombo.setValue("NORMALE");

        categorieCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Category changed: " + oldVal + " -> " + newVal);
            if (newVal != null && CATEGORY_TYPES.containsKey(newVal)) {
                List<String> types = CATEGORY_TYPES.get(newVal);
                System.out.println("Setting types: " + types);
                typeDemandeCombo.setItems(FXCollections.observableArrayList(types));
                typeDemandeCombo.setValue(null);
            } else {
                typeDemandeCombo.getItems().clear();
            }
        });
    }

    public void setupDatePicker(DatePicker datePicker) {
        datePicker.setValue(LocalDate.now());
    }

    // ==================== DYNAMIC FIELDS ====================

    public void updateDynamicFields(String typeDemande, VBox container, TitledPane detailsPane) {
        System.out.println("=== updateDynamicFields called ===");
        System.out.println("Type: " + typeDemande);

        dynamicFields.clear();
        dynamicErrorLabels.clear();
        locationFieldKeys.clear();
        container.getChildren().clear();

        if (typeDemande == null || typeDemande.isEmpty()) {
            Label placeholder = new Label("Sélectionnez un type de demande pour voir les champs spécifiques");
            placeholder.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            container.getChildren().add(placeholder);
            if (detailsPane != null) {
                detailsPane.setExpanded(false);
            }
            System.out.println("No type selected, showing placeholder");
            return;
        }

        if (detailsPane != null) {
            detailsPane.setExpanded(true);
        }

        List<FieldDefinition> fields = getFieldsForType(typeDemande);
        System.out.println("Fields count for '" + typeDemande + "': " + fields.size());

        if (fields.isEmpty()) {
            Label noFields = new Label("Aucun champ spécifique requis pour ce type");
            noFields.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            container.getChildren().add(noFields);
            return;
        }

        Label header = new Label("📋 Informations spécifiques pour: " + typeDemande);
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");
        container.getChildren().add(header);

        for (FieldDefinition field : fields) {
            System.out.println("Creating field: " + field.key + " (" + field.label + ")");
            VBox fieldBox = createFieldBox(field);
            container.getChildren().add(fieldBox);
        }

        System.out.println("Dynamic fields created: " + dynamicFields.size());
    }

    private List<FieldDefinition> getFieldsForType(String typeDemande) {
        List<FieldDefinition> fields = new ArrayList<>();

        switch (typeDemande) {
            // ===== CONGÉ =====
            case "Congé":
                fields.add(new FieldDefinition("typeConge", "Type de congé", FieldType.COMBO,
                        true, Arrays.asList("Congé annuel", "Congé maladie", "Congé sans solde",
                        "Congé maternité", "Congé paternité", "Congé exceptionnel")));
                fields.add(new FieldDefinition("dateDebut", "Date de début", FieldType.DATE, true));
                fields.add(new FieldDefinition("dateFin", "Date de fin", FieldType.DATE, true));
                fields.add(new FieldDefinition("nombreJours", "Nombre de jours", FieldType.NUMBER, true));
                fields.add(new FieldDefinition("motif", "Motif", FieldType.TEXTAREA, false));
                break;

            // ===== ATTESTATION DE TRAVAIL =====
            case "Attestation de travail":
                fields.add(new FieldDefinition("nombreExemplaires", "Nombre d'exemplaires",
                        FieldType.NUMBER, true));
                fields.add(new FieldDefinition("motifAttestation", "Motif de la demande", FieldType.COMBO,
                        true, Arrays.asList("Démarches administratives", "Banque", "Visa",
                        "Location immobilière", "Autre")));
                fields.add(new FieldDefinition("destinataire", "Destinataire (si connu)",
                        FieldType.TEXT, false));
                break;

            // ===== ATTESTATION DE SALAIRE =====
            case "Attestation de salaire":
                fields.add(new FieldDefinition("nombreExemplaires", "Nombre d'exemplaires",
                        FieldType.NUMBER, true));
                fields.add(new FieldDefinition("periode", "Période concernée", FieldType.COMBO,
                        true, Arrays.asList("Dernier mois", "3 derniers mois", "6 derniers mois",
                        "Année en cours", "Année précédente")));
                fields.add(new FieldDefinition("motif", "Motif", FieldType.TEXT, false));
                break;

            // ===== CERTIFICAT DE TRAVAIL =====
            case "Certificat de travail":
                fields.add(new FieldDefinition("nombreExemplaires", "Nombre d'exemplaires",
                        FieldType.NUMBER, true));
                fields.add(new FieldDefinition("motif", "Motif", FieldType.TEXT, false));
                break;

            // ===== MUTATION =====
            case "Mutation":
                fields.add(new FieldDefinition("departementActuel", "Département actuel",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("departementSouhaite", "Département souhaité",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("lieuMutation", "Lieu de mutation",
                        FieldType.LOCATION, true));
                fields.add(new FieldDefinition("posteSouhaite", "Poste souhaité",
                        FieldType.TEXT, false));
                fields.add(new FieldDefinition("motif", "Motif de la demande",
                        FieldType.TEXTAREA, true));
                break;

            // ===== DÉMISSION =====
            case "Démission":
                fields.add(new FieldDefinition("dateSouhaitee", "Date de départ souhaitée",
                        FieldType.DATE, true));
                fields.add(new FieldDefinition("preavis", "Durée de préavis", FieldType.COMBO,
                        true, Arrays.asList("1 mois", "2 mois", "3 mois", "Dispense demandée")));
                fields.add(new FieldDefinition("motif", "Motif de départ",
                        FieldType.TEXTAREA, false));
                break;

            // ===== AVANCE SUR SALAIRE =====
            case "Avance sur salaire":
                fields.add(new FieldDefinition("montant", "Montant demandé (TND)",
                        FieldType.NUMBER, true));
                fields.add(new FieldDefinition("modaliteRemboursement", "Modalité de remboursement",
                        FieldType.COMBO, true, Arrays.asList("1 mois", "2 mois", "3 mois", "6 mois")));
                fields.add(new FieldDefinition("motif", "Motif de la demande",
                        FieldType.TEXTAREA, true));
                break;

            // ===== REMBOURSEMENT =====
            case "Remboursement":
                fields.add(new FieldDefinition("typeRemboursement", "Type de remboursement",
                        FieldType.COMBO, true, Arrays.asList("Frais de transport",
                        "Frais de mission", "Frais de formation", "Frais médicaux", "Autre")));
                fields.add(new FieldDefinition("montant", "Montant (TND)", FieldType.NUMBER, true));
                fields.add(new FieldDefinition("dateDepense", "Date de la dépense",
                        FieldType.DATE, true));
                fields.add(new FieldDefinition("justificatif", "Justificatif joint", FieldType.COMBO,
                        true, Arrays.asList("Oui", "Non - à fournir")));
                fields.add(new FieldDefinition("details", "Détails", FieldType.TEXTAREA, false));
                break;

            // ===== MATÉRIEL DE BUREAU =====
            case "Matériel de bureau":
                fields.add(new FieldDefinition("typeMateriel", "Type de matériel", FieldType.COMBO,
                        true, Arrays.asList("Fournitures", "Mobilier", "Équipement", "Autre")));
                fields.add(new FieldDefinition("descriptionMateriel", "Description du matériel",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("quantite", "Quantité", FieldType.NUMBER, true));
                fields.add(new FieldDefinition("urgence", "Urgence", FieldType.COMBO,
                        false, Arrays.asList("Normale", "Urgente", "Très urgente")));
                break;

            // ===== BADGE D'ACCÈS =====
            case "Badge d'accès":
                fields.add(new FieldDefinition("motifBadge", "Motif de la demande", FieldType.COMBO,
                        true, Arrays.asList("Nouveau badge", "Badge perdu", "Badge défectueux",
                        "Extension d'accès")));
                fields.add(new FieldDefinition("zonesAcces", "Zones d'accès demandées",
                        FieldType.TEXT, false));
                break;

            // ===== CARTE DE VISITE =====
            case "Carte de visite":
                fields.add(new FieldDefinition("quantiteCarte", "Quantité", FieldType.COMBO,
                        true, Arrays.asList("50", "100", "200", "500")));
                fields.add(new FieldDefinition("titreFonction", "Titre/Fonction à afficher",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("telephone", "Numéro de téléphone",
                        FieldType.TEXT, false));
                fields.add(new FieldDefinition("email", "Email", FieldType.TEXT, false));
                break;

            // ===== MATÉRIEL INFORMATIQUE =====
            case "Matériel informatique":
                fields.add(new FieldDefinition("typeMaterielInfo", "Type de matériel", FieldType.COMBO,
                        true, Arrays.asList("Ordinateur portable", "Ordinateur fixe", "Écran",
                        "Clavier/Souris", "Casque", "Webcam", "Autre")));
                fields.add(new FieldDefinition("motifMateriel", "Motif", FieldType.COMBO,
                        true, Arrays.asList("Nouveau besoin", "Remplacement", "Mise à niveau")));
                fields.add(new FieldDefinition("specifications", "Spécifications souhaitées",
                        FieldType.TEXTAREA, false));
                break;

            // ===== ACCÈS SYSTÈME =====
            case "Accès système":
                fields.add(new FieldDefinition("systeme", "Système/Application",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("typeAcces", "Type d'accès", FieldType.COMBO,
                        true, Arrays.asList("Lecture seule", "Lecture/Écriture", "Administrateur")));
                fields.add(new FieldDefinition("justification", "Justification",
                        FieldType.TEXTAREA, true));
                break;

            // ===== LOGICIEL =====
            case "Logiciel":
                fields.add(new FieldDefinition("nomLogiciel", "Nom du logiciel",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("version", "Version (si applicable)",
                        FieldType.TEXT, false));
                fields.add(new FieldDefinition("typeLicence", "Type de licence", FieldType.COMBO,
                        false, Arrays.asList("Achat", "Abonnement mensuel", "Abonnement annuel",
                        "Open source")));
                fields.add(new FieldDefinition("justificationLogiciel", "Justification du besoin",
                        FieldType.TEXTAREA, true));
                break;

            // ===== PROBLÈME TECHNIQUE =====
            case "Problème technique":
                fields.add(new FieldDefinition("typeProbleme", "Type de problème", FieldType.COMBO,
                        true, Arrays.asList("Matériel", "Logiciel", "Réseau", "Email",
                        "Imprimante", "Autre")));
                fields.add(new FieldDefinition("descriptionProbleme", "Description du problème",
                        FieldType.TEXTAREA, true));
                fields.add(new FieldDefinition("impact", "Impact sur le travail", FieldType.COMBO,
                        true, Arrays.asList("Bloquant", "Important", "Modéré", "Faible")));
                break;

            // ===== FORMATION INTERNE =====
            case "Formation interne":
                fields.add(new FieldDefinition("nomFormation", "Nom de la formation",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("formateur", "Formateur (si connu)",
                        FieldType.TEXT, false));
                fields.add(new FieldDefinition("dateSouhaiteeFormation", "Date souhaitée",
                        FieldType.DATE, false));
                fields.add(new FieldDefinition("objectifFormation", "Objectif de la formation",
                        FieldType.TEXTAREA, true));
                break;

            // ===== FORMATION EXTERNE =====
            case "Formation externe":
                fields.add(new FieldDefinition("nomFormationExt", "Nom de la formation",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("organisme", "Organisme de formation",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("lieuFormation", "Lieu de formation",
                        FieldType.LOCATION, true));
                fields.add(new FieldDefinition("duree", "Durée", FieldType.TEXT, true));
                fields.add(new FieldDefinition("cout", "Coût estimé (TND)",
                        FieldType.NUMBER, false));
                fields.add(new FieldDefinition("dateDebutFormation", "Date de début souhaitée",
                        FieldType.DATE, false));
                fields.add(new FieldDefinition("objectif", "Objectif et bénéfices attendus",
                        FieldType.TEXTAREA, true));
                break;

            // ===== CERTIFICATION =====
            case "Certification":
                fields.add(new FieldDefinition("nomCertification", "Nom de la certification",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("organismeCertif", "Organisme certificateur",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("lieuExamen", "Lieu d'examen",
                        FieldType.LOCATION, true));
                fields.add(new FieldDefinition("coutCertif", "Coût (TND)", FieldType.NUMBER, false));
                fields.add(new FieldDefinition("datePassage", "Date de passage souhaitée",
                        FieldType.DATE, false));
                fields.add(new FieldDefinition("justificationCertif", "Justification",
                        FieldType.TEXTAREA, true));
                break;

            // ===== TÉLÉTRAVAIL =====
            case "Télétravail":
                fields.add(new FieldDefinition("typeTeletravail", "Type de demande", FieldType.COMBO,
                        true, Arrays.asList("Télétravail régulier", "Télétravail occasionnel",
                        "Télétravail exceptionnel")));
                fields.add(new FieldDefinition("joursParSemaine", "Jours par semaine", FieldType.COMBO,
                        true, Arrays.asList("1 jour", "2 jours", "3 jours", "4 jours", "Temps plein")));
                fields.add(new FieldDefinition("joursSouhaites", "Jours souhaités (ex: Lundi, Mardi)",
                        FieldType.TEXT, false));
                fields.add(new FieldDefinition("adresseTeletravail", "Adresse de télétravail",
                        FieldType.LOCATION, true));
                fields.add(new FieldDefinition("dateDebutTeletravail", "Date de début", FieldType.DATE, true));
                fields.add(new FieldDefinition("dateFinTeletravail", "Date de fin (si temporaire)",
                        FieldType.DATE, false));
                fields.add(new FieldDefinition("motifTeletravail", "Motif", FieldType.TEXTAREA, false));
                break;

            // ===== CHANGEMENT D'HORAIRES =====
            case "Changement d'horaires":
                fields.add(new FieldDefinition("horairesActuels", "Horaires actuels",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("horairesSouhaites", "Horaires souhaités",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("dateDebutHoraires", "Date de début", FieldType.DATE, true));
                fields.add(new FieldDefinition("dureeChangement", "Durée", FieldType.COMBO,
                        false, Arrays.asList("Temporaire - 1 mois", "Temporaire - 3 mois",
                        "Temporaire - 6 mois", "Permanent")));
                fields.add(new FieldDefinition("motifHoraires", "Motif", FieldType.TEXTAREA, true));
                break;

            // ===== HEURES SUPPLÉMENTAIRES =====
            case "Heures supplémentaires":
                fields.add(new FieldDefinition("dateHeuresSup", "Date", FieldType.DATE, true));
                fields.add(new FieldDefinition("nombreHeures", "Nombre d'heures",
                        FieldType.NUMBER, true));
                fields.add(new FieldDefinition("heureDebut", "Heure de début (ex: 18:00)",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("heureFin", "Heure de fin (ex: 21:00)",
                        FieldType.TEXT, true));
                fields.add(new FieldDefinition("motifHeuresSup", "Motif/Projet", FieldType.TEXTAREA, true));
                fields.add(new FieldDefinition("valideParResponsable", "Validé par responsable", FieldType.COMBO,
                        true, Arrays.asList("Oui", "En attente de validation")));
                break;

            default:
                System.out.println("Unknown type: " + typeDemande);
                break;
        }

        return fields;
    }

    private VBox createFieldBox(FieldDefinition field) {
        VBox fieldBox = new VBox(5);
        fieldBox.setPadding(new Insets(5, 0, 5, 0));
        fieldBox.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5; -fx-padding: 10;");

        Label label = new Label(field.label + (field.required ? " *" : ""));
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11;");
        dynamicErrorLabels.put(field.key, errorLabel);

        if (field.type == FieldType.LOCATION) {
            // Create location field with map button
            HBox locationBox = createLocationField(field);
            fieldBox.getChildren().addAll(label, locationBox, errorLabel);
        } else {
            Control control = createControl(field);
            dynamicFields.put(field.key, control);
            addClearErrorListener(control, errorLabel);
            fieldBox.getChildren().addAll(label, control, errorLabel);
        }

        return fieldBox;
    }

    private Control createControl(FieldDefinition field) {
        Control control;

        switch (field.type) {
            case TEXT:
                TextField textField = new TextField();
                textField.setPromptText("Entrez " + field.label.toLowerCase());
                textField.setPrefWidth(400);
                control = textField;
                break;

            case NUMBER:
                TextField numberField = new TextField();
                numberField.setPromptText("Entrez un nombre");
                numberField.setPrefWidth(200);
                numberField.textProperty().addListener((o, ov, nv) -> {
                    if (!nv.matches("\\d*\\.?\\d*")) {
                        numberField.setText(ov);
                    }
                });
                control = numberField;
                break;

            case TEXTAREA:
                TextArea textArea = new TextArea();
                textArea.setPromptText("Entrez " + field.label.toLowerCase());
                textArea.setPrefRowCount(3);
                textArea.setPrefWidth(400);
                textArea.setWrapText(true);
                control = textArea;
                break;

            case DATE:
                DatePicker datePicker = new DatePicker();
                datePicker.setPromptText("Sélectionnez une date");
                datePicker.setPrefWidth(200);
                control = datePicker;
                break;

            case COMBO:
                ComboBox<String> comboBox = new ComboBox<>();
                if (field.options != null) {
                    comboBox.setItems(FXCollections.observableArrayList(field.options));
                }
                comboBox.setPromptText("Sélectionnez...");
                comboBox.setPrefWidth(300);
                control = comboBox;
                break;

            default:
                control = new TextField();
                break;
        }

        return control;
    }

    // Create location field with map button - stores TextField in dynamicFields
    private HBox createLocationField(FieldDefinition field) {
        HBox locationBox = new HBox(10);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        locationBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(locationBox, Priority.ALWAYS);

        TextField locationField = new TextField();
        locationField.setPromptText("Cliquez sur 📍 pour sélectionner une adresse");
        locationField.setMaxWidth(Double.MAX_VALUE);
        locationField.setPrefWidth(350);
        HBox.setHgrow(locationField, Priority.ALWAYS);

        Button mapButton = new Button("📍 Carte");
        mapButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 15;");
        mapButton.setMinWidth(100);

        mapButton.setOnAction(e -> {
            MapPickerDialog dialog = new MapPickerDialog();
            String currentAddress = locationField.getText();

            Optional<MapPickerDialog.LocationResult> result;
            if (currentAddress != null && !currentAddress.isEmpty()) {
                result = dialog.showAndWait(currentAddress);
            } else {
                result = dialog.showAndWait();
            }

            result.ifPresent(location -> {
                locationField.setText(location.getAddress());
                locationField.setUserData(location);
                System.out.println("Location selected: " + location.getAddress());
            });
        });

        // Hover effects
        mapButton.setOnMouseEntered(e ->
                mapButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 15;"));
        mapButton.setOnMouseExited(e ->
                mapButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 15;"));

        locationBox.getChildren().addAll(locationField, mapButton);

        // Store the TextField (which IS a Control) in dynamicFields
        dynamicFields.put(field.key, locationField);
        locationFieldKeys.add(field.key);

        // Add error clear listener
        Label errorLabel = dynamicErrorLabels.get(field.key);
        if (errorLabel != null) {
            locationField.textProperty().addListener((o, ov, nv) -> {
                if (nv != null && !nv.trim().isEmpty()) {
                    clearFieldError(locationField, errorLabel);
                }
            });
        }

        return locationBox;
    }

    private void addClearErrorListener(Control control, Label errorLabel) {
        if (control instanceof TextField) {
            ((TextField) control).textProperty().addListener((o, ov, nv) -> {
                if (nv != null && !nv.trim().isEmpty()) {
                    clearFieldError(control, errorLabel);
                }
            });
        } else if (control instanceof TextArea) {
            ((TextArea) control).textProperty().addListener((o, ov, nv) -> {
                if (nv != null && !nv.trim().isEmpty()) {
                    clearFieldError(control, errorLabel);
                }
            });
        } else if (control instanceof ComboBox) {
            ((ComboBox<?>) control).valueProperty().addListener((o, ov, nv) -> {
                if (nv != null) {
                    clearFieldError(control, errorLabel);
                }
            });
        } else if (control instanceof DatePicker) {
            ((DatePicker) control).valueProperty().addListener((o, ov, nv) -> {
                if (nv != null) {
                    clearFieldError(control, errorLabel);
                }
            });
        }
    }

    // ==================== VALIDATION ====================

    public boolean validateForm(TextField titreField, Label titreError,
                                ComboBox<String> categorieCombo, Label categorieError,
                                ComboBox<String> typeDemandeCombo, Label typeError,
                                ComboBox<String> prioriteCombo, Label prioriteError,
                                TextArea descriptionArea, Label descriptionError,
                                ComboBox<String> statusCombo, Label statusError,
                                DatePicker dateCreationPicker, Label dateError) {
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

        if (statusCombo != null && statusCombo.getValue() == null) {
            statusError.setText("Le statut est obligatoire");
            statusCombo.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (dateCreationPicker.getValue() == null) {
            dateError.setText("La date est obligatoire");
            dateCreationPicker.setStyle("-fx-border-color: red;");
            valid = false;
        }

        if (!validateDynamicFields()) {
            valid = false;
        }

        return valid;
    }

    public boolean validateDynamicFields() {
        boolean valid = true;

        for (Map.Entry<String, Control> entry : dynamicFields.entrySet()) {
            String key = entry.getKey();
            Control control = entry.getValue();
            Label errorLabel = dynamicErrorLabels.get(key);

            boolean isEmpty = isFieldEmpty(control);

            // Check if required by looking at parent VBox label
            if (control.getParent() != null) {
                VBox parent = null;

                // For location fields, the parent is HBox, grandparent is VBox
                if (control.getParent() instanceof HBox) {
                    if (control.getParent().getParent() instanceof VBox) {
                        parent = (VBox) control.getParent().getParent();
                    }
                } else if (control.getParent() instanceof VBox) {
                    parent = (VBox) control.getParent();
                }

                if (parent != null && !parent.getChildren().isEmpty()) {
                    Node firstChild = parent.getChildren().get(0);
                    if (firstChild instanceof Label) {
                        Label fieldLabel = (Label) firstChild;
                        boolean isRequired = fieldLabel.getText().endsWith("*");

                        if (isRequired && isEmpty) {
                            if (errorLabel != null) {
                                errorLabel.setText("Ce champ est obligatoire");
                            }
                            control.setStyle("-fx-border-color: red;");
                            valid = false;
                        }
                    }
                }
            }
        }

        return valid;
    }

    private boolean isFieldEmpty(Control control) {
        if (control instanceof TextField) {
            return ((TextField) control).getText().trim().isEmpty();
        } else if (control instanceof TextArea) {
            return ((TextArea) control).getText().trim().isEmpty();
        } else if (control instanceof ComboBox) {
            return ((ComboBox<?>) control).getValue() == null;
        } else if (control instanceof DatePicker) {
            return ((DatePicker) control).getValue() == null;
        }
        return true;
    }

    public void clearFieldError(Control control, Label errorLabel) {
        if (control != null) {
            control.setStyle("");
        }
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    // ==================== FIELD VALUE MANIPULATION ====================

    public void setFieldValue(Control control, String value) {
        if (control == null || value == null || value.isEmpty()) {
            return;
        }

        if (control instanceof TextField) {
            ((TextField) control).setText(value);
        } else if (control instanceof TextArea) {
            ((TextArea) control).setText(value);
        } else if (control instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> comboBox = (ComboBox<String>) control;
            comboBox.setValue(value);
        } else if (control instanceof DatePicker) {
            try {
                LocalDate date = LocalDate.parse(value);
                ((DatePicker) control).setValue(date);
            } catch (Exception e) {
                System.out.println("Could not parse date: " + value);
            }
        }
    }

    public String getFieldValue(Control control) {
        if (control instanceof TextField) {
            return ((TextField) control).getText().trim();
        } else if (control instanceof TextArea) {
            return ((TextArea) control).getText().trim();
        } else if (control instanceof ComboBox) {
            Object value = ((ComboBox<?>) control).getValue();
            return value != null ? value.toString() : "";
        } else if (control instanceof DatePicker) {
            LocalDate date = ((DatePicker) control).getValue();
            return date != null ? date.format(DateTimeFormatter.ISO_DATE) : "";
        }
        return "";
    }

    // ==================== JSON BUILDING ====================

    public String buildDetailsJson() {
        if (dynamicFields.isEmpty()) {
            return "{}";
        }

        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Control> entry : dynamicFields.entrySet()) {
            String key = entry.getKey();
            String value = getFieldValue(entry.getValue());

            if (value != null && !value.isEmpty()) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(escapeJson(key)).append("\":\"")
                        .append(escapeJson(value)).append("\"");
                first = false;
            }
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

    // ==================== JSON PARSING ====================

    public Map<String, String> parseDetailsJson(String json) {
        Map<String, String> result = new LinkedHashMap<>();

        if (json == null || json.equals("{}") || json.trim().isEmpty()) {
            return result;
        }

        try {
            String content = json.trim();
            if (content.startsWith("{")) content = content.substring(1);
            if (content.endsWith("}")) content = content.substring(0, content.length() - 1);

            List<String> pairs = splitJsonPairs(content);

            for (String pair : pairs) {
                int colonIndex = pair.indexOf(':');
                if (colonIndex > 0) {
                    String key = removeQuotes(pair.substring(0, colonIndex).trim());
                    String value = removeQuotes(pair.substring(colonIndex + 1).trim());
                    key = unescapeJson(key);
                    value = unescapeJson(value);
                    result.put(key, value);
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
        }

        return result;
    }

    private List<String> splitJsonPairs(String content) {
        List<String> pairs = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (c == ',' && !inQuotes) {
                pairs.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            pairs.add(current.toString().trim());
        }
        return pairs;
    }

    private String removeQuotes(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private String unescapeJson(String text) {
        return text.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    // ==================== GETTERS ====================

    public Map<String, Control> getDynamicFields() {
        return dynamicFields;
    }

    public Map<String, Label> getDynamicErrorLabels() {
        return dynamicErrorLabels;
    }

    public boolean isLocationField(String key) {
        return locationFieldKeys.contains(key);
    }

    // ==================== INNER CLASSES ====================

    private enum FieldType {
        TEXT, NUMBER, TEXTAREA, DATE, COMBO, LOCATION
    }

    private static class FieldDefinition {
        String key;
        String label;
        FieldType type;
        boolean required;
        List<String> options;

        FieldDefinition(String key, String label, FieldType type, boolean required) {
            this(key, label, type, required, null);
        }

        FieldDefinition(String key, String label, FieldType type, boolean required, List<String> options) {
            this.key = key;
            this.label = label;
            this.type = type;
            this.required = required;
            this.options = options;
        }
    }
}