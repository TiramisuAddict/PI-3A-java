package controller.annonces;

import entity.Commentaire;
import entity.Like;
import entity.Participation;
import entity.EventImage;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import service.CommentaireCRUD;
import service.EventImageCRUD;
import service.LikeCRUD;
import service.ParticipationCRUD;
import service.PostCRUD;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private EventImageCRUD eventImageCRUD = new EventImageCRUD();
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
                        "-fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1); " +
                        "-fx-border-color: #e0ddd8; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8;"
        );

        HBox header = createPostHeader(post);
        VBox content = createPostContent(post);

        card.getChildren().addAll(header, content);

        if (post.getTypePost() == 2) {
            VBox eventDetails = createEventDetails(post);
            card.getChildren().add(eventDetails);

            VBox photoGallery = createPhotoGallery(post);
            if (photoGallery != null) {
                card.getChildren().add(photoGallery);
            }
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
        header.setPadding(new Insets(16, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar with color based on post type
        StackPane avatar = new StackPane();
        avatar.setPrefSize(48, 48);
        avatar.setMinSize(48, 48);
        avatar.setMaxSize(48, 48);
        String avatarColor = post.getTypePost() == 1
                ? "linear-gradient(135deg, #0a66c2 0%, #0077b5 100%)"
                : "linear-gradient(135deg, #e67e22 0%, #f39c12 100%)";
        avatar.setStyle(
                "-fx-background-color: " + avatarColor + "; " +
                        "-fx-background-radius: 24;"
        );

        Label initials = new Label(post.getTypePost() == 1 ? "RH" : "📅");
        initials.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700;");
        avatar.getChildren().add(initials);

        // User info column
        VBox userInfo = new VBox(2);
        HBox.setHgrow(userInfo, Priority.ALWAYS);

        Label companyName = new Label("Ressources Humaines");
        companyName.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #000000e6;");

        // Relative time (LinkedIn style)
        String relativeTime = getRelativeTime(post.getDateCreation());
        HBox timeRow = new HBox(4);
        timeRow.setAlignment(Pos.CENTER_LEFT);
        Label postTime = new Label(relativeTime);
        postTime.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        Label globe = new Label("•  🌐");
        globe.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
        timeRow.getChildren().addAll(postTime, globe);

        userInfo.getChildren().addAll(companyName, timeRow);

        // Type badge — small and subtle
        Label typeBadge = new Label(post.getTypePost() == 1 ? "Annonce" : "Événement");
        typeBadge.setStyle(
                "-fx-background-color: " + (post.getTypePost() == 1 ? "#e8f0fe" : "#fef3e2") + "; " +
                        "-fx-text-fill: " + (post.getTypePost() == 1 ? "#0a66c2" : "#d4700a") + "; " +
                        "-fx-padding: 4 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: 700;"
        );

        header.getChildren().addAll(avatar, userInfo, typeBadge);
        return header;
    }

    private String getRelativeTime(LocalDateTime dateCreation) {
        long minutes = java.time.Duration.between(dateCreation, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "À l'instant";
        if (minutes < 60) return minutes + " min";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h";
        long days = hours / 24;
        if (days < 7) return days + "j";
        long weeks = days / 7;
        if (weeks < 4) return weeks + " sem.";
        long months = days / 30;
        if (months < 12) return months + " mois";
        return (months / 12) + " an" + (months / 12 > 1 ? "s" : "");
    }

    private VBox createPostContent(Post post) {
        VBox content = new VBox(8);
        content.setPadding(new Insets(0, 16, 16, 16));

        Label title = new Label(post.getTitre());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #000000e6;");
        title.setWrapText(true);

        Label contentText = new Label(post.getContenu());
        contentText.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000cc; -fx-line-spacing: 3px;");
        contentText.setWrapText(true);

        content.getChildren().addAll(title, contentText);
        return content;
    }

    private VBox createEventDetails(Post post) {
        VBox eventBox = new VBox(10);
        eventBox.setPadding(new Insets(12, 16, 12, 16));
        eventBox.setStyle(
                "-fx-background-color: #f9f9f9; " +
                        "-fx-border-color: #e0ddd8; " +
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

    private VBox createPhotoGallery(Post post) {
        try {
            List<EventImage> images = eventImageCRUD.getByPost(post.getIdPost());
            if (images.isEmpty()) return null;

            VBox galleryBox = new VBox(0);
            galleryBox.setStyle("-fx-border-color: #e0ddd8; -fx-border-width: 1 0 0 0;");

            int total = images.size();

            if (total == 1) {
                StackPane pane = buildPhotoPane(images.get(0), 632, 380, null);
                if (pane != null) galleryBox.getChildren().add(pane);
            } else if (total == 2) {
                HBox row = new HBox(3);
                for (EventImage img : images) {
                    StackPane p = buildPhotoPane(img, 314, 300, null);
                    if (p != null) row.getChildren().add(p);
                }
                galleryBox.getChildren().add(row);
            } else {
                // Carousel
                galleryBox.getChildren().add(buildCarousel(images));
            }

            return galleryBox;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private StackPane buildCarousel(List<EventImage> images) {
        final int[] currentIndex = {0};
        int total = images.size();
        // Use 100% width of container — maxWidth is 632 but let it be flexible
        double W = 632, H = 360;

        StackPane carousel = new StackPane();
        carousel.setPrefSize(W, H);
        carousel.setMaxWidth(Double.MAX_VALUE);
        carousel.setPrefHeight(H);
        carousel.setMaxHeight(H);
        carousel.setMinHeight(H);
        carousel.setStyle("-fx-background-color: black;");

        // Main image view — stretch to fill
        ImageView iv = new ImageView();
        iv.setFitHeight(H);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        // Counter label top-right
        Label counter = new Label("1 / " + total);
        counter.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-font-weight: 600; -fx-padding: 4 10; -fx-background-radius: 12;");
        StackPane.setAlignment(counter, Pos.TOP_RIGHT);
        StackPane.setMargin(counter, new Insets(10, 10, 0, 0));

        // Collect dots list for update
        List<javafx.scene.shape.Circle> dotList = new ArrayList<>();

        // Load image helper
        Runnable loadImage = () -> {
            try {
                File f = new File(images.get(currentIndex[0]).getImagePath());
                if (f.exists()) {
                    iv.setImage(new Image(f.toURI().toString()));
                }
                counter.setText((currentIndex[0] + 1) + " / " + total);
                for (int i = 0; i < dotList.size(); i++) {
                    dotList.get(i).setFill(i == currentIndex[0]
                            ? javafx.scene.paint.Color.WHITE
                            : javafx.scene.paint.Color.color(1, 1, 1, 0.45));
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        };

        loadImage.run();

        // Left arrow button
        Button btnLeft = new Button("‹");
        styleArrowBtn(btnLeft);
        btnLeft.setOpacity(0);
        StackPane.setAlignment(btnLeft, Pos.CENTER_LEFT);
        StackPane.setMargin(btnLeft, new Insets(0, 0, 0, 8));

        // Right arrow button
        Button btnRight = new Button("›");
        styleArrowBtn(btnRight);
        btnRight.setOpacity(0);
        StackPane.setAlignment(btnRight, Pos.CENTER_RIGHT);
        StackPane.setMargin(btnRight, new Insets(0, 8, 0, 0));

        // Navigation logic
        btnLeft.setOnAction(e -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                loadImage.run();
            }
        });

        btnRight.setOnAction(e -> {
            if (currentIndex[0] < total - 1) {
                currentIndex[0]++;
                loadImage.run();
            }
        });

        // Show arrows on hover using opacity (avoids mouse-exit-on-child bug)
        carousel.setOnMouseEntered(e -> {
            btnLeft.setOpacity(currentIndex[0] > 0 ? 1 : 0);
            btnRight.setOpacity(currentIndex[0] < total - 1 ? 1 : 0);
        });
        carousel.setOnMouseExited(e -> {
            // Only hide if mouse truly left the carousel bounds
            javafx.geometry.Bounds bounds = carousel.localToScreen(carousel.getBoundsInLocal());
            if (bounds != null && !bounds.contains(e.getScreenX(), e.getScreenY())) {
                btnLeft.setOpacity(0);
                btnRight.setOpacity(0);
            }
        });

        // Dots
        HBox dots = new HBox(6);
        dots.setAlignment(Pos.CENTER);
        dots.setPickOnBounds(false);
        StackPane.setAlignment(dots, Pos.BOTTOM_CENTER);
        StackPane.setMargin(dots, new Insets(0, 0, 10, 0));

        for (int i = 0; i < total; i++) {
            javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(4);
            dot.setFill(i == 0
                    ? javafx.scene.paint.Color.WHITE
                    : javafx.scene.paint.Color.color(1, 1, 1, 0.45));
            dotList.add(dot);
            dots.getChildren().add(dot);
        }

        carousel.getChildren().addAll(iv, counter, btnLeft, btnRight, dots);
        return carousel;
    }

    private void styleArrowBtn(Button btn) {
        btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.9); " +
                        "-fx-text-fill: #222; " +
                        "-fx-font-size: 22px; " +
                        "-fx-font-weight: 700; " +
                        "-fx-background-radius: 20; " +
                        "-fx-min-width: 38px; -fx-max-width: 38px; " +
                        "-fx-min-height: 38px; -fx-max-height: 38px; " +
                        "-fx-cursor: hand; -fx-padding: 0 0 3 0;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: white; " +
                        "-fx-text-fill: #000; " +
                        "-fx-font-size: 22px; " +
                        "-fx-font-weight: 700; " +
                        "-fx-background-radius: 20; " +
                        "-fx-min-width: 38px; -fx-max-width: 38px; " +
                        "-fx-min-height: 38px; -fx-max-height: 38px; " +
                        "-fx-cursor: hand; -fx-padding: 0 0 3 0; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 1);"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.9); " +
                        "-fx-text-fill: #222; " +
                        "-fx-font-size: 22px; " +
                        "-fx-font-weight: 700; " +
                        "-fx-background-radius: 20; " +
                        "-fx-min-width: 38px; -fx-max-width: 38px; " +
                        "-fx-min-height: 38px; -fx-max-height: 38px; " +
                        "-fx-cursor: hand; -fx-padding: 0 0 3 0;"
        ));
    }

    private StackPane buildPhotoPane(EventImage img, double w, double h, String badge) {
        try {
            File f = new File(img.getImagePath());
            if (!f.exists()) return null;

            Image image = new Image(f.toURI().toString(), w, h, false, true);
            ImageView iv = new ImageView(image);
            iv.setFitWidth(w);
            iv.setFitHeight(h);
            iv.setPreserveRatio(false);

            StackPane pane = new StackPane(iv);
            pane.setPrefSize(w, h);
            pane.setMaxSize(w, h);
            pane.setMinSize(w, h);
            pane.setStyle("-fx-cursor: hand;");

            if (badge != null) {
                // Dark overlay
                javafx.scene.shape.Rectangle overlay = new javafx.scene.shape.Rectangle(w, h);
                overlay.setFill(javafx.scene.paint.Color.color(0, 0, 0, 0.5));

                Label badgeLabel = new Label(badge);
                badgeLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: white;");

                pane.getChildren().addAll(overlay, badgeLabel);
            }

            return pane;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
        HBox statsBar = new HBox(16);
        statsBar.setPadding(new Insets(8, 16, 8, 16));
        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.setStyle("-fx-border-color: #e0ddd8; -fx-border-width: 0 0 1 0;");

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

                if (post.getCapaciteMax() != null) {
                    int placesRestantes = post.getCapaciteMax() - participantsCount;
                    boolean isComplet = placesRestantes <= 0;

                    Label capaciteLabel = new Label(isComplet ? "COMPLET" : placesRestantes + " place" + (placesRestantes > 1 ? "s" : "") + " restante" + (placesRestantes > 1 ? "s" : ""));
                    capaciteLabel.setStyle(isComplet
                            ? "-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: white; -fx-background-color: #ef4444; -fx-padding: 3 10; -fx-background-radius: 10;"
                            : "-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #10b981; -fx-background-color: #d1fae5; -fx-padding: 3 10; -fx-background-radius: 10;");
                    statsBar.getChildren().add(capaciteLabel);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statsBar;
    }

    private HBox createActionBar(Post post) {
        HBox actionBar = new HBox(0);
        actionBar.setPadding(new Insets(4, 8, 4, 8));
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setStyle("-fx-border-color: #e0ddd8; -fx-border-width: 1 0 0 0;");

        Button likeBtn = createActionButton("👍  J'aime", post, "like");
        Button commentBtn = createActionButton("💬  Commenter", post, "comment");
        actionBar.getChildren().addAll(likeBtn, commentBtn);

        if (post.getTypePost() == 2) {
            Button interestedBtn = createActionButton("⭐  Intéressé", post, "interested");
            actionBar.getChildren().add(interestedBtn);

            boolean eventFull = false;
            if (post.getCapaciteMax() != null) {
                try {
                    boolean alreadyGoing = participationCRUD.hasUserParticipated(currentUserId, post.getIdPost(), "GOING");
                    if (!alreadyGoing) {
                        int participants = participationCRUD.countByPost(post.getIdPost());
                        eventFull = participants >= post.getCapaciteMax();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (eventFull) {
                Button complet = new Button("🚫  Complet");
                complet.setDisable(true);
                complet.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: #999999; " +
                                "-fx-font-weight: 600; " +
                                "-fx-font-size: 13px; " +
                                "-fx-padding: 10 16; " +
                                "-fx-cursor: default;"
                );
                actionBar.getChildren().add(complet);
            } else {
                Button goingBtn = createActionButton("✅  Je participe", post, "going");
                actionBar.getChildren().add(goingBtn);
            }
        }

        return actionBar;
    }

    private Button createActionButton(String text, Post post, String action) {
        Button btn = new Button(text);
        HBox.setHgrow(btn, Priority.ALWAYS);

        String base = "-fx-background-color: transparent; -fx-text-fill: #666666; " +
                "-fx-font-weight: 600; -fx-font-size: 13px; -fx-padding: 10 14; " +
                "-fx-cursor: hand; -fx-background-radius: 4;";
        String hover = "-fx-background-color: #f3f2ef; -fx-text-fill: #000000cc; " +
                "-fx-font-weight: 600; -fx-font-size: 13px; -fx-padding: 10 14; " +
                "-fx-cursor: hand; -fx-background-radius: 4;";
        String active = "-fx-background-color: #e8e6e1; -fx-text-fill: #0a66c2; " +
                "-fx-font-weight: 700; -fx-font-size: 13px; -fx-padding: 10 14; " +
                "-fx-cursor: hand; -fx-background-radius: 4;";

        // Check active state
        try {
            if ("like".equals(action) && likeCRUD.hasLiked(currentUserId, post.getIdPost())) {
                btn.setStyle(active);
            } else if (("interested".equals(action) || "going".equals(action))) {
                String statusType = "interested".equals(action) ? "INTERESTED" : "GOING";
                if (participationCRUD.hasUserParticipated(currentUserId, post.getIdPost(), statusType)) {
                    btn.setStyle(active);
                } else {
                    btn.setStyle(base);
                }
            } else {
                btn.setStyle(base);
            }
        } catch (SQLException e) {
            btn.setStyle(base);
        }

        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#0a66c2")) btn.setStyle(hover);
        });
        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#0a66c2")) btn.setStyle(base);
        });

        btn.setOnAction(e -> handleAction(action, post));

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
            if ("GOING".equals(status)) {
                Post post = postCRUD.getById(postId);
                if (post != null && post.getCapaciteMax() != null) {
                    int participants = participationCRUD.countByPost(postId);
                    if (participants >= post.getCapaciteMax()) {
                        showError("Événement complet", "Toutes les places sont déjà réservées.");
                        return;
                    }
                }
            }

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