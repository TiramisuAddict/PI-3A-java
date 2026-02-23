package controller.offres;

import entity.Offre;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import service.OffreCRUD;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OffrePortalController {

    private final OffreCRUD crudOffre = new OffreCRUD();

    @FXML private FlowPane offersGrid;
    @FXML private TextField txtSearch;
    @FXML private Label lblCount;

    private List<Offre> offresListCached = new ArrayList<>();

    @FXML
    public void initialize() {
        loadDataFromDatabase();

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });
    }

    private void loadDataFromDatabase() {
        try {
            offresListCached = crudOffre.afficher();
            renderOffersGrid(offresListCached);
        } catch (SQLException e) {
            System.err.println("Erreur chargement portail: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String query = txtSearch.getText().toLowerCase().trim();

        List<Offre> filteredList = offresListCached.stream()
                .filter(o -> o.getTitrePoste().toLowerCase().contains(query) ||
                        o.getTypeContrat().getDisplayName().toLowerCase().contains(query))
                .toList();

        renderOffersGrid(filteredList);
    }

    private void renderOffersGrid(List<Offre> list) {
        offersGrid.getChildren().clear();
        for (Offre o : list) {
            offersGrid.getChildren().add(createOfferCard(o));
        }
        lblCount.setText(list.size() + " Offres trouvées");
    }

    private VBox createOfferCard(Offre o) {
        VBox card = new VBox(10);
        card.setPrefWidth(320);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("job-card");
        card.setStyle("-fx-background-color: -color-bg-default; -fx-border-color: -color-border-muted; -fx-background-radius: 10;");

        // Labels
        Label lblTitle = new Label(o.getTitrePoste());
        lblTitle.setStyle("-fx-text-fill: -color-accent-fg; -fx-font-weight: bold; -fx-font-size: 15;");
        lblTitle.setWrapText(true);

        Label lblInfo = new Label(o.getTypeContrat().getDisplayName());
        lblInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        Label lblDate = new Label("Expire le: " + o.getDateLimite().toString());
        lblDate.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12;");

        card.getChildren().addAll(lblTitle, lblInfo, lblDate);

        // Hover Effects
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-border-color: -color-accent-emphasis; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().split("-fx-border-color")[0] + "-fx-border-color: -color-border-muted;"));

        // Click Logic
        card.setOnMouseClicked(e -> openCandidatureOverlay(o));

        return card;
    }

    private void openCandidatureOverlay(Offre o) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/offres/candidature.fxml"));
            Parent candidatureOverlay = loader.load();

            // 1. Injection des données dans le controller de destination
            CandidatureController controller = loader.getController();
            controller.setOfferId(o.getId());

            StackPane contentArea = (StackPane) offersGrid.getScene().lookup("#contentArea");

            if (contentArea != null) {
                if (!contentArea.getChildren().isEmpty()) {
                    Node portalViewNode = contentArea.getChildren().get(0);
                    portalViewNode.setEffect(new GaussianBlur(40));
                    portalViewNode.setOpacity(0.7);
                }

                contentArea.getChildren().add(candidatureOverlay);
            }

        } catch (IOException ex) {
            System.err.println("Impossible d'ouvrir la postulation: " + ex.getMessage());
        }
    }
}