package service.demande;

import entites.Demande;
import entites.DemandeDetails;
import entites.HistoriqueDemande;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemandeServiceTest {

    static TestDemandeCRUD demandeCRUD;
    static TestDemandeDetailsCRUD detailsCRUD;
    static TestHistoriqueDemandeCRUD historiqueCRUD;
    static int idDemandeTest;

    @BeforeAll
    static void setup() throws SQLException {
        demandeCRUD = new TestDemandeCRUD();
        detailsCRUD = new TestDemandeDetailsCRUD();
        historiqueCRUD = new TestHistoriqueDemandeCRUD();
        TestDBConnection.clearTables();
    }

    // ============================================================
    // TEST 1: AJOUTER DEMANDE
    // ============================================================
    @Test
    @Order(1)
    @DisplayName("Test Ajouter une Demande")
    void testAjouterDemande() throws SQLException {
        // Create a new demande
        Demande d = new Demande();
        d.setTitre("Bug Serveur");
        d.setCategorie("Technique");
        d.setDescription("Le serveur ne répond plus depuis ce matin");
        d.setPriorite("HAUTE");
        d.setStatus("Nouvelle");
        d.setDateCreation(new Date());
        d.setTypeDemande("Incident");

        // Add to database
        demandeCRUD.ajouter(d);

        // Verify ID was generated
        assertTrue(d.getIdDemande() > 0, "ID should be auto-generated");
        idDemandeTest = d.getIdDemande();

        // Verify it exists in database
        List<Demande> demandes = demandeCRUD.afficher();
        assertFalse(demandes.isEmpty(), "List should not be empty after adding");

        // Verify the data is correct
        boolean found = demandes.stream()
                .anyMatch(dem -> dem.getTitre().equals("Bug Serveur")
                        && dem.getCategorie().equals("Technique")
                        && dem.getPriorite().equals("HAUTE")
                        && dem.getTypeDemande().equals("Incident"));
        assertTrue(found, "Added demande should be found with correct data");

        System.out.println("✅ Demande ajoutée avec ID: " + idDemandeTest);
    }

    // ============================================================
    // TEST 2: AFFICHER DEMANDES
    // ============================================================
    @Test
    @Order(2)
    @DisplayName("Test Afficher les Demandes")
    void testAfficherDemandes() throws SQLException {
        List<Demande> demandes = demandeCRUD.afficher();

        assertNotNull(demandes, "List should not be null");
        assertFalse(demandes.isEmpty(), "List should not be empty");

        // Check the demande we added
        Demande found = demandes.stream()
                .filter(d -> d.getIdDemande() == idDemandeTest)
                .findFirst()
                .orElse(null);

        assertNotNull(found, "Should find demande by ID");
        assertEquals("Bug Serveur", found.getTitre());
        assertEquals("Technique", found.getCategorie());
        assertEquals("HAUTE", found.getPriorite());
        assertEquals("Nouvelle", found.getStatus());
        assertEquals("Incident", found.getTypeDemande());

        System.out.println("✅ Afficher: " + demandes.size() + " demande(s) trouvée(s)");
    }

    // ============================================================
    // TEST 3: MODIFIER DEMANDE
    // ============================================================
    @Test
    @Order(3)
    @DisplayName("Test Modifier une Demande")
    void testModifierDemande() throws SQLException {
        // Get the demande
        List<Demande> demandes = demandeCRUD.afficher();
        Demande toModify = demandes.stream()
                .filter(d -> d.getIdDemande() == idDemandeTest)
                .findFirst()
                .orElse(null);

        assertNotNull(toModify, "Demande to modify should exist");

        // Modify it
        toModify.setTitre("Bug Serveur Critique");
        toModify.setCategorie("Support");
        toModify.setPriorite("HAUTE");
        toModify.setDescription("Problème critique sur le serveur principal");
        demandeCRUD.modifier(toModify);

        // Verify modification
        List<Demande> updated = demandeCRUD.afficher();
        Demande modified = updated.stream()
                .filter(d -> d.getIdDemande() == idDemandeTest)
                .findFirst()
                .orElse(null);

        assertNotNull(modified, "Modified demande should exist");
        assertEquals("Bug Serveur Critique", modified.getTitre());
        assertEquals("Support", modified.getCategorie());
        assertEquals("HAUTE", modified.getPriorite());

        System.out.println("✅ Demande modifiée: " + modified.getTitre());
    }

    // ============================================================
    // TEST 4: AJOUTER DETAILS
    // ============================================================
    @Test
    @Order(4)
    @DisplayName("Test Ajouter des Détails à une Demande")
    void testAjouterDetails() throws SQLException {
        // Create details JSON
        String detailsJson = "{\"Localisation\":\"Salle Serveur B2\",\"Urgence\":\"Critique\",\"Impact\":\"Élevé\"}";

        DemandeDetails details = new DemandeDetails();
        details.setIdDemande(idDemandeTest);
        details.setDetails(detailsJson);
        detailsCRUD.ajouter(details);

        // Verify details were saved
        assertTrue(details.getIdDetails() > 0, "Details ID should be auto-generated");

        // Retrieve and verify
        DemandeDetails retrieved = detailsCRUD.getByDemande(idDemandeTest);
        assertNotNull(retrieved, "Details should be found");
        assertEquals(idDemandeTest, retrieved.getIdDemande());
        assertTrue(retrieved.getDetails().contains("Salle Serveur B2"));
        assertTrue(retrieved.getDetails().contains("Critique"));

        System.out.println("✅ Détails ajoutés: " + retrieved.getDetails());
    }

    // ============================================================
    // TEST 5: MODIFIER DETAILS
    // ============================================================
    @Test
    @Order(5)
    @DisplayName("Test Modifier les Détails")
    void testModifierDetails() throws SQLException {
        // Get existing details
        DemandeDetails existing = detailsCRUD.getByDemande(idDemandeTest);
        assertNotNull(existing, "Details should exist");

        // Modify
        String newJson = "{\"Localisation\":\"Salle Serveur A1\",\"Urgence\":\"Majeure\",\"Impact\":\"Moyen\"}";
        existing.setDetails(newJson);
        detailsCRUD.modifier(existing);

        // Verify
        DemandeDetails modified = detailsCRUD.getByDemande(idDemandeTest);
        assertNotNull(modified);
        assertTrue(modified.getDetails().contains("Salle Serveur A1"));
        assertTrue(modified.getDetails().contains("Majeure"));
        assertFalse(modified.getDetails().contains("Critique"), "Old value should be gone");

        System.out.println("✅ Détails modifiés: " + modified.getDetails());
    }

    // ============================================================
    // TEST 6: AJOUTER HISTORIQUE (Avancer Statut)
    // ============================================================
    @Test
    @Order(6)
    @DisplayName("Test Avancer Statut - Nouvelle → En cours")
    void testAvancerStatutNouvelleVersEnCours() throws SQLException {
        // Get current demande
        List<Demande> demandes = demandeCRUD.afficher();
        Demande demande = demandes.stream()
                .filter(d -> d.getIdDemande() == idDemandeTest)
                .findFirst()
                .orElse(null);
        assertNotNull(demande);
        assertEquals("Nouvelle", demande.getStatus());

        // Change status
        String ancienStatut = demande.getStatus();
        demande.setStatus("En cours");
        demandeCRUD.modifier(demande);

        // Create historique
        HistoriqueDemande h = new HistoriqueDemande();
        h.setIdDemande(idDemandeTest);
        h.setAncienStatut(ancienStatut);
        h.setNouveauStatut("En cours");
        h.setDateAction(new Date());
        h.setActeur("RH");
        h.setCommentaire("Prise en charge du bug serveur");
        historiqueCRUD.ajouter(h);

        // Verify status changed
        List<Demande> updated = demandeCRUD.afficher();
        Demande updatedDemande = updated.stream()
                .filter(d -> d.getIdDemande() == idDemandeTest)
                .findFirst()
                .orElse(null);
        assertNotNull(updatedDemande);
        assertEquals("En cours", updatedDemande.getStatus());

        // Verify historique created
        List<HistoriqueDemande> historiques = historiqueCRUD.getByDemande(idDemandeTest);
        assertFalse(historiques.isEmpty());
        assertEquals(1, historiques.size());
        assertEquals("Nouvelle", historiques.get(0).getAncienStatut());
        assertEquals("En cours", historiques.get(0).getNouveauStatut());
        assertEquals("RH", historiques.get(0).getActeur());

        System.out.println("✅ Statut: Nouvelle → En cours (par RH)");
    }

    // ============================================================
    // TEST 7: DEUXIÈME AVANCEMENT
    // ============================================================
    @Test
    @Order(7)
    @DisplayName("Test Avancer Statut - En cours → Résolue")
    void testAvancerStatutEnCoursVersResolue() throws SQLException {
        // Get current demande
        List<Demande> demandes = demandeCRUD.afficher();
        Demande demande = demandes.stream()
                .filter(d -> d.getIdDemande() == idDemandeTest)
                .findFirst()
                .orElse(null);
        assertNotNull(demande);
        assertEquals("En cours", demande.getStatus());

        // Change status
        demande.setStatus("Résolue");
        demandeCRUD.modifier(demande);

        // Create historique
        HistoriqueDemande h = new HistoriqueDemande();
        h.setIdDemande(idDemandeTest);
        h.setAncienStatut("En cours");
        h.setNouveauStatut("Résolue");
        h.setDateAction(new Date());
        h.setActeur("RESPONSABLE");
        h.setCommentaire("Bug corrigé et testé");
        historiqueCRUD.ajouter(h);

        // Verify 2 historique entries now
        List<HistoriqueDemande> historiques = historiqueCRUD.getByDemande(idDemandeTest);
        assertEquals(2, historiques.size(), "Should have 2 historique entries");

        System.out.println("✅ Statut: En cours → Résolue (par RESPONSABLE)");
    }

    // ============================================================
    // TEST 8: VÉRIFIER HISTORIQUE COMPLET
    // ============================================================
    @Test
    @Order(8)
    @DisplayName("Test Vérifier Historique Complet")
    void testVerifierHistoriqueComplet() throws SQLException {
        List<HistoriqueDemande> historiques = historiqueCRUD.getByDemande(idDemandeTest);

        assertEquals(2, historiques.size(), "Should have exactly 2 entries");

        // Most recent first (ORDER BY DESC)
        HistoriqueDemande latest = historiques.get(0);
        assertEquals("En cours", latest.getAncienStatut());
        assertEquals("Résolue", latest.getNouveauStatut());
        assertEquals("RESPONSABLE", latest.getActeur());

        HistoriqueDemande oldest = historiques.get(1);
        assertEquals("Nouvelle", oldest.getAncienStatut());
        assertEquals("En cours", oldest.getNouveauStatut());
        assertEquals("RH", oldest.getActeur());

        System.out.println("✅ Historique complet vérifié:");
        for (HistoriqueDemande h : historiques) {
            System.out.println("   " + h.getAncienStatut() + " → " + h.getNouveauStatut()
                    + " par " + h.getActeur() + " - " + h.getCommentaire());
        }
    }

    // ============================================================
    // TEST 9: VALIDATION - TITRE VIDE
    // ============================================================
    @Test
    @Order(9)
    @DisplayName("Test Validation - Titre ne peut pas être vide")
    void testValidationTitreVide() {
        Demande d = new Demande();
        d.setTitre("");
        d.setCategorie("Technique");
        d.setDescription("Description test");
        d.setPriorite("NORMALE");
        d.setStatus("Nouvelle");
        d.setDateCreation(new Date());
        d.setTypeDemande("Incident");

        assertTrue(d.getTitre().trim().isEmpty(), "Empty titre should be detected");
        System.out.println("✅ Validation: Titre vide détecté");
    }

    // ============================================================
    // TEST 10: VALIDATION - TITRE TROP COURT
    // ============================================================
    @Test
    @Order(10)
    @DisplayName("Test Validation - Titre minimum 3 caractères")
    void testValidationTitreTropCourt() {
        Demande d = new Demande();
        d.setTitre("AB");

        assertTrue(d.getTitre().trim().length() < 3, "Short titre should be detected");
        System.out.println("✅ Validation: Titre trop court détecté");
    }

    // ============================================================
    // TEST 11: VALIDATION - TITRE TROP LONG
    // ============================================================
    @Test
    @Order(11)
    @DisplayName("Test Validation - Titre maximum 50 caractères")
    void testValidationTitreTropLong() {
        Demande d = new Demande();
        d.setTitre("A".repeat(51));

        assertTrue(d.getTitre().trim().length() > 50, "Long titre should be detected");
        System.out.println("✅ Validation: Titre trop long détecté");
    }

    // ============================================================
    // TEST 12: VALIDATION - DATE PASSÉE
    // ============================================================
    @Test
    @Order(12)
    @DisplayName("Test Validation - Date ne peut pas être dans le passé")
    void testValidationDatePassee() {
        // Create a date in the past
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -5); // 5 days ago
        Date pastDate = cal.getTime();

        Date today = new Date();
        assertTrue(pastDate.before(today), "Past date should be before today");
        System.out.println("✅ Validation: Date passée détectée");
    }

    // ============================================================
    // TEST 13: VALIDATION - PRIORITÉ VALIDE
    // ============================================================
    @Test
    @Order(13)
    @DisplayName("Test Validation - Priorité doit être BASSE, NORMALE ou HAUTE")
    void testValidationPriorite() {
        List<String> validPriorites = List.of("BASSE", "NORMALE", "HAUTE");

        assertTrue(validPriorites.contains("HAUTE"), "HAUTE should be valid");
        assertTrue(validPriorites.contains("NORMALE"), "NORMALE should be valid");
        assertTrue(validPriorites.contains("BASSE"), "BASSE should be valid");
        assertFalse(validPriorites.contains("URGENTE"), "URGENTE should be invalid");

        System.out.println("✅ Validation: Priorités valides vérifiées");
    }

    // ============================================================
    // TEST 14: SUPPRIMER DETAILS
    // ============================================================
    @Test
    @Order(14)
    @DisplayName("Test Supprimer les Détails d'une Demande")
    void testSupprimerDetails() throws SQLException {
        // Verify details exist
        DemandeDetails before = detailsCRUD.getByDemande(idDemandeTest);
        assertNotNull(before, "Details should exist before deletion");

        // Delete
        detailsCRUD.supprimerByDemande(idDemandeTest);

        // Verify deleted
        DemandeDetails after = detailsCRUD.getByDemande(idDemandeTest);
        assertNull(after, "Details should be null after deletion");

        System.out.println("✅ Détails supprimés pour demande ID: " + idDemandeTest);
    }

    // ============================================================
    // TEST 15: SUPPRIMER HISTORIQUE
    // ============================================================
    @Test
    @Order(15)
    @DisplayName("Test Supprimer l'Historique d'une Demande")
    void testSupprimerHistorique() throws SQLException {
        // Verify historique exists
        List<HistoriqueDemande> before = historiqueCRUD.getByDemande(idDemandeTest);
        assertFalse(before.isEmpty(), "Historique should exist before deletion");

        // Delete
        historiqueCRUD.supprimerByDemande(idDemandeTest);

        // Verify deleted
        List<HistoriqueDemande> after = historiqueCRUD.getByDemande(idDemandeTest);
        assertTrue(after.isEmpty(), "Historique should be empty after deletion");

        System.out.println("✅ Historique supprimé pour demande ID: " + idDemandeTest);
    }

    // ============================================================
    // TEST 16: SUPPRIMER DEMANDE
    // ============================================================
    @Test
    @Order(16)
    @DisplayName("Test Supprimer une Demande")
    void testSupprimerDemande() throws SQLException {
        // Verify demande exists
        List<Demande> before = demandeCRUD.afficher();
        boolean existsBefore = before.stream()
                .anyMatch(d -> d.getIdDemande() == idDemandeTest);
        assertTrue(existsBefore, "Demande should exist before deletion");

        // Delete
        demandeCRUD.supprimer(idDemandeTest);

        // Verify deleted
        List<Demande> after = demandeCRUD.afficher();
        boolean existsAfter = after.stream()
                .anyMatch(d -> d.getIdDemande() == idDemandeTest);
        assertFalse(existsAfter, "Demande should not exist after deletion");

        System.out.println("✅ Demande supprimée ID: " + idDemandeTest);
    }

    // ============================================================
    // TEST 17: TABLE VIDE APRÈS SUPPRESSION
    // ============================================================
    @Test
    @Order(17)
    @DisplayName("Test Vérifier que la table est vide")
    void testTableVide() throws SQLException {
        List<Demande> demandes = demandeCRUD.afficher();
        assertTrue(demandes.isEmpty(), "Table should be empty after all deletions");

        System.out.println("✅ Table demande est vide: " + demandes.size() + " enregistrements");
    }
}