package controller.employers.admin_sys;

import models.employe.entreprise;
import models.employe.statut;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import service.employe.entrepriseCRUD;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class demandes_entreprise implements Initializable {

    @FXML private VBox demandesContainer;
    @FXML private Label lblTotal;
    @FXML private Label lblEnAttente;
    @FXML private Label lblAcceptees;
    @FXML private Label lblRefusees;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatut;

    private entrepriseCRUD entrepriseCRUD;
    private List<entreprise> entreprises;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            entrepriseCRUD = new entrepriseCRUD();
            setupFilters();
            chargerDemandes();
        } catch (SQLException e) {
            afficherErreur("Erreur de connexion", e.getMessage());
        }
    }

    private void setupFilters() {
        filterStatut.setItems(FXCollections.observableArrayList("Toutes", "En attente", "Acceptées", "Refusées"));
        filterStatut.setValue("Toutes");
        filterStatut.setOnAction(e -> appliquerFiltres());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
    }

    private void chargerDemandes() {
        try {
            entreprises = entrepriseCRUD.afficher();
            mettreAJourStats(entreprises);
            appliquerFiltres();
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement", e.getMessage());
        }
    }

    private void appliquerFiltres() {
        if (entreprises == null) return;
        String recherche = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String filtreStatut = filterStatut.getValue();
        List<entreprise> filterTree = entreprises.stream().filter(e -> filtrerParStatut(e, filtreStatut)).filter(e -> filtrerParRecherche(e, recherche)).sorted((e1, e2) -> {
                    if (e1.getStatut() == statut.enattende && e2.getStatut() != statut.enattende) return -1;
                    if (e1.getStatut() != statut.enattende && e2.getStatut() == statut.enattende) return 1;
                    return 0;
                }).collect(Collectors.toList());

        afficherEntreprises(filterTree);
    }

    private boolean filtrerParStatut(entreprise e, String filtre) {
        if (filtre == null || "Toutes".equals(filtre)) return true;
        return switch (filtre) {
            case "En attente" -> e.getStatut() == statut.enattende;
            case "Acceptées" -> e.getStatut() == statut.acceptee;
            case "Refusées" -> e.getStatut() == statut.refusee;
            default -> true;
        };
    }

    private boolean filtrerParRecherche(entreprise e, String recherche) {
        if (recherche.isEmpty()) return true;
        return e.getNom_entreprise().toLowerCase().contains(recherche) || e.getE_mail().toLowerCase().contains(recherche) || (e.getPrenom() + " " + e.getNom()).toLowerCase().contains(recherche);
    }

    private void afficherEntreprises(List<entreprise> entreprises) {
        demandesContainer.getChildren().clear();
        if (entreprises.isEmpty()) {
            demandesContainer.getChildren().add(creerMessageVide());
            return;
        }

        for (entreprise ent : entreprises) {
            demandesContainer.getChildren().add(creerCarte(ent));
        }
    }
    private VBox creerCarte(entreprise ent) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        card.setStyle(" -fx-background-color: -color-bg-subtle; -fx-background-radius: 5; -fx-border-color: -color-border-muted; -fx-border-radius: 5;");
        HBox row = creerLigneEntreprise(ent, card);
        card.getChildren().add(row);
        return card;
    }

    private HBox creerLigneEntreprise(entreprise entreprise, VBox parentCard) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 25, 15, 25));
        row.setStyle("-fx-cursor: hand;");
        row.setOnMouseEntered(e -> parentCard.setStyle(" -fx-background-color: -color-bg-default;-fx-background-radius: 5; -fx-border-color: -color-accent-muted; -fx-border-radius: 5;"));
        row.setOnMouseExited(e -> parentCard.setStyle("-fx-background-color: -color-bg-subtle;-fx-background-radius: 5;-fx-border-color: -color-border-muted;-fx-border-radius: 5;"));
        row.setOnMouseClicked(e -> toggleDetails(parentCard, entreprise));
        Label nomLabel = creerLabel(entreprise.getNom_entreprise(), 220, true);
        Label dateLabel = creerLabel(formatDate(entreprise), 130, false);
        Label emailLabel = creerLabel(entreprise.getE_mail(), 200, false);
        HBox statutBox = new HBox(creerBadgeStatut(entreprise.getStatut()));
        statutBox.setPrefWidth(130);
        statutBox.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttonsBox = creerBoutonsAction(entreprise);
        row.getChildren().addAll(nomLabel, dateLabel, emailLabel, statutBox, spacer, buttonsBox);
        return row;
    }

    private Label creerLabel(String text, double prefWidth, boolean bold) {
        Label label = new Label(text);
        label.setPrefWidth(prefWidth);
        label.setStyle(bold ? "-fx-font-size: 14px; -fx-font-weight: bold;" : "-fx-font-size: 13px;");
        return label;
    }

    private String formatDate(entreprise ent) {
        return ent.getDate_demande() != null ? ent.getDate_demande().format(DATE_FORMAT) : "N/A";
    }

    private HBox creerBoutonsAction(entreprise entreprise) {
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.setPrefWidth(250);

        Button btnAccepter = creerBouton("Accepter", "success");
        Button btnRefuser = creerBouton("Refuser", "danger");

        btnAccepter.setOnMouseClicked(e -> e.consume());
        btnRefuser.setOnMouseClicked(e -> e.consume());

        btnAccepter.setOnAction(e -> confirmerAcceptation(entreprise));
        btnRefuser.setOnAction(e -> confirmerRefus(entreprise));

        if (entreprise.getStatut() != statut.enattende) {
            desactiverBoutons(btnAccepter, btnRefuser);
        }

        buttonsBox.getChildren().addAll(btnAccepter, btnRefuser);
        return buttonsBox;
    }

    private Button creerBouton(String text, String type) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("button", type, "outline");
        btn.setPrefHeight(32);
        btn.setStyle("-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12px;");
        return btn;
    }

    private void desactiverBoutons(Button... buttons) {
        for (Button btn : buttons) {
            btn.setDisable(true);
            btn.getStyleClass().removeAll("success", "danger", "accent");
            btn.setOpacity(0.5);
        }
    }

    private Label creerBadgeStatut(statut s) {
        Label badge = new Label();
        String baseStyle = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12;-fx-background-radius: 15; ";
        switch (s) {
            case enattende -> {
                badge.setText("En attente");
                badge.setStyle(baseStyle + "-fx-background-color: -color-warning-muted;-fx-text-fill: -color-warning-fg;");
            }
            case acceptee -> {
                badge.setText("Acceptée");
                badge.setStyle(baseStyle + "-fx-background-color: -color-success-muted;-fx-text-fill: -color-success-fg;");
            }
            case refusee -> {
                badge.setText("Refusée");
                badge.setStyle(baseStyle + "-fx-background-color: -color-danger-muted;-fx-text-fill: -color-danger-fg;");
            }
        }
        return badge;
    }

    private void toggleDetails(VBox parentCard, entreprise ent) {
        if (parentCard.getChildren().size() > 1) {
            VBox details = (VBox) parentCard.getChildren().get(1);
            ScaleTransition st = new ScaleTransition(Duration.millis(150), details);
            st.setFromY(1);
            st.setToY(0);
            st.setOnFinished(e -> parentCard.getChildren().remove(1));
            st.play();
        } else {
            VBox detailsBox = creerPanneauDetails(ent);
            parentCard.getChildren().add(detailsBox);
        }
    }

    private VBox creerPanneauDetails(entreprise ent) {
        VBox box = new VBox(20);
        box.setPadding(new Insets(25, 35, 25, 35));
        box.setStyle("-fx-background-color: -color-bg-default;-fx-border-color: -color-border-muted;-fx-border-width: 1 0 0 0;");
        HBox header = creerDetailsHeader(ent);
        HBox infoGrid = creerInfoGrid(ent);
        HBox footer = creerDetailsFooter(ent);

        box.getChildren().addAll(header, new Separator(), infoGrid, footer);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), box);
        st.setFromY(0);
        st.setToY(1);
        st.play();
        return box;
    }

    private HBox creerDetailsHeader(entreprise ent) {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = creerSigle(ent.getNom_entreprise());

        VBox titleBlock = new VBox(3);
        titleBlock.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBlock, Priority.ALWAYS);

        Label nomLabel = new Label(ent.getNom_entreprise());
        nomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label locationLabel = creerIconLabel(ent.getVille() + ", " + ent.getPays());
        Label emailLabel = creerIconLabel(ent.getE_mail());

        metaRow.getChildren().addAll(locationLabel, creerSeparateurVertical(), emailLabel);
        titleBlock.getChildren().addAll(nomLabel, metaRow);

        VBox statusBlock = new VBox(5);
        statusBlock.setAlignment(Pos.TOP_RIGHT);
        statusBlock.getChildren().add(creerBadgeStatut(ent.getStatut()));

        header.getChildren().addAll(avatar, titleBlock, statusBlock);
        return header;
    }

    private StackPane creerSigle(String nom) {
        StackPane sigle = new StackPane();
        sigle.setPrefSize(50, 50);
        sigle.setMinSize(50, 50);
        sigle.setMaxSize(50, 50);

        String[] colors = {"#4A5DEF", "#16A34A", "#F59E0B", "#DC2626","#8B5CF6", "#06B6D4", "#EC4899", "#F97316"};
        int colorIndex = Math.abs(nom.hashCode()) % colors.length;
        sigle.setStyle(String.format("-fx-background-color: %s;-fx-background-radius: 12;", colors[colorIndex]));

        String initials = nom.length() >= 2 ? nom.substring(0, 2).toUpperCase() : nom.substring(0, 1).toUpperCase();

        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;-fx-font-size: 18px;");
        sigle.getChildren().add(initialsLabel);
        return sigle;
    }

    private Label creerIconLabel( String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        return label;
    }

    private Region creerSeparateurVertical() {
        Region sep = new Region();
        sep.setPrefWidth(1);
        sep.setPrefHeight(14);
        sep.setStyle("-fx-background-color: -color-border-muted;");
        return sep;
    }
    private HBox creerInfoGrid(entreprise ent) {
        HBox grid = new HBox(30);
        grid.setPadding(new Insets(2, 0, 2, 0));

        VBox col1 = creerInfoSection("Informations entreprise", creerInfoItem("Nom", ent.getNom_entreprise()), creerInfoItem("Ville", ent.getVille()), creerInfoItem("Pays", ent.getPays()), creerInfoItem("Matricule fiscale", ent.getMatricule_fiscale())
        );
        Region divider = new Region();
        divider.setPrefWidth(1);
        divider.setStyle("-fx-background-color: -color-border-muted;");
        VBox col2 = creerInfoSection("Responsable", creerInfoItem("Nom complet", ent.getPrenom() + " " + ent.getNom()), creerInfoItem("Email", ent.getE_mail()), creerInfoItem("Téléphone", formatTelephone(ent.getTelephone())), creerInfoItem("Date de demande", formatDate(ent))
        );
        HBox.setHgrow(col1, Priority.ALWAYS);
        HBox.setHgrow(col2, Priority.ALWAYS);

        grid.getChildren().addAll(col1, divider, col2);
        return grid;
    }

    private VBox creerInfoSection(String titre, HBox... items) {
        VBox section = new VBox(12);

        Label header = new Label(titre);
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 0 0 5 0;");
        VBox itemsBox = new VBox(10);
        itemsBox.getChildren().addAll(items);

        section.getChildren().addAll(header, itemsBox);
        return section;
    }

    private HBox creerInfoItem(String label, String value) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);

        Label keyLabel = new Label(label);
        keyLabel.setMinWidth(120);
        keyLabel.setPrefWidth(120);
        keyLabel.setStyle(" -fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");

        Label valueLabel = new Label(value != null && !value.isBlank() ? value : "—");
        valueLabel.setStyle(" -fx-font-weight: bold; -fx-font-size: 12px;");
        valueLabel.setWrapText(true);
        HBox.setHgrow(valueLabel, Priority.ALWAYS);

        item.getChildren().addAll(keyLabel, valueLabel);
        return item;
    }

    private String formatTelephone(int telephone) {
        String tel = String.valueOf(telephone);
        if (tel.length() == 8) {
            return tel.substring(0, 2) + " " + tel.substring(2, 5) + " " + tel.substring(5);
        }
        return tel;
    }
    private HBox creerDetailsFooter(entreprise ent) {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 0, 0, 0));
        footer.setStyle(" -fx-border-color: -color-border-muted;-fx-border-width: 1 0 0 0;-fx-padding: 15 0 0 0;");

        Label timestamp = new Label("Demande soumise le " + formatDate(ent));
        timestamp.setStyle("-fx-text-fill: -color-fg-subtle; -fx-font-size: 11px;");
        HBox.setHgrow(timestamp, Priority.ALWAYS);

        footer.getChildren().add(timestamp);

        switch (ent.getStatut()) {
            case enattende -> {
                Button btnAccepter = creerBoutonFooter("Accepter", "success");
                Button btnRefuser = creerBoutonFooter("Refuser", "danger");

                btnAccepter.setOnAction(e -> confirmerAcceptation(ent));
                btnRefuser.setOnAction(e -> confirmerRefus(ent));

                footer.getChildren().addAll(btnRefuser, btnAccepter);
            }

            case refusee -> {
                Button btnDelete = creerBoutonFooter("Supprimer", "danger");
                btnDelete.setOnAction(e -> confirmerSuppression(ent));

                Button btnReconsider = creerBoutonFooter("Reconsidérer", "accent");
                btnReconsider.setOnAction(e -> {
                    if (confirmer("Reconsidération", "Remettre en attente " + ent.getNom_entreprise() + " ?")) {
                        try {
                            entrepriseCRUD.changerStatut(ent.getId(), statut.enattende);
                            chargerDemandes();
                        } catch (SQLException ex) {
                            afficherErreur("Erreur", ex.getMessage());
                        }
                    }
                });

                footer.getChildren().addAll(btnDelete, btnReconsider);
            }

            case acceptee -> {
                Label acceptedInfo = new Label("Entreprise approuvée");
                acceptedInfo.setStyle(" -fx-text-fill: -color-success-fg; -fx-font-weight: bold;-fx-font-size: 12px;");
                footer.getChildren().add(acceptedInfo);
            }
        }
        return footer;
    }

    private Button creerBoutonFooter(String text, String type) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("button", type, "outline");
        btn.setPrefHeight(35);
        btn.setStyle("-fx-cursor: hand;-fx-font-weight: bold;-fx-font-size: 12px;-fx-padding: 8 20;");
        return btn;
    }

    private void confirmerAcceptation(entreprise ent) {
        if (confirmer("Confirmation", "Accepter " + ent.getNom_entreprise() + " ?")) {
            try {
                entrepriseCRUD.accepterEntreprise(ent);
                chargerDemandes();
            } catch (SQLException e) {
                afficherErreur("Erreur", e.getMessage());
            }
        }
    }
    private void confirmerRefus(entreprise ent) {
        if (confirmer("Refus", "Refuser " + ent.getNom_entreprise() + " ?")) {
            try {
                entrepriseCRUD.changerStatut(ent.getId(), statut.refusee);
                chargerDemandes();
            } catch (SQLException e) {
                afficherErreur("Erreur", e.getMessage());
            }
        }
    }
    private void confirmerSuppression(entreprise ent) {
        if (confirmer("Suppression","Supprimer cette demande ? Cette action est irréversible.")) {
            try {
                entrepriseCRUD.supprimer(ent.getId());
                chargerDemandes();
            } catch (SQLException e) {
                afficherErreur("Erreur", e.getMessage());
            }
        }
    }
    private boolean confirmer(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titre);
        alert.setHeaderText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    private void mettreAJourStats(List<entreprise> entreprises) {
        lblTotal.setText(String.valueOf(entreprises.size()));
        lblEnAttente.setText(String.valueOf(entreprises.stream().filter(e -> e.getStatut() == statut.enattende).count()));
        lblAcceptees.setText(String.valueOf(entreprises.stream().filter(e -> e.getStatut() == statut.acceptee).count()));
        lblRefusees.setText(String.valueOf(entreprises.stream().filter(e -> e.getStatut() == statut.refusee).count()));
    }
    private VBox creerMessageVide() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60));
        Label message = new Label("Aucune demande trouvée");
        message.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 16px;");
        Label subMessage = new Label("Modifiez vos filtres ou attendez de nouvelles inscriptions");
        subMessage.setStyle("-fx-text-fill: -color-fg-subtle; -fx-font-size: 13px;");
        box.getChildren().addAll(message, subMessage);
        return box;
    }
    private void afficherErreur(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(msg);
        alert.show();
    }
}