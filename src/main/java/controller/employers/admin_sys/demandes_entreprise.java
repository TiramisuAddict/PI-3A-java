package controller.employers.admin_sys;

import entities.entreprise;
import entities.statut;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;
import service.entrepriseCRUD;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class demandes_entreprise implements Initializable {

    @FXML private VBox demandesContainer;
    @FXML private Label lblTotal;
    @FXML private Label lblEnAttente;
    @FXML private Label lblAcceptees;
    @FXML private Label lblRefusees;

    private entrepriseCRUD entrepriseCRUD;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            entrepriseCRUD = new entrepriseCRUD();
            chargerDemandes();
        } catch (SQLException e) {
            afficherErreur("Erreur de connexion", e.getMessage());
        }
    }

    private void chargerDemandes() {
        try {
            demandesContainer.getChildren().clear();
            List<entreprise> entreprises = entrepriseCRUD.afficher();
            mettreAJourStats(entreprises);

            if (entreprises.isEmpty()) {
                demandesContainer.getChildren().add(creerMessageVide());
                return;
            }

            entreprises.sort((e1, e2) -> {
                if (e1.getStatut() == statut.enattende && e2.getStatut() != statut.enattende) return -1;
                if (e1.getStatut() != statut.enattende && e2.getStatut() == statut.enattende) return 1;
                return 0;
            });

            for (entreprise ent : entreprises) {
                VBox card = new VBox();
                card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
                HBox row = creerLigneEntreprise(ent, card);
                card.getChildren().add(row);
                demandesContainer.getChildren().add(card);
            }

        } catch (SQLException e) {
            afficherErreur("Erreur", e.getMessage());
        }
    }

    private HBox creerLigneEntreprise(entreprise entreprise, VBox parentCard) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 25, 15, 25));
        row.setSpacing(10);
        row.setStyle("-fx-cursor: hand; -fx-background-radius: 15;");
        row.setOnMouseEntered(e ->
                parentCard.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 15; -fx-border-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 4);")
        );
        row.setOnMouseExited(e -> {
            if (parentCard.getChildren().size() > 1) {
                parentCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-border-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
            } else {
                parentCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
            }
        });
        row.setOnMouseClicked(e -> toggleDetails(parentCard, entreprise));
        Label nomLabel = new Label(entreprise.getNom_entreprise());
        nomLabel.setPrefWidth(220);
        nomLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-font-weight: bold;");
        Label dateLabel = new Label();
        if (entreprise.getDate_demande() != null) {
            dateLabel.setText(entreprise.getDate_demande().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        } else {
            dateLabel.setText("N/A");
        }
        dateLabel.setPrefWidth(130);
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #000000;");
        Label emailLabel = new Label(entreprise.getE_mail());
        emailLabel.setPrefWidth(200);
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #000000;");
        HBox statutBox = new HBox(creerBadgeStatut(entreprise.getStatut()));
        statutBox.setPrefWidth(130);
        statutBox.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.setPrefWidth(250);

        Button btnAccepter = creerBoutonAvecTexte("Accepter", "/icons/accept.png", "#16A34A");
        Button btnRefuser = creerBoutonAvecTexte("Refuser", "/icons/refuse.png", "#DC2626");

        btnAccepter.setOnMouseClicked(e -> e.consume());
        btnRefuser.setOnMouseClicked(e -> e.consume());

        btnAccepter.setOnAction(e -> confirmerAcceptation(entreprise, statutBox, btnAccepter, btnRefuser));
        btnRefuser.setOnAction(e -> confirmerRefus(entreprise, statutBox, btnAccepter, btnRefuser));

        if (entreprise.getStatut() != statut.enattende) {
            desactiverBoutons(btnAccepter, btnRefuser);
        }

        buttonsBox.getChildren().addAll(btnAccepter, btnRefuser);
        row.getChildren().addAll(nomLabel, dateLabel, emailLabel, statutBox, spacer, buttonsBox);
        return row;
    }

    private void toggleDetails(VBox parentCard, entreprise ent) {
        if (parentCard.getChildren().size() > 1) {
            parentCard.getChildren().remove(1);
        } else {
            VBox detailsBox = creerPanneauDetails(ent);
            parentCard.getChildren().add(detailsBox);
        }
    }

    private VBox creerPanneauDetails(entreprise ent) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20, 40, 20, 40));
        box.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 0 0 15 15; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Détails de l'entreprise");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000; -fx-font-size: 14px;");
        headerBox.getChildren().add(title);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().add(spacer);

        if (ent.getStatut() == statut.refusee) {
            Button btnDelete = creerBoutonAvecTexte("Supprimer", null, "#dc2626");

            btnDelete.setOnAction(e -> confirmerSuppression(ent));
            headerBox.getChildren().add(btnDelete);
        }

        HBox content = new HBox(50);
        VBox col1 = new VBox(10);
        col1.getChildren().addAll(
                creerInfoLigne("Nom", ent.getNom_entreprise()),
                creerInfoLigne("Ville", ent.getVille()),
                creerInfoLigne("Pays", ent.getPays())
        );
        VBox col2 = new VBox(10);
        col2.getChildren().addAll(
                creerInfoLigne("Responsable", ent.getPrenom() + " " + ent.getNom()),
                creerInfoLigne("Matricule", ent.getMatricule_fiscale()),
                creerInfoLigne("Téléphone", String.valueOf(ent.getTelephone()))
        );

        content.getChildren().addAll(col1, col2);
        box.getChildren().addAll(headerBox, content);

        ScaleTransition st = new ScaleTransition(Duration.millis(200), box);
        st.setFromY(0);
        st.setToY(1);
        st.play();

        return box;
    }

    private void confirmerSuppression(entreprise ent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer cette demande ?");
        alert.setContentText("Cette action est irréversible.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                entrepriseCRUD.supprimer(ent.getId());
                chargerDemandes();
            } catch (SQLException e) {
                afficherErreur("Erreur", e.getMessage());
            }
        }
    }

    private HBox creerInfoLigne(String label, String value) {
        HBox line = new HBox(10);
        Label l = new Label(label + ":");
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000; -fx-min-width: 100px;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #334155;");
        line.getChildren().addAll(l, v);
        return line;
    }

    private Button creerBoutonAvecTexte(String text, String iconPath, String colorHex) {
        Button btn = new Button(text);
        String styleBase = String.format("-fx-background-color: white; -fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 13px; -fx-border-color: %s; -fx-border-width: 1.5; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;",
                colorHex, colorHex
        );
        btn.setStyle(styleBase);

        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            icon.setFitWidth(16);
            icon.setFitHeight(16);
            btn.setGraphic(icon);
            btn.setGraphicTextGap(8);
        } catch (Exception e) {}

        btn.setOnMouseEntered(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(String.format("-fx-background-color: %s15; -fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 13px; -fx-border-color: %s; -fx-border-width: 1.5; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;", colorHex, colorHex, colorHex));
                btn.setEffect(new DropShadow(5, Color.web(colorHex, 0.25)));
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(styleBase);
                btn.setEffect(null);
            }
        });

        return btn;
    }

    private void desactiverBoutons(Button... buttons) {
        for (Button btn : buttons) {
            btn.setDisable(true);
            btn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 12;");
            btn.setEffect(null);
            if (btn.getGraphic() != null) btn.getGraphic().setOpacity(0.5);
        }
    }

    private Label creerBadgeStatut(statut statut) {
        Label badge = new Label();
        String style = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 15; -fx-background-insets: 0;";
        switch (statut) {
            case enattende:
                badge.setText("En attente");
                badge.setStyle(style + "-fx-background-color: #fef3c7; -fx-text-fill: #F59E0B;");
                break;
            case acceptee:
                badge.setText("Acceptée");
                badge.setStyle(style + "-fx-background-color: #dcfce7; -fx-text-fill: #16A34A;");
                break;
            case refusee:
                badge.setText("Refusée");
                badge.setStyle(style + "-fx-background-color: #fee2e2; -fx-text-fill: #DC2626;");
                break;
        }
        return badge;
    }

    private void confirmerAcceptation(entreprise ent, HBox container, Button btnOk, Button btnNo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Accepter " + ent.getNom_entreprise() + " ?");
        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                entrepriseCRUD.accepterEntreprise(ent);
                container.getChildren().clear();
                container.getChildren().add(creerBadgeStatut(statut.acceptee));
                desactiverBoutons(btnOk, btnNo);
                chargerDemandes();
            } catch (SQLException e) {
                afficherErreur("Erreur", e.getMessage());
            }
        }
    }
    private void confirmerRefus(entreprise ent, HBox container, Button btnOk, Button btnNo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Refus");
        alert.setHeaderText("Refuser " + ent.getNom_entreprise() + " ?");
        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                entrepriseCRUD.changerStatut(ent.getId(), statut.refusee);
                container.getChildren().clear();
                container.getChildren().add(creerBadgeStatut(statut.refusee));
                desactiverBoutons(btnOk, btnNo);
                chargerDemandes();
            } catch (SQLException e) {
                afficherErreur("Erreur", e.getMessage());
            }
        }
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
        box.setPadding(new Insets(50));
        Label l = new Label("Aucune demande");
        l.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16px;");
        box.getChildren().add(l);
        return box;
    }
    private void afficherErreur(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titre);
        a.setContentText(msg);
        a.show();
    }
}