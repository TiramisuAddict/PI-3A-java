package controller.demandes;

import entites.Demande;
import entites.HistoriqueDemande;
import service.demande.DemandeCRUD;
import service.demande.HistoriqueDemandeCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
<<<<<<< HEAD
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
=======
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class AvancerDemandeController implements Initializable {

    @FXML private Label infoTitreLabel;
    @FXML private Label infoTypeLabel;
    @FXML private Label infoPrioriteLabel;
    @FXML private Label infoStatusLabel;

    @FXML private HBox statusFlowContainer;

    @FXML private ComboBox<String> nouveauStatutCombo;
    @FXML private ComboBox<String> acteurCombo;
    @FXML private TextArea commentaireArea;
    @FXML private Label statutError;
    @FXML private Label acteurError;
    @FXML private Label commentaireError;

    @FXML private TableView<HistoriqueDemande> historiqueTable;
    @FXML private TableColumn<HistoriqueDemande, String> ancienStatutCol;
    @FXML private TableColumn<HistoriqueDemande, String> nouveauStatutCol;
    @FXML private TableColumn<HistoriqueDemande, String> acteurCol;
    @FXML private TableColumn<HistoriqueDemande, Date> dateActionCol;
    @FXML private TableColumn<HistoriqueDemande, String> commentaireCol;

    private DemandeCRUD demandeCRUD;
    private HistoriqueDemandeCRUD historiqueCRUD;
    private DemandesController parentController;
    private Demande currentDemande;

    private static final List<String> STATUS_ORDER = Arrays.asList(
            "Nouvelle", "En cours", "En attente", "Résolue", "Fermée"
    );

    public void setParentController(DemandesController parentController) {
        this.parentController = parentController;
    }

    public void setDemande(Demande demande) {
        this.currentDemande = demande;
        fillDemandeInfo(demande);
        buildStatusFlow(demande.getStatus());
        setupAllowedStatuses(demande.getStatus());
        loadHistorique(demande.getIdDemande());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        demandeCRUD = new DemandeCRUD();
        historiqueCRUD = new HistoriqueDemandeCRUD();

<<<<<<< HEAD
=======
        // Acteur ComboBox - matches ENUM('RH','ADMIN','RESPONSABLE')
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        acteurCombo.setItems(FXCollections.observableArrayList(
                "RH", "ADMIN", "RESPONSABLE"
        ));

        initializeHistoriqueTable();
        setupRealtimeValidation();
    }

    private void initializeHistoriqueTable() {
        ancienStatutCol.setCellValueFactory(new PropertyValueFactory<>("ancienStatut"));
        nouveauStatutCol.setCellValueFactory(new PropertyValueFactory<>("nouveauStatut"));
        acteurCol.setCellValueFactory(new PropertyValueFactory<>("acteur"));
        dateActionCol.setCellValueFactory(new PropertyValueFactory<>("dateAction"));
        commentaireCol.setCellValueFactory(new PropertyValueFactory<>("commentaire"));

<<<<<<< HEAD
=======
        // Color for ancien statut
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        ancienStatutCol.setCellFactory(col -> new TableCell<HistoriqueDemande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
<<<<<<< HEAD
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle(getStatusTextStyle(item)); }
            }
        });

=======
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(getStatusTextStyle(item));
                }
            }
        });

        // Color for nouveau statut
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        nouveauStatutCol.setCellFactory(col -> new TableCell<HistoriqueDemande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
<<<<<<< HEAD
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle(getStatusTextStyle(item)); }
            }
        });

=======
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(getStatusTextStyle(item));
                }
            }
        });

        // Color for acteur
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        acteurCol.setCellFactory(col -> new TableCell<HistoriqueDemande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
<<<<<<< HEAD
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
=======
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
                    setText(item);
                    switch (item) {
                        case "RH": setStyle("-fx-text-fill: #8e44ad; -fx-font-weight: bold;"); break;
                        case "ADMIN": setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;"); break;
                        case "RESPONSABLE": setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;"); break;
                        default: setStyle("");
                    }
                }
            }
        });
    }

    private String getStatusTextStyle(String status) {
        switch (status) {
            case "Nouvelle": return "-fx-text-fill: #3498db; -fx-font-weight: bold;";
            case "En cours": return "-fx-text-fill: #f39c12; -fx-font-weight: bold;";
            case "En attente": return "-fx-text-fill: #e67e22; -fx-font-weight: bold;";
            case "Résolue": return "-fx-text-fill: #27ae60; -fx-font-weight: bold;";
            case "Fermée": return "-fx-text-fill: #95a5a6; -fx-font-weight: bold;";
            default: return "";
        }
    }

    private void fillDemandeInfo(Demande demande) {
        infoTitreLabel.setText(demande.getTitre());
        infoTypeLabel.setText(demande.getTypeDemande());

        infoPrioriteLabel.setText(demande.getPriorite());
        switch (demande.getPriorite()) {
            case "HAUTE": infoPrioriteLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;"); break;
            case "NORMALE": infoPrioriteLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;"); break;
            case "BASSE": infoPrioriteLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;"); break;
        }

        infoStatusLabel.setText(demande.getStatus());
        infoStatusLabel.setStyle(getStatusTextStyle(demande.getStatus()) + "-fx-font-size: 14;");
    }

    private void buildStatusFlow(String currentStatus) {
        statusFlowContainer.getChildren().clear();

        for (int i = 0; i < STATUS_ORDER.size(); i++) {
            String status = STATUS_ORDER.get(i);
            Label statusLabel = new Label(status);
            statusLabel.setMinWidth(80);
            statusLabel.setAlignment(Pos.CENTER);

            int currentIndex = STATUS_ORDER.indexOf(currentStatus);
            int statusIndex = STATUS_ORDER.indexOf(status);

            if (statusIndex < currentIndex) {
                statusLabel.setStyle("-fx-background-color: #d5f5e3; -fx-text-fill: #27ae60; " +
                        "-fx-padding: 8 12; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 11;");
            } else if (statusIndex == currentIndex) {
                statusLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-padding: 8 12; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 12; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);");
            } else {
                statusLabel.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #bdc3c7; " +
                        "-fx-padding: 8 12; -fx-background-radius: 15; -fx-font-size: 11;");
            }

            statusFlowContainer.getChildren().add(statusLabel);

            if (i < STATUS_ORDER.size() - 1) {
                Label arrow = new Label("→");
                arrow.setStyle("-fx-font-size: 16; -fx-text-fill: #bdc3c7;");
                statusFlowContainer.getChildren().add(arrow);
            }
        }
    }

    private void setupAllowedStatuses(String currentStatus) {
        List<String> allowed = new ArrayList<>();

        switch (currentStatus) {
            case "Nouvelle":
                allowed.addAll(Arrays.asList("En cours", "En attente"));
                break;
            case "En cours":
                allowed.addAll(Arrays.asList("En attente", "Résolue"));
                break;
            case "En attente":
                allowed.addAll(Arrays.asList("En cours", "Résolue"));
                break;
            case "Résolue":
                allowed.addAll(Arrays.asList("Fermée", "En cours"));
                break;
            case "Fermée":
                break;
        }

        nouveauStatutCombo.setItems(FXCollections.observableArrayList(allowed));

        if (allowed.isEmpty()) {
            nouveauStatutCombo.setDisable(true);
            nouveauStatutCombo.setPromptText("Aucune transition possible");
        }
    }

    private void loadHistorique(int idDemande) {
        try {
            List<HistoriqueDemande> historiques = historiqueCRUD.getByDemande(idDemande);
            ObservableList<HistoriqueDemande> list = FXCollections.observableArrayList(historiques);
            historiqueTable.setItems(list);
        } catch (SQLException e) {
            System.out.println("Error loading historique: " + e.getMessage());
        }
    }

    private void setupRealtimeValidation() {
        nouveauStatutCombo.valueProperty().addListener((obs, o, n) -> {
<<<<<<< HEAD
            if (n != null) { statutError.setText(""); nouveauStatutCombo.setStyle(""); }
        });
        acteurCombo.valueProperty().addListener((obs, o, n) -> {
            if (n != null) { acteurError.setText(""); acteurCombo.setStyle(""); }
        });
        commentaireArea.textProperty().addListener((obs, o, n) -> {
            if (!n.trim().isEmpty()) { commentaireError.setText(""); commentaireArea.setStyle(""); }
=======
            if (n != null) {
                statutError.setText("");
                nouveauStatutCombo.setStyle("");
            }
        });

        acteurCombo.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                acteurError.setText("");
                acteurCombo.setStyle("");
            }
        });

        commentaireArea.textProperty().addListener((obs, o, n) -> {
            if (!n.trim().isEmpty()) {
                commentaireError.setText("");
                commentaireArea.setStyle("");
            }
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        });
    }

    @FXML
    private void avancerStatut() {
        if (!validateForm()) return;

        String ancienStatut = currentDemande.getStatus();
        String nouveauStatut = nouveauStatutCombo.getValue();
        String acteur = acteurCombo.getValue();
        String commentaire = commentaireArea.getText().trim();

        try {
<<<<<<< HEAD
            currentDemande.setStatus(nouveauStatut);
            demandeCRUD.modifier(currentDemande);

=======
            // Update demande status
            currentDemande.setStatus(nouveauStatut);
            demandeCRUD.modifier(currentDemande);

            // Create historique entry
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
            HistoriqueDemande historique = new HistoriqueDemande();
            historique.setIdDemande(currentDemande.getIdDemande());
            historique.setAncienStatut(ancienStatut);
            historique.setNouveauStatut(nouveauStatut);
            historique.setDateAction(new java.util.Date());
            historique.setActeur(acteur);
            historique.setCommentaire(commentaire);
            historiqueCRUD.ajouter(historique);

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Statut changé de \"" + ancienStatut + "\" à \"" + nouveauStatut + "\"\nPar: " + acteur);

            retourListe();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (nouveauStatutCombo.getValue() == null) {
            statutError.setText("Sélectionnez un nouveau statut");
            nouveauStatutCombo.setStyle("-fx-border-color: red; -fx-border-width: 1.5;");
            isValid = false;
        }

        if (acteurCombo.getValue() == null) {
            acteurError.setText("Sélectionnez un acteur");
            acteurCombo.setStyle("-fx-border-color: red; -fx-border-width: 1.5;");
            isValid = false;
        }

        if (commentaireArea.getText().trim().isEmpty()) {
            commentaireError.setText("Le commentaire est obligatoire");
            commentaireArea.setStyle("-fx-border-color: red; -fx-border-width: 1.5;");
            isValid = false;
        } else if (commentaireArea.getText().trim().length() < 5) {
            commentaireError.setText("Minimum 5 caractères");
            commentaireArea.setStyle("-fx-border-color: red; -fx-border-width: 1.5;");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void retourListe() {
        try {
<<<<<<< HEAD
            NavigationHelper.loadView(infoTitreLabel, "demandes.fxml");
=======
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/demandes.fxml"));
            Parent root = loader.load();
            infoTitreLabel.getScene().setRoot(root);
>>>>>>> b2242b4f91f46ba2b636098f6c0f8aa2658accf5
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}