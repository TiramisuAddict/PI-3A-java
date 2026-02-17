package controller.demandes;

import entites.Demande;
import entites.DemandeDetails;
import entites.HistoriqueDemande;
import service.demande.DemandeCRUD;
import service.demande.DemandeDetailsCRUD;
import service.demande.HistoriqueDemandeCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DemandesController implements Initializable {

    @FXML private TextField rechercheField;
    @FXML private TableView<Demande> demandesTable;
    @FXML private TableColumn<Demande, String> titreCol;
    @FXML private TableColumn<Demande, String> categorieCol;
    @FXML private TableColumn<Demande, String> typeCol;
    @FXML private TableColumn<Demande, String> prioriteCol;
    @FXML private TableColumn<Demande, String> statusCol;
    @FXML private TableColumn<Demande, Date> dateCol;
    @FXML private TableColumn<Demande, Void> actionsCol;

    // Details Panel
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

    private ObservableList<Demande> demandesList = FXCollections.observableArrayList();
    private DemandeCRUD demandeCRUD;
    private DemandeDetailsCRUD detailsCRUD;
    private HistoriqueDemandeCRUD historiqueCRUD;
    private DemandeFormHelper formHelper;
    private Demande selectedDemande;

    // ============ INITIALIZATION ============

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        demandeCRUD = new DemandeCRUD();
        detailsCRUD = new DemandeDetailsCRUD();
        historiqueCRUD = new HistoriqueDemandeCRUD();
        formHelper = new DemandeFormHelper();

        initializeTableColumns();
        loadDemandes();

        // Table selection listener
        demandesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedDemande = newVal;
                showDetails(newVal);
            }
        });

        // Real-time search
        rechercheField.textProperty().addListener((obs, oldVal, newVal) -> {
            placeholderBox.setVisible(true);
            placeholderBox.setManaged(true);
            detailsContent.setVisible(false);
            detailsContent.setManaged(false);
            selectedDemande = null;

            if (newVal == null || newVal.trim().isEmpty()) {
                loadDemandes();
            } else {
                String searchTerm = newVal.toLowerCase().trim();
                ObservableList<Demande> filtered = FXCollections.observableArrayList();
                for (Demande d : demandesList) {
                    if (d.getTitre() != null && d.getTitre().toLowerCase().contains(searchTerm)) {
                        filtered.add(d);
                    }
                }
                demandesTable.setItems(filtered);
                demandesTable.refresh();
            }
        });
    }

    // ============ TABLE COLUMNS ============

    private void initializeTableColumns() {
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        categorieCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeDemande"));
        prioriteCol.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        // Priority colors
        prioriteCol.setCellFactory(column -> new TableCell<Demande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "HAUTE": setStyle("-fx-text-fill: red; -fx-font-weight: bold;"); break;
                        case "NORMALE": setStyle("-fx-text-fill: blue;"); break;
                        case "BASSE": setStyle("-fx-text-fill: green;"); break;
                        default: setStyle("");
                    }
                }
            }
        });

        // Status colors
        statusCol.setCellFactory(column -> new TableCell<Demande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Nouvelle": setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;"); break;
                        case "En cours": setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); break;
                        case "En attente": setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;"); break;
                        case "Résolue": setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); break;
                        case "Fermée": setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;"); break;
                        default: setStyle("");
                    }
                }
            }
        });

        // Actions column
        actionsCol.setCellFactory(column -> new TableCell<Demande, Void>() {
            private final Button modifierBtn = new Button("✏ Modifier");
            private final Button supprimerBtn = new Button("🗑 Supprimer");
            private final HBox container = new HBox(5, modifierBtn, supprimerBtn);

            {
                container.setAlignment(Pos.CENTER);
                modifierBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10; -fx-cursor: hand;");
                supprimerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10; -fx-cursor: hand;");

                modifierBtn.setOnAction(event -> {
                    Demande demande = getTableView().getItems().get(getIndex());
                    ouvrirModifier(demande);
                });

                supprimerBtn.setOnAction(event -> {
                    Demande demande = getTableView().getItems().get(getIndex());
                    supprimerDemande(demande);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    // ============ DETAILS PANEL ============

    private void showDetails(Demande demande) {
        placeholderBox.setVisible(false);
        placeholderBox.setManaged(false);
        detailsContent.setVisible(true);
        detailsContent.setManaged(true);

        detailTitreLabel.setText(demande.getTitre());
        detailCategorieLabel.setText(demande.getCategorie());
        detailTypeLabel.setText(demande.getTypeDemande());
        detailDescriptionLabel.setText(demande.getDescription());

        // Priorite with color
        detailPrioriteLabel.setText(demande.getPriorite());
        switch (demande.getPriorite()) {
            case "HAUTE": detailPrioriteLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 13;"); break;
            case "NORMALE": detailPrioriteLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold; -fx-font-size: 13;"); break;
            case "BASSE": detailPrioriteLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 13;"); break;
        }

        // Status with color
        detailStatusLabel.setText(demande.getStatus());
        switch (demande.getStatus()) {
            case "Nouvelle": detailStatusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-font-size: 13;"); break;
            case "En cours": detailStatusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-font-size: 13;"); break;
            case "En attente": detailStatusLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 13;"); break;
            case "Résolue": detailStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 13;"); break;
            case "Fermée": detailStatusLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold; -fx-font-size: 13;"); break;
        }

        if (demande.getDateCreation() != null) {
            detailDateLabel.setText(demande.getDateCreation().toString());
        }

        loadSpecificDetails(demande);
        loadHistorique(demande);
    }

    private void loadSpecificDetails(Demande demande) {
        detailSpecificContainer.getChildren().clear();
        try {
            DemandeDetails details = detailsCRUD.getByDemande(demande.getIdDemande());
            if (details != null && !details.getDetails().equals("{}")) {
                Map<String, String> parsed = formHelper.parseDetailsJson(details.getDetails());

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(5);

                int row = 0;
                for (Map.Entry<String, String> entry : parsed.entrySet()) {
                    Label keyLabel = new Label(entry.getKey() + ":");
                    keyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
                    keyLabel.setMinWidth(130);

                    Label valueLabel = new Label(entry.getValue());
                    valueLabel.setWrapText(true);

                    grid.add(keyLabel, 0, row);
                    grid.add(valueLabel, 1, row);
                    row++;
                }

                detailSpecificContainer.getChildren().add(grid);
            } else {
                Label noDetails = new Label("Aucun détail spécifique");
                noDetails.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                detailSpecificContainer.getChildren().add(noDetails);
            }
        } catch (SQLException e) {
            System.out.println("Error loading details: " + e.getMessage());
        }
    }

    private void loadHistorique(Demande demande) {
        historiqueContainer.getChildren().clear();
        try {
            List<HistoriqueDemande> historiques = historiqueCRUD.getByDemande(demande.getIdDemande());

            if (historiques.isEmpty()) {
                Label noHistory = new Label("Aucun historique disponible");
                noHistory.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                historiqueContainer.getChildren().add(noHistory);
                return;
            }

            for (HistoriqueDemande h : historiques) {
                VBox card = createHistoriqueCard(h);
                historiqueContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            System.out.println("Error loading historique: " + e.getMessage());
        }
    }

    private VBox createHistoriqueCard(HistoriqueDemande h) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-padding: 10;");

        HBox statusLine = new HBox(5);
        statusLine.setAlignment(Pos.CENTER_LEFT);

        Label ancienLabel = new Label(h.getAncienStatut());
        ancienLabel.setStyle(getStatusStyle(h.getAncienStatut()) +
                "-fx-padding: 3 8; -fx-background-radius: 10;");

        Label arrowLabel = new Label("  →  ");
        arrowLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label nouveauLabel = new Label(h.getNouveauStatut());
        nouveauLabel.setStyle(getStatusStyle(h.getNouveauStatut()) +
                "-fx-padding: 3 8; -fx-background-radius: 10;");

        statusLine.getChildren().addAll(ancienLabel, arrowLabel, nouveauLabel);

        HBox metaLine = new HBox(15);
        metaLine.setAlignment(Pos.CENTER_LEFT);

        Label acteurLabel = new Label("👤 " + h.getActeur());
        acteurLabel.setStyle("-fx-text-fill: #8e44ad; -fx-font-weight: bold; -fx-font-size: 11;");

        Label dateLabel = new Label("📅 " + h.getDateAction().toString());
        dateLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");

        metaLine.getChildren().addAll(acteurLabel, dateLabel);

        Label commentLabel = new Label("💬 " + h.getCommentaire());
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-text-fill: #555;");

        card.getChildren().addAll(statusLine, metaLine, commentLabel);
        return card;
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "Nouvelle": return "-fx-background-color: #d4edfc; -fx-text-fill: #2980b9; -fx-font-weight: bold;";
            case "En cours": return "-fx-background-color: #fdebd0; -fx-text-fill: #e67e22; -fx-font-weight: bold;";
            case "En attente": return "-fx-background-color: #fadbd8; -fx-text-fill: #c0392b; -fx-font-weight: bold;";
            case "Résolue": return "-fx-background-color: #d5f5e3; -fx-text-fill: #27ae60; -fx-font-weight: bold;";
            case "Fermée": return "-fx-background-color: #e5e8e8; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;";
            default: return "-fx-text-fill: black;";
        }
    }

    // ============ DATA LOADING ============

    public void loadDemandes() {
        try {
            demandesList.clear();
            demandesList.addAll(demandeCRUD.afficher());
            ObservableList<Demande> tableData = FXCollections.observableArrayList(demandesList);
            demandesTable.setItems(tableData);
            demandesTable.refresh();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
        }
    }

    // ============ NAVIGATION — Using NavigationHelper ============

    @FXML
    private void ouvrirAjouter() {
        try {
            FXMLLoader loader = NavigationHelper.loadView(demandesTable, "ajouter-demande.fxml");
            AjouterDemandeController controller = loader.getController();
            controller.setParentController(this);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ouvrirModifier(Demande demande) {
        try {
            FXMLLoader loader = NavigationHelper.loadView(demandesTable, "modifier-demande.fxml");
            ModifierDemandeController controller = loader.getController();
            controller.setParentController(this);
            controller.setDemande(demande);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirAvancer() {
        if (selectedDemande == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Sélectionnez une demande!");
            return;
        }

        if (selectedDemande.getStatus().equals("Fermée")) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Cette demande est déjà fermée!");
            return;
        }

        try {
            FXMLLoader loader = NavigationHelper.loadView(demandesTable, "avancer-demande.fxml");
            AvancerDemandeController controller = loader.getController();
            controller.setParentController(this);
            controller.setDemande(selectedDemande);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============ DELETE ============

    private void supprimerDemande(Demande demande) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la demande");
        confirm.setContentText("Supprimer \"" + demande.getTitre() + "\" ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                historiqueCRUD.supprimerByDemande(demande.getIdDemande());
                detailsCRUD.supprimerByDemande(demande.getIdDemande());
                demandeCRUD.supprimer(demande.getIdDemande());
                loadDemandes();

                placeholderBox.setVisible(true);
                placeholderBox.setManaged(true);
                detailsContent.setVisible(false);
                detailsContent.setManaged(false);
                selectedDemande = null;
                rechercheField.clear();

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande supprimée!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
            }
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