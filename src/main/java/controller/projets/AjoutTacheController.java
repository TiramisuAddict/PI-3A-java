package controller.projets;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;

import Models.Tache;
import Models.priority;
import Models.statut_t;

import service.TacheCRUD;
import service.Equipe_projet;
import service.WorkloadBalancingAPI;
import service.WorkloadBalancingAPI.WorkloadAnalysisResult;
import service.WorkloadBalancingAPI.EmployeeWorkload;

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

    // Workload API components
    @FXML private VBox suggestionBox;
    @FXML private Label suggestionLabel;
    @FXML private Button btnApplySuggestion;
    @FXML private Button btnViewWorkload;

    private int projectId;
    private Runnable onSaved;
    private Tache tacheToEdit = null; // For edit mode

    // Workload API
    private final WorkloadBalancingAPI workloadAPI = new WorkloadBalancingAPI();
    private WorkloadAnalysisResult currentAnalysis = null;

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

            // Run workload analysis and show suggestion (only for new tasks)
            if (tacheToEdit == null) {
                analyzeAndSuggestEmployee();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showInfo("Erreur chargement employés du projet.");
        }
    }

    /**
     * Analyze workload and suggest the best employee
     */
    private void analyzeAndSuggestEmployee() {
        try {
            currentAnalysis = workloadAPI.analyzeProjectWorkload(projectId);

            if (currentAnalysis.hasSuggestion()) {
                EmployeeWorkload suggested = currentAnalysis.getSuggestedEmployee();

                // Show suggestion in UI
                showSuggestion(
                    "💡 Suggestion IA: " + suggested.getEmployeeName(),
                    currentAnalysis.getSuggestionReason(),
                    suggested.getEmployeeId()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Don't show error - suggestion is optional feature
        }
    }

    /**
     * Display the workload suggestion in the UI
     */
    private void showSuggestion(String title, String reason, int suggestedEmployeeId) {
        if (suggestionBox != null) {
            suggestionLabel.setText(title + "\n" + reason);
            suggestionBox.setVisible(true);
            suggestionBox.setManaged(true);

            btnApplySuggestion.setOnAction(e -> {
                // Find and select the suggested employee
                for (EmployeeOption opt : employeeBox.getItems()) {
                    if (opt.id() == suggestedEmployeeId) {
                        employeeBox.setValue(opt);
                        showInfo("✅ Employé suggéré sélectionné!");
                        break;
                    }
                }
            });

            btnViewWorkload.setOnAction(e -> showWorkloadDetails());
        }
    }

    /**
     * Show detailed workload analysis in a popup
     */
    private void showWorkloadDetails() {
        if (currentAnalysis == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Analyse de Charge de Travail");
        alert.setHeaderText("📊 Smart Workload Balancing API");

        // Build detailed content
        StringBuilder content = new StringBuilder();
        content.append("Classement des employés (du plus disponible au moins disponible):\n\n");

        int rank = 1;
        for (EmployeeWorkload w : currentAnalysis.getRankedEmployees()) {
            content.append(rank++).append(". ").append(w.getAvailabilityStatus())
                   .append(" ").append(w.getEmployeeName()).append("\n")
                   .append("    • Tâches actives: ").append(w.getTotalActiveTasks()).append("\n")
                   .append("    • Haute priorité: ").append(w.getHighPriorityTasks()).append("\n")
                   .append("    • Tâches urgentes: ").append(w.getUrgentTasks()).append("\n")
                   .append("    • Score charge: ").append(String.format("%.1f", w.getWorkloadScore())).append("\n\n");
        }

        if (currentAnalysis.hasSuggestion()) {
            content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            content.append("💡 RECOMMANDATION:\n");
            content.append(currentAnalysis.getSuggestionReason());
        }

        TextArea textArea = new TextArea(content.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(500);
        textArea.setPrefHeight(400);
        textArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");

        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefWidth(550);
        alert.showAndWait();
    }

    /**
     * Set default status for new task (used by Kanban board when clicking add in a specific column)
     */
    public void setDefaultStatus(statut_t status) {
        if (status != null && statusBox != null) {
            statusBox.setValue(status);
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
