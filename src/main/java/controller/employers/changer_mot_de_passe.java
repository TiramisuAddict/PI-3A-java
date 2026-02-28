package controller.employers;

import entities.employers.compte;
import entities.employers.session;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.util.Duration;
import service.employers.compteCRUD;
import service.employers.hachageMotDePasse;
import utils.employers.UI;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class changer_mot_de_passe implements Initializable {

    @FXML private PasswordField txtMotDePasseActuel;
    @FXML private PasswordField txtNouveauMotDePasse;
    @FXML private PasswordField txtConfirmationMotDePasse;
    @FXML private Label lblMessage;
    @FXML private Button btnConfirmer;

    private compteCRUD compteCrud;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            compteCrud = new compteCRUD();
        } catch (SQLException e) {
            UI.afficherMessage(lblMessage, "Erreur de connexion à la base de données.", true);
        }
    }

    @FXML
    private void confirmerChangement() {
        resetStyles();
        lblMessage.setText("");

        if (!validerChamps()) return;

        String actuel = txtMotDePasseActuel.getText();
        String nouveau = txtNouveauMotDePasse.getText();

        try {
            compte compteConnecte = session.getCompte();
            if (compteConnecte == null) {
                UI.afficherMessage(lblMessage, "Session expirée. Veuillez vous reconnecter.", true);
                return;
            }

            // Vérifier mot de passe actuel
            String hashedActuel = hachageMotDePasse.hashPassword(actuel);
            if (!hashedActuel.equals(compteConnecte.getPassword())) {
                UI.marquerErreur(txtMotDePasseActuel);
                UI.afficherMessage(lblMessage, "Le mot de passe actuel est incorrect.", true);
                return;
            }

            // Mettre à jour le mot de passe
            String hashedNouveau = hachageMotDePasse.hashPassword(nouveau);
            compteConnecte.setPassword(hashedNouveau);
            compteCrud.modifierMotDePasse(compteConnecte.getId(), hashedNouveau);
            session.setCompte(compteConnecte);

            UI.afficherMessage(lblMessage, "Mot de passe modifié avec succès !", false);
            desactiverFormulaire();

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> fermerFenetre());
            pause.play();

        } catch (SQLException e) {
            UI.afficherMessage(lblMessage, "Erreur lors de la modification : " + e.getMessage(), true);
        }
    }

    private boolean validerChamps() {
        String actuel = txtMotDePasseActuel.getText();
        String nouveau = txtNouveauMotDePasse.getText();
        String confirmation = txtConfirmationMotDePasse.getText();
        boolean valid = true;

        if (actuel == null || actuel.trim().isEmpty()) {
            UI.marquerErreur(txtMotDePasseActuel);
            valid = false;
        }
        if (nouveau == null || nouveau.trim().isEmpty()) {
            UI.marquerErreur(txtNouveauMotDePasse);
            valid = false;
        }
        if (confirmation == null || confirmation.trim().isEmpty()) {
            UI.marquerErreur(txtConfirmationMotDePasse);
            valid = false;
        }

        if (!valid) {
            UI.afficherMessage(lblMessage, "Veuillez remplir tous les champs.", true);
            return false;
        }

        if (!nouveau.equals(confirmation)) {
            UI.marquerErreur(txtNouveauMotDePasse);
            UI.marquerErreur(txtConfirmationMotDePasse);
            UI.afficherMessage(lblMessage, "Les mots de passe ne correspondent pas.", true);
            return false;
        }

        if (actuel.equals(nouveau)) {
            UI.marquerErreur(txtNouveauMotDePasse);
            UI.afficherMessage(lblMessage, "Le nouveau mot de passe doit être différent de l'actuel.", true);
            return false;
        }

        if (nouveau.length() < 6) {
            UI.marquerErreur(txtNouveauMotDePasse);
            UI.afficherMessage(lblMessage, "Le mot de passe doit contenir au moins 6 caractères.", true);
            return false;
        }

        return true;
    }

    private void resetStyles() {
        UI.effacerErreur(txtMotDePasseActuel);
        UI.effacerErreur(txtNouveauMotDePasse);
        UI.effacerErreur(txtConfirmationMotDePasse);
    }

    private void desactiverFormulaire() {
        txtMotDePasseActuel.setDisable(true);
        txtNouveauMotDePasse.setDisable(true);
        txtConfirmationMotDePasse.setDisable(true);
        btnConfirmer.setDisable(true);
    }

    private void fermerFenetre() {
        if (btnConfirmer.getScene() != null && btnConfirmer.getScene().getWindow() != null) {
            btnConfirmer.getScene().getWindow().hide();
        }
    }
}