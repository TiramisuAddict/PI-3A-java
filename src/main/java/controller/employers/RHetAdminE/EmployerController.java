package controller.employers.RHetAdminE;

import entities.employe.employe;
import entities.employe.role;
import entities.employe.session;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import service.employe.employeCRUD;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class EmployerController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private Tab tabListe, tabDetails, tabAjouter;
    @FXML private VBox employesContainer;
    @FXML private Label lblNombreEmployes;
    @FXML private Button btnImportCSV;
    @FXML private VBox formCardDetails;
    @FXML private Label lblTitreDetails, lblError;
    @FXML private TextField txtNom, txtPrenom, txtEmail, txtTelephone, txtPoste;
    @FXML private ComboBox<String> comboRole;
    @FXML private DatePicker dateEmbauche;
    @FXML private VBox formCardAjout;
    @FXML private Label lblErrorAjout;
    @FXML private TextField txtNomAjout, txtPrenomAjout, txtEmailAjout, txtTelephoneAjout, txtPosteAjout;
    @FXML private ComboBox<String> comboRoleAjout;
    @FXML private DatePicker dateEmbaucheAjout;
    private employeCRUD employeCRUD;
    private employe employeSelectionne;
    private int idEntrepriseConnecte;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            employeCRUD = new employeCRUD();
            initSession();
            initComboBoxRoles();
            configurerDesign();
            chargerEmployes();

        } catch (SQLException e) {
            afficherErreur("Erreur Init", e.getMessage());
        }
    }

    private void initSession() throws SQLException {
        if (session.getEmploye() != null) {
            idEntrepriseConnecte = session.getEmploye().getIdEntreprise();
        } else if (session.getCompte() != null) {
            employe emp = employeCRUD.getById(session.getCompte().getId_employe());
            if (emp != null) {
                session.setEmploye(emp);
                idEntrepriseConnecte = emp.getIdEntreprise();
            }
        }
    }

    private void configurerDesign() {
        String cardStyle = "-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-padding: 30;";
        if (formCardDetails != null) formCardDetails.setStyle(cardStyle);
        if (formCardAjout != null) formCardAjout.setStyle(cardStyle);
        stylerInputs(txtNom, txtPrenom, txtEmail, txtTelephone, txtPoste, comboRole, dateEmbauche);
        stylerInputs(txtNomAjout, txtPrenomAjout, txtEmailAjout, txtTelephoneAjout, txtPosteAjout, comboRoleAjout, dateEmbaucheAjout);
        comboRole.setPrefWidth(180);
        comboRoleAjout.setPrefWidth(180);
        if (btnImportCSV != null) {
            btnImportCSV.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 15;");
            btnImportCSV.setOnMouseEntered(e -> btnImportCSV.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 15;"));
            btnImportCSV.setOnMouseExited(e -> btnImportCSV.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 15;"));
        }
    }

    private void stylerInputs(Control... controls) {
        String base = "-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-text-fill: #334155;";
        String focus = "-fx-background-color: #ffffff; -fx-border-color: #4f46e5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-text-fill: #4A5DEF; -fx-effect: dropshadow(three-pass-box, rgba(79, 70, 229, 0.2), 5, 0, 0, 0);";
        String error = "-fx-background-color: #fef2f2; -fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;";

        for (Control c : controls) {
            c.setStyle(base);
            c.getProperties().put("base", base);
            c.getProperties().put("error", error);

            c.focusedProperty().addListener((obs, old, isFocused) -> {
                if (isFocused) c.setStyle(focus);
                else c.setStyle(base);
            });
        }
    }

    private void animerTransition(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    @FXML
    private void ouvrirAjoutEmploye() {
        resetFormulaireAjout();
        tabAjouter.setDisable(false);
        tabPane.getSelectionModel().select(tabAjouter);
        if (formCardAjout != null) animerTransition(formCardAjout);
    }

    @FXML
    private void ouvrirDetails(employe emp) {
        employeSelectionne = emp;
        remplirFormulaireDetails(emp);
        tabDetails.setDisable(false);
        tabPane.getSelectionModel().select(tabDetails);
        if (formCardDetails != null) animerTransition(formCardDetails);
    }

    @FXML
    private void ajouterEmploye() {
        if (!validerFormulaire(txtNomAjout, txtPrenomAjout, txtEmailAjout, txtTelephoneAjout, txtPosteAjout, comboRoleAjout, lblErrorAjout)) return;

        try {
            employe nv = new employe();
            nv.setNom(txtNomAjout.getText().trim());
            nv.setPrenom(txtPrenomAjout.getText().trim());
            nv.setE_mail(txtEmailAjout.getText().trim());
            nv.setTelephone(Integer.parseInt(txtTelephoneAjout.getText().trim()));
            nv.setPoste(txtPosteAjout.getText().trim());
            nv.setRole(role.fromString(comboRoleAjout.getValue()));
            nv.setDate_embauche(dateEmbaucheAjout.getValue());
            nv.setIdEntreprise(idEntrepriseConnecte);

            employeCRUD.add(nv);

            afficherSucces("Succès", "L'employé a été ajouté.");
            retourListe();

        } catch (SQLException e) {
            lblErrorAjout.setText("Erreur technique : " + e.getMessage());
        } catch (NumberFormatException e) {
            marquerErreur(txtTelephoneAjout);
            lblErrorAjout.setText("Format téléphone invalide.");
        }
    }

    @FXML
    private void sauvegarderModifications() {
        if (!validerFormulaire(txtNom, txtPrenom, txtEmail, txtTelephone, txtPoste, comboRole, lblError)) return;

        try {
            employeSelectionne.setNom(txtNom.getText().trim());
            employeSelectionne.setPrenom(txtPrenom.getText().trim());
            employeSelectionne.setE_mail(txtEmail.getText().trim());
            employeSelectionne.setTelephone(Integer.parseInt(txtTelephone.getText().trim()));
            employeSelectionne.setPoste(txtPoste.getText().trim());
            employeSelectionne.setRole(role.fromString(comboRole.getValue()));
            employeSelectionne.setDate_embauche(dateEmbauche.getValue());

            employeCRUD.modifier(employeSelectionne);

            afficherSucces("Mise à jour", "Les modifications sont enregistrées.");
            retourListe();

        } catch (SQLException e) {
            lblError.setText("Erreur : " + e.getMessage());
        } catch (NumberFormatException e) {
            marquerErreur(txtTelephone);
            lblError.setText("Téléphone invalide.");
        }
    }

    public void retourListe() {
        tabDetails.setDisable(true);
        tabAjouter.setDisable(true);
        tabPane.getSelectionModel().select(tabListe);
        chargerEmployes();
    }

    @FXML
    private void importerDepuisCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importer CSV Employés");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fc.showOpenDialog(tabPane.getScene().getWindow());

        if (file == null) return;

        int ok = 0, err = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (i++ == 0 && line.toLowerCase().contains("nom")) continue;
                String[] p = line.split(";", -1);
                if (p.length < 6) { err++; continue; }

                try {
                    employe e = new employe();
                    e.setNom(p[0].trim()); e.setPrenom(p[1].trim()); e.setE_mail(p[2].trim());
                    e.setTelephone(Integer.parseInt(p[3].trim())); e.setPoste(p[4].trim());
                    e.setRole(parseRole(p[5].trim()));
                    e.setDate_embauche(p.length > 6 && !p[6].isEmpty() ? LocalDate.parse(p[6].trim()) : null);
                    e.setIdEntreprise(idEntrepriseConnecte);
                    employeCRUD.add(e);
                    ok++;
                } catch (Exception ex) { err++; }
            }
            chargerEmployes();
            afficherSucces("Import CSV", ok + " importés avec succès.");
        } catch (Exception e) {
            afficherErreur("Erreur CSV", e.getMessage());
        }
    }

    private boolean validerFormulaire(TextField nom, TextField prenom, TextField email, TextField tel, TextField poste, ComboBox<String> roleCB, Label errLbl) {
        boolean valid = true;
        stylerInputs(nom, prenom, email, tel, poste, roleCB);
        if (nom.getText().trim().isEmpty()) { marquerErreur(nom); valid = false; }
        if (prenom.getText().trim().isEmpty()) { marquerErreur(prenom); valid = false; }
        if (poste.getText().trim().isEmpty()) { marquerErreur(poste); valid = false; }
        if (roleCB.getValue() == null) { marquerErreur(roleCB); valid = false; }

        if (email.getText().trim().isEmpty() || !email.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            marquerErreur(email); valid = false;
        }

        try {
            if(tel.getText().trim().isEmpty()) throw new NumberFormatException();
            Integer.parseInt(tel.getText().trim());
        } catch (NumberFormatException e) {
            marquerErreur(tel); valid = false;
        }

        if (!valid) {
            errLbl.setText("Veuillez corriger les champs en rouge.");
            errLbl.setStyle("-fx-text-fill: #DC2626;");
        }
        return valid;
    }

    private void marquerErreur(Control c) {
        c.setStyle((String) c.getProperties().get("error"));
    }
    private void chargerEmployes() {
        remplirListe(null);
    }

    private void rechercherEmployes(String query) {
        remplirListe(query);
    }

    private void remplirListe(String query) {
        try {
            employesContainer.getChildren().clear();
            employesContainer.setSpacing(15);
            employesContainer.setPadding(new Insets(10));

            List<employe> all = employeCRUD.afficher(idEntrepriseConnecte);
            List<employe> list = (query == null || query.isEmpty()) ? all : all.stream()
                    .filter(e -> e.getNom().toLowerCase().contains(query.toLowerCase()) ||
                            e.getPrenom().toLowerCase().contains(query.toLowerCase()) ||
                            e.getPoste().toLowerCase().contains(query.toLowerCase()))
                    .toList();

            lblNombreEmployes.setText(list.size() + " employés");

            if (list.isEmpty()) {
                Label l = new Label("Aucune donnée trouvée.");
                l.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
                l.setAlignment(Pos.CENTER);
                l.setMaxWidth(Double.MAX_VALUE);
                employesContainer.getChildren().add(l);
                return;
            }

            for (employe emp : list) {
                employesContainer.getChildren().add(creerCarteEmploye(emp));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private VBox creerCarteEmploye(employe emp) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 3);");

        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 20, 15, 20));
        row.setStyle("-fx-background-radius: 12; -fx-cursor: hand;");

        row.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 5);"));
        row.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 3);"));
        row.setOnMouseClicked(e -> ouvrirDetails(emp));

        Label lblNom = new Label(emp.getPrenom() + " " + emp.getNom());
        lblNom.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        lblNom.setPrefWidth(200);

        Label lblPoste = new Label(emp.getPoste());
        lblPoste.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        lblPoste.setPrefWidth(150);

        Label lblEmail = new Label(emp.getE_mail());
        lblEmail.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        lblEmail.setPrefWidth(220);

        HBox badgeBox = new HBox(creerBadgeRole(emp.getRole()));
        badgeBox.setPrefWidth(130);
        badgeBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnDel = creerBoutonAvecTexte("Supprimer", "#DC2626");

        if (emp.getRole() == role.ADMINISTRATEUR_ENTREPRISE) {
            btnDel.setDisable(true);
            btnDel.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");
        } else {
            btnDel.setOnAction(e -> { e.consume(); confirmerSuppression(emp); });
        }

        row.getChildren().addAll(lblNom, lblPoste, lblEmail, badgeBox, spacer, btnDel);
        card.getChildren().add(row);
        return card;
    }
    private Button creerBoutonAvecTexte(String text, String colorHex) {
        Button btn = new Button(text);
        String styleBase = String.format(
                "-fx-background-color: white; -fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 12px; -fx-border-color: %s; -fx-border-width: 1.5;-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;",
                colorHex, colorHex
        );
        btn.setStyle(styleBase);
        btn.setOnMouseEntered(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(String.format("-fx-background-color: %s15; -fx-text-fill: %s;-fx-font-weight: bold; -fx-font-size: 12px; -fx-border-color: %s;-fx-border-width: 1.5; -fx-border-radius: 6; -fx-background-radius: 6;-fx-padding: 6 12; -fx-cursor: hand;", colorHex, colorHex, colorHex));
                btn.setEffect(new DropShadow(5, Color.web(colorHex, 0.25)));
            }
        });
        btn.setOnMouseExited(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(styleBase);
                btn.setEffect(null);
            }
        });

        return btn;
    }

    private Label creerBadgeRole(role r) {
        Label l = new Label(r.getLibelle());
        String s = "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10;";
        switch (r) {
            case ADMINISTRATEUR_ENTREPRISE: l.setStyle(s + "-fx-background-color: #eff6ff; -fx-text-fill: #2563eb;"); break;
            case RH: l.setStyle(s + "-fx-background-color: #fdf2f8; -fx-text-fill: #db2777;"); break;
            case CHEF_PROJET: l.setStyle(s + "-fx-background-color: #f0fdf4; -fx-text-fill: #16A34A;"); break;
            default: l.setStyle(s + "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;"); break;
        }
        return l;
    }
    private void resetFormulaireAjout() {
        txtNomAjout.clear(); txtPrenomAjout.clear(); txtEmailAjout.clear();
        txtTelephoneAjout.clear(); txtPosteAjout.clear();
        comboRoleAjout.setValue(null);
        dateEmbaucheAjout.setValue(LocalDate.now());
        lblErrorAjout.setText("");
        stylerInputs(txtNomAjout, txtPrenomAjout, txtEmailAjout, txtTelephoneAjout, txtPosteAjout, comboRoleAjout);
    }

    private void remplirFormulaireDetails(employe emp) {
        txtNom.setText(emp.getNom()); txtPrenom.setText(emp.getPrenom());
        txtEmail.setText(emp.getE_mail()); txtTelephone.setText(String.valueOf(emp.getTelephone()));
        txtPoste.setText(emp.getPoste()); comboRole.setValue(emp.getRole().getLibelle());
        dateEmbauche.setValue(emp.getDate_embauche());
        lblTitreDetails.setText(emp.getPrenom() + " " + emp.getNom());
        lblError.setText("");
        comboRole.setDisable(emp.getRole() == role.ADMINISTRATEUR_ENTREPRISE);
        stylerInputs(txtNom, txtPrenom, txtEmail, txtTelephone, txtPoste, comboRole);
    }

    private void confirmerSuppression(employe emp) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression"); alert.setHeaderText("Supprimer " + emp.getNom() + " ?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try { employeCRUD.supprimer(emp.getId_employé()); chargerEmployes(); }
            catch (SQLException e) { afficherErreur("Erreur", e.getMessage()); }
        }
    }

    private void initComboBoxRoles() {
        List<String> roles = List.of(role.RH.getLibelle(), role.EMPLOYE.getLibelle(), role.CHEF_PROJET.getLibelle());
        comboRole.setItems(FXCollections.observableArrayList(roles));
        comboRoleAjout.setItems(FXCollections.observableArrayList(roles));
    }

    private role parseRole(String t) {
        if(t==null) return role.EMPLOYE; String v = t.trim().toLowerCase();
        if(v.contains("rh")) return role.RH;
        if(v.contains("chef")) return role.CHEF_PROJET;
        if(v.contains("admin")) return role.ADMINISTRATEUR_ENTREPRISE;
        return role.EMPLOYE;
    }

    private void afficherSucces(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }
    private void afficherErreur(String t, String c) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }
}