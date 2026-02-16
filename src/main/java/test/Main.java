package test;

import entites.Demande;
import service.demande.DemandeCRUD;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        DemandeCRUD crud = new DemandeCRUD();

        // ============ TEST AJOUTER ============
        System.out.println("========== TEST AJOUTER ==========");
        try {
            Demande demande = new Demande();
            demande.setTitre("Test Demande");
            demande.setCategorie("Technique");
            demande.setDescription("Ceci est un test");
            demande.setPriorite("NORMALE");        // ← ENUM value
            demande.setStatus("Nouvelle");
            demande.setDateCreation(new Date());
            demande.setTypeDemande("Incident");

            crud.ajouter(demande);
            System.out.println("✅ Demande ajoutée avec ID: " + demande.getIdDemande());
        } catch (SQLException e) {
            System.out.println("❌ Ajouter failed: " + e.getMessage());
        }

        // ============ TEST AFFICHER ============
        System.out.println("\n========== TEST AFFICHER ==========");
        try {
            List<Demande> demandes = crud.afficher();
            System.out.println("✅ Nombre de demandes: " + demandes.size());
            for (Demande d : demandes) {
                System.out.println("   ID: " + d.getIdDemande()
                        + " | Titre: " + d.getTitre()
                        + " | Categorie: " + d.getCategorie()
                        + " | Priorite: " + d.getPriorite()
                        + " | Status: " + d.getStatus()
                        + " | Type: " + d.getTypeDemande()
                        + " | Date: " + d.getDateCreation());
            }
        } catch (SQLException e) {
            System.out.println("❌ Afficher failed: " + e.getMessage());
        }

        // ============ TEST MODIFIER ============
        System.out.println("\n========== TEST MODIFIER ==========");
        try {
            List<Demande> demandes = crud.afficher();
            if (!demandes.isEmpty()) {
                Demande toModify = demandes.get(demandes.size() - 1);
                toModify.setTitre("Titre Modifié");
                toModify.setStatus("En cours");
                toModify.setPriorite("HAUTE");     // ← ENUM value
                crud.modifier(toModify);
                System.out.println("✅ Demande ID " + toModify.getIdDemande() + " modifiée!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Modifier failed: " + e.getMessage());
        }

        // ============ VERIFY MODIFICATION ============
        System.out.println("\n========== VERIFY MODIFICATION ==========");
        try {
            List<Demande> demandes = crud.afficher();
            for (Demande d : demandes) {
                System.out.println("   ID: " + d.getIdDemande()
                        + " | Titre: " + d.getTitre()
                        + " | Priorite: " + d.getPriorite()
                        + " | Status: " + d.getStatus());
            }
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }

        // ============ TEST SUPPRIMER ============
        System.out.println("\n========== TEST SUPPRIMER ==========");
        try {
            List<Demande> demandes = crud.afficher();
            if (!demandes.isEmpty()) {
                int lastId = demandes.get(demandes.size() - 1).getIdDemande();
                crud.supprimer(lastId);
                System.out.println("✅ Demande ID " + lastId + " supprimée!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Supprimer failed: " + e.getMessage());
        }

        // ============ FINAL COUNT ============
        System.out.println("\n========== DONNÉES RESTANTES ==========");
        try {
            List<Demande> demandes = crud.afficher();
            System.out.println("Total demandes: " + demandes.size());
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}