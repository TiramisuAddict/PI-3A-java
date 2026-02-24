package test;

import service.api.AIDocumentGeneratorService;
import service.api.AIDocumentGeneratorService.GeneratedDocument;

import java.io.File;
import java.util.Date;

public class TestDocumentGenerator {
    public static void main(String[] args) {
        AIDocumentGeneratorService service = new AIDocumentGeneratorService();

        System.out.println("Testing AI Document Generator...\n");

        service.generateDocumentAsync(
                "Attestation travail",
                "Ahmed Ben Ali",
                "Développeur Senior",
                "EMP001",
                new Date(),
                "L'employé demande une attestation de travail pour des démarches administratives."
        ).thenAccept(doc -> {

            if (doc.isValid) {

                System.out.println("✓ Document généré avec succès!\n");
                System.out.println(doc.getFormattedText());

                service.exportToPDFAsync(doc, "test_document.pdf").join();
                service.exportToWordAsync(doc, "test_document.docx").join();

                System.out.println("\n✓ PDF et Word créés !");
            } else {
                System.out.println("✗ Échec de génération");
            }

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        }).join();   // ✅ THIS LINE WAITS FOR COMPLETION
    }}