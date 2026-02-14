package controller.projets;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// ======= ADAPT THESE IMPORTS =======
import service.ProjetCRUD;
import service.TacheCRUD;
import service.EmployeeCRUD;
import service.Equipe_projet;
import service.EmployeeCRUD.EmployeeInfo;
import Models.Projet;
import Models.Tache;
import Models.statut;
import Models.statut_t;
import Models.priority;

public class ProjetsController {

    // LEFT
    @FXML private TextField searchField;
    @FXML private ComboBox<statut> statusFilter;
    @FXML private ComboBox<String> sortBox;
    @FXML private Button btnNewProject;
    @FXML private Button btnRefresh;
    @FXML private ListView<Projet> projectsList;
    @FXML private Label projectsCountLabel;
    @FXML private Label activeCountLabel;
    @FXML private Label emptyStateLabel;

    // RIGHT (details panel)
    @FXML private TextField detailNomField;
    @FXML private TextArea detailDescArea;
    @FXML private DatePicker detailDebutPicker;
    @FXML private DatePicker detailFinPrevuePicker;
    @FXML private ComboBox<statut> detailStatutBox;
    @FXML private ComboBox<priority> detailPrioriteBox;
    @FXML private ComboBox<EmployeeInfo> detailResponsableBox;
    @FXML private Button btnSaveDetails;
    @FXML private Button btnDeleteProject;
    @FXML private Label detailsInfoLabel;

    // Team management in details panel
    @FXML private Label teamCountLabel;
    @FXML private ComboBox<EmployeeInfo> addEmployeeBox;
    @FXML private Button btnAddEmployee;
    @FXML private ListView<EmployeeInfo> detailTeamListView;

    // TASKS TAB
    @FXML private Label selectedProjectTitle;
    @FXML private TextField taskSearchField;
    @FXML private ComboBox<statut_t> taskStatusFilter;
    @FXML private Button btnRefreshTasks;
    @FXML private Button btnCreateTaskInline;
    @FXML private TableView<Tache> tasksTable;

    // Task stats labels
    @FXML private Label taskTotalLabel;
    @FXML private Label taskCompletedLabel;
    @FXML private Label taskInProgressLabel;

    @FXML private TableColumn<Tache, String> colTitre;
    @FXML private TableColumn<Tache, Integer> colAssignee;
    @FXML private TableColumn<Tache, priority> colPriorite;
    @FXML private TableColumn<Tache, statut_t> colStatut;
    @FXML private TableColumn<Tache, LocalDate> colDateDebut;
    @FXML private TableColumn<Tache, LocalDate> colDueDate;
    @FXML private TableColumn<Tache, Integer> colProgress;
    @FXML private TableColumn<Tache, Void> colActions;

    @FXML private Label taskInfoLabel;


    private final ProjetCRUD projetCRUD = new ProjetCRUD();
    private final TacheCRUD tacheCRUD = new TacheCRUD();
    private final EmployeeCRUD employeeCRUD = new EmployeeCRUD();
    private final Equipe_projet equipeCRUD = new Equipe_projet();

    private final ObservableList<Projet> masterProjects = FXCollections.observableArrayList();
    private final ObservableList<Projet> filteredProjects = FXCollections.observableArrayList();
    private final ObservableList<Tache> masterTasks = FXCollections.observableArrayList();
    private final ObservableList<Tache> filteredTasks = FXCollections.observableArrayList();

    // Team management
    private final ObservableList<EmployeeInfo> allEmployees = FXCollections.observableArrayList();
    private final ObservableList<EmployeeInfo> projectTeamMembers = FXCollections.observableArrayList();

    private Projet selectedProject = null;

    @FXML
    public void initialize() {

        // fill filter combos (optional)
        statusFilter.getItems().setAll(statut.values());
        statusFilter.getItems().addFirst(null); // allow "all"
        statusFilter.setPromptText("Statut");

        sortBox.getItems().setAll("Nom (A-Z)", "Nom (Z-A)", "Date début (↑)", "Date début (↓)");
        sortBox.setPromptText("Trier");

        // details combos
        detailStatutBox.getItems().setAll(statut.values());
        detailPrioriteBox.getItems().setAll(priority.values());

        // Load employees from DB
        loadAllEmployees();

        // list
        projectsList.setItems(filteredProjects);
        setupCardCellFactory();

        // listeners
        btnRefresh.setOnAction(e -> loadProjectsFromDB());
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        sortBox.valueProperty().addListener((obs, o, n) -> applyFilters());

        projectsList.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            selectedProject = newP;
            fillDetailsFromSelected();
            loadTasksForSelectedProject();
            loadProjectTeam();
        });

        btnSaveDetails.setOnAction(e -> saveSelectedProject());
        btnDeleteProject.setOnAction(e -> deleteSelectedProject());

        btnNewProject.setOnAction(e -> openAddProject());

        // Team management buttons
        btnAddEmployee.setOnAction(e -> addEmployeeToTeam());
        setupTeamListView();

        // Tasks tab buttons
        btnCreateTaskInline.setOnAction(e -> openCreateTaskModal());
        btnRefreshTasks.setOnAction(e -> loadTasksForSelectedProject());
        taskSearchField.textProperty().addListener((obs, o, n) -> applyTaskFilters());

        // Task status filter
        taskStatusFilter.getItems().setAll(statut_t.values());
        taskStatusFilter.getItems().addFirst(null);
        taskStatusFilter.valueProperty().addListener((obs, o, n) -> applyTaskFilters());

        // Setup tasks table
        setupTasksTable();

        // initial state
        setDetailsEnabled(false);
        detailsInfoLabel.setText("");
        selectedProjectTitle.setText("(Aucun projet sélectionné)");

        // load data
        loadProjectsFromDB();
    }

    private void loadAllEmployees() {
        try {
            List<EmployeeInfo> employees = employeeCRUD.getAllEmployees();
            allEmployees.setAll(employees);
            detailResponsableBox.setItems(FXCollections.observableArrayList(employees));
            addEmployeeBox.setItems(FXCollections.observableArrayList(employees));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les employés: " + e.getMessage());
        }
    }

    // ===================== LOAD / FILTER =====================

    private void loadProjectsFromDB() {
        try {
            List<Projet> data = projetCRUD.afficher(); // adapt if method name differs
            masterProjects.setAll(data);
            applyFilters();

            // auto select first item if exists
            /*if (!filteredProjects.isEmpty()) {
                projectsList.getSelectionModel().selectFirst();
            } else {
                selectedProject = null;
                clearDetails();
            }*/

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur chargement", e.getMessage());
        }
    }

    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        statut stFilter = statusFilter.getValue();
        String sort = sortBox.getValue();

        List<Projet> tmp = masterProjects.stream()
                .filter(p -> matchesQuery(p, q))
                .filter(p -> stFilter == null || (p.getStatut() == stFilter))
                .collect(Collectors.toList());

        // sorting
        if (sort != null) {
            switch (sort) {
                case "Nom (A-Z)" -> tmp.sort((a,b) -> safe(a.getNom()).compareToIgnoreCase(safe(b.getNom())));
                case "Nom (Z-A)" -> tmp.sort((a,b) -> safe(b.getNom()).compareToIgnoreCase(safe(a.getNom())));
                case "Date début (↑)" -> tmp.sort((a,b) -> safeLocalDate(a.getDate_debut()).compareTo(safeLocalDate(b.getDate_debut())));
                case "Date début (↓)" -> tmp.sort((a,b) -> safeLocalDate(b.getDate_debut()).compareTo(safeLocalDate(a.getDate_debut())));
            }
        }

        filteredProjects.setAll(tmp);

        updateCounters();
        updateEmptyState();

        // keep selection if possible
        if (selectedProject != null && filteredProjects.contains(selectedProject)) {
            projectsList.getSelectionModel().select(selectedProject);
        }
    }

    private boolean matchesQuery(Projet p, String q) {
        if (q.isBlank()) return true;
        String nom = safe(p.getNom()).toLowerCase();
        String desc = safe(p.getDescription()).toLowerCase();
        return nom.contains(q) || desc.contains(q);
    }

    private void updateCounters() {
        projectsCountLabel.setText(String.valueOf(masterProjects.size()));

        long actifs = masterProjects.stream()
                .filter(p -> p.getStatut() == statut.EN_COURS)
                .count();

        activeCountLabel.setText(String.valueOf(actifs));
    }

    private void updateEmptyState() {
        boolean empty = filteredProjects.isEmpty();
        emptyStateLabel.setVisible(empty);
        emptyStateLabel.setManaged(empty);
    }

    // ===================== TASKS TABLE SETUP =====================

    private void setupTasksTable() {
        // Set up column cell value factories
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colAssignee.setCellValueFactory(new PropertyValueFactory<>("id_employe"));
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priority_tache"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut_tache"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_deb"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("date_limite"));
        colProgress.setCellValueFactory(new PropertyValueFactory<>("progression"));

        // Custom cell factory for priority with colored badges
        colPriorite.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(priority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(prettyEnum(item));
                    badge.setStyle(getPriorityBadgeStyle(item));
                    setGraphic(badge);
                }
            }
        });

        // Custom cell factory for status with colored badges
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(statut_t item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(prettyEnum(item));
                    badge.setStyle(getTaskStatusBadgeStyle(item));
                    setGraphic(badge);
                }
            }
        });

        // Custom cell factory for progress bar
        colProgress.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    ProgressBar bar = new ProgressBar(item / 100.0);
                    bar.setPrefWidth(80);
                    bar.setStyle("-fx-accent: #10B981;");
                    Label lbl = new Label(item + "%");
                    lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
                    HBox box = new HBox(6, bar, lbl);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });

        // Action buttons column (Edit / Delete)
        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Tache, Void> call(TableColumn<Tache, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("✏️");
                    private final Button btnDelete = new Button("🗑");
                    private final HBox pane = new HBox(6, btnEdit, btnDelete);

                    {
                        btnEdit.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 8;");
                        btnDelete.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 8;");
                        pane.setAlignment(Pos.CENTER);

                        btnEdit.setOnAction(e -> {
                            Tache tache = getTableView().getItems().get(getIndex());
                            openEditTaskModal(tache);
                        });

                        btnDelete.setOnAction(e -> {
                            Tache tache = getTableView().getItems().get(getIndex());
                            deleteTask(tache);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        });

        // Bind table to filtered tasks list
        tasksTable.setItems(filteredTasks);
    }

    private String getPriorityBadgeStyle(priority p) {
        String base = "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 700;";
        return switch (p) {
            case HAUTE -> base + "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;";
            case MOYENNE -> base + "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;";
            case BASSE -> base + "-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A;";
        };
    }

    private String getTaskStatusBadgeStyle(statut_t s) {
        String base = "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 700;";
        return switch (s) {
            case A_FAIRE -> base + "-fx-background-color: #E2E8F0; -fx-text-fill: #475569;";
            case EN_COURS -> base + "-fx-background-color: #DBEAFE; -fx-text-fill: #2563EB;";
            case BLOCQUEE -> base + "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;";
            case TERMINEE -> base + "-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A;";
        };
    }

    // ===================== TEAM MANAGEMENT =====================

    private void setupTeamListView() {
        detailTeamListView.setItems(projectTeamMembers);
        detailTeamListView.setCellFactory(lv -> new ListCell<>() {
            private final Button btnRemove = new Button("✕");
            private final HBox container = new HBox(10);
            private final Label nameLabel = new Label();

            {
                btnRemove.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-background-radius: 50; -fx-min-width: 24; -fx-min-height: 24; -fx-cursor: hand;");
                btnRemove.setOnAction(e -> {
                    EmployeeInfo emp = getItem();
                    if (emp != null) {
                        removeEmployeeFromTeam(emp);
                    }
                });
                container.setAlignment(Pos.CENTER_LEFT);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                container.getChildren().addAll(nameLabel, spacer, btnRemove);
            }

            @Override
            protected void updateItem(EmployeeInfo emp, boolean empty) {
                super.updateItem(emp, empty);
                if (empty || emp == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText("👤 " + emp.getFullName());
                    nameLabel.setStyle("-fx-font-weight: 600;");
                    setGraphic(container);
                }
            }
        });
    }

    private void loadProjectTeam() {
        projectTeamMembers.clear();
        if (selectedProject == null) {
            updateTeamCount();
            return;
        }

        try {
            List<Integer> teamIds = equipeCRUD.getEmployeeIdsForProject(selectedProject.getProjet_id());
            List<EmployeeInfo> teamMembers = allEmployees.stream()
                    .filter(e -> teamIds.contains(e.id()))
                    .collect(Collectors.toList());
            projectTeamMembers.setAll(teamMembers);
            updateAddEmployeeDropdown();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateTeamCount();
    }

    private void updateAddEmployeeDropdown() {
        // Show only employees not already in the team
        List<EmployeeInfo> available = allEmployees.stream()
                .filter(e -> !projectTeamMembers.contains(e))
                .collect(Collectors.toList());
        addEmployeeBox.setItems(FXCollections.observableArrayList(available));
        addEmployeeBox.setValue(null);
    }

    private void addEmployeeToTeam() {
        if (selectedProject == null) return;
        EmployeeInfo selected = addEmployeeBox.getValue();
        if (selected == null) {
            showError("Sélection requise", "Veuillez sélectionner un employé à ajouter.");
            return;
        }

        try {
            equipeCRUD.addEmployeeToProject(selectedProject.getProjet_id(), selected.id());
            loadProjectTeam();
            detailsInfoLabel.setText("✅ " + selected.getFullName() + " ajouté à l'équipe.");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ajouter l'employé: " + e.getMessage());
        }
    }

    private void removeEmployeeFromTeam(EmployeeInfo emp) {
        if (selectedProject == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Retirer " + emp.getFullName() + " de l'équipe ?");
        confirm.setContentText("L'employé sera retiré du projet.");

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    equipeCRUD.removeEmployeeFromProject(selectedProject.getProjet_id(), emp.id());
                    loadProjectTeam();
                    detailsInfoLabel.setText("✅ " + emp.getFullName() + " retiré de l'équipe.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Erreur", "Impossible de retirer l'employé: " + e.getMessage());
                }
            }
        });
    }

    private void updateTeamCount() {
        teamCountLabel.setText(projectTeamMembers.size() + " membre(s)");
    }

    // ===================== DETAILS PANEL =====================

    private void fillDetailsFromSelected() {
        if (selectedProject == null) {
            clearDetails();
            selectedProjectTitle.setText("(Aucun projet sélectionné)");
            return;
        }

        setDetailsEnabled(true);
        selectedProjectTitle.setText(safe(selectedProject.getNom()));

        detailNomField.setText(selectedProject.getNom());
        detailDescArea.setText(selectedProject.getDescription() == null ? "" : selectedProject.getDescription());

        if (selectedProject.getDate_debut() != null) {
            detailDebutPicker.setValue(selectedProject.getDate_debut());
        } else {
            detailDebutPicker.setValue(null);
        }

        if (selectedProject.getDate_fin_prevue() != null) {
            detailFinPrevuePicker.setValue(selectedProject.getDate_fin_prevue());
        } else {
            detailFinPrevuePicker.setValue(null);
        }

        detailStatutBox.setValue(selectedProject.getStatut());
        detailPrioriteBox.setValue(selectedProject.getPriority());

        // Find and set responsable
        int respId = selectedProject.getResponsable_id();
        for (EmployeeInfo emp : allEmployees) {
            if (emp.id() == respId) {
                detailResponsableBox.setValue(emp);
                break;
            }
        }

        detailsInfoLabel.setText("");
    }

    private void clearDetails() {
        setDetailsEnabled(false);
        detailNomField.clear();
        detailDescArea.clear();
        detailDebutPicker.setValue(null);
        detailFinPrevuePicker.setValue(null);
        detailStatutBox.setValue(null);
        detailPrioriteBox.setValue(null);
        detailResponsableBox.setValue(null);
        detailsInfoLabel.setText("Sélectionnez un projet pour afficher ses détails.");
    }

    private void setDetailsEnabled(boolean enabled) {
        detailNomField.setDisable(!enabled);
        detailDescArea.setDisable(!enabled);
        detailDebutPicker.setDisable(!enabled);
        detailFinPrevuePicker.setDisable(!enabled);
        detailStatutBox.setDisable(!enabled);
        detailPrioriteBox.setDisable(!enabled);
        detailResponsableBox.setDisable(!enabled);

        btnSaveDetails.setDisable(!enabled);
        btnDeleteProject.setDisable(!enabled);
    }

    // ===================== SAVE / DELETE =====================

    private void saveSelectedProject() {
        if (selectedProject == null) return;

        String nom = detailNomField.getText();
        if (nom == null || nom.isBlank()) {
            showError("Validation", "Le nom du projet est obligatoire.");
            return;
        }

        EmployeeInfo resp = detailResponsableBox.getValue();
        if (resp == null) {
            showError("Validation", "Veuillez choisir un responsable.");
            return;
        }

        LocalDate debut = detailDebutPicker.getValue();
        if (debut == null) {
            showError("Validation", "La date début est obligatoire.");
            return;
        }

        LocalDate finPrev = detailFinPrevuePicker.getValue();
        if (finPrev != null && finPrev.isBefore(debut)) {
            showError("Validation", "La date fin prévue doit être après la date début.");
            return;
        }

        statut st = detailStatutBox.getValue();
        priority pr = detailPrioriteBox.getValue();

        try {
            // Update selectedProject object
            selectedProject.setNom(nom.trim());
            selectedProject.setDescription(detailDescArea.getText());
            selectedProject.setResponsable_id(resp.id());
            selectedProject.setDate_debut(debut);
            selectedProject.setDate_fin_prevue(finPrev);
            selectedProject.setStatut(st);
            selectedProject.setPriority(pr);

            projetCRUD.modifier(selectedProject);

            detailsInfoLabel.setText("✅ Projet mis à jour.");
            loadProjectsFromDB();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur SQL", e.getMessage());
        }
    }

    private void deleteSelectedProject() {
        if (selectedProject == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le projet ?");
        confirm.setContentText("Projet : " + safe(selectedProject.getNom()) + "\nCette action est irréversible.");

        ButtonType btnYes = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnYes, btnNo);

        confirm.showAndWait().ifPresent(type -> {
            if (type == btnYes) {
                try {
                    projetCRUD.supprimer(selectedProject.getProjet_id()); // adapt getter name
                    selectedProject = null;
                    loadProjectsFromDB();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Erreur suppression", e.getMessage());
                }
            }
        });
    }

    // ===================== ADD PROJECT MODAL =====================

    private void openAddProject() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutProjet.fxml"));
            Parent root = loader.load();

            AjoutProjetController ctrl = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Nouveau projet");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            if (ctrl.isSaved()) {
                loadProjectsFromDB();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    // ===================== LIST CARD CELL =====================

    private void setupCardCellFactory() {
        projectsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Projet p, boolean empty) {
                super.updateItem(p, empty);

                if (empty || p == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                HBox row = new HBox(12);
                row.setMinHeight(70);
                row.setStyle(
                        "-fx-padding: 12;" +
                                "-fx-background-color: white;" +
                                "-fx-background-radius: 14;" +
                                "-fx-border-radius: 14;" +
                                "-fx-border-color: #E5E7EB;"
                );


                VBox left = new VBox(4);

                Label title = new Label(safe(p.getNom()));
                title.setStyle("-fx-font-size: 14px; -fx-font-weight: 800;");

                String subtitleText =
                        "Responsable #" + p.getResponsable_id() +
                                " • Priorité: " + prettyEnum(p.getPriority());

                Label subtitle = new Label(subtitleText);
                subtitle.setStyle("-fx-opacity: 0.75;");

                left.getChildren().addAll(title, subtitle);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label badge = new Label(prettyEnum(p.getStatut()));
                badge.setStyle(badgeStyleForStatut(p.getStatut()));

                row.getChildren().addAll(left, spacer, badge);

                VBox wrapper = new VBox(row);
                wrapper.setStyle("-fx-padding: 6 0 6 0;");

                setText(null);
                setGraphic(wrapper);

            }
        });
    }


    private String safe(String s) {
        return (s == null || s.isBlank()) ? "(Sans nom)" : s;
    }

    private String prettyEnum(Object e) {
        if (e == null) return "—";
        String s = e.toString().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private LocalDate safeLocalDate(LocalDate d) {
        return d == null ? LocalDate.MIN : d;
    }

    private String badgeStyleForStatut(Object statutObj) {
        String base = "-fx-padding: 6 12; -fx-background-radius: 999; -fx-font-weight: 800;";
        if (statutObj == null) return base + "-fx-background-color: #E5E7EB; -fx-text-fill: #374151;";

        String s = statutObj.toString().toUpperCase();

        if (s.contains("EN_COURS")) return base + "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;";
        if (s.contains("PLANIF")) return base + "-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8;";
        if (s.contains("ATTENTE")) return base + "-fx-background-color: #FEF9C3; -fx-text-fill: #854D0E;";
        if (s.contains("TERMINE")) return base + "-fx-background-color: #E5E7EB; -fx-text-fill: #111827;";
        if (s.contains("ANNULE")) return base + "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;";

        return base + "-fx-background-color: #E5E7EB; -fx-text-fill: #374151;";
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void openCreateTaskModal() {
        if (selectedProject == null) {
            showError("Aucun projet sélectionné", "Veuillez d'abord sélectionner un projet.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutTache.fxml"));
            Parent root = loader.load();

            AjoutTacheController c = loader.getController();
            c.setProjectId(selectedProject.getProjet_id());
            c.setOnSaved(this::loadTasksForSelectedProject);
            c.loadEmployeesForProject();

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle("Nouvelle tâche");
            modal.setScene(new Scene(root));
            modal.showAndWait();

        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire de tâche: " + ex.getMessage());
        }
    }

    private void loadTasksForSelectedProject() {
        if (selectedProject == null) {
            masterTasks.clear();
            filteredTasks.clear();
            updateTaskInfo();
            return;
        }

        try {
            List<Tache> allTasks = tacheCRUD.afficher();

            // Filter tasks for the selected project
            List<Tache> projectTasks = allTasks.stream()
                    .filter(t -> t.getId_projet() == selectedProject.getProjet_id())
                    .collect(Collectors.toList());

            masterTasks.setAll(projectTasks);
            applyTaskFilters();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur chargement tâches", e.getMessage());
        }
    }

    private void applyTaskFilters() {
        String q = taskSearchField.getText() == null ? "" : taskSearchField.getText().trim().toLowerCase();
        statut_t statusFilterVal = taskStatusFilter.getValue();

        List<Tache> tmp = masterTasks.stream()
                .filter(t -> matchesTaskQuery(t, q))
                .filter(t -> statusFilterVal == null || t.getStatut_tache() == statusFilterVal)
                .collect(Collectors.toList());

        filteredTasks.setAll(tmp);
        updateTaskInfo();
    }

    private boolean matchesTaskQuery(Tache t, String q) {
        if (q.isBlank()) return true;
        String titre = t.getTitre() == null ? "" : t.getTitre().toLowerCase();
        String desc = t.getDescription() == null ? "" : t.getDescription().toLowerCase();
        return titre.contains(q) || desc.contains(q);
    }

    private void updateTaskInfo() {
        int total = masterTasks.size();
        long completed = masterTasks.stream()
                .filter(t -> t.getStatut_tache() == statut_t.TERMINEE)
                .count();
        long inProgress = masterTasks.stream()
                .filter(t -> t.getStatut_tache() == statut_t.EN_COURS)
                .count();

        taskTotalLabel.setText(String.valueOf(total));
        taskCompletedLabel.setText(String.valueOf(completed));
        taskInProgressLabel.setText(String.valueOf(inProgress));
        taskInfoLabel.setText(filteredTasks.size() + " tâche(s) affichée(s)");
    }

    // ===================== TASK EDIT / DELETE =====================

    private void openEditTaskModal(Tache tache) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutTache.fxml"));
            Parent root = loader.load();

            AjoutTacheController c = loader.getController();
            c.setProjectId(selectedProject.getProjet_id());
            c.setOnSaved(this::loadTasksForSelectedProject);
            c.loadEmployeesForProject();
            c.setTacheToEdit(tache); // Set task to edit mode

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle("Modifier la tâche");
            modal.setScene(new Scene(root));
            modal.showAndWait();

        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire: " + ex.getMessage());
        }
    }

    private void deleteTask(Tache tache) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cette tâche ?");
        confirm.setContentText("Tâche : " + tache.getTitre() + "\nCette action est irréversible.");

        ButtonType btnYes = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnYes, btnNo);

        confirm.showAndWait().ifPresent(type -> {
            if (type == btnYes) {
                try {
                    tacheCRUD.supprimer(tache.getId_tache());
                    loadTasksForSelectedProject();
                    taskInfoLabel.setText("✅ Tâche supprimée.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Erreur", "Impossible de supprimer la tâche: " + e.getMessage());
                }
            }
        });
    }

}
