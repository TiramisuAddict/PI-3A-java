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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AnnonceController {

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

    // Form fields
    @FXML private TextField txtTitre;
    @FXML private TextArea txtContenu;
    @FXML private ComboBox<String> comboTypePost;
    @FXML private CheckBox chkActive;
    @FXML private DatePicker dateEvenement;
    @FXML private DatePicker dateFinEvenement;
    @FXML private TextField txtLieu;
    @FXML private TextField txtCapaciteMax;

    // Error labels
    @FXML private Label lblTitreError;
    @FXML private Label lblContenuError;
    @FXML private Label lblTypeError;
    @FXML private Label lblCapaciteError;

    // Containers
    @FXML private VBox postsContainer;
    @FXML private VBox formContainer;
    @FXML private VBox eventFieldsContainer;

    // Buttons
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnAjouter;
    @FXML private Label formTitle;

    private PostCRUD postCRUD = new PostCRUD();
    private Post selectedPost = null;

    // Filter state: "ALL", "ANNONCE", "EVENT"
    private String currentFilter = "ALL";

    @FXML
    public void initialize() {
        // Initialize ComboBox with post types
        comboTypePost.getItems().addAll("Annonce", "Événement");

        // Add listener to show/hide event fields based on type
        comboTypePost.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Événement".equals(newVal)) {
                eventFieldsContainer.setVisible(true);
                eventFieldsContainer.setManaged(true);
            } else {
                eventFieldsContainer.setVisible(false);
                eventFieldsContainer.setManaged(false);
            }
        });

        // Add input validation listeners
        addValidationListeners();

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
        // Show ALL posts (Annonces + Events)
        currentFilter = "ALL";
        refreshPosts();
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

    // ========== FILTER METHODS (PUBLIC) ==========

    /**
     * Show only Annonces
     */
    public void filterAnnonces() {
        currentFilter = "ANNONCE";
        refreshPosts();
    }

    /**
     * Show only Events
     */
    public void filterEvents() {
        currentFilter = "EVENT";
        refreshPosts();
    }

    /**
     * Show all posts
     */
    public void showAll() {
        currentFilter = "ALL";
        refreshPosts();
    }

    // ========== VALIDATION ==========

    private void addValidationListeners() {
        txtTitre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                hideError(lblTitreError);
            }
        });

        txtContenu.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                hideError(lblContenuError);
            }
        });

        comboTypePost.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                hideError(lblTypeError);
            }
        });

        txtCapaciteMax.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtCapaciteMax.setText(newVal.replaceAll("[^\\d]", ""));
            }
            hideError(lblCapaciteError);
        });
    }

    // ========== POSTS MANAGEMENT ==========

    private void refreshPosts() {
        postsContainer.getChildren().clear();

        try {
            List<Post> posts = postCRUD.afficher();

            for (Post post : posts) {
                // Apply filter based on currentFilter
                boolean shouldDisplay = false;

                switch (currentFilter) {
                    case "ANNONCE":
                        shouldDisplay = (post.getTypePost() == 1);
                        break;
                    case "EVENT":
                        shouldDisplay = (post.getTypePost() == 2);
                        break;
                    case "ALL":
                    default:
                        shouldDisplay = true; // Show both annonces and events
                        break;
                }

                if (shouldDisplay) {
                    addPostCard(post);
                }
            }

            if (postsContainer.getChildren().isEmpty()) {
                Label emptyLabel = new Label("Aucune annonce disponible. Cliquez sur '+ Ajouter une Annonce' pour créer une annonce.");
                emptyLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
                emptyLabel.setWrapText(true);
                postsContainer.getChildren().add(emptyLabel);
            }
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les annonces: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addPostCard(Post post) {
        VBox card = new VBox();

        // Add different style class based on type
        if (post.getTypePost() == 1) {
            card.getStyleClass().add("annonce-card");
        } else {
            card.getStyleClass().add("event-card"); // Different style for events
        }

        card.setSpacing(15);
        card.setPadding(new Insets(20));
        card.setMaxWidth(Double.MAX_VALUE);

        HBox header = new HBox();
        header.setSpacing(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox imagePlaceholder = new VBox();
        imagePlaceholder.getStyleClass().add("image-placeholder");
        imagePlaceholder.setPrefSize(60, 60);
        imagePlaceholder.setMinSize(60, 60);
        imagePlaceholder.setMaxSize(60, 60);

        // Different color for event placeholder
        if (post.getTypePost() == 1) {
            imagePlaceholder.setStyle("-fx-background-color: #E8E8E8; -fx-background-radius: 8;");
        } else {
            imagePlaceholder.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 8;"); // Blue tint for events
        }

        VBox textContainer = new VBox();
        textContainer.setSpacing(8);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        Label titleLabel = new Label(post.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #1a1a1a;");
        titleLabel.setWrapText(true);

        String contentPreview = post.getContenu();
        if (contentPreview.length() > 100) {
            contentPreview = contentPreview.substring(0, 100) + "...";
        }
        Label contentLabel = new Label(contentPreview);
        contentLabel.setFont(Font.font("System", 13));
        contentLabel.setStyle("-fx-text-fill: #666666;");
        contentLabel.setWrapText(true);

        // Badge row
        HBox badgeRow = new HBox();
        badgeRow.setSpacing(8);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        Label statusBadge = new Label(post.isActive() ? "Actif" : "Inactif");
        statusBadge.getStyleClass().add(post.isActive() ? "badge-active" : "badge-inactive");
        statusBadge.setFont(Font.font("System", FontWeight.BOLD, 11));

        // Type badge with different colors
        Label typeBadge = new Label(post.getTypePost() == 1 ? "Annonce" : "Événement");
        if (post.getTypePost() == 1) {
            typeBadge.getStyleClass().add("badge-type-annonce");
        } else {
            typeBadge.getStyleClass().add("badge-type-event");
        }
        typeBadge.setFont(Font.font("System", FontWeight.BOLD, 11));

        badgeRow.getChildren().addAll(statusBadge, typeBadge);

        textContainer.getChildren().addAll(titleLabel, contentLabel, badgeRow);
        header.getChildren().addAll(imagePlaceholder, textContainer);

        // Add event info if it's an event
        VBox cardContent = new VBox();
        cardContent.setSpacing(12);
        cardContent.getChildren().add(header);

        if (post.getTypePost() == 2) {
            // Event-specific information
            VBox eventInfo = new VBox();
            eventInfo.setSpacing(6);
            eventInfo.setStyle("-fx-background-color: #F0F8FF; -fx-padding: 12; -fx-background-radius: 6; -fx-border-color: #90CAF9; -fx-border-width: 1; -fx-border-radius: 6;");

            if (post.getDateEvenement() != null) {
                Label dateLabel = new Label("📅 Date: " + post.getDateEvenement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                dateLabel.setFont(Font.font("System", 12));
                dateLabel.setStyle("-fx-text-fill: #1565C0;");
                eventInfo.getChildren().add(dateLabel);
            }

            if (post.getDateFinEvenement() != null) {
                Label dateEndLabel = new Label("📅 Fin: " + post.getDateFinEvenement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                dateEndLabel.setFont(Font.font("System", 12));
                dateEndLabel.setStyle("-fx-text-fill: #1565C0;");
                eventInfo.getChildren().add(dateEndLabel);
            }

            if (post.getLieu() != null && !post.getLieu().isEmpty()) {
                Label lieuLabel = new Label("📍 " + post.getLieu());
                lieuLabel.setFont(Font.font("System", 12));
                lieuLabel.setStyle("-fx-text-fill: #1565C0;");
                eventInfo.getChildren().add(lieuLabel);
            }

            if (post.getCapaciteMax() != null) {
                Label capaciteLabel = new Label("👥 Capacité: " + post.getCapaciteMax() + " personnes");
                capaciteLabel.setFont(Font.font("System", 12));
                capaciteLabel.setStyle("-fx-text-fill: #1565C0;");
                eventInfo.getChildren().add(capaciteLabel);
            }

            if (eventInfo.getChildren().size() > 0) {
                cardContent.getChildren().add(eventInfo);
            }
        }

        // Action buttons
        HBox actionsBox = new HBox();
        actionsBox.setSpacing(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnModifier = new Button("Modifier");
        btnModifier.getStyleClass().add("btn-card-modifier");
        btnModifier.setOnAction(e -> handleEditPost(post));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.getStyleClass().add("btn-card-supprimer");
        btnSupprimer.setOnAction(e -> handleDeletePost(post));

        actionsBox.getChildren().addAll(btnModifier, btnSupprimer);
        cardContent.getChildren().add(actionsBox);

        card.getChildren().add(cardContent);
        postsContainer.getChildren().add(card);
    }

    @FXML
    private void handleShowAddForm() {
        selectedPost = null;
        clearForm();
        clearErrors();
        formTitle.setText("Ajouter une Annonce");
        btnSave.setVisible(true);
        btnSave.setManaged(true);
        btnUpdate.setVisible(false);
        btnUpdate.setManaged(false);
        formContainer.setVisible(true);
        formContainer.setManaged(true);
        chkActive.setSelected(true);
        comboTypePost.setValue("Annonce");
    }

    private void handleEditPost(Post post) {
        selectedPost = post;
        clearErrors();

        txtTitre.setText(post.getTitre());
        txtContenu.setText(post.getContenu());
        comboTypePost.setValue(post.getTypePost() == 1 ? "Annonce" : "Événement");
        chkActive.setSelected(post.isActive());

        if (post.getTypePost() == 2) {
            if (post.getDateEvenement() != null) {
                dateEvenement.setValue(post.getDateEvenement());
            }
            if (post.getDateFinEvenement() != null) {
                dateFinEvenement.setValue(post.getDateFinEvenement());
            }
            if (post.getLieu() != null) {
                txtLieu.setText(post.getLieu());
            }
            if (post.getCapaciteMax() != null) {
                txtCapaciteMax.setText(String.valueOf(post.getCapaciteMax()));
            }
        }

        formTitle.setText("Modifier l'Annonce");
        btnSave.setVisible(false);
        btnSave.setManaged(false);
        btnUpdate.setVisible(true);
        btnUpdate.setManaged(true);
        formContainer.setVisible(true);
        formContainer.setManaged(true);
        formContainer.requestFocus();
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        Post post = new Post();
        populatePostFromForm(post);
        post.setDateCreation(LocalDateTime.now());
        post.setUtilisateurId(1);

        try {
            postCRUD.ajouter(post);
            showSuccess("Succès", "Annonce ajoutée avec succès!");
            handleCancelForm();
            refreshPosts();
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'ajouter l'annonce: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedPost == null) {
            showError("Erreur", "Aucune annonce sélectionnée");
            return;
        }

        if (!validateForm()) {
            return;
        }

        populatePostFromForm(selectedPost);

        try {
            postCRUD.modifier(selectedPost);
            showSuccess("Succès", "Annonce modifiée avec succès!");
            handleCancelForm();
            refreshPosts();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de modifier l'annonce: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populatePostFromForm(Post post) {
        post.setTitre(txtTitre.getText().trim());
        post.setContenu(txtContenu.getText().trim());
        post.setTypePost(comboTypePost.getValue().equals("Annonce") ? 1 : 2);
        post.setActive(chkActive.isSelected());

        if (post.getTypePost() == 2) {
            post.setDateEvenement(dateEvenement.getValue());
            post.setDateFinEvenement(dateFinEvenement.getValue());
            post.setLieu(txtLieu.getText().trim().isEmpty() ? null : txtLieu.getText().trim());

            if (!txtCapaciteMax.getText().trim().isEmpty()) {
                post.setCapaciteMax(Integer.parseInt(txtCapaciteMax.getText().trim()));
            } else {
                post.setCapaciteMax(null);
            }
        } else {
            post.setDateEvenement(null);
            post.setDateFinEvenement(null);
            post.setLieu(null);
            post.setCapaciteMax(null);
        }
    }

    private void handleDeletePost(Post post) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'annonce");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette annonce?\n\n" +
                "Titre: " + post.getTitre());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                postCRUD.supprimer(post.getIdPost());
                showSuccess("Succès", "Annonce supprimée avec succès!");
                refreshPosts();
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer l'annonce: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancelForm() {
        clearForm();
        clearErrors();
        formContainer.setVisible(false);
        formContainer.setManaged(false);
        selectedPost = null;
    }

    private void clearForm() {
        txtTitre.clear();
        txtContenu.clear();
        comboTypePost.setValue(null);
        chkActive.setSelected(true);
        dateEvenement.setValue(null);
        dateFinEvenement.setValue(null);
        txtLieu.clear();
        txtCapaciteMax.clear();
    }

    private void clearErrors() {
        hideError(lblTitreError);
        hideError(lblContenuError);
        hideError(lblTypeError);
        hideError(lblCapaciteError);
    }

    private boolean validateForm() {
        boolean isValid = true;
        clearErrors();

        if (txtTitre.getText() == null || txtTitre.getText().trim().isEmpty()) {
            showFieldError(lblTitreError, "Le titre est obligatoire");
            isValid = false;
        } else if (txtTitre.getText().trim().length() < 3) {
            showFieldError(lblTitreError, "Le titre doit contenir au moins 3 caractères");
            isValid = false;
        } else if (txtTitre.getText().trim().length() > 255) {
            showFieldError(lblTitreError, "Le titre ne peut pas dépasser 255 caractères");
            isValid = false;
        }

        if (txtContenu.getText() == null || txtContenu.getText().trim().isEmpty()) {
            showFieldError(lblContenuError, "Le contenu est obligatoire");
            isValid = false;
        } else if (txtContenu.getText().trim().length() < 10) {
            showFieldError(lblContenuError, "Le contenu doit contenir au moins 10 caractères");
            isValid = false;
        }

        if (comboTypePost.getValue() == null) {
            showFieldError(lblTypeError, "Veuillez sélectionner un type de post");
            isValid = false;
        }

        if ("Événement".equals(comboTypePost.getValue())) {
            if (dateEvenement.getValue() != null && dateFinEvenement.getValue() != null) {
                if (dateFinEvenement.getValue().isBefore(dateEvenement.getValue())) {
                    showError("Validation", "La date de fin doit être après la date de début");
                    isValid = false;
                }
            }

            if (!txtCapaciteMax.getText().trim().isEmpty()) {
                try {
                    int capacite = Integer.parseInt(txtCapaciteMax.getText().trim());
                    if (capacite <= 0) {
                        showFieldError(lblCapaciteError, "La capacité doit être supérieure à 0");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    showFieldError(lblCapaciteError, "La capacité doit être un nombre valide");
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
