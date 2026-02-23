package controller.offres;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainFrontOfficeController {

    @FXML private StackPane contentArea;
    @FXML private Button btnNavOffres, btnNavEntreprises, btnNavSuivi, btnNavContact;

    @FXML
    public void initialize() {
        showOffres();
    }

    @FXML
    private void showOffres() {
        loadView("/offres/offres-portal.fxml");
        updateActiveLink(btnNavOffres);
    }

    @FXML
    private void showEntreprises() {
        //loadView("/view/front/entreprises_portal.fxml");
        //updateActiveLink(btnNavEntreprises);
    }

    @FXML
    private void showSuivi() {
        loadView("/offres/suivi-candidature.fxml");
        updateActiveLink(btnNavSuivi);
    }

    @FXML
    private void showContact() {
        //loadView("/view/front/contact.fxml");
        //updateActiveLink(btnNavContact);
    }

    @FXML
    private void handleConnexion() {
        System.out.println("Redirecting to Login...");
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error loading: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void updateActiveLink(Button activeBtn) {
        // Reset styles for all
        btnNavOffres.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: normal;");
        btnNavEntreprises.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: normal;");
        btnNavSuivi.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: normal;");
        btnNavContact.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: normal;");

        // Highlight the active one
        activeBtn.setStyle("-fx-text-fill: -color-accent-fg; -fx-font-weight: bold; -fx-border-color: -color-accent-fg; -fx-border-width: 0 0 2 0;");
    }
}