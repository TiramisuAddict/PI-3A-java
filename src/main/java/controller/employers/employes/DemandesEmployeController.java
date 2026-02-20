package controller.employers.employes;

import controller.demandes.DemandeFormHelper;
import controller.demandes.NavigationHelper;
import entities.demande.Demande;
import entities.demande.DemandeDetails;
import entities.demande.HistoriqueDemande;
import entities.employe.session;
import service.demande.DemandeCRUD;
import service.demande.DemandeDetailsCRUD;
import service.demande.HistoriqueDemandeCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class DemandesEmployeController implements Initializable {

    @FXML private TabPane mainTabPane;
    @FXML private TextField rechercheField;
    @FXML private ComboBox<String> filterStatutCombo;
    @FXML private ComboBox<String> filterPrioriteCombo;
    @FXML private VBox cardsContainer;

    @FXML private Label lblSummaryTotal, lblSummaryNouvelle,
            lblSummaryEnCours, lblSummaryEnAttente,
            lblSummaryResolue, lblSummaryFermee;

    @FXML private VBox placeholderBox, detailsContent;
    @FXML private Label detailTitreLabel, detailStatusBadge,
            detailCategorieLabel, detailTypeLabel,
            detailPrioriteLabel, detailStatusLabel,
            detailDateLabel, detailDescriptionLabel;
    @FXML private VBox detailSpecificContainer;
    @FXML private HBox statusProgressBar;
    @FXML private VBox historiqueContainer;

    private ObservableList<Demande> allDemandes = FXCollections.observableArrayList();
    private ObservableList<Demande> filteredDemandes = FXCollections.observableArrayList();
    private DemandeCRUD demandeCRUD;
    private DemandeDetailsCRUD detailsCRUD;
    private HistoriqueDemandeCRUD historiqueCRUD;
    private DemandeFormHelper formHelper;
    private Demande selectedDemande;
    private VBox selectedCard;

    private static final String[] STATUS_ORDER = {
            "Nouvelle", "En cours", "En attente", "Résolue", "Fermée"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        demandeCRUD = new DemandeCRUD();
        detailsCRUD = new DemandeDetailsCRUD();
        historiqueCRUD = new HistoriqueDemandeCRUD();
        formHelper = new DemandeFormHelper();

        filterStatutCombo.setItems(FXCollections.observableArrayList(
                "Tous", "Nouvelle", "En cours", "En attente",
                "Résolue", "Fermée"));
        filterStatutCombo.setValue("Tous");

        filterPrioriteCombo.setItems(FXCollections.observableArrayList(
                "Toutes", "HAUTE", "NORMALE", "BASSE"));
        filterPrioriteCombo.setValue("Toutes");

        loadDemandes();

        rechercheField.textProperty().addListener((o, ov, nv) -> applyFilters());
        filterStatutCombo.valueProperty().addListener((o, ov, nv) -> applyFilters());
        filterPrioriteCombo.valueProperty().addListener((o, ov, nv) -> applyFilters());
    }

    public void loadDemandes() {
        try {
            allDemandes.clear();
            int empId = session.getEmploye() != null
                    ? session.getEmploye().getId_employé() : -1;
            if (empId > 0) {
                allDemandes.addAll(demandeCRUD.getByEmploye(empId));
            }
            applyFilters();
            updateSummary();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void applyFilters() {
        filteredDemandes.clear();
        String search = rechercheField.getText() != null
                ? rechercheField.getText().toLowerCase().trim() : "";
        String sf = filterStatutCombo.getValue();
        String pf = filterPrioriteCombo.getValue();

        for (Demande d : allDemandes) {
            boolean ms = search.isEmpty()
                    || (d.getTitre() != null && d.getTitre().toLowerCase().contains(search));
            boolean mst = sf == null || sf.equals("Tous") || d.getStatus().equals(sf);
            boolean mp = pf == null || pf.equals("Toutes") || d.getPriorite().equals(pf);
            if (ms && mst && mp) filteredDemandes.add(d);
        }
        buildCards();
        resetDetails();
    }

    private void resetDetails() {
        placeholderBox.setVisible(true);
        placeholderBox.setManaged(true);
        detailsContent.setVisible(false);
        detailsContent.setManaged(false);
        selectedDemande = null;
        selectedCard = null;
    }

    private void updateSummary() {
        int t = allDemandes.size();
        int n = 0, ec = 0, ea = 0, r = 0, f = 0;
        for (Demande d : allDemandes) {
            switch (d.getStatus()) {
                case "Nouvelle": n++; break;
                case "En cours": ec++; break;
                case "En attente": ea++; break;
                case "Résolue": r++; break;
                case "Fermée": f++; break;
            }
        }
        lblSummaryTotal.setText("Total: " + t);
        lblSummaryNouvelle.setText("🔵 Nouvelle: " + n);
        lblSummaryEnCours.setText("🟡 En cours: " + ec);
        lblSummaryEnAttente.setText("🟠 En attente: " + ea);
        lblSummaryResolue.setText("🟢 Résolue: " + r);
        lblSummaryFermee.setText("⚫ Fermée: " + f);
    }

    private void buildCards() {
        cardsContainer.getChildren().clear();
        if (filteredDemandes.isEmpty()) {
            VBox empty = new VBox(10);
            empty.setAlignment(Pos.CENTER);
            empty.setStyle("-fx-padding: 60;");

            Label icon = new Label("📭");
            icon.setStyle("-fx-font-size: 48;");

            Label title = new Label("Aucune demande");
            title.setStyle("-fx-font-size: 18; -fx-text-fill: #999; -fx-font-weight: bold;");

            Label subtitle = new Label("Cliquez sur '+ Nouvelle Demande' pour en créer une");
            subtitle.setStyle("-fx-font-size: 12; -fx-text-fill: #bbb;");
            subtitle.setWrapText(true);
            subtitle.setAlignment(Pos.CENTER);

            empty.getChildren().addAll(icon, title, subtitle);
            cardsContainer.getChildren().add(empty);
            return;
        }

        for (Demande d : filteredDemandes) {
            cardsContainer.getChildren().add(createCard(d));
        }
    }

    private VBox createCard(Demande d) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.setStyle(cardStyle(false));
        card.setCursor(Cursor.HAND);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(d.getTitre());
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);

        Label badge = new Label(d.getStatus());
        badge.setStyle(badgeStyle(d.getStatus()));
        badge.setMinWidth(Region.USE_PREF_SIZE);

        top.getChildren().addAll(title, badge);

        HBox mid = new HBox(15);
        mid.setAlignment(Pos.CENTER_LEFT);

        Label cat = new Label("🏷 " + d.getCategorie());
        cat.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");

        Label type = new Label("📂 " + d.getTypeDemande());
        type.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");

        Label prio = new Label("⚡ " + d.getPriorite());
        prio.setStyle(prioStyle(d.getPriorite()));

        mid.getChildren().addAll(cat, type, prio);

        HBox bot = new HBox(10);
        bot.setAlignment(Pos.CENTER_LEFT);

        Label dt = new Label("📅 " + (d.getDateCreation() != null
                ? d.getDateCreation().toString() : "N/A"));
        dt.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String descText = d.getDescription() != null
                ? (d.getDescription().length() > 50
                ? d.getDescription().substring(0, 50) + "..." : d.getDescription()) : "";
        Label desc = new Label(descText);
        desc.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10; -fx-font-style: italic;");

        bot.getChildren().addAll(dt, spacer, desc);

        card.getChildren().addAll(top, mid, bot);

        card.setOnMouseClicked(e -> {
            if (selectedCard != null) selectedCard.setStyle(cardStyle(false));
            selectedCard = card;
            card.setStyle(cardStyle(true));
            selectedDemande = d;
            showDetails(d);
        });

        // Hover effects
        card.setOnMouseEntered(e -> {
            if (card != selectedCard) card.setStyle(hoverStyle());
        });
        card.setOnMouseExited(e -> {
            if (card != selectedCard) card.setStyle(cardStyle(false));
        });

        return card;
    }

    private String cardStyle(boolean selected) {
        return selected
                ? "-fx-background-color: #ebf5fb; -fx-background-radius: 10; -fx-border-color: #3498db; -fx-border-radius: 10; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.3), 10, 0, 0, 2);"
                : "-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);";
    }

    private String hoverStyle() {
        return "-fx-background-color: #fafafa; -fx-background-radius: 10; -fx-border-color: #bdc3c7; -fx-border-radius: 10; -fx-border-width: 1;";
    }

    private String badgeStyle(String status) {
        String base = "-fx-padding: 3 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 10;";
        switch (status) {
            case "Nouvelle": return base + " -fx-background-color: #d4edfc; -fx-text-fill: #2980b9;";
            case "En cours": return base + " -fx-background-color: #fdebd0; -fx-text-fill: #e67e22;";
            case "En attente": return base + " -fx-background-color: #fadbd8; -fx-text-fill: #c0392b;";
            case "Résolue": return base + " -fx-background-color: #d5f5e3; -fx-text-fill: #27ae60;";
            case "Fermée": return base + " -fx-background-color: #e5e8e8; -fx-text-fill: #7f8c8d;";
            default: return base;
        }
    }

    private String prioStyle(String priority) {
        switch (priority) {
            case "HAUTE": return "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 11;";
            case "NORMALE": return "-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-font-size: 11;";
            case "BASSE": return "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 11;";
            default: return "-fx-font-size: 11;";
        }
    }

    private String statusTagStyle(String status) {
        switch (status) {
            case "Nouvelle": return "-fx-background-color: #d4edfc; -fx-text-fill: #2980b9; -fx-font-weight: bold;";
            case "En cours": return "-fx-background-color: #fdebd0; -fx-text-fill: #e67e22; -fx-font-weight: bold;";
            case "En attente": return "-fx-background-color: #fadbd8; -fx-text-fill: #c0392b; -fx-font-weight: bold;";
            case "Résolue": return "-fx-background-color: #d5f5e3; -fx-text-fill: #27ae60; -fx-font-weight: bold;";
            case "Fermée": return "-fx-background-color: #e5e8e8; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;";
            default: return "";
        }
    }

    private void showDetails(Demande d) {
        placeholderBox.setVisible(false);
        placeholderBox.setManaged(false);
        detailsContent.setVisible(true);
        detailsContent.setManaged(true);

        detailTitreLabel.setText(d.getTitre());
        detailStatusBadge.setText(d.getStatus());
        detailStatusBadge.setStyle(badgeStyle(d.getStatus()) + " -fx-font-size: 12;");
        detailCategorieLabel.setText(d.getCategorie());
        detailTypeLabel.setText(d.getTypeDemande());
        detailDescriptionLabel.setText(d.getDescription());
        detailPrioriteLabel.setText(d.getPriorite());
        detailPrioriteLabel.setStyle(prioStyle(d.getPriorite()) + " -fx-font-size: 13;");
        detailStatusLabel.setText(d.getStatus());
        detailDateLabel.setText(d.getDateCreation() != null ? d.getDateCreation().toString() : "N/A");

        loadSpecificDetails(d);
        buildProgress(d);
        loadHistorique(d);
    }

    private void loadSpecificDetails(Demande d) {
        detailSpecificContainer.getChildren().clear();
        try {
            DemandeDetails det = detailsCRUD.getByDemande(d.getIdDemande());
            if (det != null && !det.getDetails().equals("{}")) {
                Map<String, String> p = formHelper.parseDetailsJson(det.getDetails());
                GridPane g = new GridPane();
                g.setHgap(10);
                g.setVgap(5);
                int row = 0;
                for (Map.Entry<String, String> e : p.entrySet()) {
                    Label k = new Label(e.getKey() + ":");
                    k.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
                    k.setMinWidth(130);
                    Label v = new Label(e.getValue());
                    v.setWrapText(true);
                    g.add(k, 0, row);
                    g.add(v, 1, row);
                    row++;
                }
                detailSpecificContainer.getChildren().add(g);
            } else {
                Label l = new Label("Aucun détail spécifique");
                l.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                detailSpecificContainer.getChildren().add(l);
            }
        } catch (SQLException e) {
            System.out.println("Error loading specific details: " + e.getMessage());
        }
    }

    private void buildProgress(Demande d) {
        statusProgressBar.getChildren().clear();
        int currentIndex = -1;
        for (int i = 0; i < STATUS_ORDER.length; i++) {
            if (STATUS_ORDER[i].equals(d.getStatus())) {
                currentIndex = i;
                break;
            }
        }

        for (int i = 0; i < STATUS_ORDER.length; i++) {
            VBox step = new VBox(4);
            step.setAlignment(Pos.CENTER);
            step.setPrefWidth(80);

            Label circle = new Label();
            circle.setPrefSize(28, 28);
            circle.setMinSize(28, 28);
            circle.setMaxSize(28, 28);
            circle.setAlignment(Pos.CENTER);

            if (i < currentIndex) {
                circle.setText("✓");
                circle.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 14; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12; -fx-alignment: center;");
            } else if (i == currentIndex) {
                circle.setText("●");
                circle.setStyle("-fx-background-color: #3498db; -fx-background-radius: 14; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-alignment: center;");
            } else {
                circle.setText(String.valueOf(i + 1));
                circle.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 14; -fx-text-fill: #bdc3c7; -fx-font-weight: bold; -fx-font-size: 11; -fx-alignment: center;");
            }

            Label name = new Label(STATUS_ORDER[i]);
            name.setStyle("-fx-font-size: 9; -fx-text-fill: " + (i <= currentIndex ? "#2c3e50" : "#bdc3c7") + "; -fx-font-weight: " + (i == currentIndex ? "bold" : "normal") + ";");
            name.setAlignment(Pos.CENTER);

            step.getChildren().addAll(circle, name);
            statusProgressBar.getChildren().add(step);

            if (i < STATUS_ORDER.length - 1) {
                Region line = new Region();
                line.setPrefHeight(2);
                line.setMinHeight(2);
                line.setMaxHeight(2);
                line.setPrefWidth(30);
                line.setMinWidth(20);
                HBox.setHgrow(line, Priority.ALWAYS);
                line.setStyle("-fx-background-color: " + (i < currentIndex ? "#27ae60" : "#ecf0f1") + ";");

                HBox lineBox = new HBox(line);
                lineBox.setAlignment(Pos.CENTER);
                lineBox.setPadding(new Insets(0, 0, 15, 0));
                statusProgressBar.getChildren().add(lineBox);
            }
        }
    }

    private void loadHistorique(Demande d) {
        historiqueContainer.getChildren().clear();
        try {
            List<HistoriqueDemande> list = historiqueCRUD.getByDemande(d.getIdDemande());
            if (list.isEmpty()) {
                VBox noHistory = new VBox(8);
                noHistory.setAlignment(Pos.CENTER);
                noHistory.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

                Label icon = new Label("⏳");
                icon.setStyle("-fx-font-size: 24;");

                Label msg1 = new Label("Votre demande est en attente de traitement");
                msg1.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-font-size: 12;");
                msg1.setWrapText(true);
                msg1.setAlignment(Pos.CENTER);

                Label msg2 = new Label("Le service RH vous répondra prochainement");
                msg2.setStyle("-fx-text-fill: #bbb; -fx-font-size: 11;");

                noHistory.getChildren().addAll(icon, msg1, msg2);
                historiqueContainer.getChildren().add(noHistory);
                return;
            }

            for (HistoriqueDemande h : list) {
                VBox card = new VBox(6);
                card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-padding: 12;");

                // Status transition
                HBox statusLine = new HBox(5);
                statusLine.setAlignment(Pos.CENTER_LEFT);

                Label ancien = new Label(h.getAncienStatut());
                ancien.setStyle(statusTagStyle(h.getAncienStatut()) + " -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 11;");

                Label arrow = new Label("  →  ");
                arrow.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

                Label nouveau = new Label(h.getNouveauStatut());
                nouveau.setStyle(statusTagStyle(h.getNouveauStatut()) + " -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 11;");

                statusLine.getChildren().addAll(ancien, arrow, nouveau);

                HBox metaLine = new HBox(15);
                metaLine.setAlignment(Pos.CENTER_LEFT);

                Label acteur = new Label("👤 " + h.getActeur());
                acteur.setStyle("-fx-text-fill: #8e44ad; -fx-font-weight: bold; -fx-font-size: 11;");

                Label date = new Label("📅 " + h.getDateAction().toString());
                date.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");

                metaLine.getChildren().addAll(acteur, date);

                VBox commentBox = new VBox(3);
                commentBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6; -fx-padding: 8;");

                Label commentLabel = new Label("💬 Réponse :");
                commentLabel.setStyle("-fx-text-fill: #666; -fx-font-weight: bold; -fx-font-size: 11;");

                Label commentText = new Label(h.getCommentaire());
                commentText.setWrapText(true);
                commentText.setStyle("-fx-text-fill: #333; -fx-font-size: 12;");

                commentBox.getChildren().addAll(commentLabel, commentText);

                card.getChildren().addAll(statusLine, metaLine, commentBox);
                historiqueContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            System.out.println("Error loading historique: " + e.getMessage());
        }
    }

    @FXML
    private void ouvrirAjouter() {
        try {
            System.out.println("=== ouvrirAjouter called ===");

            FXMLLoader loader = NavigationHelper.loadView("/emp/employes/ajouter-demande-employe.fxml");
            AjouterDemandeEmployeController ctrl = loader.getController();
            ctrl.setParentController(this);

            System.out.println("=== Navigation successful ===");

        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger le formulaire: " + e.getMessage());
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