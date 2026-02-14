package controller.projets;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;

import Models.Tache;
import Models.priority;
import Models.statut_t;

import service.TacheCRUD;
import service.Equipe_projet;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AjoutTacheController implements Initializable {

    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;
    @FXML private Label modalTitle;

    @FXML private TextField titleField;
    @FXML private TextArea descArea;

    @FXML private ComboBox<statut_t> statusBox;
    @FXML private ComboBox<EmployeeOption> employeeBox;
    @FXML private DatePicker startPicker;
    @FXML private DatePicker duePicker;

    @FXML private ComboBox<priority> priorityBox;
    @FXML private Spinner<Integer> progressSpinner;

    @FXML private Label infoLabel;

    private int projectId;
    private Runnable onSaved;
    private Tache tacheToEdit = null; // For edit mode

    private final TacheCRUD tacheCRUD = new TacheCRUD();
    private final Equipe_projet groupeProjetCRUD = new Equipe_projet();

    public void setProjectId(int projectId) { this.projectId = projectId; }
    public void setOnSaved(Runnable onSaved) { this.onSaved = onSaved; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusBox.getItems().setAll(statut_t.values());
        statusBox.setValue(statut_t.A_FAIRE);

        priorityBox.getItems().setAll(priority.values());
        priorityBox.setValue(priority.MOYENNE);

        progressSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0, 5));
        progressSpinner.setEditable(true);

        startPicker.setValue(LocalDate.now());

        btnClose.setOnAction(this::close);
        btnCancel.setOnAction(this::close);
        btnSave.setOnAction(e -> save());

        // Hide info label initially
        if (infoLabel != null) {
            infoLabel.setVisible(false);
            infoLabel.setManaged(false);
        }
    }

    public void loadEmployeesForProject() {
        try {
            List<EmployeeOption> options = groupeProjetCRUD.getEmployeesForProject(projectId);
            employeeBox.getItems().setAll(options);
        } catch (SQLException e) {
            e.printStackTrace();
            showInfo("Erreur chargement employés du projet.");
        }
    }

    /**
     * Set a task to edit. Call this after loadEmployeesForProject()
     */
    public void setTacheToEdit(Tache t) {
        this.tacheToEdit = t;

        // Update modal title and button
        if (modalTitle != null) {
            modalTitle.setText("✏️ Modifier la tâche");
        }
        btnSave.setText("💾 Enregistrer");

        // Fill form with task data
        titleField.setText(t.getTitre());
        descArea.setText(t.getDescription());
        statusBox.setValue(t.getStatut_tache());
        priorityBox.setValue(t.getPriority_tache());
        progressSpinner.getValueFactory().setValue(t.getProgression());

        if (t.getDate_deb() != null) startPicker.setValue(t.getDate_deb());
        if (t.getDate_limite() != null) duePicker.setValue(t.getDate_limite());

        // Select the assigned employee
        for (EmployeeOption opt : employeeBox.getItems()) {
            if (opt.id() == t.getId_employe()) {
                employeeBox.setValue(opt);
                break;
            }
        }
    }

    private void save() {
        hideInfo();

        String titre = titleField.getText() == null ? "" : titleField.getText().trim();
        if (titre.isBlank()) {
            showInfo("Le titre est obligatoire.");
            return;
        }

        statut_t st = statusBox.getValue();
        if (st == null) {
            showInfo("Veuillez choisir un statut.");
            return;
        }

        EmployeeOption emp = employeeBox.getValue();
        if (emp == null) {
            showInfo("Veuillez choisir un employé assigné.");
            return;
        }

        LocalDate debut = startPicker.getValue();
        LocalDate limite = duePicker.getValue();
        if (debut != null && limite != null && limite.isBefore(debut)) {
            showInfo("La date limite doit être >= date début.");
            return;
        }

        // Team validation
        try {
            boolean ok = groupeProjetCRUD.exists(projectId, emp.id());
            if (!ok) {
                showInfo("Cet employé n'appartient pas à l'équipe du projet.");
                return;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showInfo("Erreur validation équipe projet.");
            return;
        }

        // Build or update entity
        Tache t = tacheToEdit != null ? tacheToEdit : new Tache();
        t.setId_projet(projectId);
        t.setId_employe(emp.id());
        t.setTitre(titre);
        t.setDescription(descArea.getText());
        t.setStatut_tache(st);
        t.setPriority_tache(priorityBox.getValue());
        t.setProgression(progressSpinner.getValue());

        if (debut != null) t.setDate_deb(debut);
        if (limite != null) t.setDate_limite(limite);

        try {
            if (tacheToEdit != null) {
                // UPDATE
                tacheCRUD.modifier(t);
            } else {
                // INSERT
                tacheCRUD.ajouter(t);
            }

            if (onSaved != null) onSaved.run();
            close(null);

        } catch (SQLException ex) {
            ex.printStackTrace();
            showInfo("Erreur : " + ex.getMessage());
        }
    }

    private void showInfo(String msg) {
        if (infoLabel != null) {
            infoLabel.setText(msg);
            infoLabel.setVisible(true);
            infoLabel.setManaged(true);
        }
    }

    private void hideInfo() {
        if (infoLabel != null) {
            infoLabel.setText("");
            infoLabel.setVisible(false);
            infoLabel.setManaged(false);
        }
    }

    private void close(javafx.event.ActionEvent e) {
        Stage s;
        if (e != null) {
            s = (Stage) ((Node) e.getSource()).getScene().getWindow();
        } else {
            s = (Stage) btnCancel.getScene().getWindow();
        }
        s.close();
    }

    // Helper record for employee display in ComboBox
    public record EmployeeOption(int id, String label) {
        @Override public String toString() { return label; }
    }
}
