package controller.offres;

import entity.Candidat;
import entity.Offre;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import service.CandidatCRUD;
import service.OffreCRUD;
import utils.FilePickerUtil;
import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class CandidatureController {

    @FXML private StackPane rootNode;
    @FXML private VBox postulationFormView, succesEnvoiView;

    @FXML private WebView detailsWebView;
    private WebEngine descriptionEngine;

    @FXML private TextField txtNom, txtPrenom, txtTel, txtEmail;
    @FXML private Button btnUploadCV, btnUploadLettre;
    @FXML private Label lblTrackingCode;

    private File fileCV, fileLettre;
    private boolean cvUploaded = false;
    private boolean lettreUploaded = false;
    private int idOffreSelectionnee;
    private Offre offreActuelle;

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NOM_REGEX = Pattern.compile("^[A-Za-zÀ-ÿ\\s'-]{2,}$");

    @FXML
    public void initialize() {
        descriptionEngine = detailsWebView.getEngine();
        modifierVisibiliteVues(postulationFormView);
    }

    private boolean isPostulationValide() {
        boolean isValide = true;

        String errorStyle = "-fx-border-color: -color-danger-emphasis; -fx-border-width: 1; -fx-border-radius: 5;";

        resetStylesFormulaire();

        // Validation Nom
        if (txtNom.getText().trim().isEmpty() || !NOM_REGEX.matcher(txtNom.getText().trim()).matches()) {
            txtNom.setStyle(errorStyle);
            isValide = false;
        }

        // Validation Prénom
        if (txtPrenom.getText().trim().isEmpty() || !NOM_REGEX.matcher(txtPrenom.getText().trim()).matches()) {
            txtPrenom.setStyle(errorStyle);
            isValide = false;
        }

        // Validation Email
        if (txtEmail.getText().trim().isEmpty() || !EMAIL_REGEX.matcher(txtEmail.getText().trim()).matches()) {
            txtEmail.setStyle(errorStyle);
            isValide = false;
        }

        // Validation Téléphone (8 chiffres)
        if (txtTel.getText().trim().length() != 8) {
            txtTel.setStyle(errorStyle);
            isValide = false;
        }

        // Validation CV
        if (!cvUploaded) {
            btnUploadCV.setStyle(errorStyle);
            isValide = false;
        }

        return isValide;
    }

    private void resetStylesFormulaire() {
        String defaultBorder = "-fx-border-color: -color-border-default; -fx-border-width: 1; -fx-border-radius: 5;";

        txtNom.setStyle(defaultBorder);
        txtPrenom.setStyle(defaultBorder);
        txtTel.setStyle(defaultBorder);
        txtEmail.setStyle(defaultBorder);

        if (!cvUploaded) btnUploadCV.setStyle("");
        if (!lettreUploaded) btnUploadLettre.setStyle("");
    }

    @FXML
    private void handlePostuler() {
        if (!isPostulationValide()) return;

        try {
            String trackingCode = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
            byte[] cvBytes = Files.readAllBytes(fileCV.toPath());
            byte[] lettreBytes = (fileLettre != null) ? Files.readAllBytes(fileLettre.toPath()) : null;

            Candidat nouveauCandidat = new Candidat(
                    trackingCode, txtNom.getText().trim(), txtPrenom.getText().trim(),
                    txtEmail.getText().trim(), txtTel.getText().trim(), fileCV.getName(),
                    cvBytes, (fileLettre != null ? fileLettre.getName() : ""),
                    lettreBytes, "En attente", "", new java.sql.Date(System.currentTimeMillis()),
                    idOffreSelectionnee
            );

            new CandidatCRUD().ajouter(nouveauCandidat);

            lblTrackingCode.setText(trackingCode);
            modifierVisibiliteVues(succesEnvoiView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void pickCV() {
        File file = FilePickerUtil.pickPdf(btnUploadCV.getScene().getWindow(), "Upload CV");
        if (file != null) {
            this.fileCV = file;
            this.cvUploaded = true;
            btnUploadCV.setText("✔ " + file.getName());
            btnUploadCV.setStyle("-fx-border-color: -color-success-emphasis; -fx-text-fill: -color-success-emphasis;");
        }
    }

    @FXML
    private void pickLettre() {
        File file = FilePickerUtil.pickPdf(btnUploadLettre.getScene().getWindow(), "Upload Lettre");
        if (file != null) {
            this.fileLettre = file;
            this.lettreUploaded = true;
            btnUploadLettre.setText("✔ " + file.getName());
            btnUploadLettre.setStyle("-fx-border-color: -color-success-emphasis; -fx-text-fill: -color-success-emphasis;");
        }
    }

    private void modifierVisibiliteVues(VBox vueActive) {
        postulationFormView.setVisible(false); postulationFormView.setManaged(false);
        succesEnvoiView.setVisible(false); succesEnvoiView.setManaged(false);

        vueActive.setVisible(true);
        vueActive.setManaged(true);
    }

    @FXML
    private void handleClose() {
        StackPane parent = (StackPane) rootNode.getParent();
        if (parent != null) {
            parent.getChildren().remove(rootNode);
            if (!parent.getChildren().isEmpty()) {
                Node contentPortal = parent.getChildren().get(0);
                contentPortal.setEffect(null);
                contentPortal.setOpacity(1.0);
            }
        }
    }

    public void setOfferId(int offerId) {
        this.idOffreSelectionnee = offerId;
        try {
            this.offreActuelle = new OffreCRUD().getById(offerId);
            if (offreActuelle != null) {
                descriptionEngine.loadContent(offreActuelle.getDescription());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}