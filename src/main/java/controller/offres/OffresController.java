package controller.offres;

import entity.EtatOffre;
import entity.Offre;
import entity.TypeContrat;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import service.OffreCRUD;
import utils.LayoutAnimator;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class OffresController {

    @FXML private TextField txtCode, txtTitre;
    @FXML private ComboBox<TypeContrat> comboType;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<EtatOffre> comboEtat;

    @FXML private VBox offersContainer;
    @FXML private FlowPane StatisticsPane;

    OffreCRUD crud = new OffreCRUD();

    //Under Construction
    private HBox selectedPathCard = null;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> filterType;

    @FXML
    public void initialize() {

        new LayoutAnimator(offersContainer);
        new LayoutAnimator(StatisticsPane);

        // Setup Form ComboBoxes
        comboType.setItems(FXCollections.observableArrayList(TypeContrat.values()));
        comboEtat.setItems(FXCollections.observableArrayList(EtatOffre.values()));

        filterType.setItems(FXCollections.observableArrayList("Tous", "CDI", "CDD", "CVP", "Stage"));

        refreshDashboard();
    }

    @FXML
    public void generateCodeOffer(ActionEvent actionEvent) {
        String prefix = (txtTitre.getText().length() >= 3) ? txtTitre.getText().substring(0, 3).toUpperCase() : txtTitre.getText().toUpperCase();

        long currentTime = System.currentTimeMillis();

        String timeBase36 = Long.toString(currentTime, 36).toUpperCase();
        String suffix = timeBase36.substring(timeBase36.length() - 5);

        txtCode.setText(prefix + suffix);
    }

    private void refreshDashboard() {
        offersContainer.getChildren().clear();

        List<Offre> offres;
        try {
            offres = crud.afficher();
            for (Offre o : offres) {
                addOfferCard(o);
            }
        }catch (SQLException e) {
            System.out.println("Erreur lors du chargement des offres: " + e.getMessage());
        }
    }

    private String formatDate(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    private void addOfferCard(Offre o) {
        HBox card = new HBox();
        //unstable
        card.getStyleClass().add("glass-card");

        card.setSpacing(20);
        card.setAlignment(Pos.CENTER_LEFT);

        card.setStyle("-fx-background-color: -color-bg-subtle; " +
                "-fx-background-radius: 5; " +
                "-fx-padding: 15; " +
                "-fx-border-color: -color-border-muted; " +
                "-fx-border-radius: 5; " +
                "-fx-cursor: hand;");

        // Icon
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(45, 45);
        iconBox.setStyle("-fx-background-color: -color-accent-3; -fx-background-radius: 5;");
        Label iconLabel = new Label("💼");
        iconLabel.setStyle("-fx-text-fill: white;");
        iconBox.getChildren().add(iconLabel);

        // Text Content
        VBox details = new VBox();
        HBox.setHgrow(details, Priority.ALWAYS);
        Label titleLabel = new Label(o.getTitrePoste());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label subLabel = new Label(o.getCodeOffre() + " • " + o.getTypeContrat() + " • Expire le: " + formatDate(o.getDateLimite().toString()));

        subLabel.getStyleClass().add("text-muted");
        details.getChildren().addAll(titleLabel, subLabel);

        // Status Badge
        Label badge = new Label(o.getEtat().getDisplayName().toUpperCase());
        String badgeStyle = o.getEtat().equals(EtatOffre.OUVERT) ? "-color-success-fg" : "-color-danger-fg";
        String badgeBg = o.getEtat().equals(EtatOffre.OUVERT) ? "-color-success-muted" : "-color-danger-muted";
        badge.setStyle("-fx-background-color: " + badgeBg + "; -fx-text-fill: " + badgeStyle + "; " + "-fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;");

        card.getChildren().addAll(iconBox, details, badge);

        card.setOnMouseClicked(e -> {
            // 1. CLEAR previous selection
            if (selectedPathCard != null) {
                selectedPathCard.getStyleClass().remove("card-selected");
            }// 2. APPLY new selection
            card.getStyleClass().add("card-selected");
            selectedPathCard = card;

            txtCode.setText(o.getCodeOffre());
            txtTitre.setText(o.getTitrePoste());
            comboType.setValue(o.getTypeContrat());
            comboEtat.setValue(o.getEtat());
            dpDate.setValue(LocalDate.parse(o.getDateLimite().toString()));
        });

        offersContainer.getChildren().add(card);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        Offre o = new Offre(txtCode.getText(), 1, txtTitre.getText(), comboType.getValue(), java.sql.Date.valueOf(dpDate.getValue()), comboEtat.getValue());

        try {
            crud.ajouter(o);
        } catch (Exception e) {
            System.out.println("Erreur lors de l'ajout de l'offre: " + e.getMessage());
            return;
        }

        addOfferCard(o);

        refreshDashboard();
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        Offre o = new Offre(txtCode.getText(), 1, txtTitre.getText(), comboType.getValue(), java.sql.Date.valueOf(dpDate.getValue()), comboEtat.getValue());

        try {
            int temp = crud.getIdByCodeOffre(o.getCodeOffre());
            o.setId(temp);

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la recuperation d'ID " + ex);
        }

        try {
            crud.modifier(o);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de l'offre: " + e.getMessage());
            return;
        }

        refreshDashboard();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedPathCard != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette offre ?");

            ButtonType buttonYes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
            ButtonType buttonNo = new ButtonType("Non", ButtonBar.ButtonData.NO);

            alert.getButtonTypes().setAll(buttonYes, buttonNo);

            alert.showAndWait().ifPresent(type -> {
                if (type == buttonYes) {
                    try {
                        crud.supprimer(crud.getIdByCodeOffre(txtCode.getText()));
                    } catch (SQLException e) {
                        System.out.println("Erreur lors de la suppression de l'offre: " + e.getMessage());
                    }
                }
            });

            offersContainer.getChildren().remove(selectedPathCard);

            handleNewOffer(null);

            selectedPathCard = null;
        }
    }

    @FXML
    private void handleNewOffer(ActionEvent event) {
        txtCode.clear();
        txtTitre.clear();
        dpDate.setValue(null);
        comboType.getSelectionModel().clearSelection();
        comboEtat.getSelectionModel().clearSelection();

        if (selectedPathCard != null) {
            selectedPathCard.getStyleClass().remove("card-selected");
        }
    }
}