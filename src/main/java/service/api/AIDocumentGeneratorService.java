package service.api;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class AIDocumentGeneratorService {

    private static final String API_URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "llama3";
    private static final int TIMEOUT_CONNECT = 30000;
    private static final int TIMEOUT_READ = 120000;

    // ═══════════════════════════════════════════════════════════════════════
    // MAIN GENERATION METHOD
    // ═══════════════════════════════════════════════════════════════════════

    public CompletableFuture<GeneratedDocument> generateDocumentAsync(
            String type,
            String employeeName,
            String position,
            String employeeID,
            Date hireDate,
            String additionalInfo) {

        return CompletableFuture.supplyAsync(() -> {
            System.out.println("═══════════════════════════════════════════════");
            System.out.println("🚀 Starting document generation...");
            System.out.println("   Type: " + type);
            System.out.println("   Employee: " + employeeName);
            System.out.println("   Position: " + position);
            System.out.println("═══════════════════════════════════════════════");

            // Try AI generation first
            if (isOllamaAvailable()) {
                try {
                    String prompt = buildPrompt(type, employeeName, position, employeeID, additionalInfo);
                    String aiResponse = callOllama(prompt);

                    if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                        GeneratedDocument doc = parseAIResponse(aiResponse, type, employeeName);
                        if (doc != null && doc.isValid) {
                            System.out.println("✅ AI document generated successfully!");
                            return doc;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ AI generation failed, using template: " + e.getMessage());
                }
            } else {
                System.out.println("⚠️ Ollama not available, using template");
            }

            // Fallback to template
            System.out.println("📝 Using template-based document...");
            return createTemplateDocument(type, employeeName, position, employeeID, additionalInfo);
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CHECK OLLAMA AVAILABILITY
    // ═══════════════════════════════════════════════════════════════════════

    private boolean isOllamaAvailable() {
        try {
            URL url = new URL("http://localhost:11434/api/tags");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();
            conn.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BUILD AI PROMPT
    // ═══════════════════════════════════════════════════════════════════════

    private String buildPrompt(String type, String name, String position, String id, String info) {
        String today = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(new Date());
        String refDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

        return "Vous êtes un assistant RH professionnel. Générez un document RH formel.\n\n" +
                "IMPORTANT: Répondez UNIQUEMENT avec un objet JSON valide. Pas de texte avant ou après.\n\n" +
                "Structure JSON requise:\n" +
                "{\n" +
                "  \"title\": \"Titre du document\",\n" +
                "  \"reference\": \"REF/RH/" + refDate + "/001\",\n" +
                "  \"header\": \"ENTREPRISE\\n123 Avenue\\nTunis\",\n" +
                "  \"recipient\": \"À qui de droit\",\n" +
                "  \"subject\": \"Objet du document\",\n" +
                "  \"body\": \"Contenu principal...\",\n" +
                "  \"closing\": \"Cordialement,\",\n" +
                "  \"signature\": \"Service RH\",\n" +
                "  \"footer\": \"Document généré électroniquement\",\n" +
                "  \"summary\": \"Résumé\"\n" +
                "}\n\n" +
                "Informations:\n" +
                "- Type: " + type + "\n" +
                "- Employé: " + name + "\n" +
                "- Poste: " + position + "\n" +
                "- ID: " + id + "\n" +
                "- Date: " + today + "\n" +
                "- Contexte: " + (info != null ? info : "Aucun") + "\n\n" +
                "Répondez avec le JSON uniquement:";
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CALL OLLAMA API
    // ═══════════════════════════════════════════════════════════════════════

    private String callOllama(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        conn.setConnectTimeout(TIMEOUT_CONNECT);
        conn.setReadTimeout(TIMEOUT_READ);

        JSONObject body = new JSONObject();
        body.put("model", MODEL);
        body.put("stream", false);

        JSONObject options = new JSONObject();
        options.put("temperature", 0.3);
        body.put("options", options);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", "Tu es un assistant RH. Réponds uniquement en JSON valide."));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", prompt));
        body.put("messages", messages);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            return null;
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getJSONObject("message").getString("content");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PARSE AI RESPONSE - FIXED (No more error spam)
    // ═══════════════════════════════════════════════════════════════════════

    private GeneratedDocument parseAIResponse(String aiResponse, String fallbackTitle, String employeeName) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return null;
        }

        try {
            String cleaned = aiResponse.trim();

            // Remove markdown code blocks
            cleaned = cleaned.replaceAll("```json\\s*", "");
            cleaned = cleaned.replaceAll("```\\s*", "");
            cleaned = cleaned.trim();

            // Find JSON object boundaries
            int start = cleaned.indexOf("{");
            int end = cleaned.lastIndexOf("}");

            if (start == -1 || end == -1 || end <= start) {
                return null; // No valid JSON found, will use template
            }

            String jsonStr = cleaned.substring(start, end + 1);

            // Try to parse
            JSONObject json = new JSONObject(jsonStr);

            GeneratedDocument doc = new GeneratedDocument();
            doc.title = json.optString("title", fallbackTitle);
            doc.reference = json.optString("reference",
                    "REF/RH/" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/001");
            doc.header = json.optString("header", "").replace("\\n", "\n");
            doc.recipient = json.optString("recipient", "À qui de droit");
            doc.subject = json.optString("subject", doc.title);
            doc.body = json.optString("body", "").replace("\\n", "\n");
            doc.closing = json.optString("closing", "Cordialement,");
            doc.signature = json.optString("signature", "Service RH").replace("\\n", "\n");
            doc.footer = json.optString("footer", "Document généré électroniquement");
            doc.summary = json.optString("summary", "");
            doc.employeeName = employeeName;
            doc.generatedDate = new Date();
            doc.isValid = !doc.body.isEmpty();

            return doc.isValid ? doc : null;

        } catch (Exception e) {
            // Silent fail - will use template instead
            // No need to print stack trace since template fallback works
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TEMPLATE DOCUMENT (Always works!)
    // ═══════════════════════════════════════════════════════════════════════

    private GeneratedDocument createTemplateDocument(String type, String name,
                                                     String position, String id, String info) {
        String today = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(new Date());
        String refDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

        GeneratedDocument doc = new GeneratedDocument();
        doc.employeeName = name;
        doc.generatedDate = new Date();
        doc.reference = "REF/RH/" + refDate + "/001";
        doc.header = "ENTREPRISE\n123 Avenue Principale\nTunis, Tunisie\nTél: +216 XX XXX XXX";
        doc.recipient = "À qui de droit";
        doc.closing = "Cordialement,";
        doc.signature = "Le Directeur des Ressources Humaines\n\n[Signature]\n\nService RH";
        doc.footer = "Document généré électroniquement - Valide sans signature";
        doc.isValid = true;

        String typeLower = type != null ? type.toLowerCase() : "";

        if (typeLower.contains("attestation") || typeLower.contains("travail")) {
            doc.title = "ATTESTATION DE TRAVAIL";
            doc.subject = "Attestation de Travail";
            doc.body = "Je soussigné(e), Directeur des Ressources Humaines, atteste par la présente que:\n\n" +
                    "Nom et Prénom: " + name + "\n" +
                    "Poste occupé: " + position + "\n" +
                    "Numéro d'employé: " + id + "\n\n" +
                    "travaille au sein de notre entreprise.\n\n" +
                    (info != null && !info.isEmpty() ? "Informations complémentaires:\n" + info + "\n\n" : "") +
                    "Cette attestation est délivrée à l'intéressé(e) pour servir et valoir ce que de droit.\n\n" +
                    "Fait à Tunis, le " + today;
            doc.summary = "Attestation de travail pour " + name;

        } else if (typeLower.contains("congé") || typeLower.contains("conge")) {
            doc.title = "AUTORISATION DE CONGÉ";
            doc.subject = "Autorisation de Congé";
            doc.body = "Suite à la demande de congé soumise par:\n\n" +
                    "Nom et Prénom: " + name + "\n" +
                    "Poste: " + position + "\n" +
                    "Numéro d'employé: " + id + "\n\n" +
                    "Nous avons le plaisir de vous informer que votre demande de congé a été approuvée.\n\n" +
                    (info != null && !info.isEmpty() ? "Détails:\n" + info + "\n\n" : "") +
                    "Nous vous souhaitons un excellent repos.\n\n" +
                    "Fait à Tunis, le " + today;
            doc.summary = "Autorisation de congé pour " + name;

        } else if (typeLower.contains("salaire") || typeLower.contains("certificat")) {
            doc.title = "CERTIFICAT DE SALAIRE";
            doc.subject = "Certificat de Salaire";
            doc.body = "Je soussigné(e), Directeur des Ressources Humaines, certifie que:\n\n" +
                    "Nom et Prénom: " + name + "\n" +
                    "Poste: " + position + "\n" +
                    "Numéro d'employé: " + id + "\n\n" +
                    "est employé(e) au sein de notre entreprise et perçoit une rémunération mensuelle.\n\n" +
                    (info != null && !info.isEmpty() ? "Informations:\n" + info + "\n\n" : "") +
                    "Ce certificat est délivré à la demande de l'intéressé(e).\n\n" +
                    "Fait à Tunis, le " + today;
            doc.summary = "Certificat de salaire pour " + name;

        } else if (typeLower.contains("recommandation")) {
            doc.title = "LETTRE DE RECOMMANDATION";
            doc.subject = "Lettre de Recommandation";
            doc.body = "À qui de droit,\n\n" +
                    "J'ai le plaisir de recommander " + name + " qui a occupé le poste de " + position +
                    " au sein de notre entreprise.\n\n" +
                    "Durant sa période d'emploi, " + name + " a fait preuve de professionnalisme, " +
                    "de compétence et d'un excellent esprit d'équipe.\n\n" +
                    (info != null && !info.isEmpty() ? "Commentaires:\n" + info + "\n\n" : "") +
                    "Je recommande vivement " + name + " pour toute opportunité professionnelle future.\n\n" +
                    "Fait à Tunis, le " + today;
            doc.summary = "Lettre de recommandation pour " + name;

        } else if (typeLower.contains("équipement") || typeLower.contains("equipement") || typeLower.contains("materiel")) {
            doc.title = "DEMANDE D'ÉQUIPEMENT";
            doc.subject = "Demande d'Équipement";
            doc.body = "Concernant la demande d'équipement soumise par:\n\n" +
                    "Nom et Prénom: " + name + "\n" +
                    "Poste: " + position + "\n" +
                    "Numéro d'employé: " + id + "\n\n" +
                    "Nous accusons réception de votre demande d'équipement.\n\n" +
                    (info != null && !info.isEmpty() ? "Détails de la demande:\n" + info + "\n\n" : "") +
                    "Votre demande sera traitée dans les plus brefs délais.\n\n" +
                    "Fait à Tunis, le " + today;
            doc.summary = "Demande d'équipement pour " + name;

        } else {
            // Default template for any other type
            doc.title = type != null ? type.toUpperCase() : "DOCUMENT RH";
            doc.subject = type != null ? type : "Document RH";
            doc.body = "Document généré pour:\n\n" +
                    "Nom et Prénom: " + name + "\n" +
                    "Poste: " + position + "\n" +
                    "Numéro d'employé: " + id + "\n\n" +
                    "Type de document: " + (type != null ? type : "Document") + "\n\n" +
                    (info != null && !info.isEmpty() ? "Informations:\n" + info + "\n\n" : "") +
                    "Fait à Tunis, le " + today;
            doc.summary = (type != null ? type : "Document") + " pour " + name;
        }

        System.out.println("✅ Template document created: " + doc.title);
        return doc;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EXPORT TO PDF
    // ═══════════════════════════════════════════════════════════════════════

    public CompletableFuture<File> exportToPDFAsync(GeneratedDocument doc, String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document(
                        PageSize.A4, 50, 50, 50, 50);

                File file = new File(path);
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }

                PdfWriter.getInstance(pdfDocument, new FileOutputStream(file));
                pdfDocument.open();

                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
                Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);
                Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, BaseColor.GRAY);

                // Header
                if (doc.header != null && !doc.header.isEmpty()) {
                    for (String line : doc.header.split("\n")) {
                        Paragraph p = new Paragraph(line.trim(), headerFont);
                        p.setAlignment(Element.ALIGN_RIGHT);
                        pdfDocument.add(p);
                    }
                    pdfDocument.add(Chunk.NEWLINE);
                }

                // Reference
                if (doc.reference != null && !doc.reference.isEmpty()) {
                    Paragraph ref = new Paragraph("Réf: " + doc.reference, headerFont);
                    pdfDocument.add(ref);
                    pdfDocument.add(Chunk.NEWLINE);
                }

                // Date
                String dateStr = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(doc.generatedDate);
                Paragraph datePara = new Paragraph("Tunis, le " + dateStr, headerFont);
                datePara.setAlignment(Element.ALIGN_RIGHT);
                pdfDocument.add(datePara);
                pdfDocument.add(Chunk.NEWLINE);
                pdfDocument.add(Chunk.NEWLINE);

                // Title
                Paragraph title = new Paragraph(doc.title, titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                pdfDocument.add(title);
                pdfDocument.add(Chunk.NEWLINE);
                pdfDocument.add(Chunk.NEWLINE);

                // Recipient
                if (doc.recipient != null && !doc.recipient.isEmpty()) {
                    pdfDocument.add(new Paragraph(doc.recipient, bodyFont));
                    pdfDocument.add(Chunk.NEWLINE);
                }

                // Body
                if (doc.body != null && !doc.body.isEmpty()) {
                    for (String line : doc.body.split("\n")) {
                        Paragraph p = new Paragraph(line, bodyFont);
                        p.setAlignment(Element.ALIGN_JUSTIFIED);
                        pdfDocument.add(p);
                    }
                    pdfDocument.add(Chunk.NEWLINE);
                }

                // Closing
                if (doc.closing != null && !doc.closing.isEmpty()) {
                    pdfDocument.add(new Paragraph(doc.closing, bodyFont));
                    pdfDocument.add(Chunk.NEWLINE);
                }

                // Signature
                if (doc.signature != null && !doc.signature.isEmpty()) {
                    pdfDocument.add(Chunk.NEWLINE);
                    for (String line : doc.signature.split("\n")) {
                        Paragraph p = new Paragraph(line.trim(), bodyFont);
                        p.setAlignment(Element.ALIGN_RIGHT);
                        pdfDocument.add(p);
                    }
                }

                // Footer
                if (doc.footer != null && !doc.footer.isEmpty()) {
                    pdfDocument.add(Chunk.NEWLINE);
                    pdfDocument.add(Chunk.NEWLINE);
                    Paragraph footer = new Paragraph(doc.footer, footerFont);
                    footer.setAlignment(Element.ALIGN_CENTER);
                    pdfDocument.add(footer);
                }

                pdfDocument.close();
                System.out.println("✅ PDF exported successfully: " + file.getAbsolutePath());
                return file;

            } catch (Exception e) {
                System.err.println("❌ PDF export error: " + e.getMessage());
                return null;
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EXPORT TO WORD
    // ═══════════════════════════════════════════════════════════════════════

    public CompletableFuture<File> exportToWordAsync(GeneratedDocument doc, String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                XWPFDocument wordDocument = new XWPFDocument();
                File file = new File(path);

                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }

                // Header
                if (doc.header != null && !doc.header.isEmpty()) {
                    XWPFParagraph headerPara = wordDocument.createParagraph();
                    headerPara.setAlignment(ParagraphAlignment.RIGHT);
                    for (String line : doc.header.split("\n")) {
                        XWPFRun run = headerPara.createRun();
                        run.setText(line.trim());
                        run.setFontSize(10);
                        run.setColor("666666");
                        run.addBreak();
                    }
                }

                // Reference
                if (doc.reference != null && !doc.reference.isEmpty()) {
                    XWPFParagraph refPara = wordDocument.createParagraph();
                    XWPFRun refRun = refPara.createRun();
                    refRun.setText("Réf: " + doc.reference);
                    refRun.setFontSize(10);
                    refRun.setColor("666666");
                }

                // Date
                XWPFParagraph datePara = wordDocument.createParagraph();
                datePara.setAlignment(ParagraphAlignment.RIGHT);
                XWPFRun dateRun = datePara.createRun();
                dateRun.setText("Tunis, le " + new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(doc.generatedDate));
                dateRun.setFontSize(10);
                dateRun.addBreak();

                // Title
                XWPFParagraph titlePara = wordDocument.createParagraph();
                titlePara.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun titleRun = titlePara.createRun();
                titleRun.setText(doc.title);
                titleRun.setBold(true);
                titleRun.setFontSize(16);
                titleRun.addBreak();
                titleRun.addBreak();

                // Recipient
                if (doc.recipient != null && !doc.recipient.isEmpty()) {
                    XWPFParagraph recipientPara = wordDocument.createParagraph();
                    XWPFRun recipientRun = recipientPara.createRun();
                    recipientRun.setText(doc.recipient);
                    recipientRun.setFontSize(11);
                    recipientRun.addBreak();
                }

                // Body
                if (doc.body != null && !doc.body.isEmpty()) {
                    XWPFParagraph bodyPara = wordDocument.createParagraph();
                    bodyPara.setAlignment(ParagraphAlignment.BOTH);
                    for (String line : doc.body.split("\n")) {
                        XWPFRun bodyRun = bodyPara.createRun();
                        bodyRun.setText(line);
                        bodyRun.setFontSize(11);
                        bodyRun.addBreak();
                    }
                }

                // Closing
                if (doc.closing != null && !doc.closing.isEmpty()) {
                    wordDocument.createParagraph();
                    XWPFParagraph closingPara = wordDocument.createParagraph();
                    XWPFRun closingRun = closingPara.createRun();
                    closingRun.setText(doc.closing);
                    closingRun.setFontSize(11);
                }

                // Signature
                if (doc.signature != null && !doc.signature.isEmpty()) {
                    XWPFParagraph sigPara = wordDocument.createParagraph();
                    sigPara.setAlignment(ParagraphAlignment.RIGHT);
                    for (String line : doc.signature.split("\n")) {
                        XWPFRun sigRun = sigPara.createRun();
                        sigRun.setText(line.trim());
                        sigRun.setFontSize(11);
                        sigRun.addBreak();
                    }
                }

                // Footer
                if (doc.footer != null && !doc.footer.isEmpty()) {
                    wordDocument.createParagraph();
                    XWPFParagraph footerPara = wordDocument.createParagraph();
                    footerPara.setAlignment(ParagraphAlignment.CENTER);
                    XWPFRun footerRun = footerPara.createRun();
                    footerRun.setText(doc.footer);
                    footerRun.setFontSize(9);
                    footerRun.setItalic(true);
                    footerRun.setColor("999999");
                }

                FileOutputStream out = new FileOutputStream(file);
                wordDocument.write(out);
                out.close();
                wordDocument.close();

                System.out.println("✅ Word exported successfully: " + file.getAbsolutePath());
                return file;

            } catch (Exception e) {
                System.err.println("❌ Word export error: " + e.getMessage());
                return null;
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GENERATED DOCUMENT CLASS
    // ═══════════════════════════════════════════════════════════════════════

    public static class GeneratedDocument {
        public boolean isValid = false;
        public String title = "";
        public String reference = "";
        public String header = "";
        public String recipient = "";
        public String subject = "";
        public String body = "";
        public String closing = "";
        public String signature = "";
        public String footer = "";
        public String summary = "";
        public Date generatedDate;
        public String employeeName = "";

        public String getFormattedText() {
            StringBuilder sb = new StringBuilder();

            if (header != null && !header.isEmpty()) {
                sb.append(header).append("\n\n");
            }
            if (reference != null && !reference.isEmpty()) {
                sb.append("Réf: ").append(reference).append("\n\n");
            }

            sb.append("══════════════════════════════════════════\n");
            sb.append(title).append("\n");
            sb.append("══════════════════════════════════════════\n\n");

            if (recipient != null && !recipient.isEmpty()) {
                sb.append(recipient).append("\n\n");
            }
            if (body != null && !body.isEmpty()) {
                sb.append(body).append("\n\n");
            }
            if (closing != null && !closing.isEmpty()) {
                sb.append(closing).append("\n\n");
            }
            if (signature != null && !signature.isEmpty()) {
                sb.append(signature).append("\n");
            }
            if (footer != null && !footer.isEmpty()) {
                sb.append("\n──────────────────────────────────────────\n");
                sb.append(footer);
            }

            return sb.toString();
        }
    }
}