package controller.employers.RHetAdminE;

import controller.demandes.DemandeFormHelper;
import controller.demandes.NavigationHelper;
import entities.demande.Demande;
import entities.demande.DemandeDetails;
import entities.demande.HistoriqueDemande;
import service.api.AIDocumentGeneratorService;
import service.api.WeatherService;
import service.demande.DemandeCRUD;
import service.demande.DemandeDetailsCRUD;
import service.demande.HistoriqueDemandeCRUD;
import service.employe.employeCRUD;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DemandesController implements Initializable {

    // ═══════════════════════════════════════════════════════════════════════
    // FXML COMPONENTS
    // ═══════════════════════════════════════════════════════════════════════

    @FXML private TabPane mainTabPane;
    @FXML private TextField rechercheField;
    @FXML private TableView<Demande> demandesTable;
    @FXML private TableColumn<Demande, String> titreCol;
    @FXML private TableColumn<Demande, String> categorieCol;
    @FXML private TableColumn<Demande, String> typeCol;
    @FXML private TableColumn<Demande, String> prioriteCol;
    @FXML private TableColumn<Demande, String> statusCol;
    @FXML private TableColumn<Demande, Date> dateCol;
    @FXML private TableColumn<Demande, Void> actionsCol;

    @FXML private VBox placeholderBox;
    @FXML private VBox detailsContent;
    @FXML private Label detailTitreLabel;
    @FXML private Label detailCategorieLabel;
    @FXML private Label detailTypeLabel;
    @FXML private Label detailPrioriteLabel;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailDescriptionLabel;
    @FXML private VBox detailSpecificContainer;
    @FXML private VBox historiqueContainer;
    @FXML private Button avancerBtn;

    @FXML private Label lblTotalDemandes;
    @FXML private Label lblTotalHistorique;
    @FXML private Label lblDemandesResolues;
    @FXML private HBox statutStatsContainer;
    @FXML private HBox prioriteStatsContainer;
    @FXML private HBox typeStatsContainer;
    @FXML private HBox categorieStatsContainer;
    @FXML private HBox acteurStatsContainer;

    @FXML private VBox weatherWidgetContainer;
    @FXML private ImageView weatherIcon;
    @FXML private Label cityLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label humidityLabel;
    @FXML private Label windLabel;
    @FXML private Label feelsLikeLabel;
    @FXML private Label recommendationLabel;
    @FXML private Label lastUpdateLabel;
    @FXML private Label alertLabel;
    @FXML private HBox alertBox;
    @FXML private VBox forecastSection;
    @FXML private HBox forecastContainer;
    @FXML private Button toggleForecastBtn;
    @FXML private VBox weatherTipsBox;
    @FXML private Label weatherTipLabel;
    @FXML private Label quickStatTotal;
    @FXML private Label quickStatPending;
    @FXML private Label quickStatUrgent;

    // ═══════════════════════════════════════════════════════════════════════
    // SERVICES & DATA
    // ═══════════════════════════════════════════════════════════════════════

    private ObservableList<Demande> demandesList = FXCollections.observableArrayList();
    private DemandeCRUD demandeCRUD;
    private DemandeDetailsCRUD detailsCRUD;
    private HistoriqueDemandeCRUD historiqueCRUD;
    private employeCRUD employeCrud;
    private DemandeFormHelper formHelper;
    private WeatherService weatherService;
    private Demande selectedDemande;
    private boolean forecastVisible = false;

    // Loading stage for proper control
    private Stage loadingStage;

    // ═══════════════════════════════════════════════════════════════════════
    // INITIALIZE
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("═══════════════════════════════════════════════");
            System.out.println("🚀 Initializing DemandesController...");
            System.out.println("═══════════════════════════════════════════════");

            demandeCRUD = new DemandeCRUD();
            detailsCRUD = new DemandeDetailsCRUD();
            historiqueCRUD = new HistoriqueDemandeCRUD();
            employeCrud = new employeCRUD();
            formHelper = new DemandeFormHelper();
            weatherService = new WeatherService();

            initializeTableColumns();
            loadDemandes();

            if (demandesTable != null) {
                demandesTable.getSelectionModel().selectedItemProperty()
                        .addListener((obs, oldVal, newVal) -> {
                            if (newVal != null) {
                                selectedDemande = newVal;
                                showDetails(newVal);
                            }
                        });
            }

            if (rechercheField != null) {
                rechercheField.textProperty().addListener((obs, oldVal, newVal) -> filterDemandes(newVal));
            }

            if (mainTabPane != null) {
                mainTabPane.getSelectionModel().selectedItemProperty()
                        .addListener((obs, oldTab, newTab) -> {
                            if (newTab != null && newTab.getText().contains("Statistiques")) {
                                loadStatistiques();
                            }
                        });
            }

            loadStatistiques();
            initializeWeatherWidget();
            updateQuickStats();

            System.out.println("✅ DemandesController initialized successfully!");

        } catch (Exception e) {
            System.err.println("❌ INIT ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // LOADING DIALOG - PROPER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════════════════

    private void showLoadingDialog(String title, String message) {
        Platform.runLater(() -> {
            try {
                loadingStage = new Stage();
                loadingStage.initModality(Modality.APPLICATION_MODAL);
                loadingStage.initStyle(StageStyle.UNDECORATED);
                loadingStage.setResizable(false);

                VBox content = new VBox(20);
                content.setAlignment(Pos.CENTER);
                content.setPadding(new Insets(40));
                content.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");

                ProgressIndicator progress = new ProgressIndicator();
                progress.setPrefSize(60, 60);

                Label titleLabel = new Label(title);
                titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                Label messageLabel = new Label(message);
                messageLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #666;");
                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(300);

                content.getChildren().addAll(progress, titleLabel, messageLabel);

                Scene scene = new Scene(content);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                loadingStage.setScene(scene);
                loadingStage.initStyle(StageStyle.TRANSPARENT);

                // Center on parent
                if (mainTabPane != null && mainTabPane.getScene() != null) {
                    Stage parent = (Stage) mainTabPane.getScene().getWindow();
                    loadingStage.initOwner(parent);
                    loadingStage.setX(parent.getX() + (parent.getWidth() - 400) / 2);
                    loadingStage.setY(parent.getY() + (parent.getHeight() - 200) / 2);
                }

                loadingStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void hideLoadingDialog() {
        Platform.runLater(() -> {
            try {
                if (loadingStage != null) {
                    loadingStage.close();
                    loadingStage = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GENERATE DOCUMENT - FIXED
    // ═══════════════════════════════════════════════════════════════════════

    @FXML
    public void generateDocument(ActionEvent event) {
        try {
            if (selectedDemande == null) {
                showAlert(Alert.AlertType.WARNING, "Avertissement",
                        "⚠️ Veuillez sélectionner une demande d'abord !");
                return;
            }

            // Choose output folder
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choisir où sauvegarder les documents");

            String userHome = System.getProperty("user.home");
            File defaultDir = new File(userHome, "Documents");
            if (defaultDir.exists()) {
                chooser.setInitialDirectory(defaultDir);
            }

            File folder = chooser.showDialog(mainTabPane.getScene().getWindow());
            if (folder == null) return;

            // Get employee info
            entities.employe.employe emp = employeCrud.getById(selectedDemande.getIdEmploye());
            if (emp == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "❌ Employé non trouvé !");
                return;
            }

            String nom = emp.getNom() != null ? emp.getNom().trim() : "";
            String prenom = emp.getPrenom() != null ? emp.getPrenom().trim() : "";
            String employeeName = (prenom + " " + nom).trim();
            if (employeeName.isEmpty()) employeeName = "Employe_" + emp.getId_employé();

            String position = emp.getPoste() != null ? emp.getPoste() : "Employé";
            String employeeId = "EMP" + emp.getId_employé();

            // Get demande details
            DemandeDetails details = null;
            try {
                details = detailsCRUD.getByDemande(selectedDemande.getIdDemande());
            } catch (Exception e) {
                System.err.println("Warning: Could not load demande details: " + e.getMessage());
            }

            String additionalInfo = selectedDemande.getDescription() != null ? selectedDemande.getDescription() : "";
            if (details != null && details.getDetails() != null && !details.getDetails().isEmpty()) {
                additionalInfo += "\n\nDétails supplémentaires:\n" + details.getDetails();
            }

            // Show loading dialog
            showLoadingDialog("🔄 Génération en cours...",
                    "Type: " + selectedDemande.getTypeDemande() + "\n" +
                            "Employé: " + employeeName);

            // Generate document
            AIDocumentGeneratorService docService = new AIDocumentGeneratorService();

            final String finalEmployeeName = employeeName;
            final String finalPosition = position;
            final String finalEmployeeId = employeeId;
            final String finalAdditionalInfo = additionalInfo;
            final File finalFolder = folder;

            docService.generateDocumentAsync(
                    selectedDemande.getTypeDemande(),
                    finalEmployeeName,
                    finalPosition,
                    finalEmployeeId,
                    selectedDemande.getDateCreation(),
                    finalAdditionalInfo
            ).thenAccept(doc -> {
                // Hide loading FIRST
                hideLoadingDialog();

                Platform.runLater(() -> {
                    try {
                        if (doc != null && doc.isValid) {
                            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String baseName = sanitizeFilename(doc.title + "_" + finalEmployeeName);

                            String pdfPath = finalFolder.getAbsolutePath() + File.separator + baseName + "_" + timestamp + ".pdf";
                            String wordPath = finalFolder.getAbsolutePath() + File.separator + baseName + "_" + timestamp + ".docx";

                            // Export files
                            docService.exportToPDFAsync(doc, pdfPath)
                                    .thenAccept(pdfFile -> System.out.println("✅ PDF: " + pdfFile));

                            docService.exportToWordAsync(doc, wordPath)
                                    .thenAccept(wordFile -> System.out.println("✅ Word: " + wordFile));

                            // Show success dialog
                            showSuccessDialog(finalFolder, baseName + "_" + timestamp);

                        } else {
                            showAlert(Alert.AlertType.WARNING, "Avertissement",
                                    "⚠️ Document généré avec le modèle par défaut.\n\n" +
                                            "Vérifiez que Ollama est lancé:\n  ollama run llama3");
                        }
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "❌ Erreur: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }).exceptionally(ex -> {
                hideLoadingDialog();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "❌ Erreur: " + ex.getMessage() + "\n\nVérifiez que Ollama est lancé.");
                });
                return null;
            });

        } catch (Exception e) {
            hideLoadingDialog();
            showAlert(Alert.AlertType.ERROR, "Erreur", "❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSuccessDialog(File folder, String baseName) {
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Succès ✅");
        successAlert.setHeaderText("Documents générés avec succès !");
        successAlert.setContentText(
                "📄 " + baseName + ".pdf\n" +
                        "📄 " + baseName + ".docx\n\n" +
                        "📂 Dossier:\n" + folder.getAbsolutePath());

        ButtonType openFolderBtn = new ButtonType("📂 Ouvrir le dossier");
        ButtonType closeBtn = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        successAlert.getButtonTypes().setAll(openFolderBtn, closeBtn);

        successAlert.showAndWait().ifPresent(response -> {
            if (response == openFolderBtn) {
                openFolder(folder);
            }
        });
    }

    private void openFolder(File folder) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                new Thread(() -> {
                    try {
                        java.awt.Desktop.getDesktop().open(folder);
                    } catch (Exception e) {
                        System.err.println("Could not open folder: " + e.getMessage());
                    }
                }).start();
            }
        } catch (Exception e) {
            System.err.println("Desktop not supported: " + e.getMessage());
        }
    }

    private String sanitizeFilename(String s) {
        if (s == null || s.isEmpty()) {
            return "document";
        }

        // Clean the string first
        String clean = s.replaceAll("[^a-zA-Z0-9àâäéèêëïîôùûüçÀÂÄÉÈÊËÏÎÔÙÛÜÇ\\s_-]", "")
                .replaceAll("\\s+", "_")
                .trim();

        // Check if clean string is empty after sanitization
        if (clean.isEmpty()) {
            return "document";
        }

        // Now use the CLEAN string's length, not the original
        if (clean.length() > 50) {
            clean = clean.substring(0, 50);
        }

        return clean;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TABLE INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════

    private void initializeTableColumns() {
        try {
            if (titreCol != null) titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
            if (categorieCol != null) categorieCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
            if (typeCol != null) typeCol.setCellValueFactory(new PropertyValueFactory<>("typeDemande"));

            if (prioriteCol != null) {
                prioriteCol.setCellValueFactory(new PropertyValueFactory<>("priorite"));
                prioriteCol.setCellFactory(col -> new TableCell<Demande, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            switch (item.toUpperCase()) {
                                case "HAUTE":
                                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                                    break;
                                case "NORMALE":
                                    setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                                    break;
                                case "BASSE":
                                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                                    break;
                                default:
                                    setStyle("");
                            }
                        }
                    }
                });
            }

            if (statusCol != null) {
                statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
                statusCol.setCellFactory(col -> new TableCell<Demande, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            Label badge = new Label(item);
                            badge.setPadding(new Insets(3, 8, 3, 8));
                            badge.setStyle(getStatusBadgeStyle(item));
                            setGraphic(badge);
                            setText(null);
                        }
                    }
                });
            }

            if (dateCol != null) {
                dateCol.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
                dateCol.setCellFactory(col -> new TableCell<Demande, Date>() {
                    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    @Override
                    protected void updateItem(Date item, boolean empty) {
                        super.updateItem(item, empty);
                        setText((empty || item == null) ? null : sdf.format(item));
                    }
                });
            }

            if (actionsCol != null) {
                actionsCol.setCellFactory(col -> new TableCell<Demande, Void>() {
                    private final Button modBtn = new Button("✏️");
                    private final Button delBtn = new Button("🗑️");
                    private final HBox box = new HBox(5, modBtn, delBtn);

                    {
                        box.setAlignment(Pos.CENTER);
                        modBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                        delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

                        modBtn.setOnAction(e -> ouvrirModifier(getTableView().getItems().get(getIndex())));
                        delBtn.setOnAction(e -> supprimerDemande(getTableView().getItems().get(getIndex())));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : box);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getStatusBadgeStyle(String status) {
        String baseStyle = "-fx-background-radius: 10; -fx-font-size: 10; -fx-font-weight: bold; ";
        switch (status) {
            case "Nouvelle": return baseStyle + "-fx-background-color: #3498db; -fx-text-fill: white;";
            case "En cours": return baseStyle + "-fx-background-color: #f39c12; -fx-text-fill: white;";
            case "En attente": return baseStyle + "-fx-background-color: #e67e22; -fx-text-fill: white;";
            case "Résolue": return baseStyle + "-fx-background-color: #27ae60; -fx-text-fill: white;";
            case "Fermée": return baseStyle + "-fx-background-color: #95a5a6; -fx-text-fill: white;";
            default: return baseStyle + "-fx-background-color: #bdc3c7; -fx-text-fill: #2c3e50;";
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // LOAD DATA
    // ═══════════════════════════════════════════════════════════════════════

    public void loadDemandes() {
        try {
            demandesList.clear();
            demandesList.addAll(demandeCRUD.afficher());
            if (demandesTable != null) demandesTable.setItems(demandesList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterDemandes(String searchText) {
        try {
            resetDetailsPanel();
            if (searchText == null || searchText.trim().isEmpty()) {
                loadDemandes();
            } else {
                String s = searchText.toLowerCase().trim();
                ObservableList<Demande> filtered = FXCollections.observableArrayList();
                for (Demande d : demandesList) {
                    if ((d.getTitre() != null && d.getTitre().toLowerCase().contains(s)) ||
                            (d.getCategorie() != null && d.getCategorie().toLowerCase().contains(s)) ||
                            (d.getTypeDemande() != null && d.getTypeDemande().toLowerCase().contains(s))) {
                        filtered.add(d);
                    }
                }
                demandesTable.setItems(filtered);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SHOW DETAILS - FIXED
    // ═══════════════════════════════════════════════════════════════════════

    private void showDetails(Demande d) {
        try {
            if (placeholderBox != null) {
                placeholderBox.setVisible(false);
                placeholderBox.setManaged(false);
            }
            if (detailsContent != null) {
                detailsContent.setVisible(true);
                detailsContent.setManaged(true);
            }

            if (detailTitreLabel != null) detailTitreLabel.setText(d.getTitre() != null ? d.getTitre() : "Sans titre");
            if (detailCategorieLabel != null) detailCategorieLabel.setText(d.getCategorie() != null ? d.getCategorie() : "N/A");
            if (detailTypeLabel != null) detailTypeLabel.setText(d.getTypeDemande() != null ? d.getTypeDemande() : "N/A");
            if (detailDescriptionLabel != null) detailDescriptionLabel.setText(d.getDescription() != null ? d.getDescription() : "Aucune description");

            if (detailPrioriteLabel != null) {
                String priorite = d.getPriorite() != null ? d.getPriorite() : "N/A";
                detailPrioriteLabel.setText(priorite);
                switch (priorite.toUpperCase()) {
                    case "HAUTE":
                        detailPrioriteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-background-color: #fadbd8; -fx-padding: 3 8; -fx-background-radius: 5;");
                        break;
                    case "NORMALE":
                        detailPrioriteLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-background-color: #d4e6f1; -fx-padding: 3 8; -fx-background-radius: 5;");
                        break;
                    case "BASSE":
                        detailPrioriteLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-background-color: #d5f5e3; -fx-padding: 3 8; -fx-background-radius: 5;");
                        break;
                    default:
                        detailPrioriteLabel.setStyle("-fx-font-weight: bold;");
                }
            }

            if (detailStatusLabel != null) {
                String status = d.getStatus() != null ? d.getStatus() : "N/A";
                detailStatusLabel.setText(status);
                detailStatusLabel.setStyle(getStatusLabelStyle(status));
            }

            if (detailDateLabel != null) {
                if (d.getDateCreation() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH);
                    detailDateLabel.setText(sdf.format(d.getDateCreation()));
                } else {
                    detailDateLabel.setText("Date non disponible");
                }
            }

            loadDemandeDetails(d.getIdDemande());
            loadHistorique(d.getIdDemande());

            if (avancerBtn != null) {
                boolean isClosed = "Fermée".equals(d.getStatus());
                avancerBtn.setDisable(isClosed);
                avancerBtn.setText(isClosed ? "✅ Demande Fermée" : "⚡ Avancer Statut");
                avancerBtn.setStyle(isClosed ?
                        "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-cursor: default; -fx-background-radius: 5; -fx-padding: 8 15;" :
                        "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 8 15;");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getStatusLabelStyle(String status) {
        String baseStyle = "-fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 8; -fx-font-size: 12;";
        switch (status) {
            case "Nouvelle": return baseStyle + "-fx-background-color: #3498db; -fx-text-fill: white;";
            case "En cours": return baseStyle + "-fx-background-color: #f39c12; -fx-text-fill: white;";
            case "En attente": return baseStyle + "-fx-background-color: #e67e22; -fx-text-fill: white;";
            case "Résolue": return baseStyle + "-fx-background-color: #27ae60; -fx-text-fill: white;";
            case "Fermée": return baseStyle + "-fx-background-color: #95a5a6; -fx-text-fill: white;";
            default: return baseStyle + "-fx-background-color: #bdc3c7; -fx-text-fill: #2c3e50;";
        }
    }

    private void loadDemandeDetails(int idDemande) {
        try {
            if (detailSpecificContainer == null) return;
            detailSpecificContainer.getChildren().clear();

            DemandeDetails details = detailsCRUD.getByDemande(idDemande);

            if (details != null && details.getDetails() != null && !details.getDetails().isEmpty()) {
                String detailsText = details.getDetails();

                try {
                    if (detailsText.trim().startsWith("{")) {
                        org.json.JSONObject json = new org.json.JSONObject(detailsText);
                        for (String key : json.keySet()) {
                            HBox row = createDetailRow(formatKey(key), json.optString(key, "N/A"));
                            detailSpecificContainer.getChildren().add(row);
                        }
                    } else {
                        Label detailLabel = new Label(detailsText);
                        detailLabel.setWrapText(true);
                        detailLabel.setStyle("-fx-text-fill: #333;");
                        detailSpecificContainer.getChildren().add(detailLabel);
                    }
                } catch (Exception jsonEx) {
                    Label detailLabel = new Label(detailsText);
                    detailLabel.setWrapText(true);
                    detailLabel.setStyle("-fx-text-fill: #333;");
                    detailSpecificContainer.getChildren().add(detailLabel);
                }
            } else {
                Label noDetails = new Label("Aucun détail spécifique");
                noDetails.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                detailSpecificContainer.getChildren().add(noDetails);
            }

        } catch (SQLException e) {
            Label errorLabel = new Label("Erreur de chargement des détails");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            if (detailSpecificContainer != null) detailSpecificContainer.getChildren().add(errorLabel);
        }
    }

    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label keyLabel = new Label(label + ":");
        keyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-min-width: 120;");
        keyLabel.setMinWidth(120);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #333;");
        valueLabel.setWrapText(true);

        row.getChildren().addAll(keyLabel, valueLabel);
        return row;
    }

    private String formatKey(String key) {
        if (key == null) return "";
        return key.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("_", " ")
                .substring(0, 1).toUpperCase() +
                key.replaceAll("([a-z])([A-Z])", "$1 $2")
                        .replaceAll("_", " ")
                        .substring(1);
    }

    private void loadHistorique(int idDemande) {
        try {
            if (historiqueContainer == null) return;
            historiqueContainer.getChildren().clear();

            List<HistoriqueDemande> historiques = historiqueCRUD.getByDemande(idDemande);

            if (historiques != null && !historiques.isEmpty()) {
                for (HistoriqueDemande h : historiques) {
                    VBox histItem = createHistoriqueItem(h);
                    historiqueContainer.getChildren().add(histItem);
                }
            } else {
                Label noHist = new Label("Aucun historique disponible");
                noHist.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 10;");
                historiqueContainer.getChildren().add(noHist);
            }

        } catch (SQLException e) {
            Label errorLabel = new Label("Erreur de chargement de l'historique");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            if (historiqueContainer != null) historiqueContainer.getChildren().add(errorLabel);
        }
    }

    private VBox createHistoriqueItem(HistoriqueDemande h) {
        VBox item = new VBox(5);
        item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        HBox statusChange = new HBox(8);
        statusChange.setAlignment(Pos.CENTER_LEFT);

        Label oldStatus = new Label(h.getAncienStatut());
        oldStatus.setStyle(getStatusBadgeStyle(h.getAncienStatut()));
        oldStatus.setPadding(new Insets(2, 6, 2, 6));

        Label arrow = new Label("→");
        arrow.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");

        Label newStatus = new Label(h.getNouveauStatut());
        newStatus.setStyle(getStatusBadgeStyle(h.getNouveauStatut()));
        newStatus.setPadding(new Insets(2, 6, 2, 6));

        statusChange.getChildren().addAll(oldStatus, arrow, newStatus);

        HBox metaInfo = new HBox(15);
        metaInfo.setAlignment(Pos.CENTER_LEFT);

        Label acteur = new Label("👤 " + (h.getActeur() != null ? h.getActeur() : "Inconnu"));
        acteur.setStyle("-fx-text-fill: #8e44ad; -fx-font-weight: bold; -fx-font-size: 11;");

        Label dateLabel = new Label("📅 " + (h.getDateAction() != null ?
                new SimpleDateFormat("dd/MM/yyyy HH:mm").format(h.getDateAction()) : "N/A"));
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");

        metaInfo.getChildren().addAll(acteur, dateLabel);

        item.getChildren().addAll(statusChange, metaInfo);

        if (h.getCommentaire() != null && !h.getCommentaire().isEmpty()) {
            Label comment = new Label("💬 " + h.getCommentaire());
            comment.setStyle("-fx-text-fill: #333; -fx-font-size: 11;");
            comment.setWrapText(true);
            item.getChildren().add(comment);
        }

        return item;
    }

    private void resetDetailsPanel() {
        try {
            if (placeholderBox != null) {
                placeholderBox.setVisible(true);
                placeholderBox.setManaged(true);
            }
            if (detailsContent != null) {
                detailsContent.setVisible(false);
                detailsContent.setManaged(false);
            }
            selectedDemande = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // WEATHER WIDGET
    // ═══════════════════════════════════════════════════════════════════════

    private void initializeWeatherWidget() {
        try {
            if (alertBox != null) { alertBox.setVisible(false); alertBox.setManaged(false); }
            if (forecastSection != null) { forecastSection.setVisible(false); forecastSection.setManaged(false); }
            refreshWeather();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void refreshWeather() {
        try {
            if (temperatureLabel != null) temperatureLabel.setText("...");
            if (descriptionLabel != null) descriptionLabel.setText("Chargement...");

            weatherService.getCurrentWeatherAsync().thenAccept(weather ->
                    Platform.runLater(() -> {
                        if (weather != null && weather.isValid) updateWeatherUI(weather);
                        else showWeatherError();
                    })
            );
        } catch (Exception e) {
            showWeatherError();
        }
    }

    private void updateWeatherUI(WeatherService.WeatherData weather) {
        try {
            if (cityLabel != null) cityLabel.setText(weather.cityName);
            if (temperatureLabel != null) temperatureLabel.setText(String.format("%.0f°C", weather.temperature));
            if (descriptionLabel != null) descriptionLabel.setText(weather.description);
            if (humidityLabel != null) humidityLabel.setText(weather.humidity + "%");
            if (windLabel != null) windLabel.setText(String.format("%.1f m/s", weather.windSpeed));
            if (feelsLikeLabel != null) feelsLikeLabel.setText(String.format("%.0f°C", weather.feelsLike));
            if (weatherIcon != null) {
                try { weatherIcon.setImage(new Image(weather.getIconUrl(), true)); } catch (Exception ignored) {}
            }
            if (lastUpdateLabel != null) {
                lastUpdateLabel.setText("Mis à jour: " + new SimpleDateFormat("HH:mm").format(new Date()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showWeatherError() {
        if (temperatureLabel != null) temperatureLabel.setText("--°C");
        if (descriptionLabel != null) descriptionLabel.setText("Non disponible");
    }

    @FXML
    public void toggleForecast() {
        forecastVisible = !forecastVisible;
        if (forecastSection != null) {
            forecastSection.setVisible(forecastVisible);
            forecastSection.setManaged(forecastVisible);
        }
        if (toggleForecastBtn != null) {
            toggleForecastBtn.setText(forecastVisible ? "📅 Masquer ▲" : "📅 Voir la semaine ▼");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════════════════

    @FXML
    public void refreshStatistiques() {
        loadStatistiques();
    }

    private void loadStatistiques() {
        try {
            if (lblTotalDemandes != null) lblTotalDemandes.setText(String.valueOf(demandeCRUD.countAll()));
            if (lblTotalHistorique != null) lblTotalHistorique.setText(String.valueOf(historiqueCRUD.countAll()));
            if (lblDemandesResolues != null) lblDemandesResolues.setText(String.valueOf(demandeCRUD.countByStatus("Résolue")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateQuickStats() {
        try {
            List<Demande> all = demandeCRUD.afficher();
            int total = all.size(), pending = 0, urgent = 0;
            for (Demande d : all) {
                if ("En attente".equals(d.getStatus()) || "Nouvelle".equals(d.getStatus())) pending++;
                if ("HAUTE".equalsIgnoreCase(d.getPriorite()) && !"Fermée".equals(d.getStatus())) urgent++;
            }
            if (quickStatTotal != null) quickStatTotal.setText(String.valueOf(total));
            if (quickStatPending != null) quickStatPending.setText(String.valueOf(pending));
            if (quickStatUrgent != null) quickStatUrgent.setText(String.valueOf(urgent));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ACTIONS
    // ═══════════════════════════════════════════════════════════════════════

    @FXML
    public void ouvrirAvancer() {
        try {
            if (selectedDemande == null) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Sélectionnez une demande !");
                return;
            }
            if ("Fermée".equals(selectedDemande.getStatus())) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Cette demande est déjà fermée !");
                return;
            }
            FXMLLoader loader = NavigationHelper.loadView("/emp/RHetAdminE/avancer-demande.fxml");
            AvancerDemandeController ctrl = loader.getController();
            ctrl.setParentController(this);
            ctrl.setDemande(selectedDemande);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void ouvrirModifier(Demande demande) {
        try {
            FXMLLoader loader = NavigationHelper.loadView("/emp/RHetAdminE/modifier-demande.fxml");
            ModifierDemandeController ctrl = loader.getController();
            ctrl.setParentController(this);
            ctrl.setDemande(demande);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void supprimerDemande(Demande demande) {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setContentText("Supprimer \"" + demande.getTitre() + "\" ?");
            if (confirm.showAndWait().get() == ButtonType.OK) {
                historiqueCRUD.supprimerByDemande(demande.getIdDemande());
                detailsCRUD.supprimerByDemande(demande.getIdDemande());
                demandeCRUD.supprimer(demande.getIdDemande());
                loadDemandes();
                resetDetailsPanel();
                updateQuickStats();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
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