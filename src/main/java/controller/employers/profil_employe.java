package controller.employers;

import entities.employe;
import entities.entreprise;
import entities.role;
import entities.session;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import service.employeCRUD;
import service.entrepriseCRUD;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class profil_employe implements Initializable {

    @FXML private VBox profileCard;
    @FXML private HBox profileRow;
    @FXML private StackPane avatarContainer;
    @FXML private Circle cercleArriere;
    @FXML private ImageView imgProfil;
    @FXML private VBox overlayPhoto;
    @FXML private Label lblNomComplet;
    @FXML private Label lblEmailDisplay;
    @FXML private Label lblRoleBadge;

    @FXML private TextField txtNomProfil;
    @FXML private TextField txtPrenomProfil;
    @FXML private TextField txtEmailProfil;
    @FXML private TextField txtTelProfil;
    @FXML private Label lblInfoMessage;

    @FXML private Label lblRoleInfo;
    @FXML private Label lblPosteReadonly;
    @FXML private Label lblDateReadonly;
    @FXML private Label lblEntrepriseNom;
    @FXML private Label lblEntrepriseVille;
    @FXML private Label lblEntreprisePays;

    private employeCRUD employeCRUD;
    private entrepriseCRUD entrepriseCrud;
    private employe employeConnecte;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final String PROFILES_DIR = System.getProperty("user.home");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            employeCRUD = new employeCRUD();
            entrepriseCrud = new entrepriseCRUD();
            chargerProfil();
            setupHoverEffects();
        } catch (Exception e) {
            afficherErreur("Erreur d'initialisation", e.getMessage());
        }
    }

    private void chargerProfil() throws SQLException {
        if (session.getEmploye() != null) {
            employeConnecte = session.getEmploye();
        } else if (session.getCompte() != null) {
            employeConnecte = employeCRUD.getById(session.getCompte().getId_employe());
            session.setEmploye(employeConnecte);
        }
        if (employeConnecte == null) return;

        employeConnecte = employeCRUD.getById(employeConnecte.getId_employé());
        session.setEmploye(employeConnecte);

        remplirProfileRow();
        remplirFormulaire();
        remplirInfoPro();
    }

    private void setupHoverEffects() {
        avatarContainer.setOnMouseEntered(e -> {
            overlayPhoto.setVisible(true);
            overlayPhoto.setManaged(true);
            FadeTransition ft = new FadeTransition(Duration.millis(200), overlayPhoto);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        });
        avatarContainer.setOnMouseExited(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), overlayPhoto);
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(ev -> {
                overlayPhoto.setVisible(false);
                overlayPhoto.setManaged(false);
            });
            ft.play();
        });
    }

    private void remplirProfileRow() {
        lblNomComplet.setText(employeConnecte.getPrenom() + " " + employeConnecte.getNom());
        lblEmailDisplay.setText(employeConnecte.getE_mail());
        styliserBadgeRole(lblRoleBadge, employeConnecte.getRole());
        chargerImageProfil();
    }

    private void remplirFormulaire() {
        txtNomProfil.setText(employeConnecte.getNom());
        txtPrenomProfil.setText(employeConnecte.getPrenom());
        txtEmailProfil.setText(employeConnecte.getE_mail());
        txtTelProfil.setText(String.valueOf(employeConnecte.getTelephone()));
    }

    private void remplirInfoPro() {
        lblRoleInfo.setText(employeConnecte.getRole().getLibelle());
        lblPosteReadonly.setText(employeConnecte.getPoste() != null ? employeConnecte.getPoste() : "—");
        lblDateReadonly.setText(employeConnecte.getDate_embauche() != null ? employeConnecte.getDate_embauche().format(DATE_FORMAT) : "—");

        chargerEntreprise();
    }

    private void chargerEntreprise() {
        try {
            int idEntreprise = employeConnecte.getIdEntreprise();
            if (idEntreprise > 0) {
                entreprise ent = entrepriseCrud.getById(idEntreprise);
                if (ent != null) {
                    lblEntrepriseNom.setText(ent.getNom_entreprise());
                    lblEntrepriseVille.setText(ent.getVille() != null ? ent.getVille() : "—");
                    lblEntreprisePays.setText(ent.getPays() != null ? ent.getPays() : "—");
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement entreprise: " + e.getMessage());
        }

        lblEntrepriseNom.setText("—");
        lblEntrepriseVille.setText("—");
        lblEntreprisePays.setText("—");
    }

    private void chargerImageProfil() {
        Image image = null;
        String imagePath = employeConnecte.getImageProfil();

        if (imagePath != null && !imagePath.isBlank()) {
            File testFile = new File(imagePath);
            if (testFile.isAbsolute() && testFile.exists()) {
                try {
                    image = new Image(testFile.toURI().toString(), 180, 180, true, true);
                } catch (Exception ignored) {}
            }
            if (image == null) {
                try {
                    URL resource = getClass().getResource(imagePath);
                    if (resource != null) {
                        image = new Image(resource.toExternalForm(), 180, 180, true, true);
                    }
                } catch (Exception ignored) {}
            }
        }

        if (image == null) {
            try {
                URL defaultUrl = getClass().getResource(employe.DEFAULT_IMAGE);
                if (defaultUrl != null) {
                    image = new Image(defaultUrl.toExternalForm(), 180, 180, true, true);
                }
            } catch (Exception ignored) {}
        }

        if (image != null) {
            imgProfil.setImage(image);
            imgProfil.setSmooth(true);

            double radius = 45.0;
            Circle clip = new Circle(radius);
            clip.setCenterX(radius);
            clip.setCenterY(radius);
            imgProfil.setClip(clip);

            Image img = imgProfil.getImage();
            if (img != null && img.getWidth() > 0 && img.getHeight() > 0) {
                double w = img.getWidth();
                double h = img.getHeight();
                double side = Math.min(w, h);
                double x = (w - side) / 2;
                double y = (h - side) / 2;
                imgProfil.setViewport(
                        new javafx.geometry.Rectangle2D(x, y, side, side));
                imgProfil.setPreserveRatio(false);
            }
        }
    }

    @FXML
    private void changerPhotoProfil() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une photo de profil");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fc.showOpenDialog(
                avatarContainer.getScene().getWindow());
        if (file == null) return;

        try {
            String destPath = sauvegarderImage(file, employeConnecte.getId_employé());
            employeConnecte.setImageProfil(destPath);
            employeCRUD.modifier(employeConnecte);
            session.setEmploye(employeConnecte);

            chargerImageProfil();
            afficherMessage(lblInfoMessage, "✓ Photo mise à jour", false);
        } catch (Exception e) {
            afficherMessage(lblInfoMessage, "Erreur : " + e.getMessage(), true);
        }
    }

    private String sauvegarderImage(File source, int idEmploye)
            throws Exception {
        File dir = new File(PROFILES_DIR);
        if (!dir.exists()) dir.mkdirs();

        String extension = source.getName().substring(source.getName().lastIndexOf("."));
        String fileName = "profile_" + idEmploye + "_" + System.currentTimeMillis() + extension;
        File dest = new File(dir, fileName);

        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        if (employeConnecte.hasCustomImage()) {
            try {
                File oldFile = new File(employeConnecte.getImageProfil());
                if (oldFile.exists()
                        && !oldFile.getAbsolutePath().equals(dest.getAbsolutePath())) {
                    oldFile.delete();
                }
            } catch (Exception ignored) {}
        }
        return dest.getAbsolutePath();
    }
    @FXML
    private void sauvegarderInfos() {
        lblInfoMessage.setText("");

        String nom = txtNomProfil.getText().trim();
        String prenom = txtPrenomProfil.getText().trim();
        String email = txtEmailProfil.getText().trim();
        String tel = txtTelProfil.getText().trim();
        txtNomProfil.setStyle("");
        txtPrenomProfil.setStyle("");
        txtEmailProfil.setStyle("");
        txtTelProfil.setStyle("");

        String errorStyle = "-fx-border-color: #DC2626; -fx-border-width: 1; -fx-border-radius: 3;";
        boolean valid = true;

        if (nom.isEmpty()) {
            txtNomProfil.setStyle(errorStyle);
            valid = false;
        }
        if (prenom.isEmpty()) {
            txtPrenomProfil.setStyle(errorStyle);
            valid = false;
        }
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            txtEmailProfil.setStyle(errorStyle);
            valid = false;
        }
        if (tel.isEmpty()) {
            txtTelProfil.setStyle(errorStyle);
            valid = false;
        } else {
            try {
                Integer.parseInt(tel);
            } catch (NumberFormatException e) {
                txtTelProfil.setStyle(errorStyle);
                valid = false;
            }
        }

        if (!valid) {
            afficherMessage(lblInfoMessage, "il y a un champ vide ou l'email n'est pas valide", true);
            return;
        }

        try {
            employeConnecte.setNom(nom);
            employeConnecte.setPrenom(prenom);
            employeConnecte.setE_mail(email);
            employeConnecte.setTelephone(Integer.parseInt(tel));

            employeCRUD.modifier(employeConnecte);
            session.setEmploye(employeConnecte);

            remplirProfileRow();
            afficherMessage(lblInfoMessage, "Informations mises à jour", false);
        } catch (SQLException e) {
            afficherMessage(lblInfoMessage, "Erreur : " + e.getMessage(), true);
        }
    }
    @FXML
    private void changerMotDePasse() {

    }

    private void styliserBadgeRole(Label badge, role r) {
        badge.setText(r.getLibelle());
        String base = "-fx-font-size: 11px;-fx-font-weight: bold;-fx-padding: 4 12;-fx-background-radius: 15;";
        switch (r) {
            case ADMINISTRATEUR_ENTREPRISE -> badge.setStyle(base + "-fx-background-color: -color-accent-muted;-fx-text-fill: -color-accent-fg;");
            case RH -> badge.setStyle(base + "-fx-background-color: -color-danger-muted;-fx-text-fill: -color-danger-fg;");
            case CHEF_PROJET -> badge.setStyle(base + "-fx-background-color: -color-success-muted;-fx-text-fill: -color-success-fg;");
            default -> badge.setStyle(base + "-fx-background-color: -color-bg-subtle;-fx-text-fill: -color-fg-muted;-fx-border-color: -color-border-muted;-fx-border-radius: 15;");
        }
    }

    private void afficherMessage(Label label, String message, boolean isError) {
        label.setText(message);
        label.setStyle(isError ? "-fx-text-fill: -color-danger-fg;" : "-fx-text-fill: -color-success-fg;");

        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(400), label);
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(ev -> {
                label.setText("");
                label.setOpacity(1);
            });
            ft.play();
        });
        pause.play();
    }

    private void afficherErreur(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}