package controller.annonces;

import entity.Post;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import service.PostCRUD;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for DISPLAY-ONLY view of announcements
 * NO Add, Edit, or Delete functionality
 * Only shows announcements in read-only mode
 */
public class AnnonceDisplayController {

    // Sidebar elements
    @FXML private VBox sidebar;
    @FXML private Button btnHome;
    @FXML private Button btnFormation;
    @FXML private Button btnDemande;
    @FXML private Button btnEmployer;
    @FXML private Button btnProjet;
    @FXML private Button btnOffre;
    @FXML private BorderPane rootPane;

    private boolean isExpanded = false;

    // Content elements
    @FXML private VBox postsContainer;
    @FXML private ComboBox<String> comboFilter;

    private PostCRUD postCRUD = new PostCRUD();

    @FXML
    public void initialize() {
        // Initialize filter combo box
        comboFilter.getItems().addAll("Tous", "Annonces", "Événements", "Actifs seulement");
        comboFilter.setValue("Tous");

        // Add listener to filter combo
        comboFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            refreshPosts();
        });

        // Load posts on init
        refreshPosts();
    }

    // ========== SIDEBAR NAVIGATION ==========

    @FXML
    private void handleToggleSidebar() {
        double endWidth = isExpanded ? 68 : 200;
        Timeline timeline = new Timeline();
        KeyValue widthValue = new KeyValue(sidebar.prefWidthProperty(), endWidth);
        KeyFrame widthFrame = new KeyFrame(Duration.millis(150), widthValue);
        timeline.getKeyFrames().add(widthFrame);

        if (!isExpanded) {
            sidebar.getStyleClass().add("expanded");
        } else {
            sidebar.getStyleClass().remove("expanded");
        }

        timeline.play();
        isExpanded = !isExpanded;
    }

    @FXML
    private void showHome(ActionEvent event) {
        updateActiveButton(btnHome);
    }

    @FXML
    private void showFormation(ActionEvent event) {
        loadView("formations");
        updateActiveButton(btnFormation);
    }

    @FXML
    private void showDemande(ActionEvent event) {
        loadView("demandes");
        updateActiveButton(btnDemande);
    }

    @FXML
    private void showEmployer(ActionEvent event) {
        loadView("employers");
        updateActiveButton(btnEmployer);
    }

    @FXML
    private void showProjet(ActionEvent event) {
        loadView("projets");
        updateActiveButton(btnProjet);
    }

    @FXML
    private void showOffres(ActionEvent event) {
        loadView("offres");
        updateActiveButton(btnOffre);
    }

    private void loadView(String fxmlFileName) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/" + fxmlFileName + ".fxml"));
            rootPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page: " + fxmlFileName);
        }
    }

    private void updateActiveButton(Button activeBtn) {
        btnHome.getStyleClass().remove("nav-active");
        btnFormation.getStyleClass().remove("nav-active");
        btnDemande.getStyleClass().remove("nav-active");
        btnEmployer.getStyleClass().remove("nav-active");
        btnProjet.getStyleClass().remove("nav-active");
        btnOffre.getStyleClass().remove("nav-active");

        activeBtn.getStyleClass().add("nav-active");
    }

    // ========== DISPLAY POSTS ==========

    /**
     * Refresh the list of posts from database
     * Applies current filter
     */
    private void refreshPosts() {
        postsContainer.getChildren().clear();

        try {
            List<Post> posts = postCRUD.afficher();
            String filter = comboFilter.getValue();

            for (Post post : posts) {
                // Apply filter
                if (shouldDisplayPost(post, filter)) {
                    addDisplayCard(post);
                }
            }

            if (postsContainer.getChildren().isEmpty()) {
                Label emptyLabel = new Label("Aucune annonce disponible pour le moment.");
                emptyLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px; -fx-padding: 20;");
                emptyLabel.setWrapText(true);
                postsContainer.getChildren().add(emptyLabel);
            }
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les annonces: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if post should be displayed based on filter
     */
    private boolean shouldDisplayPost(Post post, String filter) {
        switch (filter) {
            case "Annonces":
                return post.getTypePost() == 1;
            case "Événements":
                return post.getTypePost() == 2;
            case "Actifs seulement":
                return post.isActive();
            case "Tous":
            default:
                return true;
        }
    }

    /**
     * Create a display-only card (NO action buttons)
     */
    private void addDisplayCard(Post post) {
        VBox card = new VBox();
        card.getStyleClass().add("annonce-card-display");
        card.setSpacing(15);
        card.setPadding(new Insets(20));
        card.setMaxWidth(Double.MAX_VALUE);

        // Header with image and text
        HBox header = new HBox();
        header.setSpacing(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Image placeholder
        VBox imagePlaceholder = new VBox();
        imagePlaceholder.getStyleClass().add("image-placeholder");
        imagePlaceholder.setPrefSize(60, 60);
        imagePlaceholder.setMinSize(60, 60);
        imagePlaceholder.setMaxSize(60, 60);
        imagePlaceholder.setStyle("-fx-background-color: #E8E8E8; -fx-background-radius: 8;");

        // Text content
        VBox textContainer = new VBox();
        textContainer.setSpacing(8);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        // Title
        Label titleLabel = new Label(post.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #1a1a1a;");
        titleLabel.setWrapText(true);

        // Full content (not truncated for display view)
        Label contentLabel = new Label(post.getContenu());
        contentLabel.setFont(Font.font("System", 13));
        contentLabel.setStyle("-fx-text-fill: #666666;");
        contentLabel.setWrapText(true);

        // Metadata row
        HBox metadataBox = new HBox();
        metadataBox.setSpacing(10);
        metadataBox.setAlignment(Pos.CENTER_LEFT);

        // Status badge
        Label statusBadge = new Label(post.isActive() ? "Actif" : "Inactif");
        statusBadge.getStyleClass().add(post.isActive() ? "badge-active" : "badge-inactive");
        statusBadge.setFont(Font.font("System", FontWeight.BOLD, 11));

        // Type badge
        Label typeBadge = new Label(post.getTypePost() == 1 ? "Annonce" : "Événement");
        typeBadge.getStyleClass().add("badge-type");
        typeBadge.setFont(Font.font("System", FontWeight.BOLD, 11));

        // Date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label("Publié le: " + post.getDateCreation().format(formatter));
        dateLabel.setFont(Font.font("System", 11));
        dateLabel.setStyle("-fx-text-fill: #888888;");

        metadataBox.getChildren().addAll(statusBadge, typeBadge, dateLabel);

        textContainer.getChildren().addAll(titleLabel, contentLabel, metadataBox);
        header.getChildren().addAll(imagePlaceholder, textContainer);

        // Event-specific info (if type is Event)
        if (post.getTypePost() == 2) {
            VBox eventInfo = new VBox();
            eventInfo.setSpacing(5);
            eventInfo.setStyle("-fx-background-color: #f0f8ff; -fx-padding: 10; -fx-background-radius: 6;");

            Label eventTitle = new Label("Informations Événement");
            eventTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
            eventTitle.setStyle("-fx-text-fill: #1a1a1a;");

            if (post.getDateEvenement() != null) {
                Label dateEvent = new Label("📅 Date: " + post.getDateEvenement());
                dateEvent.setFont(Font.font("System", 12));
                eventInfo.getChildren().add(dateEvent);
            }

            if (post.getDateFinEvenement() != null) {
                Label dateEndEvent = new Label("📅 Date de fin: " + post.getDateFinEvenement());
                dateEndEvent.setFont(Font.font("System", 12));
                eventInfo.getChildren().add(dateEndEvent);
            }

            if (post.getLieu() != null && !post.getLieu().isEmpty()) {
                Label lieu = new Label("📍 Lieu: " + post.getLieu());
                lieu.setFont(Font.font("System", 12));
                eventInfo.getChildren().add(lieu);
            }

            if (post.getCapaciteMax() != null) {
                Label capacite = new Label("👥 Capacité: " + post.getCapaciteMax() + " personnes");
                capacite.setFont(Font.font("System", 12));
                eventInfo.getChildren().add(capacite);
            }

            if (eventInfo.getChildren().size() > 1) {
                eventInfo.getChildren().add(0, eventTitle);
                card.getChildren().addAll(header, eventInfo);
            } else {
                card.getChildren().add(header);
            }
        } else {
            card.getChildren().add(header);
        }

        // Add clickable effect (optional - for future detail view)
        card.setOnMouseEntered(e -> card.setStyle("-fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-cursor: default;"));

        postsContainer.getChildren().add(card);
    }

    /**
     * Show error alert
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
