package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import models.employe.session;
import models.employe.employe;

import java.io.IOException;

public class WelcomeController {

    @FXML
    private Label lblEmployeName;

    @FXML
    private Button btnContinue;

    @FXML
    private Button btnCancel;

    @FXML
    private void initialize() {
        // Récupérer l'employé connecté depuis la session
        employe emp = session.getEmploye();

        if (emp != null) {
            // Afficher le nom et prénom de l'employé
            lblEmployeName.setText(emp.getPrenom() + " " + emp.getNom());
        }
    }

    @FXML
    private void handleContinue() {
        try {
            // Charger main-view.fxml
            Parent mainView = FXMLLoader.load(getClass().getResource("/main-view.fxml"));
            btnContinue.getScene().setRoot(mainView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        try {
            // Retourner au login
            session.logout();
            Parent loginView = FXMLLoader.load(getClass().getResource("/emp/Login.fxml"));
            btnCancel.getScene().setRoot(loginView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

