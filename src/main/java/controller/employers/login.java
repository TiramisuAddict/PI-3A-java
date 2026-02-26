package controller.employers;

import entities.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.adminCRUD;
import service.compteCRUD;
import service.employeCRUD;
import service.visiteurCRUD;

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
    private visiteurCRUD visiteurService;

    @FXML
    public void initialize() {
        try {
            compteService = new compteCRUD();
            adminService = new adminCRUD();
            employeService = new employeCRUD();
            visiteurService = new visiteurCRUD();
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur de connexion à la base de données !");
        }
    }

    @FXML
    private void Login() {
        String email = textFieldEmail.getText().trim();
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
            compte compteConnecte = compteService.authentifier(email, password);
            if (compteConnecte != null) {
                employe emp = employeService.getById(compteConnecte.getId_employe());
                if (emp != null) {
                    session.setCompte(compteConnecte);
                    session.setEmploye(emp);
                    role r = emp.getRole();

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
            visiteur v = visiteurService.authentifier(email, password);
            if (v != null) {
                session.setVisiteur(v);
                System.out.println("Connexion réussie : Visiteur - " + v.getNom() + " " + v.getPrenom());
                openInterfaceVisiteur();
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
            stage.setTitle("Espace RH");
            stage.setScene(new Scene(root));
            stage.show();
            closeLoginWindow();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Impossible d'ouvrir l'interface RH.");
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

    private void openInterfaceVisiteur() {
        try {
            // path interface condidat 3and taha
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/emp/visiteur/espace_visiteur.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Espace Candidat");
            stage.setScene(new Scene(root));
            stage.show();
            closeLoginWindow();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur ouverture interface Candidat.");
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

    @FXML
    private void openCompteVisiteur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/emp/compteVisiteur.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Créer un compte candidat");
            stage.setScene(new Scene(root));
            stage.show();
            closeLoginWindow();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur ouverture formulaire candidat.");
        }
    }

    private void closeLoginWindow() {
        Stage stage = (Stage) buttonConnecter.getScene().getWindow();
        stage.close();
    }
}