package controller.annonces;

import entity.Commentaire;
import entity.Like;
import entity.Post;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import service.CommentaireCRUD;
import service.EventImageCRUD;
import service.LikeCRUD;
import service.PostCRUD;
import entity.EventImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AnnonceController {

    @FXML private VBox sidebar;
    @FXML private Button btnHome, btnFormation, btnDemande, btnEmployer, btnProjet, btnOffre;
    @FXML private BorderPane rootPane;
    @FXML private TabPane mainTabPane;

    @FXML private TextField txtRecherchePost;
    @FXML private TextField txtTitre;
    @FXML private TextArea txtContenu;
    @FXML private ComboBox<String> comboTypePost;
    @FXML private CheckBox chkActive;
    @FXML private DatePicker dateEvenement, dateFinEvenement;
    @FXML private TextField txtLieu, txtCapaciteMax;

    @FXML private Label lblTitreError, lblContenuError, lblTypeError, lblCapaciteError;
    @FXML private VBox postsContainer, formContainer, eventFieldsContainer;
    @FXML private Button btnSave, btnUpdate, btnAjouter;
    @FXML private Label formTitle;

    @FXML private VBox commentsContainer;

    @FXML private Label lblTotalPosts, lblTotalLikes, lblTotalComments;
    @FXML private Label lblPostsChange, lblLikesChange, lblCommentsChange;
    @FXML private PieChart chartPostTypes;
    @FXML private BarChart<String, Number> chartEngagement;
    @FXML private LineChart<String, Number> chartActivity;
    @FXML private VBox detailedStatsContainer;

    private boolean isExpanded = false;
    private PostCRUD postCRUD = new PostCRUD();
    private CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private LikeCRUD likeCRUD = new LikeCRUD();
    private EventImageCRUD eventImageCRUD = new EventImageCRUD();
    private Post selectedPost = null;
    private String currentFilter = "ALL";
    private HBox imageGalleryContainer = null;
    private static final int MAX_PHOTOS = 10;
    private static final String IMAGES_DIR = "src/main/resources/images/events/";

    @FXML
    public void initialize() {
        comboTypePost.getItems().addAll("Annonce", "Événement");

        comboTypePost.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Événement".equals(newVal)) {
                eventFieldsContainer.setVisible(true);
                eventFieldsContainer.setManaged(true);
            } else {
                eventFieldsContainer.setVisible(false);
                eventFieldsContainer.setManaged(false);
            }
        });

        buildImageUploadSection();
        addValidationListeners();
        refreshPosts();
        loadCommentsByPost();
        loadStatistiques();
    }

    private void buildImageUploadSection() {
        // Create images directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(IMAGES_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }

        VBox imageSection = new VBox(10);
        imageSection.setStyle("-fx-border-color: -color-border-muted; -fx-border-width: 1 0 0 0; -fx-padding: 15 0 0 0;");

        Label imageLabel = new Label("Photos de l'événement (max " + MAX_PHOTOS + ")");
        imageLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: -color-fg-default;");

        Button btnUpload = new Button("+ Ajouter des photos");
        btnUpload.setStyle("-fx-background-color: -color-base-0; -fx-text-fill: -color-fg-default; " +
                "-fx-border-color: -color-border-muted; -fx-border-width: 1; -fx-border-radius: 6; " +
                "-fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-weight: 600;");
        btnUpload.setOnAction(e -> handleUploadImages());

        imageGalleryContainer = new HBox(10);
        imageGalleryContainer.setStyle("-fx-padding: 5 0;");

        imageSection.getChildren().addAll(imageLabel, btnUpload, imageGalleryContainer);
        eventFieldsContainer.getChildren().add(imageSection);
    }

    private void handleUploadImages() {
        // Need a post ID — save first if creating
        int targetPostId = -1;
        if (selectedPost != null) {
            targetPostId = selectedPost.getIdPost();
        } else {
            // Creating new post: save it first, then upload photos
            if (!validateForm()) return;
            Post post = new Post();
            populatePostFromForm(post);
            post.setDateCreation(LocalDateTime.now());
            post.setUtilisateurId(1);
            try {
                postCRUD.ajouter(post);
                // Get the newly inserted post (last one)
                List<Post> all = postCRUD.afficher();
                selectedPost = all.get(all.size() - 1);
                targetPostId = selectedPost.getIdPost();
                // Switch form to edit mode
                formTitle.setText("Modifier l'Annonce");
                btnSave.setVisible(false);
                btnSave.setManaged(false);
                btnUpdate.setVisible(true);
                btnUpdate.setManaged(true);
                refreshPosts();
            } catch (SQLException e) {
                showError("Erreur", "Sauvegardez d'abord l'annonce avant d'ajouter des photos.");
                return;
            }
        }

        try {
            int current = eventImageCRUD.countByPost(targetPostId);
            if (current >= MAX_PHOTOS) {
                showError("Limite atteinte", "Maximum " + MAX_PHOTOS + " photos par événement.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner des photos");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            List<File> files = fileChooser.showOpenMultipleDialog(eventFieldsContainer.getScene().getWindow());
            if (files == null || files.isEmpty()) return;

            int added = 0;
            for (File file : files) {
                if (current + added >= MAX_PHOTOS) break;

                String fileName = targetPostId + "_" + System.currentTimeMillis() + "_" + file.getName();
                java.nio.file.Path dest = Paths.get(IMAGES_DIR + fileName);
                Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                EventImage img = new EventImage(targetPostId, IMAGES_DIR + fileName, current + added + 1);
                eventImageCRUD.ajouter(img);
                added++;
            }

            refreshImageGallery(targetPostId);
            showSuccess("Succès", added + " photo(s) ajoutée(s).");
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ajouter les photos: " + e.getMessage());
        }
    }

    private void refreshImageGallery(int postId) {
        if (imageGalleryContainer == null) return;
        imageGalleryContainer.getChildren().clear();

        try {
            List<EventImage> images = eventImageCRUD.getByPost(postId);
            for (EventImage img : images) {
                StackPane thumb = new StackPane();
                thumb.setPrefSize(90, 90);
                thumb.setMaxSize(90, 90);

                try {
                    File f = new File(img.getImagePath());
                    Image image = new Image(f.toURI().toString(), 90, 90, true, true);
                    ImageView iv = new ImageView(image);
                    iv.setFitWidth(90);
                    iv.setFitHeight(90);
                    iv.setPreserveRatio(false);
                    iv.setStyle("-fx-background-radius: 8;");
                    thumb.getChildren().add(iv);
                } catch (Exception e) {
                    Label placeholder = new Label("?");
                    placeholder.setStyle("-fx-text-fill: #94a3b8;");
                    thumb.getChildren().add(placeholder);
                }

                // Delete button on hover
                Button btnDelete = new Button("✕");
                btnDelete.setStyle("-fx-background-color: rgba(239,68,68,0.85); -fx-text-fill: white; " +
                        "-fx-background-radius: 10; -fx-font-size: 10px; -fx-padding: 2 5; -fx-cursor: hand;");
                btnDelete.setVisible(false);
                StackPane.setAlignment(btnDelete, Pos.TOP_RIGHT);

                int imageId = img.getIdImage();
                thumb.setOnMouseEntered(e -> btnDelete.setVisible(true));
                thumb.setOnMouseExited(e -> btnDelete.setVisible(false));
                btnDelete.setOnAction(e -> {
                    try {
                        new File(img.getImagePath()).delete();
                        eventImageCRUD.supprimer(imageId);
                        refreshImageGallery(postId);
                    } catch (SQLException ex) {
                        showError("Erreur", "Impossible de supprimer la photo.");
                    }
                });

                thumb.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8; " +
                        "-fx-border-color: -color-border-muted; -fx-border-width: 1; -fx-border-radius: 8;");
                thumb.getChildren().add(btnDelete);
                imageGalleryContainer.getChildren().add(thumb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleToggleSidebar() {
        double endWidth = isExpanded ? 68 : 200;
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(150), new KeyValue(sidebar.prefWidthProperty(), endWidth))
        );

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

    @FXML
    public void rechercherPosts() {
        String keyword = txtRecherchePost.getText().trim();
        postsContainer.getChildren().clear();

        try {
            List<Post> posts = keyword.isEmpty() ?
                    postCRUD.afficher() :
                    postCRUD.searchByKeyword(keyword);

            for (Post post : posts) {
                if (shouldDisplayPost(post)) {
                    addPostCard(post);
                }
            }

            if (postsContainer.getChildren().isEmpty()) {
                Label emptyLabel = new Label("Aucun résultat trouvé");
                emptyLabel.setStyle("-fx-text-fill: #697386; -fx-font-size: 14px;");
                postsContainer.getChildren().add(emptyLabel);
            }
        } catch (SQLException e) {
            showError("Erreur", "Erreur de recherche: " + e.getMessage());
        }
    }

    public void filterAnnonces() {
        currentFilter = "ANNONCE";
        refreshPosts();
    }

    public void filterEvents() {
        currentFilter = "EVENT";
        refreshPosts();
    }

    public void showAll() {
        currentFilter = "ALL";
        refreshPosts();
    }

    private boolean shouldDisplayPost(Post post) {
        switch (currentFilter) {
            case "ANNONCE": return post.getTypePost() == 1;
            case "EVENT": return post.getTypePost() == 2;
            default: return true;
        }
    }

    private void addValidationListeners() {
        txtTitre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) hideError(lblTitreError);
        });

        txtContenu.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) hideError(lblContenuError);
        });

        comboTypePost.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) hideError(lblTypeError);
        });

        txtCapaciteMax.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtCapaciteMax.setText(newVal.replaceAll("[^\\d]", ""));
            }
            hideError(lblCapaciteError);
        });
    }

    private void refreshPosts() {
        postsContainer.getChildren().clear();

        try {
            List<Post> posts = postCRUD.afficher();

            for (Post post : posts) {
                if (shouldDisplayPost(post)) {
                    addPostCard(post);
                }
            }

            if (postsContainer.getChildren().isEmpty()) {
                Label emptyLabel = new Label("Aucune annonce disponible");
                emptyLabel.setStyle("-fx-text-fill: #697386; -fx-font-size: 14px;");
                postsContainer.getChildren().add(emptyLabel);
            }
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les annonces: " + e.getMessage());
        }
    }

    private void addPostCard(Post post) {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-padding: 24; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox textInfo = new VBox(8);
        HBox.setHgrow(textInfo, Priority.ALWAYS);

        Label titleLabel = new Label(post.getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1a1f36;");
        titleLabel.setWrapText(true);

        String contentPreview = post.getContenu();
        if (contentPreview.length() > 150) {
            contentPreview = contentPreview.substring(0, 150) + "...";
        }
        Label contentLabel = new Label(contentPreview);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #525f7f;");
        contentLabel.setWrapText(true);

        HBox badges = new HBox(8);
        Label statusBadge = new Label(post.isActive() ? "Actif" : "Inactif");
        String badgeActiveStyle =(post.isActive() ? "-fx-background-color: #d1fae5; -fx-text-fill: #10b981; " : "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; ") + "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: 600; -fx-font-size: 12px;";
        statusBadge.setStyle(badgeActiveStyle);

        Label typeBadge = new Label(post.getTypePost() == 1 ? "Annonce" : "Événement");

        String badgeTypeStyle = ( post.getTypePost() == 1 ?  "-fx-background-color: #e0e7ff; -fx-text-fill: #6366f1;" :  "-fx-background-color: #fed7aa; -fx-text-fill: #ea580c;") +"-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: 600; -fx-font-size: 12px;";
        typeBadge.setStyle (badgeTypeStyle);

        badges.getChildren().addAll(statusBadge, typeBadge);
        textInfo.getChildren().addAll(titleLabel, contentLabel, badges);

        HBox actions = new HBox(8);

        Button btnEdit = new Button("Modifier");
        btnEdit.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #1a1f36; -fx-padding: 8 16; -fx-background-radius: 6; -fx-font-weight: 600; -fx-cursor: hand; -fx-border-color: #dfe3e8; -fx-border-width: 1.5; -fx-border-radius: 6;");
        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #1a1f36; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 6; -fx-font-weight: 600; -fx-cursor: hand; -fx-border-color: #1a1f36; -fx-border-width: 1.5; -fx-border-radius: 6;"));
        btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #1a1f36; -fx-padding: 8 16; -fx-background-radius: 6; -fx-font-weight: 600; -fx-cursor: hand; -fx-border-color: #dfe3e8; -fx-border-width: 1.5; -fx-border-radius: 6;"));
        btnEdit.setOnAction(e -> handleEditPost(post));

        Button btnDelete = new Button("Supprimer");
        btnDelete.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-padding: 8 16; -fx-background-radius: 6; -fx-font-weight: 600; -fx-cursor: hand; -fx-border-color: #fecaca; -fx-border-width: 1.5; -fx-border-radius: 6;");
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 6; -fx-font-weight: 600; -fx-cursor: hand; -fx-border-color: #ef4444; -fx-border-width: 1.5; -fx-border-radius: 6;"));
        btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-padding: 8 16; -fx-background-radius: 6; -fx-font-weight: 600; -fx-cursor: hand; -fx-border-color: #fecaca; -fx-border-width: 1.5; -fx-border-radius: 6;"));
        btnDelete.setOnAction(e -> handleDeletePost(post));

        actions.getChildren().addAll(btnEdit, btnDelete);
        header.getChildren().addAll(textInfo, actions);

        card.getChildren().add(header);

        if (post.getTypePost() == 2 && (post.getDateEvenement() != null || post.getLieu() != null || post.getCapaciteMax() != null)) {
            VBox eventInfo = new VBox(8);
            eventInfo.setStyle("-fx-background-color: #f8fafc; -fx-padding: 16; -fx-background-radius: 8;");

            if (post.getDateEvenement() != null) {
                Label dateLabel = new Label("Date: " + post.getDateEvenement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #525f7f; -fx-font-weight: 600;");
                eventInfo.getChildren().add(dateLabel);
            }

            if (post.getLieu() != null && !post.getLieu().isEmpty()) {
                Label lieuLabel = new Label("Lieu: " + post.getLieu());
                lieuLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #525f7f; -fx-font-weight: 600;");
                eventInfo.getChildren().add(lieuLabel);
            }

            if (post.getCapaciteMax() != null) {
                Label capaciteLabel = new Label("Capacité: " + post.getCapaciteMax() + " personnes");
                capaciteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #525f7f; -fx-font-weight: 600;");
                eventInfo.getChildren().add(capaciteLabel);
            }

            card.getChildren().add(eventInfo);
        }

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
            if (post.getDateEvenement() != null) dateEvenement.setValue(post.getDateEvenement());
            if (post.getDateFinEvenement() != null) dateFinEvenement.setValue(post.getDateFinEvenement());
            if (post.getLieu() != null) txtLieu.setText(post.getLieu());
            if (post.getCapaciteMax() != null) txtCapaciteMax.setText(String.valueOf(post.getCapaciteMax()));
        }

        formTitle.setText("Modifier l'Annonce");
        btnSave.setVisible(false);
        btnSave.setManaged(false);
        btnUpdate.setVisible(true);
        btnUpdate.setManaged(true);
        formContainer.setVisible(true);
        formContainer.setManaged(true);

        if (post.getTypePost() == 2) {
            refreshImageGallery(post.getIdPost());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        Post post = new Post();
        populatePostFromForm(post);
        post.setDateCreation(LocalDateTime.now());
        post.setUtilisateurId(1);

        try {
            postCRUD.ajouter(post);
            showSuccess("Succès", "Annonce ajoutée avec succès!");
            handleCancelForm();
            refreshPosts();
            loadStatistiques();
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'ajouter l'annonce: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedPost == null || !validateForm()) return;

        populatePostFromForm(selectedPost);

        try {
            postCRUD.modifier(selectedPost);
            showSuccess("Succès", "Annonce modifiée avec succès!");
            handleCancelForm();
            refreshPosts();
            loadStatistiques();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de modifier l'annonce: " + e.getMessage());
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
            post.setCapaciteMax(txtCapaciteMax.getText().trim().isEmpty() ? null : Integer.parseInt(txtCapaciteMax.getText().trim()));
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
        alert.setContentText("Êtes-vous sûr de vouloir supprimer: " + post.getTitre() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    postCRUD.supprimer(post.getIdPost());
                    showSuccess("Succès", "Annonce supprimée!");
                    refreshPosts();
                    loadStatistiques();
                } catch (SQLException e) {
                    showError("Erreur", "Impossible de supprimer: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCancelForm() {
        clearForm();
        clearErrors();
        if (imageGalleryContainer != null) imageGalleryContainer.getChildren().clear();
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
        }

        if (txtContenu.getText() == null || txtContenu.getText().trim().isEmpty()) {
            showFieldError(lblContenuError, "Le contenu est obligatoire");
            isValid = false;
        }

        if (comboTypePost.getValue() == null) {
            showFieldError(lblTypeError, "Sélectionnez un type");
            isValid = false;
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

    private void loadCommentsByPost() {
        if (commentsContainer == null) return;
        commentsContainer.getChildren().clear();

        try {
            List<Post> posts = postCRUD.afficher();

            for (Post post : posts) {
                List<Commentaire> comments = commentaireCRUD.getByPost(post.getIdPost());

                if (!comments.isEmpty()) {
                    VBox postCard = createPostCommentsCard(post, comments);
                    commentsContainer.getChildren().add(postCard);
                }
            }

            if (commentsContainer.getChildren().isEmpty()) {
                Label empty = new Label("Aucun commentaire pour le moment");
                empty.setStyle("-fx-text-fill: #697386; -fx-font-size: 14px;");
                commentsContainer.getChildren().add(empty);
            }
        } catch (SQLException e) {
            showError("Erreur", "Erreur chargement commentaires: " + e.getMessage());
        }
    }

    private VBox createPostCommentsCard(Post post, List<Commentaire> comments) {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-padding: 24; -fx-background-radius: 12;");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox postInfo = new VBox(4);
        HBox.setHgrow(postInfo, Priority.ALWAYS);

        Label postTitle = new Label(post.getTitre());
        postTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1a1f36;");

        Label commentCount = new Label(comments.size() + " commentaire" + (comments.size() > 1 ? "s" : ""));
        commentCount.setStyle("-fx-font-size: 13px; -fx-text-fill: #697386;");

        postInfo.getChildren().addAll(postTitle, commentCount);
        header.getChildren().add(postInfo);

        VBox commentsList = new VBox(12);
        for (Commentaire c : comments) {
            HBox commentBox = new HBox(12);
            commentBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12; -fx-background-radius: 8;");
            commentBox.setAlignment(Pos.TOP_LEFT);

            VBox commentContent = new VBox(6);
            HBox.setHgrow(commentContent, Priority.ALWAYS);

            HBox commentHeader = new HBox(8);
            commentHeader.setAlignment(Pos.CENTER_LEFT);

            Label userName = new Label("Utilisateur #" + c.getUtilisateurId());
            userName.setStyle("-fx-font-weight: 600; -fx-text-fill: #1a1f36; -fx-font-size: 13px;");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            Label date = new Label(c.getDateCommentaire().format(formatter));
            date.setStyle("-fx-text-fill: #8898aa; -fx-font-size: 12px;");

            commentHeader.getChildren().addAll(userName, date);

            Label text = new Label(c.getContenu());
            text.setStyle("-fx-text-fill: #525f7f; -fx-font-size: 14px;");
            text.setWrapText(true);

            commentContent.getChildren().addAll(commentHeader, text);

            Button btnDelete = new Button("Supprimer");
            btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-weight: 600; -fx-cursor: hand; -fx-font-size: 12px;");
            btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-font-weight: 600; -fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 4;"));
            btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-weight: 600; -fx-cursor: hand; -fx-font-size: 12px;"));
            btnDelete.setOnAction(e -> deleteComment(c.getIdCommentaire()));

            commentBox.getChildren().addAll(commentContent, btnDelete);
            commentsList.getChildren().add(commentBox);
        }

        card.getChildren().addAll(header, commentsList);
        return card;
    }

    private void deleteComment(int commentId) {
        try {
            commentaireCRUD.supprimer(commentId);
            loadCommentsByPost();
            loadStatistiques();
        } catch (SQLException e) {
            showError("Erreur", "Erreur suppression: " + e.getMessage());
        }
    }

    private void loadStatistiques() {
        try {
            PostCRUD.StatistiquesGlobales stats = postCRUD.getStatistiquesGlobales();

            if (lblTotalPosts != null) lblTotalPosts.setText(String.valueOf(stats.totalPosts));
            if (lblTotalComments != null) lblTotalComments.setText(String.valueOf(stats.totalCommentaires));
            if (lblTotalLikes != null) lblTotalLikes.setText(String.valueOf(stats.totalLikes));

            loadPieChart(stats);
            loadBarChart();
            loadLineChart();
        } catch (SQLException e) {
            showError("Erreur", "Erreur statistiques: " + e.getMessage());
        }
    }

    private void loadPieChart(PostCRUD.StatistiquesGlobales stats) {
        if (chartPostTypes == null) return;

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Annonces", stats.totalAnnonces),
                new PieChart.Data("Événements", stats.totalEvenements)
        );

        chartPostTypes.setData(pieData);
        chartPostTypes.setLabelsVisible(true);
    }

    private void loadBarChart() {
        if (chartEngagement == null) return;

        try {
            List<Post> posts = postCRUD.afficher();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Engagement");

            for (int i = 0; i < Math.min(5, posts.size()); i++) {
                Post p = posts.get(i);
                int likes = likeCRUD.countByPost(p.getIdPost());
                int comments = commentaireCRUD.countByPost(p.getIdPost());
                int total = likes + comments;

                String shortTitle = p.getTitre().length() > 15 ?
                        p.getTitre().substring(0, 15) + "..." : p.getTitre();

                series.getData().add(new XYChart.Data<>(shortTitle, total));
            }

            chartEngagement.getData().clear();
            chartEngagement.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadLineChart() {
        if (chartActivity == null) return;

        XYChart.Series<String, Number> postsSeries = new XYChart.Series<>();
        postsSeries.setName("Posts");

        XYChart.Series<String, Number> likesSeries = new XYChart.Series<>();
        likesSeries.setName("Likes");

        XYChart.Series<String, Number> commentsSeries = new XYChart.Series<>();
        commentsSeries.setName("Commentaires");

        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"};
        Random rand = new Random();

        for (String month : months) {
            postsSeries.getData().add(new XYChart.Data<>(month, rand.nextInt(20) + 5));
            likesSeries.getData().add(new XYChart.Data<>(month, rand.nextInt(50) + 10));
            commentsSeries.getData().add(new XYChart.Data<>(month, rand.nextInt(30) + 5));
        }

        chartActivity.getData().clear();
        chartActivity.getData().addAll(postsSeries, likesSeries, commentsSeries);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}