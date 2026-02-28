package controller.employers;

import entities.employers.visiteur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.employers.visiteurCRUD;

import java.io.IOException;
import java.sql.SQLException;

public class compteVisiteur {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblError;

    private visiteurCRUD visiteurService;

    @FXML
    void initialize() {
        try {
            visiteurService = new visiteurCRUD();
            lblError.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
            lblError.setText("Erreur de connexion à la base de données.");
        }
    }

    @FXML
    private void creerCompte() {
        lblError.setText("");

        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String tel = txtTelephone.getText().trim();
        String pwd = txtPassword.getText();
        String confirmPwd = txtConfirmPassword.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || tel.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
            lblError.setText("Veuillez remplir tous les champs.");
            return;
        }

        if (!nom.matches("[a-zA-ZÀ-ÿ\\s-]+")) {
            lblError.setText("Le nom ne doit contenir que des lettres.");
            return;
        }

        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s-]+")) {
            lblError.setText("Le prénom ne doit contenir que des lettres.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            lblError.setText("Adresse email invalide.");
            return;
        }

        if (!tel.matches("\\d{8}")) {
            lblError.setText("Le téléphone doit contenir exactement 8 chiffres.");
            return;
        }

        if (pwd.length() < 6) {
            lblError.setText("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!pwd.equals(confirmPwd)) {
            lblError.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            visiteur v = new visiteur(nom, prenom, email, pwd, Integer.parseInt(tel));
            visiteurService.ajouter(v);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Votre compte a été créé avec succès !\nConnectez-vous depuis la page principale.");
            alert.showAndWait();
            viderFormulaire();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/emp/login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();

            Stage currentStage = (Stage) txtNom.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                lblError.setText("Cet email est déjà utilisé.");
            } else {
                lblError.setText("Erreur lors de la création du compte.");
            }
        }
    }

    private void viderFormulaire() {
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtTelephone.clear();
        txtPassword.clear();
        txtConfirmPassword.clear();
    }

    @FXML
    void retourLogin(ActionEvent event) {
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