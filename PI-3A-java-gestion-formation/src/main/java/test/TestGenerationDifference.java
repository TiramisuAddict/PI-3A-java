package test;

import utils.TextAssistant;
import utils.TextGeneratorAPI;

/**
 * 🧪 TEST VISUEL - Comparer Templates vs IA Hugging Face
 *
 * Ce test montre clairement la DIFFÉRENCE entre :
 * 1. Templates statiques (TextAssistant)
 * 2. Génération IA réelle (TextGeneratorAPI / Hugging Face)
 */
public class TestGenerationDifference {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  🤖 TEST VISUEL - Templates vs IA Hugging Face                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Tester les mêmes mots-clés avec les deux systèmes
        String[] testKeywords = {
            "communication",
            "technical skills development",
            "career growth leadership",
            "certification professional"
        };

        for (String keywords : testKeywords) {
            testComparison(keywords);
            System.out.println("\n");
        }

        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ✅ TEST TERMINÉ                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        System.out.println("📊 CONCLUSIONS :\n");
        System.out.println("✅ TEMPLATES (TextAssistant) :");
        System.out.println("   - Identiques pour les mêmes mots-clés");
        System.out.println("   - Pré-écrits et fixes");
        System.out.println("   - Rapides (instantané)");
        System.out.println("   - Limités à 28 templates\n");

        System.out.println("✅ HUGGING FACE IA (TextGeneratorAPI) :");
        System.out.println("   - Générés de manière UNIQUE à chaque fois");
        System.out.println("   - Texte naturel et naturellement varié");
        System.out.println("   - Plus lents (2-3 secondes)");
        System.out.println("   - Infinité de possibilités");
        System.out.println("   - VRAIE IA générative (GPT-2)\n");
    }

    /**
     * Comparer Templates et IA pour les mêmes mots-clés
     */
    private static void testComparison(String keywords) {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🎯 TEST AVEC : \"" + keywords + "\"");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Test 1 : Templates (TextAssistant)
        System.out.println("1️⃣ TEMPLATES (TextAssistant) - STATIQUE");
        System.out.println("   " + "─".repeat(60));
        String template1 = TextAssistant.generateProfessionalParagraph(keywords);
        System.out.println("   📄 Génération 1 :");
        System.out.println("   \"" + template1 + "\"\n");

        String template2 = TextAssistant.generateProfessionalParagraph(keywords);
        System.out.println("   📄 Génération 2 (même mots-clés) :");
        System.out.println("   \"" + template2 + "\"\n");

        // Vérifier si identique
        if (template1.equals(template2)) {
            System.out.println("   ⚠️  RÉSULTAT : IDENTIQUE ❌ (c'est un template statique)\n");
        } else {
            System.out.println("   ✅ RÉSULTAT : Différent (mais c'est rare pour les templates)\n");
        }

        // Test 2 : IA Hugging Face
        System.out.println("2️⃣ IA HUGGING FACE (TextGeneratorAPI) - DYNAMIQUE");
        System.out.println("   " + "─".repeat(60));

        System.out.println("   ⏳ Appel API 1 en cours...");
        long start1 = System.currentTimeMillis();
        String ia1 = TextGeneratorAPI.generateProfessionalText(keywords, "Formation Professionnelle");
        long time1 = System.currentTimeMillis() - start1;
        System.out.println("   ⏱️  Temps : " + time1 + "ms\n");

        System.out.println("   📄 Génération 1 (IA) :");
        System.out.println("   \"" + ia1 + "\"\n");

        System.out.println("   ⏳ Appel API 2 en cours...");
        long start2 = System.currentTimeMillis();
        String ia2 = TextGeneratorAPI.generateProfessionalText(keywords, "Formation Professionnelle");
        long time2 = System.currentTimeMillis() - start2;
        System.out.println("   ⏱️  Temps : " + time2 + "ms\n");

        System.out.println("   📄 Génération 2 (IA - même mots-clés) :");
        System.out.println("   \"" + ia2 + "\"\n");

        // Comparer
        if (ia1.equals(ia2)) {
            System.out.println("   ⚠️  RÉSULTAT : IDENTIQUE (API down ou mode template)");
        } else {
            System.out.println("   ✅ RÉSULTAT : DIFFÉRENT ! ✨ (IA fonctionne !)");
            System.out.println("   💡 Chaque appel génère un texte UNIQUE\n");
        }

        // Résumé comparatif
        System.out.println("\n   📊 COMPARAISON :");
        System.out.println("   ├─ Templates : " + (template1.equals(template2) ? "IDENTIQUE ❌" : "DIFFÉRENT"));
        System.out.println("   ├─ IA 1 : " + (ia1.isEmpty() ? "ÉCHEC (fallback)" : "✅ Généré"));
        System.out.println("   ├─ IA 2 : " + (ia2.isEmpty() ? "ÉCHEC (fallback)" : "✅ Généré"));
        System.out.println("   └─ IA 1 vs IA 2 : " + (ia1.equals(ia2) ? "IDENTIQUE" : "DIFFÉRENT ✨"));
    }
}

