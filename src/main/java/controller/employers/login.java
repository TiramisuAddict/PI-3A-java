package controller.employers;

import entities.employe.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.employe.adminCRUD;
import service.employe.compteCRUD;
import service.employe.employeCRUD;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class login {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField textFieldEmail;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button buttonConnecter;

    @FXML
    private Label errorLabel;
    private compteCRUD compteService;
    private adminCRUD adminService;
    private employeCRUD employeService;

    @FXML
    public void initialize() {
        try {
            compteService = new compteCRUD();
            adminService = new adminCRUD();
            employeService = new employeCRUD();
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur de connexion à la base de données !");
        }
    }

    @FXML
    private void Login() {
        String email = textFieldEmail.getText();
        String password = passwordField.getText();
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        try {
            administrateur_systeme admin = adminService.findbyemail(email);
            if (admin != null && admin.getMot_de_passe().equals(password)) {
                session.setAdmin(admin);
                System.out.println("Connexion réussie : Admin Système");
                openAdminInterface();
                return;
            }
            compte compteConnecte = compteService.findByEmail(email);

            if (compteConnecte != null && compteConnecte.getPassword().equals(password)) {
                employe emp = employeService.getById(compteConnecte.getId_employe());

                if (emp != null) {
                    session.setCompte(compteConnecte);
                    session.setEmploye(emp);

                    role r = emp.getRole();
                    System.out.println("Connexion : " + emp.getNom() + " (Rôle: " + r + ")");

                    if (r == role.ADMINISTRATEUR_ENTREPRISE || r == role.RH) {
                        openRHetAdminInterface();
                    } else {
                        openInterfaceEmployeSimple();
                    }

                } else {
                    errorLabel.setText("Compte trouvé mais aucun profil employé associé.");
                }
                return;
            }
            errorLabel.setText("Email ou mot de passe incorrect !");

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur technique lors de la connexion.");
        }
    }

    private void openRHetAdminInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/emp/RHetAdminE/RHetAdminE.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Espace RH ");
            stage.setScene(new Scene(root));
            stage.show();
            closeLoginWindow();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Impossible d'ouvrir l'interface Employé.");
        }
    }
    private void openAdminInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/emp/admin_sys/admin_systeme.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Administrateur Système");
            stage.setScene(new Scene(root));
            stage.show();
            closeLoginWindow();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Impossible d'ouvrir l'interface Admin.");
        }
    }
    @FXML
    private void opencompteEntreprise() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/emp/compteEntreprise.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Créer votre compte entreprise");
            stage.setScene(new Scene(root));
            stage.show();
            closeLoginWindow();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de l'ouverture de l'inscription.");
        }
    }
    private void closeLoginWindow() {
        Stage stage = (Stage) buttonConnecter.getScene().getWindow();
        stage.close();
    }
    private void openInterfaceEmployeSimple() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/emp/employes/employe.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Espace Employé");
            stage.setScene(new Scene(root));
            stage.show();

            closeLoginWindow();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur ouverture interface Employé.");
        }
    }
}