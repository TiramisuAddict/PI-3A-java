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

    //Side panel labels and inputs
    @FXML private Label lblFullname, lblEmail, lblPhone;
    @FXML private ComboBox<String> comboUpdatePhase;
    @FXML private TextArea txtNotes;

    //Table selection
    @FXML private TableView<Candidat> tableCandidats;

    @FXML private TableColumn<Candidat, String> colIdentite;
    @FXML private TableColumn<Candidat, String> colEmail;
    @FXML private TableColumn<Candidat, String> colEtat;
    @FXML private TableColumn<Candidat, Date> colDate;

    private Candidat selectedCandidat;

    //comboFilterOffre
    @FXML private ComboBox<String> comboFilterOffre;

    //Stats labels
    @FXML private Label lblEnAttente, lblPreselectionne, lblEntretien, lblAccepte;

    @FXML
    public void initialize() {
        comboUpdatePhase.setItems(FXCollections.observableArrayList("En attente", "Présélectionné", "Entretien", "Accepté", "Refusé"));

        // Fill table on selection change
        tableCandidats.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                this.selectedCandidat = newSelection;
                fillSidePanel(newSelection);
            }
        });

        setupTable();
        loadCandidatData();
    }

    public void loadOffresIntoCombo() {
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
    } // LOAD OFFER NAMES IN COMBOBOX

    @FXML
    private void loadCandidatData() {
        String selectedOffre = comboFilterOffre.getSelectionModel().getSelectedItem();
        if (selectedOffre == null) return;

        OffreCRUD offreCRUD = new OffreCRUD();
        try {
            int idOffre = offreCRUD.afficher().stream()
                    .filter(o -> o.getTitrePoste().equals(selectedOffre))
                    .map(Offre::getId)
                    .findFirst()
                    .orElseThrow(() -> new SQLException("Offre not found"));

            List<Candidat> filteredList = new service.CandidatCRUD().afficher().stream()
                    .filter(c -> c.getIdOffre() == idOffre)
                    .toList();

            ObservableList<Candidat> observableCandidats = FXCollections.observableArrayList(filteredList);
            tableCandidats.setItems(observableCandidats);

            updateStatistics(filteredList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    } //LOAD CANDIDATS BASED ON SELECTED OFFER IN COMBOBOX

    private void setupTable() {
        colIdentite.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPrenom() + " " + cellData.getValue().getNom()));

        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCandidature"));

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
    } // MAP DATA TO TABLE COLUMNS

    private void updateStatistics(List<Candidat> list) {
        long pending = list.stream().filter(c -> c.getEtat().equalsIgnoreCase("En attente")).count();
        long preselected = list.stream().filter(c -> c.getEtat().equalsIgnoreCase("Présélectionné")).count();
        long interview = list.stream().filter(c -> c.getEtat().equalsIgnoreCase("Entretien")).count();
        long accepted = list.stream().filter(c -> c.getEtat().equalsIgnoreCase("Accepté")).count();

        lblEnAttente.setText(String.valueOf(pending));
        lblPreselectionne.setText(String.valueOf(preselected));
        lblEntretien.setText(String.valueOf(interview));
        lblAccepte.setText(String.valueOf(accepted));
    } //SET STATS FROM DATABASE

    private void fillSidePanel(Candidat candidat) {
        lblFullname.setText(candidat.getPrenom() + " " + candidat.getNom());
        lblEmail.setText(candidat.getEmail());
        lblPhone.setText("📞 " + candidat.getNumTel());

        txtNotes.setText(candidat.getNote());
        comboUpdatePhase.setValue(candidat.getEtat());
    } //FILL SIDE PANEL ON TABLE ROW SELECTION

    @FXML
    private void updateCandidatStatus() {
        if (selectedCandidat == null) {
            System.out.println("Aucun candidat sélectionné");
            return;
        }

        try {
            selectedCandidat.setEtat(comboUpdatePhase.getValue());
            selectedCandidat.setNote(txtNotes.getText());

            service.CandidatCRUD crud = new service.CandidatCRUD();
            crud.modifier(selectedCandidat);

            tableCandidats.refresh();

            updateStatistics(tableCandidats.getItems());
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // UPDATE CANDIDAT STATUS AND NOTES IN DB

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

    @FXML
    private void handleDownloadCV() {
        saveFile(selectedCandidat.getCvData(),
                selectedCandidat.getCvNom());
    } // DOWNLOAD CV FILE OF SELECTED CANDIDAT

    @FXML
    private void handleDownloadLettre() {
        saveFile(selectedCandidat.getLettreMotivationData(), selectedCandidat.getLettreMotivationNom());
    } // DOWNLOAD COVER LETTER FILE OF SELECTED CANDIDAT
}