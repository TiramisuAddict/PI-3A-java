package services;
import Models.Projet;
import Models.priority;
import Models.statut;
import org.junit.jupiter.api.*;
import service.ProjetCRUD;
import java.util.List;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjetServicesTest {
    static ProjetCRUD projetCRUD;
    @BeforeAll
    public static void setup() {
        projetCRUD = new ProjetCRUD();
    }
    static int createdProjectId; // To store the ID of the project created in testAjouterProjet
    @Test
    @Order(1)
    public void testAjouterProjet() throws SQLException {
        Projet projet = new Projet(1, "Test Projet", "Description du projet de test", java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(30), null, statut.EN_COURS, priority.HAUTE);
        projetCRUD.ajouter(projet);
        List <Projet> projets = projetCRUD.afficher();
        assertFalse(projets.isEmpty(), "La liste des projets ne devrait pas être vide après l'ajout d'un projet.");
        assertTrue(projets.stream().anyMatch(p -> p.getNom().equals("Test Projet")), "Le projet ajouté devrait être présent dans la liste des projets.");
        createdProjectId = projets.stream().filter(p -> p.getNom().equals("Test Projet")).findFirst().get().getProjet_id();
    }

    @Test
    @Order(2)
    public void testModifierProjet() {
        //TODO implement test for modifierProjet method
    }

    @Test
    public void testSupprimerProjet() {
        //TODO implement test for supprimerProjet method
    }

    @Test
    public void testAfficherProjets() {
        //TODO implement test for afficherProjets method
    }
    @AfterEach
    public void cleanup() throws SQLException {
        if (createdProjectId != 0) {
            projetCRUD.supprimer(createdProjectId);
            createdProjectId = 0; // Reset after cleanup
        }
    }
}
