package controller.annonces;

import entity.Post;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import service.PostCRUD;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AnnonceController {

    @FXML
    private TextField txtTitre;
    @FXML
    private TextArea txtContenu;
    @FXML
    private ComboBox<String> comboTypePost; // Annonce / Événement
    @FXML
    private VBox postsContainer;

    private PostCRUD postCRUD = new PostCRUD();
    private Post selectedPost = null;

    @FXML
    public void initialize() {
        comboTypePost.getItems().addAll("Annonce", "Événement");
        refreshPosts();
    }

    private void refreshPosts() {
        postsContainer.getChildren().clear();

        try {
            List<Post> posts = postCRUD.afficher();
            for (Post p : posts) {
                addPostCard(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addPostCard(Post p) {
        VBox card = new VBox();
        card.getStyleClass().add("glass-card");

        Label titre = new Label(p.getTitre());
        Label contenu = new Label(p.getContenu());

        String type = p.getTypePost() == 1 ? "Annonce" : "Événement";
        Label badge = new Label(type);

        card.getChildren().addAll(titre, contenu, badge);

        card.setOnMouseClicked(e -> {
            selectedPost = p;
            txtTitre.setText(p.getTitre());
            txtContenu.setText(p.getContenu());
            comboTypePost.setValue(type);
        });

        postsContainer.getChildren().add(card);
    }

    @FXML
    private void handleSave() {
        Post p = new Post();
        p.setTitre(txtTitre.getText());
        p.setContenu(txtContenu.getText());
        p.setTypePost(comboTypePost.getValue().equals("Annonce") ? 1 : 2);
        p.setDateCreation(LocalDateTime.now());
        p.setUtilisateurId(1);
        p.setActive(true);

        try {
            postCRUD.ajouter(p);
            refreshPosts();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedPost == null) return;

        selectedPost.setTitre(txtTitre.getText());
        selectedPost.setContenu(txtContenu.getText());

        try {
            postCRUD.modifier(selectedPost);
            refreshPosts();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedPost == null) return;

        try {
            postCRUD.supprimer(selectedPost.getIdPost());
            refreshPosts();
            selectedPost = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}

