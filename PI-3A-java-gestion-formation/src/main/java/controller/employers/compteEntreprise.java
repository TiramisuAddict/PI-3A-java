package controller.employers;

import models.employe.entreprise;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.employe.entrepriseCRUD;

import java.io.IOException;
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
    @FXML private Label msgError;

    private entrepriseCRUD entrepriseCRUD;

    @FXML
    void initialize() throws SQLException {
        entrepriseCRUD = new entrepriseCRUD();
        msgError.setText("");
    }

    @FXML
    public void ajouterCompteEntreprise() {
        msgError.setText("");
        msgError.setStyle("-fx-text-fill: #DC2626;");

        String email = e_mailField.getText();
        if (nomEField.getText().isEmpty() || paysField.getText().isEmpty() ||
                villeField.getText().isEmpty() || nomField.getText().isEmpty() ||
                prenomField.getText().isEmpty() || matriculeFiescaleField.getText().isEmpty() ||
                telephoneField.getText().isEmpty() || email.isEmpty()) {

            msgError.setText("Veuillez remplir tous les champs obligatoires.");
            return;
        }
        if (!email.matches("^[a-zA-Z0-9._-]+@gmail\\.com$")) {
            msgError.setText("L'email doit être une adresse @gmail.com valide.");
            return;
        }

        try {
            int telephone;
            try {
                telephone = Integer.parseInt(telephoneField.getText());
            } catch (NumberFormatException e) {
                msgError.setText("Le numéro de téléphone ne doit contenir que des chiffres.");
                return;
            }
            entreprise entreprise = new entreprise(
                    nomEField.getText(),
                    paysField.getText(),
                    villeField.getText(),
                    nomField.getText(),
                    prenomField.getText(),
                    matriculeFiescaleField.getText(),
                    telephone,
                    email
            );

            entrepriseCRUD.ajouter(entreprise);
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