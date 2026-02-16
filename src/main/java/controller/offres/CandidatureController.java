package controller.offres;

import entity.Candidat;

import entity.Offre;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import service.CandidatCRUD;
import service.OffreCRUD;
import utils.BadgeFactory;
import utils.FilePickerUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.regex.Pattern;

import static utils.BadgeFactory.createBadge;

public class CandidatureController {

    @FXML private VBox postulerCard, suivreCard;

    //mrama detail on verification
    @FXML private Label detailPoste, detailNomEnterprise, detailTypeContrat;

    //under construction
    @FXML private VBox step1View, step2View, step3View, offerDetailsBox;
    @FXML private HBox badgeValid, badgeInvalide;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblStepOffre, lblStepInfo, lblStepEnvoi, lblTrackingCode;
    @FXML private TextField txtCodeOffre, txtNom, txtPrenom, txtTel, txtEmail;
    @FXML private Button btnNextToStep2, btnCV, btnLettre;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s'-]{2,}$");

    private File selectedCvFile;
    private File selectedLettreFile;

    private boolean cvUploaded = false;
    private boolean lettreUploaded = false;

    @FXML
    public void initialize() {
        showStep1();
    }

    //Step 1

    @FXML private void showStep1() {
        updateUI(step1View, 0.33, lblStepOffre);
    }

    //Step 2

    @FXML
    private void showStep2() {
        cvUploaded = false;
        lettreUploaded = false;
        updateUI(step2View, 0.66, lblStepInfo);
    }

    private boolean validateStep2() {
        boolean isValid = true;
        String lightErrorStyle = "-fx-border-color: -color-danger-emphasis; -fx-border-radius: 5; -fx-border-width: 1;";

        resetFieldStyles();

        if (txtNom.getText().trim().isEmpty() || !NAME_PATTERN.matcher(txtNom.getText().trim()).matches()) {
            txtNom.setStyle(lightErrorStyle);
            isValid = false;
        }

        if (txtPrenom.getText().trim().isEmpty() || !NAME_PATTERN.matcher(txtPrenom.getText().trim()).matches()) {
            txtPrenom.setStyle(lightErrorStyle);
            isValid = false;
        }

        if (!PHONE_PATTERN.matcher(txtTel.getText().trim()).matches()) {
            txtTel.setStyle(lightErrorStyle);
            isValid = false;
        }

        if (!EMAIL_PATTERN.matcher(txtEmail.getText().trim()).matches()) {
            txtEmail.setStyle(lightErrorStyle);
            isValid = false;
        }

        if (!cvUploaded) {
            btnCV.setStyle("-fx-background-color: -color-bg-default; -fx-border-color: -color-danger-emphasis; -fx-border-radius: 5; -fx-border-width: 1;");
            isValid = false;
        }

        if (!lettreUploaded) {
            btnLettre.setStyle("-fx-background-color: -color-bg-default; -fx-border-color: -color-danger-emphasis; -fx-border-radius: 5; -fx-border-width: 1;");
            isValid = false;
        }

        return isValid;
    }

    private void resetFieldStyles() {
        String defaultStyle = "";
        txtNom.setStyle(defaultStyle);
        txtPrenom.setStyle(defaultStyle);
        txtTel.setStyle(defaultStyle);
        txtEmail.setStyle(defaultStyle);

        if (!cvUploaded) btnCV.setStyle("-fx-background-color: -color-bg-default; -fx-border-color: -color-border-default; -fx-border-radius: 5; -fx-cursor: hand;");
        if (!lettreUploaded) btnLettre.setStyle("-fx-background-color: -color-bg-default; -fx-border-color: -color-border-default; -fx-border-radius: 5; -fx-cursor: hand;");
    }

    //Step 3

    public String generateCodeCandidature() {
        String prefix = (txtPrenom.getText().length() >= 3) ? txtPrenom.getText().substring(0, 3).toUpperCase() : txtPrenom.getText().toUpperCase();

        long currentTime = System.currentTimeMillis();

        String timeBase36 = Long.toString(currentTime, 36).toUpperCase();
        String suffix = timeBase36.substring(timeBase36.length() - 5);

        return prefix + suffix;
    }

    @FXML
    private void showStep3() {
        if (!validateStep2()) return;

        try {
            String code = generateCodeCandidature();

            byte[] cvData = java.nio.file.Files.readAllBytes(selectedCvFile.toPath());
            byte[] lettreData = java.nio.file.Files.readAllBytes(selectedLettreFile.toPath());

            //get
            int idOffre = 0;
            try {
                OffreCRUD offreCRUD = new OffreCRUD();
                idOffre = offreCRUD.getIdByCodeOffre(txtCodeOffre.getText());
            } catch (SQLException e) {
                System.out.println(e);
            }

            Candidat candidat = new Candidat(
                    code,
                    txtNom.getText().trim(),
                    txtPrenom.getText().trim(),
                    txtEmail.getText().trim(),
                    txtTel.getText().trim(),
                    selectedCvFile.getName(),
                    cvData,
                    selectedLettreFile.getName(),
                    lettreData,
                    "En attente",
                    "",
                    new java.sql.Date(System.currentTimeMillis()),
                    idOffre
            );

            CandidatCRUD candidatCRUD = new CandidatCRUD();

            try {
                candidatCRUD.ajouter(candidat);
            } catch (SQLException e){
                System.out.println(e);
            }

            lblTrackingCode.setText(code);
            updateUI(step3View, 1.0, lblStepEnvoi);

        } catch (java.io.IOException e) {
            // This handles cases where the file might have been moved/deleted
            // between selection and submission
            btnCV.setStyle("-fx-border-color: -color-danger-emphasis;");
        }
    }

    //NavBar Navigation
    private void updateUI(VBox activeView, double progress, Label activeLabel) {
        VBox[] allSteps = {step1View, step2View, step3View};
        for (VBox step : allSteps) {
            step.setVisible(false);
            step.setManaged(false);
        }

        activeView.setVisible(true);
        activeView.setManaged(true);
        progressBar.setProgress(progress);

        Label[] allLabels = {lblStepOffre, lblStepInfo, lblStepEnvoi};
        for (Label lbl : allLabels) lbl.setTextFill(Color.GRAY);
        activeLabel.setTextFill(Color.BLACK);
    }

    // --- FORM ACTIONS ---

    @FXML
    private void verifyCodeCandidature() {
        String code = txtCodeOffre.getText().trim();

        boolean isCorrect = false;
        try {
            OffreCRUD offreCRUD = new OffreCRUD();
            int idOffre = offreCRUD.getIdByCodeOffre(code);
            isCorrect = idOffre > 0;

            Offre o = offreCRUD.getById(idOffre);

            detailNomEnterprise.setText("Entreprise: " + "Société Générale");
            detailTypeContrat.setText("Type de Contrat: " + o.getTypeContrat());
            detailPoste.setText("Poste: " + o.getTitrePoste());

        } catch (SQLException e) {
            System.out.println("Offre with code '" + code + "' not found.");
        }
        setOfferVerificationUI(isCorrect);
    }

    private void setOfferVerificationUI(boolean isSuccess) {
        badgeValid.setVisible(isSuccess);
        badgeValid.setManaged(isSuccess);
        offerDetailsBox.setVisible(isSuccess);
        offerDetailsBox.setManaged(isSuccess);
        btnNextToStep2.setDisable(!isSuccess);

        badgeInvalide.setVisible(!isSuccess);
        badgeInvalide.setManaged(!isSuccess);
    }
    //==================================================

    @FXML
    private void pickCV() {
        File file = FilePickerUtil.pickPdf(btnCV.getScene().getWindow(), "Sélectionner votre CV");

        if (file != null) {

                selectedCvFile = file;
                updateFileButton(btnCV, file.getName(), true);
                cvUploaded = true;

        }else {
            cvUploaded = false;
        }
    }

    @FXML
    private void pickLettre() {
        File file = FilePickerUtil.pickPdf(btnLettre.getScene().getWindow(), "Sélectionner votre Lettre de Motivation");

        if (file != null) {

                selectedLettreFile = file;
                updateFileButton(btnLettre, file.getName(), true);
                lettreUploaded = true;

        }
        else {
            cvUploaded = false;
        }
    }

    /**
     * Helper to handle the common styling/text changes for file buttons
     */
    private void updateFileButton(Button btn, String text, boolean isValid) {
        if (isValid) {
            btn.setText(text);
            btn.setStyle("-fx-border-color: -color-success-emphasis; -fx-text-fill: -color-success-emphasis;");
        } else {
            btn.setText(text);
            btn.setStyle("-fx-border-color: -color-danger-emphasis; -fx-text-fill: -color-danger-emphasis;");
        }
    }

    //==================================================

    @FXML private void handleSuivi() { toggleMainCards(false); }
    @FXML private void handlePost() { toggleMainCards(true); }

    private void toggleMainCards(boolean showPostuler) {
        postulerCard.setVisible(showPostuler);
        postulerCard.setManaged(showPostuler);
        suivreCard.setVisible(!showPostuler);
        suivreCard.setManaged(!showPostuler);
    }

}