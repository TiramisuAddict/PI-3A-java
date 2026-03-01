package controller.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import service.api.AIDocumentGeneratorService;
import service.api.AIDocumentGeneratorService.GeneratedDocument;

import java.io.File;
import java.util.Date;

public class DocumentGeneratorPanel extends VBox {

    private AIDocumentGeneratorService docService;
    private TextArea documentPreview;
    private Label statusLabel;
    private Label folderLabel;
    private ProgressIndicator loadingIndicator;
    private Button exportPDFButton;
    private Button exportWordButton;
    private Button copyButton;
    private GeneratedDocument currentDocument;
    private File selectedFolder;

    public DocumentGeneratorPanel() {
        try {
            docService = new AIDocumentGeneratorService();
            setupUI();
        } catch (Exception e) {
            System.err.println("DocumentGeneratorPanel init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e0e0e0; -fx-border-radius: 12;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("📄");
        icon.setStyle("-fx-font-size: 28;");

        VBox titleBox = new VBox(2);
        Label title = new Label("Document Généré");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        statusLabel = new Label("En attente...");
        statusLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");
        titleBox.getChildren().addAll(title, statusLabel);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(24, 24);
        loadingIndicator.setVisible(false);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(icon, titleBox, spacer, loadingIndicator);

        // Folder selection
        HBox folderBox = new HBox(10);
        folderBox.setAlignment(Pos.CENTER_LEFT);

        Button chooseFolderBtn = new Button("📁 Choisir dossier");
        chooseFolderBtn.setStyle("-fx-background-color: #4A5DEF; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 15;");
        chooseFolderBtn.setOnAction(e -> chooseFolder());

        folderLabel = new Label("Aucun dossier sélectionné");
        folderLabel.setStyle("-fx-text-fill: #999;");
        folderBox.getChildren().addAll(chooseFolderBtn, folderLabel);

        // Preview
        Label previewLabel = new Label("📋 Aperçu");
        previewLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

        documentPreview = new TextArea();
        documentPreview.setWrapText(true);
        documentPreview.setPrefRowCount(15);
        documentPreview.setStyle("-fx-font-family: Consolas;");
        VBox.setVgrow(documentPreview, Priority.ALWAYS);

        // Buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        exportPDFButton = new Button("📥 PDF");
        exportPDFButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 15;");
        exportPDFButton.setDisable(true);
        exportPDFButton.setOnAction(e -> exportPDF());

        exportWordButton = new Button("📄 Word");
        exportWordButton.setStyle("-fx-background-color: #2185d0; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 15;");
        exportWordButton.setDisable(true);
        exportWordButton.setOnAction(e -> exportWord());

        copyButton = new Button("📋 Copier");
        copyButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 15;");
        copyButton.setDisable(true);
        copyButton.setOnAction(e -> copyText());

        buttons.getChildren().addAll(exportPDFButton, exportWordButton, copyButton);

        getChildren().addAll(header, folderBox, new Separator(), previewLabel, documentPreview, buttons);
    }

    private void chooseFolder() {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choisir dossier");
            File folder = chooser.showDialog(getScene().getWindow());
            if (folder != null) {
                selectedFolder = folder;
                folderLabel.setText("📂 " + folder.getName());
                folderLabel.setStyle("-fx-text-fill: #27ae60;");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Erreur lors de la sélection du dossier");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    public void generateDocument(String type, String name, String position, String id, Date date, String info) {
        if (selectedFolder == null) {
            statusLabel.setText("⚠️ Choisissez un dossier d'abord");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        if (docService == null) {
            statusLabel.setText("❌ Erreur: Service de génération non initialisé");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        statusLabel.setText("🔄 Génération en cours...");
        statusLabel.setStyle("-fx-text-fill: #f39c12;");
        loadingIndicator.setVisible(true);

        docService.generateDocumentAsync(type, name, position, id, date, info)
                .thenAccept(doc -> Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    if (doc != null && doc.isValid) {
                        currentDocument = doc;
                        documentPreview.setText(doc.getFormattedText());
                        statusLabel.setText("✅ Document généré avec succès !");
                        statusLabel.setStyle("-fx-text-fill: #27ae60;");

                        String base = sanitize(doc.title);
                        String pdfPath = selectedFolder.getPath() + File.separator + base + ".pdf";
                        String wordPath = selectedFolder.getPath() + File.separator + base + ".docx";

                        docService.exportToPDFAsync(doc, pdfPath)
                                .thenAccept(f -> {
                                    Platform.runLater(() -> {
                                        if (f != null) {
                                            statusLabel.setText("✅ PDF exporté avec succès !");
                                            statusLabel.setStyle("-fx-text-fill: #27ae60;");
                                        } else {
                                            statusLabel.setText("❌ Erreur lors de l'exportation du PDF");
                                            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                                        }
                                    });
                                })
                                .exceptionally(ex -> {
                                    Platform.runLater(() -> {
                                        statusLabel.setText("❌ Erreur lors de l'exportation du PDF: " + ex.getMessage());
                                        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                                    });
                                    return null;
                                });

                        docService.exportToWordAsync(doc, wordPath)
                                .thenAccept(f -> {
                                    Platform.runLater(() -> {
                                        if (f != null) {
                                            statusLabel.setText("✅ Word exporté avec succès !");
                                            statusLabel.setStyle("-fx-text-fill: #27ae60;");
                                        } else {
                                            statusLabel.setText("❌ Erreur lors de l'exportation du Word");
                                            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                                        }
                                    });
                                })
                                .exceptionally(ex -> {
                                    Platform.runLater(() -> {
                                        statusLabel.setText("❌ Erreur lors de l'exportation du Word: " + ex.getMessage());
                                        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                                    });
                                    return null;
                                });

                        exportPDFButton.setDisable(false);
                        exportWordButton.setDisable(false);
                        copyButton.setDisable(false);
                    } else {
                        statusLabel.setText("❌ Échec de la génération du document");
                        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        statusLabel.setText("❌ Erreur: " + ex.getMessage());
                        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                    });
                    return null;
                });
    }

    private void exportPDF() {
        if (currentDocument == null) {
            statusLabel.setText("❌ Aucun document à exporter");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("document.pdf");
        File file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            docService.exportToPDFAsync(currentDocument, file.getAbsolutePath())
                    .thenAccept(f -> {
                        Platform.runLater(() -> {
                            if (f != null) {
                                statusLabel.setText("✅ PDF exporté avec succès !");
                                statusLabel.setStyle("-fx-text-fill: #27ae60;");
                            } else {
                                statusLabel.setText("❌ Erreur lors de l'exportation du PDF");
                                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            statusLabel.setText("❌ Erreur lors de l'exportation du PDF: " + ex.getMessage());
                            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                        });
                        return null;
                    });
        }
    }

    private void exportWord() {
        if (currentDocument == null) {
            statusLabel.setText("❌ Aucun document à exporter");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("document.docx");
        File file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            docService.exportToWordAsync(currentDocument, file.getAbsolutePath())
                    .thenAccept(f -> {
                        Platform.runLater(() -> {
                            if (f != null) {
                                statusLabel.setText("✅ Word exporté avec succès !");
                                statusLabel.setStyle("-fx-text-fill: #27ae60;");
                            } else {
                                statusLabel.setText("❌ Erreur lors de l'exportation du Word");
                                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            statusLabel.setText("❌ Erreur lors de l'exportation du Word: " + ex.getMessage());
                            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                        });
                        return null;
                    });
        }
    }

    private void copyText() {
        if (currentDocument == null) {
            statusLabel.setText("❌ Aucun texte à copier");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            ClipboardContent content = new ClipboardContent();
            content.putString(documentPreview.getText());
            Clipboard.getSystemClipboard().setContent(content);
            statusLabel.setText("✅ Texte copié dans le presse-papiers !");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
        } catch (Exception ex) {
            statusLabel.setText("❌ Erreur lors de la copie du texte");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    private String sanitize(String s) {
        return s == null ? "document" : s.replaceAll("[^a-zA-Z0-9]", "_");
    }
}