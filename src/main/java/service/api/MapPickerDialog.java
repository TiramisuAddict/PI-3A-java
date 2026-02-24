package service.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MapPickerDialog {

    private Stage dialogStage;
    private TextField searchField;
    private ListView<LocationResult> resultsListView;
    private TextField selectedAddressField;
    private TextField latitudeField;
    private TextField longitudeField;
    private Label statusLabel;

    private LocationResult selectedLocation;
    private boolean confirmed = false;
    private final OkHttpClient httpClient;

    public MapPickerDialog() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public Optional<LocationResult> showAndWait() {
        return showAndWait(null);
    }

    public Optional<LocationResult> showAndWait(String initialAddress) {
        createDialog();

        if (initialAddress != null && !initialAddress.isEmpty()) {
            searchField.setText(initialAddress);
            // Auto search after dialog shows
            Platform.runLater(() -> {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}
                Platform.runLater(this::searchLocation);
            });
        }

        dialogStage.showAndWait();

        if (confirmed && selectedLocation != null) {
            return Optional.of(selectedLocation);
        }
        return Optional.empty();
    }

    private void createDialog() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("📍 Sélectionner une adresse");
        dialogStage.setWidth(700);
        dialogStage.setHeight(600);
        dialogStage.setMinWidth(600);
        dialogStage.setMinHeight(500);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef);");

        // Header
        VBox headerBox = createHeader();

        // Search section
        VBox searchSection = createSearchSection();

        // Results section
        VBox resultsSection = createResultsSection();

        // Selected address section
        VBox selectedSection = createSelectedSection();

        // Buttons
        HBox buttonBox = createButtonBar();

        root.getChildren().addAll(headerBox, searchSection, resultsSection, selectedSection, buttonBox);
        VBox.setVgrow(resultsSection, Priority.ALWAYS);

        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        Label titleLabel = new Label("🗺️ Recherche d'adresse");
        titleLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Recherchez une adresse et sélectionnez dans les résultats");
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13;");

        headerBox.getChildren().addAll(titleLabel, subtitleLabel);
        return headerBox;
    }

    private VBox createSearchSection() {
        VBox searchSection = new VBox(10);
        searchSection.setPadding(new Insets(15));
        searchSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label searchLabel = new Label("🔍 Rechercher une adresse");
        searchLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #2c3e50;");

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Ex: ESPRIT Tunis, Avenue Habib Bourguiba, Rue de la Liberté...");
        searchField.setStyle("-fx-font-size: 14; -fx-padding: 12; -fx-background-radius: 8;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchBtn = new Button("🔍 Rechercher");
        searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        searchBtn.setOnAction(e -> searchLocation());

        // Enter key triggers search
        searchField.setOnAction(e -> searchLocation());

        // Hover effect
        searchBtn.setOnMouseEntered(e -> searchBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));
        searchBtn.setOnMouseExited(e -> searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));

        searchBox.getChildren().addAll(searchField, searchBtn);

        statusLabel = new Label("Entrez une adresse pour commencer la recherche");
        statusLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12;");

        searchSection.getChildren().addAll(searchLabel, searchBox, statusLabel);
        return searchSection;
    }

    private VBox createResultsSection() {
        VBox resultsSection = new VBox(10);
        resultsSection.setPadding(new Insets(15));
        resultsSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label resultsLabel = new Label("📋 Résultats de recherche");
        resultsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #2c3e50;");

        resultsListView = new ListView<>();
        resultsListView.setPrefHeight(200);
        resultsListView.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0;");
        resultsListView.setPlaceholder(new Label("🔍 Les résultats apparaîtront ici"));
        VBox.setVgrow(resultsListView, Priority.ALWAYS);

        // Custom cell factory
        resultsListView.setCellFactory(lv -> new ListCell<LocationResult>() {
            @Override
            protected void updateItem(LocationResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    VBox cellBox = new VBox(5);
                    cellBox.setPadding(new Insets(10));

                    Label addressLabel = new Label("📍 " + item.getShortAddress());
                    addressLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 13;");
                    addressLabel.setWrapText(true);

                    Label typeLabel = new Label("🏷️ " + item.getType());
                    typeLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 11;");

                    Label coordsLabel = new Label("🌐 " + item.getCoordinates());
                    coordsLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10; -fx-font-family: monospace;");

                    cellBox.getChildren().addAll(addressLabel, typeLabel, coordsLabel);
                    setGraphic(cellBox);

                    // Selection style
                    if (isSelected()) {
                        setStyle("-fx-background-color: #ebf5fb; -fx-background-radius: 8;");
                    } else {
                        setStyle("-fx-background-color: transparent;");
                    }
                }
            }
        });

        // Selection listener
        resultsListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedLocation = newVal;
                selectedAddressField.setText(newVal.getAddress());
                latitudeField.setText(String.format("%.6f", newVal.getLatitude()));
                longitudeField.setText(String.format("%.6f", newVal.getLongitude()));
                statusLabel.setText("✅ Adresse sélectionnée");
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 12;");
            }
        });

        resultsSection.getChildren().addAll(resultsLabel, resultsListView);
        return resultsSection;
    }

    private VBox createSelectedSection() {
        VBox selectedSection = new VBox(10);
        selectedSection.setPadding(new Insets(15));
        selectedSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label selectedLabel = new Label("✅ Adresse sélectionnée");
        selectedLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #27ae60;");

        selectedAddressField = new TextField();
        selectedAddressField.setPromptText("Sélectionnez une adresse dans les résultats ci-dessus ou tapez manuellement");
        selectedAddressField.setStyle("-fx-font-size: 13; -fx-padding: 10; -fx-background-radius: 8;");

        HBox coordsBox = new HBox(15);
        coordsBox.setAlignment(Pos.CENTER_LEFT);

        Label latLabel = new Label("Latitude:");
        latLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
        latitudeField = new TextField("--");
        latitudeField.setPrefWidth(130);
        latitudeField.setEditable(false);
        latitudeField.setStyle("-fx-background-color: #f5f5f5; -fx-font-family: monospace; -fx-background-radius: 5;");

        Label lngLabel = new Label("Longitude:");
        lngLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
        longitudeField = new TextField("--");
        longitudeField.setPrefWidth(130);
        longitudeField.setEditable(false);
        longitudeField.setStyle("-fx-background-color: #f5f5f5; -fx-font-family: monospace; -fx-background-radius: 5;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Open in browser button
        Button openMapBtn = new Button("🌐 Voir sur Google Maps");
        openMapBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 8 15; " +
                "-fx-background-radius: 5; -fx-cursor: hand;");
        openMapBtn.setOnAction(e -> openInGoogleMaps());

        coordsBox.getChildren().addAll(latLabel, latitudeField, lngLabel, longitudeField, spacer, openMapBtn);

        selectedSection.getChildren().addAll(selectedLabel, selectedAddressField, coordsBox);
        return selectedSection;
    }

    private HBox createButtonBar() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("❌ Annuler");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 12 20; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        cancelBtn.setOnAction(e -> {
            confirmed = false;
            dialogStage.close();
        });

        Button confirmBtn = new Button("✅ Confirmer");
        confirmBtn.setPrefWidth(150);
        confirmBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 12 20; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        confirmBtn.setOnAction(e -> confirmSelection());

        // Hover effects
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; " +
                "-fx-padding: 12 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-padding: 12 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;"));

        confirmBtn.setOnMouseEntered(e -> confirmBtn.setStyle("-fx-background-color: #1e8449; -fx-text-fill: white; " +
                "-fx-padding: 12 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;"));
        confirmBtn.setOnMouseExited(e -> confirmBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-padding: 12 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;"));

        buttonBox.getChildren().addAll(cancelBtn, confirmBtn);
        return buttonBox;
    }

    private void searchLocation() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            statusLabel.setText("⚠️ Veuillez entrer une adresse à rechercher");
            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
            return;
        }

        statusLabel.setText("🔍 Recherche en cours...");
        statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12;");
        resultsListView.setItems(FXCollections.observableArrayList());

        CompletableFuture.supplyAsync(() -> {
            try {
                String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
                String url = "https://nominatim.openstreetmap.org/search?format=json&q=" +
                        encodedQuery + "&limit=10&addressdetails=1";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Accept-Language", "fr")
                        .addHeader("User-Agent", "JavaFX-LocationPicker/1.0")
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonData = response.body().string();
                        return parseResults(jsonData);
                    }
                }
            } catch (Exception e) {
                System.out.println("Search error: " + e.getMessage());
                e.printStackTrace();
            }
            return FXCollections.<LocationResult>observableArrayList();
        }).thenAccept(results -> {
            Platform.runLater(() -> {
                resultsListView.setItems(results);
                if (results.isEmpty()) {
                    statusLabel.setText("❌ Aucun résultat trouvé. Essayez avec d'autres termes.");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
                } else {
                    statusLabel.setText("✅ " + results.size() + " résultat(s) trouvé(s) - Cliquez pour sélectionner");
                    statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
                }
            });
        });
    }

    private ObservableList<LocationResult> parseResults(String jsonData) {
        ObservableList<LocationResult> results = FXCollections.observableArrayList();

        try {
            JsonArray jsonArray = JsonParser.parseString(jsonData).getAsJsonArray();

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject obj = jsonArray.get(i).getAsJsonObject();

                String displayName = obj.has("display_name") ? obj.get("display_name").getAsString() : "Unknown";
                double lat = Double.parseDouble(obj.get("lat").getAsString());
                double lon = Double.parseDouble(obj.get("lon").getAsString());
                String type = obj.has("type") ? formatType(obj.get("type").getAsString()) : "Lieu";

                results.add(new LocationResult(displayName, lat, lon, type));
            }
        } catch (Exception e) {
            System.out.println("Parse error: " + e.getMessage());
        }

        return results;
    }

    private String formatType(String type) {
        if (type == null) return "Lieu";

        switch (type.toLowerCase()) {
            case "city": return "🏙️ Ville";
            case "town": return "🏘️ Commune";
            case "village": return "🏡 Village";
            case "suburb": return "🏘️ Quartier";
            case "neighbourhood": return "📍 Voisinage";
            case "road": case "street": return "🛣️ Rue";
            case "building": return "🏢 Bâtiment";
            case "house": return "🏠 Maison";
            case "school": case "university": return "🎓 Établissement scolaire";
            case "hospital": return "🏥 Hôpital";
            case "restaurant": return "🍽️ Restaurant";
            case "cafe": return "☕ Café";
            case "shop": case "store": return "🏪 Commerce";
            case "park": return "🌳 Parc";
            case "station": return "🚉 Station";
            case "airport": return "✈️ Aéroport";
            case "hotel": return "🏨 Hôtel";
            case "bank": return "🏦 Banque";
            case "pharmacy": return "💊 Pharmacie";
            case "mosque": return "🕌 Mosquée";
            case "church": return "⛪ Église";
            default: return "📍 " + capitalize(type);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void confirmSelection() {
        String address = selectedAddressField.getText().trim();

        if (address.isEmpty()) {
            statusLabel.setText("⚠️ Veuillez sélectionner ou entrer une adresse");
            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 12;");
            selectedAddressField.setStyle("-fx-font-size: 13; -fx-padding: 10; -fx-background-radius: 8; " +
                    "-fx-border-color: #e74c3c; -fx-border-width: 2;");
            return;
        }

        // If user typed manually without selecting
        if (selectedLocation == null || !selectedLocation.getAddress().equals(address)) {
            // Create a new location result with manual address
            try {
                double lat = latitudeField.getText().equals("--") ? 0 : Double.parseDouble(latitudeField.getText());
                double lng = longitudeField.getText().equals("--") ? 0 : Double.parseDouble(longitudeField.getText());
                selectedLocation = new LocationResult(address, lat, lng, "Manuel");
            } catch (NumberFormatException e) {
                selectedLocation = new LocationResult(address, 0, 0, "Manuel");
            }
        }

        confirmed = true;
        dialogStage.close();
    }

    private void openInGoogleMaps() {
        try {
            String lat = latitudeField.getText();
            String lng = longitudeField.getText();

            String url;
            if (!lat.equals("--") && !lng.equals("--")) {
                url = "https://www.google.com/maps?q=" + lat + "," + lng;
            } else if (selectedAddressField.getText() != null && !selectedAddressField.getText().isEmpty()) {
                String encoded = java.net.URLEncoder.encode(selectedAddressField.getText(),
                        java.nio.charset.StandardCharsets.UTF_8);
                url = "https://www.google.com/maps/search/" + encoded;
            } else {
                statusLabel.setText("⚠️ Sélectionnez d'abord une adresse");
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
                return;
            }

            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            statusLabel.setText("🌐 Ouverture dans Google Maps...");
            statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12;");
        } catch (Exception e) {
            statusLabel.setText("❌ Impossible d'ouvrir le navigateur");
            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
        }
    }

    // ==================== LOCATION RESULT CLASS ====================

    public static class LocationResult {
        private final String address;
        private final double latitude;
        private final double longitude;
        private final String type;

        public LocationResult(String address, double latitude, double longitude) {
            this(address, latitude, longitude, "Lieu");
        }

        public LocationResult(String address, double latitude, double longitude, String type) {
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.type = type;
        }

        public String getAddress() {
            return address;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getType() {
            return type;
        }

        public String getShortAddress() {
            if (address == null) return "";
            if (address.length() > 100) {
                return address.substring(0, 97) + "...";
            }
            return address;
        }

        public String getCoordinates() {
            if (latitude == 0 && longitude == 0) {
                return "Non disponible";
            }
            return String.format("%.6f, %.6f", latitude, longitude);
        }

        @Override
        public String toString() {
            return address;
        }
    }
}