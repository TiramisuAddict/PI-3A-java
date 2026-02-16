package controller.offres;

import entity.Candidat;
import entity.Offre;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import java.sql.Date;

import javafx.stage.FileChooser;
import service.OffreCRUD;
import utils.BadgeFactory;

import java.io.FileOutputStream;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CandidatController {

    //construction
    @FXML private Label lblFullname, lblEmail, lblPhone;

    @FXML private ComboBox<String> comboUpdatePhase;

    @FXML private TextArea txtNotes;

    private Candidat selectedCandidat;

    //comboFilterOffre
    @FXML private ComboBox<String> comboFilterOffre;

    @FXML private TableView<Candidat> tableCandidats;
    @FXML private TableColumn<Candidat, String> colIdentite;
    @FXML private TableColumn<Candidat, String> colEmail;
    @FXML private TableColumn<Candidat, String> colEtat;
    @FXML private TableColumn<Candidat, Date> colDate;

    @FXML
    public void initialize() {
        // 1. Setup Phase ComboBox options
        comboUpdatePhase.setItems(FXCollections.observableArrayList(
                "En attente", "Présélectionné", "Entretien", "Accepté", "Refusé"
        ));

        setupTable();
        loadOffresIntoCombo();
        loadCandidatData();

        // 2. AUTO-FILL LOGIC: Listen for table selection
        tableCandidats.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                this.selectedCandidat = newSelection;
                fillSidePanel(newSelection);
            }
        });
    }

    //TAHA : fill comboFilterOffre with the list of offers from the database (OffreCRUD)
    private void loadOffresIntoCombo() {
        try {
            OffreCRUD offreCRUD = new OffreCRUD();
            List<Offre> offres = offreCRUD.afficher();
            ObservableList <String> offreNames = offres.stream()
                    .map(Offre::getTitrePoste)
                    .collect(Collectors.toCollection(FXCollections::observableArrayList)
            );
            comboFilterOffre.setItems(offreNames);
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fillSidePanel(Candidat candidat) {
        lblFullname.setText(candidat.getPrenom() + " " + candidat.getNom());
        lblEmail.setText(candidat.getEmail());
        lblPhone.setText("📞 " + candidat.getNumTel());

        // Auto-fill the inputs
        txtNotes.setText(candidat.getNote());
        comboUpdatePhase.setValue(candidat.getEtat());
    }

    // 3. SAVE LOGIC: Update Database and UI
    @FXML
    private void handleSaveUpdate() {
        if (selectedCandidat == null) {
            System.out.println("Aucun candidat sélectionné");
            return;
        }

        try {
            // Update the object in memory
            selectedCandidat.setEtat(comboUpdatePhase.getValue());
            selectedCandidat.setNote(txtNotes.getText());

            // Update the Database
            service.CandidatCRUD crud = new service.CandidatCRUD();
            crud.modifier(selectedCandidat); // Make sure you have a modifier method

            // Visual feedback: Refresh the table row
            tableCandidats.refresh();

            // Refresh statistics (since the phase changed)
            //updateStatistics(tableCandidats.getItems());

            System.out.println("Candidat mis à jour avec succès !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TAHA : LOAD CNDIDAT DATA
    @FXML // MUST have this so FXML can see the method
    private void loadCandidatData() {
        // 1. Get the selected offer title
        String selectedOffre = comboFilterOffre.getSelectionModel().getSelectedItem();
        if (selectedOffre == null) return;

        OffreCRUD offreCRUD = new OffreCRUD();
        try {
            // 2. Find the ID of the offer
            int idOffre = offreCRUD.afficher().stream()
                    .filter(o -> o.getTitrePoste().equals(selectedOffre))
                    .map(Offre::getId)
                    .findFirst()
                    .orElseThrow(() -> new SQLException("Offre not found"));

            // 3. Get the list of candidates for this specific ID
            List<Candidat> filteredList = new service.CandidatCRUD().afficher().stream()
                    .filter(c -> c.getIdOffre() == idOffre)
                    .toList();

            ObservableList<Candidat> observableCandidats = FXCollections.observableArrayList(filteredList);
            tableCandidats.setItems(observableCandidats);

            // 5. Update your statistics too! (Optional but recommended)
            //updateStatistics(filteredList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        // 1. Map Identity (Combine First Name + Last Name)
        colIdentite.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPrenom() + " " + cellData.getValue().getNom()));

        // 2. Map Email
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // 3. Map Date
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCandidature"));

        // 4. Map Etat with a Custom Badge (Recruitment Phase)
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        colEtat.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(BadgeFactory.createBadge(item));
                }
            }
        });
    }

    private void showCandidatDetails(Candidat candidat) {
        lblFullname.setText(candidat.getPrenom() + " " + candidat.getNom());
        lblEmail.setText(candidat.getEmail());
        lblPhone.setText("📞 " + candidat.getNumTel()); // Show Phone
        txtNotes.setText(candidat.getNote());
    }

    @FXML
    private void handleDownloadCV() {
        saveFile(selectedCandidat.getCvData(), selectedCandidat.getCvNom());
    }

    @FXML
    private void handleDownloadLettre() {
        saveFile(selectedCandidat.getLettreMotivationData(), selectedCandidat.getLettreMotivationNom());
    }

    /**
     * Logic to save byte[] from DB to a physical file on the computer
     */
    private void saveFile(byte[] data, String defaultName) {
        if (data == null || data.length == 0) {
            System.out.println("Aucun fichier disponible.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le document");
        fileChooser.setInitialFileName(defaultName);

        // Let user choose where to save
        File destination = fileChooser.showSaveDialog(lblFullname.getScene().getWindow());

        if (destination != null) {
            try (FileOutputStream fos = new FileOutputStream(destination)) {
                fos.write(data);
                System.out.println("Fichier enregistré : " + destination.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}