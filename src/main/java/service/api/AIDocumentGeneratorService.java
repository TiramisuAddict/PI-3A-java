package service.api;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

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

    // ═══════════════════════════════════════════════════════════════════════════
    // MOMENTUM COLOR PALETTE
    // ═══════════════════════════════════════════════════════════════════════════

    // Primary colors
    private static final BaseColor ACCENT_PRIMARY = new BaseColor(74, 93, 239);      // #4A5DEF
    private static final BaseColor ACCENT_DARK = new BaseColor(57, 68, 213);         // #3944D5
    private static final BaseColor ACCENT_LIGHT = new BaseColor(192, 224, 255);      // #C0E0FF
    private static final BaseColor ACCENT_BG = new BaseColor(240, 245, 255);         // #F0F5FF

    // Text colors
    private static final BaseColor TEXT_DARK = new BaseColor(44, 62, 80);            // #2c3e50
    private static final BaseColor TEXT_MUTED = new BaseColor(107, 114, 128);        // #6b7280
    private static final BaseColor TEXT_LIGHT = new BaseColor(153, 153, 153);        // #999999

    // Status colors
    private static final BaseColor SUCCESS_COLOR = new BaseColor(39, 174, 96);       // #27ae60
    private static final BaseColor WARNING_COLOR = new BaseColor(243, 156, 18);      // #f39c12
    private static final BaseColor DANGER_COLOR = new BaseColor(231, 76, 60);        // #e74c3c

    // ═══════════════════════════════════════════════════════════════════════════
    // MAIN GENERATION METHOD
    // ═══════════════════════════════════════════════════════════════════════════

    public CompletableFuture<GeneratedDocument> generateDocumentAsync(
            String type, String employeeName, String position,
            String employeeID, Date hireDate, String additionalInfo) {

        return CompletableFuture.supplyAsync(() -> {
            System.out.println("═══════════════════════════════════════════════");
            System.out.println("🚀 Generating document...");
            System.out.println("   Type: " + type);
            System.out.println("   Employee: " + employeeName);
            System.out.println("═══════════════════════════════════════════════");

            // Try AI first
            if (isOllamaAvailable()) {
                try {
                    String prompt = buildPrompt(type, employeeName, position, employeeID, additionalInfo);
                    String aiResponse = callOllama(prompt);

                    if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                        GeneratedDocument doc = parseAIResponse(aiResponse, type, employeeName);
                        if (doc != null && doc.isValid) {
                            System.out.println("✅ AI document generated!");
                            return doc;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ AI failed, using template");
                }
            }

            // Fallback to template
            System.out.println("📝 Using template document...");
            return createTemplateDocument(type, employeeName, position, employeeID, additionalInfo);
        });
    }

    private boolean isOllamaAvailable() {
        try {
            URL url = new URL("http://localhost:11434/api/tags");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            int code = conn.getResponseCode();
            conn.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private String buildPrompt(String type, String name, String position, String id, String info) {
        String today = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(new Date());
        String refDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

        return "Vous êtes un assistant RH. Générez un document professionnel.\n\n" +
                "Répondez UNIQUEMENT avec un JSON valide:\n\n" +
                "{\n  \"title\": \"...\",\n  \"reference\": \"REF/RH/" + refDate + "/001\",\n" +
                "  \"header\": \"ENTREPRISE\\nAdresse\",\n  \"recipient\": \"À qui de droit\",\n" +
                "  \"subject\": \"Objet\",\n  \"body\": \"Contenu...\",\n" +
                "  \"closing\": \"Cordialement,\",\n  \"signature\": \"Service RH\",\n" +
                "  \"footer\": \"Document généré électroniquement\"\n}\n\n" +
                "Type: " + type + "\nEmployé: " + name + "\nPoste: " + position + "\n" +
                "ID: " + id + "\nDate: " + today + "\nContexte: " + (info != null ? info : "Aucun") +
                "\n\nRépondez avec le JSON:";
    }

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
        messages.put(new JSONObject().put("role", "system")
                .put("content", "Réponds uniquement en JSON valide."));
        messages.put(new JSONObject().put("role", "user").put("content", prompt));
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

        JSONObject json = new JSONObject(response.toString());
        return json.getJSONObject("message").getString("content");
    }

    private GeneratedDocument parseAIResponse(String aiResponse, String fallbackTitle, String employeeName) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) return null;

        try {
            String cleaned = aiResponse.trim()
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            int start = cleaned.indexOf("{");
            int end = cleaned.lastIndexOf("}");

            if (start == -1 || end == -1 || end <= start) return null;

            String jsonStr = cleaned.substring(start, end + 1);
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
            doc.employeeName = employeeName;
            doc.generatedDate = new Date();
            doc.isValid = !doc.body.isEmpty();

            return doc.isValid ? doc : null;

        } catch (Exception e) {
            return null;
        }
    }

    private GeneratedDocument createTemplateDocument(String type, String name,
                                                     String position, String id, String info) {
        String today = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(new Date());
        String refDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

        GeneratedDocument doc = new GeneratedDocument();
        doc.employeeName = name;
        doc.generatedDate = new Date();
        doc.reference = "REF/RH/" + refDate + "/001";
        doc.header = "MOMENTUM HR\n123 Avenue Principale\nTunis, Tunisie\nTél: +216 XX XXX XXX";
        doc.recipient = "À qui de droit";
        doc.closing = "Cordialement,";
        doc.signature = "Le Directeur des Ressources Humaines\n\n[Signature]\n\nService RH";
        doc.footer = "Document généré par Momentum HR - Valide sans signature";
        doc.isValid = true;

        String typeLower = type != null ? type.toLowerCase() : "";

        if (typeLower.contains("attestation") || typeLower.contains("travail")) {
            doc.title = "ATTESTATION DE TRAVAIL";
            doc.subject = "Attestation de Travail";
            doc.body = "Je soussigné(e), Directeur des Ressources Humaines de MOMENTUM HR, " +
                    "atteste par la présente que:\n\n" +
                    "Nom et Prénom: " + name + "\n" +
                    "Poste occupé: " + position + "\n" +
                    "Numéro d'employé: " + id + "\n\n" +
                    "travaille au sein de notre entreprise.\n\n" +
                    (info != null && !info.isEmpty() ? "Informations complémentaires:\n" + info + "\n\n" : "") +
                    "Cette attestation est délivrée à l'intéressé(e) pour servir et valoir ce que de droit.\n\n" +
                    "Fait à Tunis, le " + today;

        } else if (typeLower.contains("congé") || typeLower.contains("conge")) {
            doc.title = "AUTORISATION DE CONGÉ";
            doc.subject = "Autorisation de Congé";
            doc.body = "Suite à la demande de congé soumise par:\n\n" +
                    "Nom et Prénom: " + name + "\n" +
                    "Poste: " + position + "\n" +
                    "Numéro d'employé: " + id + "\n\n" +
                    "Nous avons le plaisir de vous informer que votre demande a été approuvée.\n\n" +
                    (info != null && !info.isEmpty() ? "Détails:\n" + info + "\n\n" : "") +
                    "Nous vous souhaitons un excellent repos.\n\n" +
                    "Fait à Tunis, le " + today;

        } else if (typeLower.contains("équipement") || typeLower.contains("equipement") || typeLower.contains("materiel")) {
            doc.title = "DEMANDE D'ÉQUIPEMENT";
            doc.subject = "Demande d'Équipement";
            doc.body = "Concernant la demande d'équipement soumise par:\n\n" +
                    "Nom et Prénom: " + name + "\n" +
                    "Poste: " + position + "\n" +
                    "Numéro d'employé: " + id + "\n\n" +
                    "Nous accusons réception de votre demande.\n\n" +
                    (info != null && !info.isEmpty() ? "Détails de la demande:\n" + info + "\n\n" : "") +
                    "Votre demande sera traitée dans les plus brefs délais.\n\n" +
                    "Fait à Tunis, le " + today;

        } else {
            doc.title = type != null ? type.toUpperCase() : "DOCUMENT RH";
            doc.subject = type != null ? type : "Document RH";
            doc.body = "Document généré pour:\n\n" +
                    "Nom et Prénom: " + name + "\n" +
                    "Poste: " + position + "\n" +
                    "Numéro d'employé: " + id + "\n\n" +
                    "Type: " + (type != null ? type : "Document") + "\n\n" +
                    (info != null && !info.isEmpty() ? "Informations:\n" + info + "\n\n" : "") +
                    "Fait à Tunis, le " + today;
        }

        doc.summary = doc.title + " pour " + name;
        System.out.println("✅ Template created: " + doc.title);
        return doc;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EXPORT TO PDF - WITH MOMENTUM STYLING
    // ═══════════════════════════════════════════════════════════════════════════

    public CompletableFuture<File> exportToPDFAsync(GeneratedDocument doc, String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                com.itextpdf.text.Document pdf = new com.itextpdf.text.Document(
                        PageSize.A4, 50, 50, 50, 50);

                File file = new File(path);
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }

                PdfWriter writer = PdfWriter.getInstance(pdf, new FileOutputStream(file));
                pdf.open();

                // Fonts
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, ACCENT_PRIMARY);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_MUTED);
                Font subjectFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, TEXT_DARK);
                Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, TEXT_DARK);
                Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, TEXT_LIGHT);
                Font accentFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, ACCENT_PRIMARY);

                // ═══════════════════════════════════════════════════════════════════
                // HEADER BAR (Momentum styled)
                // ═══════════════════════════════════════════════════════════════════
                PdfPTable headerBar = new PdfPTable(1);
                headerBar.setWidthPercentage(100);

                PdfPCell headerCell = new PdfPCell();
                headerCell.setBackgroundColor(ACCENT_PRIMARY);
                headerCell.setPadding(15);
                headerCell.setBorder(Rectangle.NO_BORDER);

                Paragraph headerText = new Paragraph();
                Font headerTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.WHITE);
                Font headerSubFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(200, 220, 255));

                headerText.add(new Chunk("MOMENTUM HR\n", headerTitleFont));
                headerText.add(new Chunk("Système de Gestion des Ressources Humaines", headerSubFont));
                headerCell.addElement(headerText);
                headerBar.addCell(headerCell);

                pdf.add(headerBar);
                pdf.add(Chunk.NEWLINE);

                // ═══════════════════════════════════════════════════════════════════
                // DOCUMENT INFO BOX
                // ═══════════════════════════════════════════════════════════════════
                PdfPTable infoBox = new PdfPTable(2);
                infoBox.setWidthPercentage(100);
                infoBox.setWidths(new float[]{1, 1});

                // Left: Reference
                PdfPCell leftCell = new PdfPCell();
                leftCell.setBorder(Rectangle.NO_BORDER);
                leftCell.setPadding(10);
                leftCell.setBackgroundColor(ACCENT_BG);

                Paragraph refPara = new Paragraph();
                refPara.add(new Chunk("Référence: ", accentFont));
                refPara.add(new Chunk(doc.reference, bodyFont));
                leftCell.addElement(refPara);
                infoBox.addCell(leftCell);

                // Right: Date
                PdfPCell rightCell = new PdfPCell();
                rightCell.setBorder(Rectangle.NO_BORDER);
                rightCell.setPadding(10);
                rightCell.setBackgroundColor(ACCENT_BG);
                rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

                String dateStr = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(doc.generatedDate);
                Paragraph datePara = new Paragraph("Tunis, le " + dateStr, headerFont);
                datePara.setAlignment(Element.ALIGN_RIGHT);
                rightCell.addElement(datePara);
                infoBox.addCell(rightCell);

                pdf.add(infoBox);
                pdf.add(Chunk.NEWLINE);
                pdf.add(Chunk.NEWLINE);

                // ═══════════════════════════════════════════════════════════════════
                // TITLE (Centered, Momentum color)
                // ═══════════════════════════════════════════════════════════════════
                Paragraph title = new Paragraph(doc.title, titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                pdf.add(title);

                // Decorative line
                PdfPTable line = new PdfPTable(1);
                line.setWidthPercentage(40);
                PdfPCell lineCell = new PdfPCell();
                lineCell.setBorder(Rectangle.BOTTOM);
                lineCell.setBorderColor(ACCENT_PRIMARY);
                lineCell.setBorderWidth(2);
                lineCell.setFixedHeight(5);
                line.addCell(lineCell);
                pdf.add(line);
                pdf.add(Chunk.NEWLINE);

                // ═══════════════════════════════════════════════════════════════════
                // RECIPIENT
                // ═══════════════════════════════════════════════════════════════════
                if (doc.recipient != null && !doc.recipient.isEmpty()) {
                    Paragraph recipient = new Paragraph(doc.recipient, subjectFont);
                    pdf.add(recipient);
                    pdf.add(Chunk.NEWLINE);
                }

                // ═══════════════════════════════════════════════════════════════════
                // SUBJECT
                // ═══════════════════════════════════════════════════════════════════
                if (doc.subject != null && !doc.subject.isEmpty() && !doc.subject.equals(doc.title)) {
                    Paragraph subject = new Paragraph();
                    subject.add(new Chunk("Objet: ", accentFont));
                    subject.add(new Chunk(doc.subject, bodyFont));
                    pdf.add(subject);
                    pdf.add(Chunk.NEWLINE);
                }

                // ═══════════════════════════════════════════════════════════════════
                // BODY
                // ═══════════════════════════════════════════════════════════════════
                if (doc.body != null && !doc.body.isEmpty()) {
                    String[] paragraphs = doc.body.split("\n\n");
                    for (String para : paragraphs) {
                        String[] lines = para.split("\n");
                        for (String bodyLine : lines) {
                            if (!bodyLine.trim().isEmpty()) {
                                Paragraph p = new Paragraph(bodyLine.trim(), bodyFont);
                                p.setAlignment(Element.ALIGN_JUSTIFIED);
                                p.setSpacingAfter(3);
                                pdf.add(p);
                            }
                        }
                        pdf.add(Chunk.NEWLINE);
                    }
                }

                pdf.add(Chunk.NEWLINE);

                // ═══════════════════════════════════════════════════════════════════
                // CLOSING
                // ═══════════════════════════════════════════════════════════════════
                if (doc.closing != null && !doc.closing.isEmpty()) {
                    Paragraph closing = new Paragraph(doc.closing, bodyFont);
                    pdf.add(closing);
                    pdf.add(Chunk.NEWLINE);
                }

                // ═══════════════════════════════════════════════════════════════════
                // SIGNATURE (Right aligned)
                // ═══════════════════════════════════════════════════════════════════
                if (doc.signature != null && !doc.signature.isEmpty()) {
                    pdf.add(Chunk.NEWLINE);
                    for (String sigLine : doc.signature.split("\n")) {
                        Paragraph p = new Paragraph(sigLine.trim(), bodyFont);
                        p.setAlignment(Element.ALIGN_RIGHT);
                        pdf.add(p);
                    }
                }

                // ═══════════════════════════════════════════════════════════════════
                // FOOTER BAR
                // ═══════════════════════════════════════════════════════════════════
                pdf.add(Chunk.NEWLINE);
                pdf.add(Chunk.NEWLINE);

                PdfPTable footerBar = new PdfPTable(1);
                footerBar.setWidthPercentage(100);

                PdfPCell footerCell = new PdfPCell();
                footerCell.setBackgroundColor(ACCENT_BG);
                footerCell.setPadding(10);
                footerCell.setBorder(Rectangle.TOP);
                footerCell.setBorderColor(ACCENT_LIGHT);

                Paragraph footerText = new Paragraph(doc.footer, footerFont);
                footerText.setAlignment(Element.ALIGN_CENTER);
                footerCell.addElement(footerText);
                footerBar.addCell(footerCell);

                pdf.add(footerBar);

                pdf.close();
                System.out.println("✅ PDF exported: " + file.getAbsolutePath());
                return file;

            } catch (Exception e) {
                System.err.println("❌ PDF error: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EXPORT TO WORD - WITH MOMENTUM STYLING
    // ═══════════════════════════════════════════════════════════════════════════

    public CompletableFuture<File> exportToWordAsync(GeneratedDocument doc, String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                XWPFDocument word = new XWPFDocument();
                File file = new File(path);

                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }

                // Header
                XWPFParagraph headerPara = word.createParagraph();
                headerPara.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun headerRun = headerPara.createRun();
                headerRun.setText("MOMENTUM HR");
                headerRun.setBold(true);
                headerRun.setFontSize(16);
                headerRun.setColor("4A5DEF"); // Momentum primary color
                headerRun.addBreak();
                XWPFRun subRun = headerPara.createRun();
                subRun.setText("Système de Gestion des Ressources Humaines");
                subRun.setFontSize(10);
                subRun.setColor("6b7280");
                subRun.addBreak();
                subRun.addBreak();

                // Reference & Date
                XWPFParagraph infoPara = word.createParagraph();
                XWPFRun infoRun = infoPara.createRun();
                infoRun.setText("Réf: " + doc.reference);
                infoRun.setFontSize(10);
                infoRun.setColor("4A5DEF");
                infoRun.addBreak();

                XWPFParagraph datePara = word.createParagraph();
                datePara.setAlignment(ParagraphAlignment.RIGHT);
                XWPFRun dateRun = datePara.createRun();
                dateRun.setText("Tunis, le " + new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(doc.generatedDate));
                dateRun.setFontSize(10);
                dateRun.setColor("6b7280");
                dateRun.addBreak();
                dateRun.addBreak();

                // Title
                XWPFParagraph titlePara = word.createParagraph();
                titlePara.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun titleRun = titlePara.createRun();
                titleRun.setText(doc.title);
                titleRun.setBold(true);
                titleRun.setFontSize(18);
                titleRun.setColor("4A5DEF");
                titleRun.addBreak();
                titleRun.addBreak();

                // Recipient
                if (doc.recipient != null && !doc.recipient.isEmpty()) {
                    XWPFParagraph recipientPara = word.createParagraph();
                    XWPFRun recipientRun = recipientPara.createRun();
                    recipientRun.setText(doc.recipient);
                    recipientRun.setBold(true);
                    recipientRun.setFontSize(11);
                    recipientRun.addBreak();
                }

                // Subject
                if (doc.subject != null && !doc.subject.isEmpty() && !doc.subject.equals(doc.title)) {
                    XWPFParagraph subjectPara = word.createParagraph();
                    XWPFRun subjectLabel = subjectPara.createRun();
                    subjectLabel.setText("Objet: ");
                    subjectLabel.setBold(true);
                    subjectLabel.setFontSize(11);
                    subjectLabel.setColor("4A5DEF");
                    XWPFRun subjectText = subjectPara.createRun();
                    subjectText.setText(doc.subject);
                    subjectText.setFontSize(11);
                    subjectText.addBreak();
                }

                // Body
                if (doc.body != null && !doc.body.isEmpty()) {
                    XWPFParagraph bodyPara = word.createParagraph();
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
                    word.createParagraph();
                    XWPFParagraph closingPara = word.createParagraph();
                    XWPFRun closingRun = closingPara.createRun();
                    closingRun.setText(doc.closing);
                    closingRun.setFontSize(11);
                    closingRun.addBreak();
                }

                // Signature
                if (doc.signature != null && !doc.signature.isEmpty()) {
                    XWPFParagraph sigPara = word.createParagraph();
                    sigPara.setAlignment(ParagraphAlignment.RIGHT);
                    for (String sigLine : doc.signature.split("\n")) {
                        XWPFRun sigRun = sigPara.createRun();
                        sigRun.setText(sigLine.trim());
                        sigRun.setFontSize(11);
                        sigRun.addBreak();
                    }
                }

                // Footer
                if (doc.footer != null && !doc.footer.isEmpty()) {
                    word.createParagraph();
                    word.createParagraph();
                    XWPFParagraph footerPara = word.createParagraph();
                    footerPara.setAlignment(ParagraphAlignment.CENTER);
                    XWPFRun footerRun = footerPara.createRun();
                    footerRun.setText("─".repeat(40));
                    footerRun.addBreak();
                    footerRun.setText(doc.footer);
                    footerRun.setFontSize(9);
                    footerRun.setItalic(true);
                    footerRun.setColor("999999");
                }

                FileOutputStream out = new FileOutputStream(file);
                word.write(out);
                out.close();
                word.close();

                System.out.println("✅ Word exported: " + file.getAbsolutePath());
                return file;

            } catch (Exception e) {
                System.err.println("❌ Word error: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GENERATED DOCUMENT CLASS
    // ═══════════════════════════════════════════════════════════════════════════

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

            sb.append("╔══════════════════════════════════════════════════════╗\n");
            sb.append("║             MOMENTUM HR - DOCUMENT OFFICIEL          ║\n");
            sb.append("╚══════════════════════════════════════════════════════╝\n\n");

            if (reference != null && !reference.isEmpty()) {
                sb.append("Réf: ").append(reference).append("\n\n");
            }

            sb.append("═══════════════════════════════════════════════════════\n");
            sb.append("  ").append(title).append("\n");
            sb.append("═══════════════════════════════════════════════════════\n\n");

            if (recipient != null && !recipient.isEmpty()) {
                sb.append(recipient).append("\n\n");
            }

            if (subject != null && !subject.isEmpty() && !subject.equals(title)) {
                sb.append("Objet: ").append(subject).append("\n\n");
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
                sb.append("\n───────────────────────────────────────────────────────\n");
                sb.append(footer);
            }

            return sb.toString();
        }
    }
}