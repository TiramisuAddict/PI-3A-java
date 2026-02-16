import entity.EtatOffre;
import entity.Offre;
import org.junit.jupiter.api.*;
import service.OffreCRUD;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OffreTest {
    static OffreCRUD oCRUD;

    @BeforeAll
    static void setup() {
        oCRUD = new OffreCRUD();
    }

    static int idOffreTest = 1;

    @Test
    @Order(1)
    void testAjouterOffre() throws SQLException {
        Offre offre = new Offre("OFFRE123", 1, "Développeur Java", entity.TypeContrat.CDI, java.sql.Date.valueOf("2024-12-31"), EtatOffre.OUVERT);
        oCRUD.ajouter(offre);
        idOffreTest = oCRUD.getIdByCodeOffre("OFFRE123");
        offre.setId(idOffreTest);
        List<Offre> offres = oCRUD.afficher();
        assertFalse(offres.isEmpty());
        assertTrue(offres.stream().anyMatch(o -> o.getId().equals(offre.getId())));

        idOffreTest = offre.getId();
        System.out.println("idOffreTest: " + idOffreTest);

    }

    @Test
    @Order(2)
    void testModifierOffre() throws SQLException {
        Offre offre = new Offre("OFFRE321", 1, "Développeur Java Senior", entity.TypeContrat.CDI, java.sql.Date.valueOf("2024-12-31"), EtatOffre.OUVERT);
        offre.setId(idOffreTest);
        oCRUD.modifier(offre);

        List<Offre> offres = oCRUD.afficher();

        boolean trouve = offres.stream().anyMatch(o -> o.getId().equals(idOffreTest) && o.getCodeOffre().equals("OFFRE321"));
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerOffre() throws SQLException {
        oCRUD.supprimer(idOffreTest);
        List<Offre> offres = oCRUD.afficher();
        boolean existe = offres.stream().anyMatch(o -> o.getId().equals(idOffreTest));
        assertFalse(existe);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        List<Offre> offres = oCRUD.afficher();
        for (Offre o : offres) {
            if (o.getCodeOffre().equals("OFFRE321")) {
                oCRUD.supprimer(o.getId());
            }
        }
    }

}
