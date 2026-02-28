package controller.employers;

import entities.employers.entreprise;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.employers.entrepriseCRUD;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class compteEntreprise {
    @FXML private TextField e_mailField;
    @FXML private TextField matriculeFiescaleField;
    @FXML private TextField nomEField;
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField paysField;
    @FXML private TextField telephoneField;
    @FXML private TextField villeField;
    @FXML private TextField siteWebField;
    @FXML private Button btnChoisirLogo;
    @FXML private HBox logoPreview;
    @FXML private ImageView imgLogoPreview;
    @FXML private Label lblLogoName;
    @FXML private Label msgError;
    private entrepriseCRUD entrepriseCRUD;
    private String logoPath = null;

    private static final String LOGOS_DIR = System.getProperty("user.home") ;
    @FXML
    void initialize() throws SQLException {
        entrepriseCRUD = new entrepriseCRUD();
        msgError.setText("");
    }
    @FXML
    private void choisirLogo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir le logo de l'entreprise");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fc.showOpenDialog(btnChoisirLogo.getScene().getWindow());
        if (file == null) return;
        if (file.length() > 5 * 1024 * 1024) {
            msgError.setText("Logo trop volumineux (max 5 Mo)");
            return;
        }
        try {
            Path dirPath = Path.of(LOGOS_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            String extension = file.getName().substring(file.getName().lastIndexOf("."));
            String fileName = "logo_" + System.currentTimeMillis() + extension;
            Path dest = dirPath.resolve(fileName);
            Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            logoPath = dest.toAbsolutePath().toString();

            System.out.println("Logo copié vers : " + logoPath);
            System.out.println("Fichier existe : " + Files.exists(dest));
            Image img = new Image(dest.toUri().toString(), 30, 30, true, true);
            imgLogoPreview.setImage(img);
            lblLogoName.setText(file.getName());
            logoPreview.setVisible(true);
            logoPreview.setManaged(true);

            btnChoisirLogo.setText("Logo sélectionné");
            btnChoisirLogo.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 10;-fx-border-color: #16a34a; -fx-border-radius: 10; -fx-text-fill: #16a34a; -fx-cursor: hand;");

        } catch (Exception e) {
            msgError.setText("Erreur lors du chargement du logo.");
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerLogo() {
        if (logoPath != null) {
            try {
                Files.deleteIfExists(Path.of(logoPath));
            } catch (IOException ignored) {}
        }
        resetLogoUI();
    }
    private void resetLogoUI() {
        logoPath = null;
        imgLogoPreview.setImage(null);
        lblLogoName.setText("");
        logoPreview.setVisible(false);
        logoPreview.setManaged(false);

        btnChoisirLogo.setText("Logo (optionnel)");
        btnChoisirLogo.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10;-fx-border-color: #e5e7eb; -fx-border-radius: 10; -fx-text-fill: #9ca3af; -fx-cursor: hand;");
    }
    @FXML
    public void ajouterCompteEntreprise() {
        msgError.setText("");
        msgError.setStyle("-fx-text-fill: #DC2626;");

        String email = e_mailField.getText().trim();
        String siteWeb = siteWebField.getText().trim();

        if (nomEField.getText().isEmpty() || paysField.getText().isEmpty() || villeField.getText().isEmpty() || nomField.getText().isEmpty() || prenomField.getText().isEmpty() || matriculeFiescaleField.getText().isEmpty() || telephoneField.getText().isEmpty() || email.isEmpty()) {
            msgError.setText("Veuillez remplir tous les champs obligatoires.");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            msgError.setText("Adresse email invalide.");
            return;
        }
        if (!siteWeb.isEmpty()
                && !siteWeb.matches("^(https?://)?[\\w.-]+\\.[a-zA-Z]{2,}(/.*)?$")) {
            msgError.setText("Format du site web invalide.");
            return;
        }

        try {
            int telephone;
            try {
                telephone = Integer.parseInt(telephoneField.getText().trim());
            } catch (NumberFormatException e) {
                msgError.setText("Le téléphone ne doit contenir que des chiffres.");
                return;
            }
            entreprise ent = new entreprise(
                    nomEField.getText().trim(),
                    paysField.getText().trim(),
                    villeField.getText().trim(),
                    nomField.getText().trim(),
                    prenomField.getText().trim(),
                    matriculeFiescaleField.getText().trim(),
                    telephone,
                    email,
                    siteWeb.isEmpty() ? null : siteWeb,
                    logoPath
            );

            entrepriseCRUD.ajouter(ent);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Votre demande a été envoyée avec succès.");
            alert.showAndWait();
            viderFormulaire();

        } catch (SQLException e) {
            msgError.setText("Erreur technique : " + e.getMessage());
        } catch (Exception e) {
            msgError.setText("Une erreur est survenue.");
            e.printStackTrace();
        }
    }
    private void viderFormulaire() {
        nomEField.clear();
        paysField.clear();
        villeField.clear();
        nomField.clear();
        prenomField.clear();
        e_mailField.clear();
        matriculeFiescaleField.clear();
        telephoneField.clear();
        siteWebField.clear();
        resetLogoUI();

        msgError.setText("");
    }

    @FXML
    void naviguerVersLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/emp/login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}