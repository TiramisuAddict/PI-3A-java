package controller.employers.RHetAdminE;

import controller.demandes.DemandeFormHelper;
import controller.demandes.NavigationHelper;
import entities.demande.Demande;
import entities.demande.DemandeDetails;
import entities.demande.HistoriqueDemande;
import service.demande.DemandeCRUD;
import service.demande.DemandeDetailsCRUD;
import service.demande.HistoriqueDemandeCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class DemandesController implements Initializable {

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

    private ObservableList<Demande> demandesList = FXCollections.observableArrayList();
    private DemandeCRUD demandeCRUD;
    private DemandeDetailsCRUD detailsCRUD;
    private HistoriqueDemandeCRUD historiqueCRUD;
    private DemandeFormHelper formHelper;
    private Demande selectedDemande;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        demandeCRUD = new DemandeCRUD();
        detailsCRUD = new DemandeDetailsCRUD();
        historiqueCRUD = new HistoriqueDemandeCRUD();
        formHelper = new DemandeFormHelper();

        initializeTableColumns();
        loadDemandes();

        demandesTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedDemande = newVal;
                        showDetails(newVal);
                    }
                });

        rechercheField.textProperty().addListener((obs, oldVal, newVal) -> {
            resetDetailsPanel();
            if (newVal == null || newVal.trim().isEmpty()) {
                loadDemandes();
            } else {
                String s = newVal.toLowerCase().trim();
                ObservableList<Demande> filtered = FXCollections.observableArrayList();
                for (Demande d : demandesList) {
                    if (d.getTitre() != null && d.getTitre().toLowerCase().contains(s)) {
                        filtered.add(d);
                    }
                }
                demandesTable.setItems(filtered);
                demandesTable.refresh();
            }
        });

        mainTabPane.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldTab, newTab) -> {
                    if (newTab != null && newTab.getText().contains("Statistiques")) {
                        loadStatistiques();
                    }
                });

        loadStatistiques();
    }

    private void resetDetailsPanel() {
        placeholderBox.setVisible(true);
        placeholderBox.setManaged(true);
        detailsContent.setVisible(false);
        detailsContent.setManaged(false);
        selectedDemande = null;
    }

    private void initializeTableColumns() {
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        categorieCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeDemande"));
        prioriteCol.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        prioriteCol.setCellFactory(col -> new TableCell<Demande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "HAUTE":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case "NORMALE":
                            setStyle("-fx-text-fill: blue;");
                            break;
                        case "BASSE":
                            setStyle("-fx-text-fill: green;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        statusCol.setCellFactory(col -> new TableCell<Demande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(getStatusStyle(item));
                }
            }
        });

        actionsCol.setCellFactory(col -> new TableCell<Demande, Void>() {
            private final Button modBtn = new Button("✏ Modifier");
            private final Button delBtn = new Button("🗑 Supprimer");
            private final HBox box = new HBox(5, modBtn, delBtn);

            {
                box.setAlignment(Pos.CENTER);
                modBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10; -fx-cursor: hand;");
                delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10; -fx-cursor: hand;");
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

    private String getStatusStyle(String s) {
        switch (s) {
            case "Nouvelle":
                return "-fx-text-fill: #3498db; -fx-font-weight: bold;";
            case "En cours":
                return "-fx-text-fill: #f39c12; -fx-font-weight: bold;";
            case "En attente":
                return "-fx-text-fill: #e67e22; -fx-font-weight: bold;";
            case "Résolue":
                return "-fx-text-fill: #27ae60; -fx-font-weight: bold;";
            case "Fermée":
                return "-fx-text-fill: #95a5a6; -fx-font-weight: bold;";
            default:
                return "";
        }
    }

    private String getStatusTagStyle(String s) {
        switch (s) {
            case "Nouvelle":
                return "-fx-background-color: #d4edfc; -fx-text-fill: #2980b9; -fx-font-weight: bold;";
            case "En cours":
                return "-fx-background-color: #fdebd0; -fx-text-fill: #e67e22; -fx-font-weight: bold;";
            case "En attente":
                return "-fx-background-color: #fadbd8; -fx-text-fill: #c0392b; -fx-font-weight: bold;";
            case "Résolue":
                return "-fx-background-color: #d5f5e3; -fx-text-fill: #27ae60; -fx-font-weight: bold;";
            case "Fermée":
                return "-fx-background-color: #e5e8e8; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;";
            default:
                return "-fx-text-fill: black;";
        }
    }

    private void showDetails(Demande d) {
        placeholderBox.setVisible(false);
        placeholderBox.setManaged(false);
        detailsContent.setVisible(true);
        detailsContent.setManaged(true);

        detailTitreLabel.setText(d.getTitre());
        detailCategorieLabel.setText(d.getCategorie());
        detailTypeLabel.setText(d.getTypeDemande());
        detailDescriptionLabel.setText(d.getDescription());
        detailPrioriteLabel.setText(d.getPriorite());

        switch (d.getPriorite()) {
            case "HAUTE":
                detailPrioriteLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 13;");
                break;
            case "NORMALE":
                detailPrioriteLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold; -fx-font-size: 13;");
                break;
            case "BASSE":
                detailPrioriteLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 13;");
                break;
        }

        detailStatusLabel.setText(d.getStatus());
        detailStatusLabel.setStyle(getStatusStyle(d.getStatus()) + " -fx-font-size: 13;");
        detailDateLabel.setText(d.getDateCreation() != null ? d.getDateCreation().toString() : "N/A");

        loadSpecificDetails(d);
        loadHistorique(d);
    }

    private void loadSpecificDetails(Demande d) {
        detailSpecificContainer.getChildren().clear();
        try {
            DemandeDetails det = detailsCRUD.getByDemande(d.getIdDemande());
            if (det != null && !det.getDetails().equals("{}")) {
                Map<String, String> parsed = formHelper.parseDetailsJson(det.getDetails());
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(5);
                int row = 0;
                for (Map.Entry<String, String> e : parsed.entrySet()) {
                    Label k = new Label(e.getKey() + ":");
                    k.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
                    k.setMinWidth(130);
                    Label v = new Label(e.getValue());
                    v.setWrapText(true);
                    grid.add(k, 0, row);
                    grid.add(v, 1, row);
                    row++;
                }
                detailSpecificContainer.getChildren().add(grid);
            } else {
                Label l = new Label("Aucun détail spécifique");
                l.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                detailSpecificContainer.getChildren().add(l);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void loadHistorique(Demande d) {
        historiqueContainer.getChildren().clear();
        try {
            List<HistoriqueDemande> list = historiqueCRUD.getByDemande(d.getIdDemande());
            if (list.isEmpty()) {
                Label l = new Label("Aucun historique");
                l.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                historiqueContainer.getChildren().add(l);
                return;
            }
            for (HistoriqueDemande h : list) {
                VBox card = new VBox(5);
                card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-padding: 10;");

                HBox sLine = new HBox(5);
                sLine.setAlignment(Pos.CENTER_LEFT);
                Label a = new Label(h.getAncienStatut());
                a.setStyle(getStatusTagStyle(h.getAncienStatut()) + " -fx-padding: 3 8; -fx-background-radius: 10;");
                Label ar = new Label("  →  ");
                ar.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                Label n = new Label(h.getNouveauStatut());
                n.setStyle(getStatusTagStyle(h.getNouveauStatut()) + " -fx-padding: 3 8; -fx-background-radius: 10;");
                sLine.getChildren().addAll(a, ar, n);

                HBox meta = new HBox(15);
                meta.setAlignment(Pos.CENTER_LEFT);
                Label act = new Label("👤 " + h.getActeur());
                act.setStyle("-fx-text-fill: #8e44ad; -fx-font-weight: bold; -fx-font-size: 11;");
                Label dt = new Label("📅 " + h.getDateAction().toString());
                dt.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");
                meta.getChildren().addAll(act, dt);

                Label cm = new Label("💬 " + h.getCommentaire());
                cm.setWrapText(true);
                cm.setStyle("-fx-text-fill: #555;");

                card.getChildren().addAll(sLine, meta, cm);
                historiqueContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ============ STATISTICS ============

    @FXML
    private void refreshStatistiques() {
        loadStatistiques();
    }

    private void loadStatistiques() {
        try {
            lblTotalDemandes.setText(String.valueOf(demandeCRUD.countAll()));
            lblTotalHistorique.setText(String.valueOf(historiqueCRUD.countAll()));
            lblDemandesResolues.setText(String.valueOf(demandeCRUD.countByStatus("Résolue")));

            buildStatCards(statutStatsContainer,
                    demandeCRUD.countGroupByStatus(),
                    new String[]{"#3498db", "#f39c12", "#e67e22", "#27ae60", "#95a5a6"});
            buildStatCards(prioriteStatsContainer,
                    demandeCRUD.countGroupByPriorite(),
                    new String[]{"#27ae60", "#3498db", "#e74c3c"});
            buildStatCards(typeStatsContainer,
                    demandeCRUD.countGroupByType(),
                    new String[]{"#9b59b6", "#1abc9c", "#e67e22", "#3498db", "#e74c3c"});
            buildStatCards(categorieStatsContainer,
                    demandeCRUD.countGroupByCategorie(),
                    new String[]{"#2ecc71", "#3498db", "#e74c3c", "#f39c12", "#9b59b6"});
            buildStatCards(acteurStatsContainer,
                    historiqueCRUD.countGroupByActeur(),
                    new String[]{"#8e44ad", "#c0392b", "#2980b9"});
        } catch (SQLException e) {
            System.out.println("Stats error: " + e.getMessage());
        }
    }

    private void buildStatCards(HBox container, Map<String, Integer> data, String[] colors) {
        container.getChildren().clear();
        if (data.isEmpty()) {
            Label l = new Label("Aucune donnée");
            l.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            container.getChildren().add(l);
            return;
        }
        int ci = 0;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            String c = colors[ci % colors.length];
            VBox card = new VBox(8);
            card.setAlignment(Pos.CENTER);
            card.setPrefWidth(150);
            card.setMinWidth(120);
            card.setStyle("-fx-background-color: white; -fx-padding: 20; " +
                    "-fx-background-radius: 10; -fx-effect: dropshadow(gaussian, " +
                    "rgba(0,0,0,0.08), 8, 0, 0, 2);");
            Label v = new Label(String.valueOf(e.getValue()));
            v.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: " + c + ";");
            Label nm = new Label(e.getKey());
            nm.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
            nm.setWrapText(true);
            nm.setAlignment(Pos.CENTER);
            Region bar = new Region();
            bar.setPrefHeight(4);
            bar.setMaxWidth(80);
            bar.setStyle("-fx-background-color: " + c + "; -fx-background-radius: 2;");
            card.getChildren().addAll(v, bar, nm);
            container.getChildren().add(card);
            ci++;
        }
    }

    // ============ DATA ============

    public void loadDemandes() {
        try {
            demandesList.clear();
            demandesList.addAll(demandeCRUD.afficher());
            demandesTable.setItems(FXCollections.observableArrayList(demandesList));
            demandesTable.refresh();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ============ NAVIGATION ============

    private void ouvrirModifier(Demande demande) {
        try {
            // FIXED: Using single parameter with leading slash
            FXMLLoader loader = NavigationHelper.loadView("/emp/RHetAdminE/modifier-demande.fxml");
            ModifierDemandeController ctrl = loader.getController();
            ctrl.setParentController(this);
            ctrl.setDemande(demande);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void ouvrirAvancer() {
        if (selectedDemande == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Sélectionnez une demande!");
            return;
        }
        if ("Fermée".equals(selectedDemande.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Cette demande est déjà fermée!");
            return;
        }
        try {
            // FIXED: Using single parameter with leading slash
            FXMLLoader loader = NavigationHelper.loadView("/emp/RHetAdminE/avancer-demande.fxml");
            AvancerDemandeController ctrl = loader.getController();
            ctrl.setParentController(this);
            ctrl.setDemande(selectedDemande);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

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
                resetDetailsPanel();
                rechercheField.clear();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande supprimée!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}