package controller.annonces;

import entity.Commentaire;
import entity.Like;
import entity.Participation;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import service.CommentaireCRUD;
import service.LikeCRUD;
import service.ParticipationCRUD;
import service.PostCRUD;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AnnonceDisplayController {

    @FXML private VBox sidebar;
    @FXML private Button btnHome;
    @FXML private Button btnFormation;
    @FXML private Button btnDemande;
    @FXML private Button btnEmployer;
    @FXML private Button btnProjet;
    @FXML private Button btnOffre;
    @FXML private BorderPane rootPane;
    @FXML private VBox postsContainer;
    @FXML private ComboBox<String> comboFilter;

    private boolean isExpanded = false;
    private PostCRUD postCRUD = new PostCRUD();
    private CommentaireCRUD commentaireCRUD = new CommentaireCRUD();
    private LikeCRUD likeCRUD = new LikeCRUD();
    private ParticipationCRUD participationCRUD = new ParticipationCRUD();
    private int currentUserId = 1;

    @FXML
    public void initialize() {
        comboFilter.getItems().addAll("Tout voir", "Annonces", "Événements");
        comboFilter.setValue("Tout voir");
        comboFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshPosts());
        refreshPosts();
    }

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

    private void refreshPosts() {
        postsContainer.getChildren().clear();

        try {
            List<Post> posts = postCRUD.afficher();
            String filter = comboFilter.getValue();

            for (Post post : posts) {
                if (shouldDisplayPost(post, filter)) {
                    VBox postCard = createProfessionalPostCard(post);
                    postsContainer.getChildren().add(postCard);
                }
            }

            if (postsContainer.getChildren().isEmpty()) {
                VBox emptyState = createEmptyState();
                postsContainer.getChildren().add(emptyState);
            }
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les annonces: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createEmptyState() {
        VBox emptyBox = new VBox(16);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setStyle("-fx-padding: 80; -fx-background-color: white; -fx-background-radius: 12;");

        Label icon = new Label("📋");
        icon.setStyle("-fx-font-size: 64px; -fx-opacity: 0.3;");

        Label message = new Label("Aucune annonce pour le moment");
        message.setStyle("-fx-font-size: 16px; -fx-font-weight: 500; -fx-text-fill: #697386;");

        emptyBox.getChildren().addAll(icon, message);
        return emptyBox;
    }

    private boolean shouldDisplayPost(Post post, String filter) {
        switch (filter) {
            case "Annonces": return post.getTypePost() == 1;
            case "Événements": return post.getTypePost() == 2;
            default: return true;
        }
    }

    private VBox createProfessionalPostCard(Post post) {
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 16, 0, 0, 4); " +
                        "-fx-border-color: #eaeef3; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12;"
        );

        HBox header = createPostHeader(post);
        VBox content = createPostContent(post);

        card.getChildren().addAll(header, content);

        if (post.getTypePost() == 2) {
            VBox eventDetails = createEventDetails(post);
            card.getChildren().add(eventDetails);
        }

        HBox statsBar = createStatsBar(post);
        card.getChildren().add(statsBar);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #eaeef3;");
        card.getChildren().add(separator);

        HBox actionBar = createActionBar(post);
        card.getChildren().add(actionBar);

        VBox commentsSection = createCommentsSection(post);
        card.getChildren().add(commentsSection);

        return card;
    }

    private HBox createPostHeader(Post post) {
        HBox header = new HBox(12);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(48, 48);
        avatar.setMinSize(48, 48);
        avatar.setMaxSize(48, 48);
        avatar.setStyle(
                "-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                        "-fx-background-radius: 24;"
        );

        Label initials = new Label("RH");
        initials.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 700;");
        avatar.getChildren().add(initials);

        VBox userInfo = new VBox(4);
        HBox.setHgrow(userInfo, Priority.ALWAYS);

        Label companyName = new Label("Ressources Humaines");
        companyName.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #1a1f36;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy à HH:mm");
        Label postTime = new Label(post.getDateCreation().format(formatter));
        postTime.setStyle("-fx-font-size: 13px; -fx-text-fill: #8898aa;");

        userInfo.getChildren().addAll(companyName, postTime);

        Label typeBadge = new Label(post.getTypePost() == 1 ? "Annonce" : "Événement");
        typeBadge.setStyle(
                "-fx-background-color: " + (post.getTypePost() == 1 ? "#f0f4ff" : "#fff7ed") + "; " +
                        "-fx-text-fill: " + (post.getTypePost() == 1 ? "#3b82f6" : "#f59e0b") + "; " +
                        "-fx-padding: 6 14; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 600;"
        );

        header.getChildren().addAll(avatar, userInfo, typeBadge);
        return header;
    }

    private VBox createPostContent(Post post) {
        VBox content = new VBox(12);
        content.setPadding(new Insets(0, 24, 20, 24));

        Label title = new Label(post.getTitre());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1a1f36;");
        title.setWrapText(true);

        Label contentText = new Label(post.getContenu());
        contentText.setStyle("-fx-font-size: 15px; -fx-text-fill: #525f7f; -fx-line-spacing: 4px;");
        contentText.setWrapText(true);

        content.getChildren().addAll(title, contentText);
        return content;
    }

    private VBox createEventDetails(Post post) {
        VBox eventBox = new VBox(12);
        eventBox.setPadding(new Insets(16, 24, 16, 24));
        eventBox.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 1 0 0 0;"
        );

        if (post.getDateEvenement() != null) {
            HBox dateRow = createInfoRow("📅", "Date", post.getDateEvenement().toString());
            eventBox.getChildren().add(dateRow);
        }

        if (post.getLieu() != null && !post.getLieu().isEmpty()) {
            HBox lieuRow = createInfoRow("📍", "Lieu", post.getLieu());
            eventBox.getChildren().add(lieuRow);
        }

        if (post.getCapaciteMax() != null) {
            HBox capaciteRow = createInfoRow("👥", "Capacité", post.getCapaciteMax() + " places");
            eventBox.getChildren().add(capaciteRow);
        }

        return eventBox;
    }

    private HBox createInfoRow(String emoji, String label, String value) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 18px;");

        VBox textBox = new VBox(2);
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 12px; -fx-text-fill: #8898aa; -fx-font-weight: 600;");

        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 14px; -fx-text-fill: #1a1f36; -fx-font-weight: 500;");

        textBox.getChildren().addAll(labelText, valueText);
        row.getChildren().addAll(icon, textBox);

        return row;
    }

    private HBox createStatsBar(Post post) {
        HBox statsBar = new HBox(20);
        statsBar.setPadding(new Insets(12, 24, 12, 24));
        statsBar.setAlignment(Pos.CENTER_LEFT);

        try {
            int likesCount = likeCRUD.countByPost(post.getIdPost());
            int commentsCount = commentaireCRUD.countByPost(post.getIdPost());

            if (likesCount > 0) {
                HBox likeBox = new HBox(6);
                likeBox.setAlignment(Pos.CENTER_LEFT);

                SVGPath heart = createHeartIcon();
                heart.setFill(Color.web("#ef4444"));

                Label likeCount = new Label(String.valueOf(likesCount));
                likeCount.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #525f7f;");
                likeBox.getChildren().addAll(heart, likeCount);
                statsBar.getChildren().add(likeBox);
            }

            if (commentsCount > 0) {
                Label commentCount = new Label(commentsCount + " commentaire" + (commentsCount > 1 ? "s" : ""));
                commentCount.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #525f7f;");
                statsBar.getChildren().add(commentCount);
            }

            if (post.getTypePost() == 2) {
                int participantsCount = participationCRUD.countByPost(post.getIdPost());
                if (participantsCount > 0) {
                    Label participantCount = new Label(participantsCount + " participant" + (participantsCount > 1 ? "s" : ""));
                    participantCount.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #525f7f;");
                    statsBar.getChildren().add(participantCount);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statsBar;
    }

    private HBox createActionBar(Post post) {
        HBox actionBar = new HBox(0);
        actionBar.setPadding(new Insets(8, 16, 8, 16));
        actionBar.setAlignment(Pos.CENTER);

        Button likeBtn = createActionButton("J'aime", post, "like");
        Button commentBtn = createActionButton("Commenter", post, "comment");

        actionBar.getChildren().addAll(likeBtn, commentBtn);

        if (post.getTypePost() == 2) {
            Button interestedBtn = createActionButton("Intéressé", post, "interested");
            Button goingBtn = createActionButton("Je participe", post, "going");
            actionBar.getChildren().addAll(interestedBtn, goingBtn);
        }

        return actionBar;
    }

    private Button createActionButton(String text, Post post, String action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS);

        String baseStyle =
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #525f7f; " +
                        "-fx-font-weight: 600; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 10 16; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 6;";

        btn.setStyle(baseStyle);

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-text-fill: #1a1f36; " +
                        "-fx-font-weight: 600; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 10 16; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 6;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        btn.setOnAction(e -> handleAction(action, post));

        try {
            if ("like".equals(action)) {
                boolean hasLiked = likeCRUD.hasLiked(currentUserId, post.getIdPost());
                if (hasLiked) {
                    btn.setStyle(
                            "-fx-background-color: #fef2f2; " +
                                    "-fx-text-fill: #ef4444; " +
                                    "-fx-font-weight: 700; " +
                                    "-fx-font-size: 14px; " +
                                    "-fx-padding: 10 16; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-background-radius: 6;"
                    );
                }
            } else if ("interested".equals(action) || "going".equals(action)) {
                String statusType = "interested".equals(action) ? "INTERESTED" : "GOING";
                boolean hasParticipated = participationCRUD.hasUserParticipated(currentUserId, post.getIdPost(), statusType);
                if (hasParticipated) {
                    btn.setStyle(
                            "-fx-background-color: #eff6ff; " +
                                    "-fx-text-fill: #3b82f6; " +
                                    "-fx-font-weight: 700; " +
                                    "-fx-font-size: 14px; " +
                                    "-fx-padding: 10 16; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-background-radius: 6;"
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return btn;
    }

    private VBox createCommentsSection(Post post) {
        VBox commentsSection = new VBox(0);

        try {
            List<Commentaire> allComments = commentaireCRUD.getByPost(post.getIdPost());
            int commentCount = allComments.size();

            if (commentCount == 0) {
                return createCommentInput(post, commentsSection);
            }

            VBox collapsedView = new VBox(16);
            collapsedView.setPadding(new Insets(16, 24, 16, 24));
            collapsedView.setStyle("-fx-background-color: #fafbfc; -fx-border-color: #eaeef3; -fx-border-width: 1 0 0 0;");

            Button viewCommentsBtn = new Button("Voir les " + commentCount + " commentaire" + (commentCount > 1 ? "s" : ""));
            viewCommentsBtn.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: #525f7f; " +
                            "-fx-font-weight: 600; " +
                            "-fx-font-size: 14px; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 8 0;"
            );

            viewCommentsBtn.setOnAction(e -> {
                collapsedView.setVisible(false);
                collapsedView.setManaged(false);
                VBox expandedView = createExpandedComments(post, allComments);
                commentsSection.getChildren().add(expandedView);
            });

            collapsedView.getChildren().add(viewCommentsBtn);
            commentsSection.getChildren().add(collapsedView);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return createCommentInput(post, commentsSection);
    }

    private VBox createCommentInput(Post post, VBox commentsSection) {
        VBox inputSection = new VBox(0);
        inputSection.setPadding(new Insets(16, 24, 20, 24));
        inputSection.setStyle("-fx-background-color: white;");

        HBox commentInput = new HBox(12);
        commentInput.setAlignment(Pos.CENTER);

        TextField txtComment = new TextField();
        txtComment.setPromptText("Ajouter un commentaire...");
        txtComment.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-background-radius: 24; " +
                        "-fx-padding: 12 20; " +
                        "-fx-font-size: 14px; " +
                        "-fx-border-color: transparent; " +
                        "-fx-text-fill: #1a1f36;"
        );
        HBox.setHgrow(txtComment, Priority.ALWAYS);

        txtComment.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                txtComment.setStyle(
                        "-fx-background-color: white; " +
                                "-fx-background-radius: 24; " +
                                "-fx-padding: 12 20; " +
                                "-fx-font-size: 14px; " +
                                "-fx-border-color: #3b82f6; " +
                                "-fx-border-width: 2; " +
                                "-fx-border-radius: 24; " +
                                "-fx-text-fill: #1a1f36;"
                );
            } else {
                txtComment.setStyle(
                        "-fx-background-color: #f8fafc; " +
                                "-fx-background-radius: 24; " +
                                "-fx-padding: 12 20; " +
                                "-fx-font-size: 14px; " +
                                "-fx-border-color: transparent; " +
                                "-fx-text-fill: #1a1f36;"
                );
            }
        });

        Button btnSendComment = new Button("Publier");
        btnSendComment.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 24; " +
                        "-fx-padding: 12 24; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: 700; " +
                        "-fx-font-size: 14px;"
        );

        btnSendComment.setOnMouseEntered(e -> btnSendComment.setStyle(
                "-fx-background-color: #2563eb; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 24; " +
                        "-fx-padding: 12 24; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: 700; " +
                        "-fx-font-size: 14px;"
        ));

        btnSendComment.setOnMouseExited(e -> btnSendComment.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 24; " +
                        "-fx-padding: 12 24; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: 700; " +
                        "-fx-font-size: 14px;"
        ));

        btnSendComment.setOnAction(e -> {
            String comment = txtComment.getText().trim();
            if (!comment.isEmpty()) {
                addComment(post.getIdPost(), comment);
                txtComment.clear();
                refreshPosts();
            }
        });

        commentInput.getChildren().addAll(txtComment, btnSendComment);
        inputSection.getChildren().add(commentInput);

        commentsSection.getChildren().add(inputSection);
        return commentsSection;
    }

    private VBox createExpandedComments(Post post, List<Commentaire> comments) {
        VBox expandedView = new VBox(12);
        expandedView.setPadding(new Insets(16, 24, 16, 24));
        expandedView.setStyle("-fx-background-color: #fafbfc; -fx-border-color: #eaeef3; -fx-border-width: 1 0 0 0;");

        Button hideBtn = new Button("Masquer les commentaires");
        hideBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #525f7f; " +
                        "-fx-font-weight: 600; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 0 0 12 0;"
        );

        hideBtn.setOnAction(e -> refreshPosts());

        expandedView.getChildren().add(hideBtn);

        for (Commentaire c : comments) {
            VBox commentBox = createCommentBox(c);
            expandedView.getChildren().add(commentBox);
        }

        return expandedView;
    }

    private VBox createCommentBox(Commentaire comment) {
        VBox commentBox = new VBox(8);
        commentBox.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 16; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #eaeef3; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12;"
        );

        HBox commentHeader = new HBox(10);
        commentHeader.setAlignment(Pos.CENTER_LEFT);

        StackPane miniAvatar = new StackPane();
        miniAvatar.setPrefSize(32, 32);
        miniAvatar.setMinSize(32, 32);
        miniAvatar.setMaxSize(32, 32);
        miniAvatar.setStyle(
                "-fx-background-color: linear-gradient(135deg, #94a3b8 0%, #64748b 100%); " +
                        "-fx-background-radius: 16;"
        );

        Label initials = new Label("U" + comment.getUtilisateurId());
        initials.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 700;");
        miniAvatar.getChildren().add(initials);

        VBox userInfo = new VBox(2);
        Label userName = new Label("Utilisateur #" + comment.getUtilisateurId());
        userName.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1a1f36;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM à HH:mm");
        Label commentTime = new Label(comment.getDateCommentaire().format(formatter));
        commentTime.setStyle("-fx-font-size: 12px; -fx-text-fill: #8898aa;");

        userInfo.getChildren().addAll(userName, commentTime);
        commentHeader.getChildren().addAll(miniAvatar, userInfo);

        Label commentText = new Label(comment.getContenu());
        commentText.setStyle("-fx-font-size: 14px; -fx-text-fill: #525f7f; -fx-line-spacing: 2px;");
        commentText.setWrapText(true);

        commentBox.getChildren().addAll(commentHeader, commentText);
        return commentBox;
    }

    private SVGPath createHeartIcon() {
        SVGPath heart = new SVGPath();
        heart.setContent("M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z");
        heart.setScaleX(0.8);
        heart.setScaleY(0.8);
        return heart;
    }

    private void handleAction(String action, Post post) {
        try {
            switch (action) {
                case "like":
                    toggleLike(post.getIdPost());
                    break;
                case "interested":
                    toggleParticipation(post.getIdPost(), "INTERESTED");
                    break;
                case "going":
                    toggleParticipation(post.getIdPost(), "GOING");
                    break;
            }
            refreshPosts();
        } catch (SQLException e) {
            showError("Erreur", "Action impossible: " + e.getMessage());
        }
    }

    private void toggleLike(int postId) throws SQLException {
        likeCRUD.toggleLike(currentUserId, postId);
    }

    private void toggleParticipation(int postId, String status) throws SQLException {
        boolean hasParticipated = participationCRUD.hasUserParticipated(currentUserId, postId, status);

        if (hasParticipated) {
            Participation p = participationCRUD.getByUserAndPost(currentUserId, postId);
            if (p != null) {
                participationCRUD.supprimer(p.getIdParticipation());
            }
        } else {
            Participation existing = participationCRUD.getByUserAndPost(currentUserId, postId);
            if (existing != null) {
                participationCRUD.updateStatut(currentUserId, postId, status);
            } else {
                Participation p = new Participation();
                p.setUtilisateurId(currentUserId);
                p.setpostId(postId);
                p.setStatutParticipation(status);
                p.setDateAction(LocalDateTime.now());
                participationCRUD.ajouter(p);
            }
        }
    }

    private void addComment(int postId, String contenu) {
        try {
            Commentaire comment = new Commentaire();
            comment.setPostId(postId);
            comment.setUtilisateurId(currentUserId);
            comment.setContenu(contenu);
            comment.setDateCommentaire(LocalDateTime.now());
            commentaireCRUD.ajouter(comment);
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'ajouter le commentaire: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}