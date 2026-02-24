package service.api;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class EmailService {

    private final Session session;
    private final boolean enabled;

    public EmailService() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", ApiConfig.SMTP_HOST);
        props.put("mail.smtp.port", ApiConfig.SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", ApiConfig.SMTP_HOST);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        ApiConfig.SMTP_USERNAME,
                        ApiConfig.SMTP_PASSWORD
                );
            }
        });

        // Enable debug mode for troubleshooting
        this.session.setDebug(false); // Set to true for debugging

        this.enabled = ApiConfig.SMTP_USERNAME != null
                && !ApiConfig.SMTP_USERNAME.isEmpty()
                && !ApiConfig.SMTP_USERNAME.equals("your_email@gmail.com")
                && ApiConfig.SMTP_PASSWORD != null
                && !ApiConfig.SMTP_PASSWORD.isEmpty();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("📧 EMAIL SERVICE INITIALIZATION");
        System.out.println("   Status: " + (enabled ? "✅ ENABLED" : "❌ DISABLED"));
        System.out.println("   SMTP Host: " + ApiConfig.SMTP_HOST);
        System.out.println("   SMTP Port: " + ApiConfig.SMTP_PORT);
        System.out.println("   Username: " + (ApiConfig.SMTP_USERNAME != null ?
                ApiConfig.SMTP_USERNAME.substring(0, Math.min(5, ApiConfig.SMTP_USERNAME.length())) + "***" : "null"));
        System.out.println("═══════════════════════════════════════════════");
    }

    public boolean isEnabled() {
        return enabled;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CORE SEND EMAIL
    // ═══════════════════════════════════════════════════════════════════════

    private boolean sendEmail(String toEmail, String toName, String subject, String htmlBody) {
        if (!enabled) {
            System.out.println("⚠️ [SIMULATION] Email would be sent to: " + toEmail);
            System.out.println("   Subject: " + subject);
            return true;
        }

        if (toEmail == null || toEmail.isEmpty() || !toEmail.contains("@")) {
            System.err.println("❌ Invalid email address: " + toEmail);
            return false;
        }

        try {
            System.out.println("📧 Sending email to: " + toEmail);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(ApiConfig.SMTP_USERNAME, ApiConfig.SMTP_FROM_NAME));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, toName));
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlBody, "text/html; charset=UTF-8");
            message.setSentDate(new java.util.Date());

            Transport.send(message);

            System.out.println("✅ Email sent successfully to: " + toEmail);
            return true;

        } catch (AuthenticationFailedException e) {
            System.err.println("❌ EMAIL AUTHENTICATION FAILED!");
            System.err.println("   Check your SMTP_USERNAME and SMTP_PASSWORD in ApiConfig.java");
            System.err.println("   For Gmail: Use App Password, not regular password");
            System.err.println("   Error: " + e.getMessage());
            return false;

        } catch (MessagingException e) {
            System.err.println("❌ EMAIL SENDING FAILED to " + toEmail);
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();
            return false;

        } catch (Exception e) {
            System.err.println("❌ UNEXPECTED EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NEW DEMANDE → RH
    // ═══════════════════════════════════════════════════════════════════════

    public CompletableFuture<Boolean> sendNewDemandeToRH(
            String employeeName, String employeeEmail,
            String demandeTitre, String demandeType, String priorite) {

        return CompletableFuture.supplyAsync(() -> {
            String subject = "📋 Nouvelle Demande: " + demandeTitre;

            String prioriteColor;
            switch (priorite != null ? priorite.toUpperCase() : "") {
                case "HAUTE": prioriteColor = "#e74c3c"; break;
                case "NORMALE": prioriteColor = "#3498db"; break;
                default: prioriteColor = "#27ae60";
            }

            String html = buildNewDemandeHtml(employeeName, employeeEmail, demandeTitre, demandeType, priorite, prioriteColor);

            return sendEmail(ApiConfig.RH_EMAIL, "Service RH", subject, html);
        });
    }

    private String buildNewDemandeHtml(String employeeName, String employeeEmail,
                                       String demandeTitre, String demandeType, String priorite, String prioriteColor) {
        return "<!DOCTYPE html><html><body style='font-family:Segoe UI,Arial,sans-serif;margin:0;padding:0;background:#f4f4f4'>"
                + "<div style='max-width:600px;margin:20px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.1)'>"
                + "  <div style='background:linear-gradient(135deg,#2c3e50,#3498db);padding:30px;text-align:center'>"
                + "    <h1 style='color:white;margin:0;font-size:24px'>📋 Nouvelle Demande</h1>"
                + "    <p style='color:#bdc3c7;margin:5px 0 0'>Système de Gestion des Demandes</p>"
                + "  </div>"
                + "  <div style='padding:30px'>"
                + "    <div style='background:#f8f9fa;border-left:4px solid #3498db;padding:15px;border-radius:0 8px 8px 0;margin-bottom:20px'>"
                + "      <p style='margin:0;color:#555'>Une nouvelle demande a été soumise par <strong>" + employeeName + "</strong></p>"
                + "    </div>"
                + "    <table style='width:100%;border-collapse:collapse'>"
                + "      <tr><td style='padding:12px;border-bottom:1px solid #eee;color:#888;width:140px'>📌 Titre</td>"
                + "          <td style='padding:12px;border-bottom:1px solid #eee;font-weight:bold'>" + demandeTitre + "</td></tr>"
                + "      <tr><td style='padding:12px;border-bottom:1px solid #eee;color:#888'>📂 Type</td>"
                + "          <td style='padding:12px;border-bottom:1px solid #eee'>" + demandeType + "</td></tr>"
                + "      <tr><td style='padding:12px;border-bottom:1px solid #eee;color:#888'>⚡ Priorité</td>"
                + "          <td style='padding:12px;border-bottom:1px solid #eee'>"
                + "            <span style='background:" + prioriteColor + ";color:white;padding:4px 12px;border-radius:12px;font-size:13px'>" + priorite + "</span></td></tr>"
                + "      <tr><td style='padding:12px;border-bottom:1px solid #eee;color:#888'>👤 Employé</td>"
                + "          <td style='padding:12px;border-bottom:1px solid #eee'>" + employeeName + "</td></tr>"
                + "      <tr><td style='padding:12px;color:#888'>📧 Email</td>"
                + "          <td style='padding:12px'><a href='mailto:" + employeeEmail + "'>" + employeeEmail + "</a></td></tr>"
                + "    </table>"
                + "  </div>"
                + "  <div style='background:#f8f9fa;padding:15px;text-align:center;color:#999;font-size:12px'>"
                + "    " + ApiConfig.COMPANY_NAME + " — Système RH"
                + "  </div>"
                + "</div></body></html>";
    }

    // ═══════════════════════════════════════════════════════════════════════
    // STATUS CHANGE → EMPLOYEE
    // ═══════════════════════════════════════════════════════════════════════

    public CompletableFuture<Boolean> sendStatusChangeToEmployee(
            String employeeName, String employeeEmail,
            String demandeTitre,
            String oldStatus, String newStatus,
            String acteur, String comment) {

        return CompletableFuture.supplyAsync(() -> {
            System.out.println("═══════════════════════════════════════════════");
            System.out.println("📧 PREPARING STATUS CHANGE EMAIL");
            System.out.println("   To: " + employeeEmail);
            System.out.println("   Name: " + employeeName);
            System.out.println("   Demande: " + demandeTitre);
            System.out.println("   Status: " + oldStatus + " → " + newStatus);
            System.out.println("═══════════════════════════════════════════════");

            if (employeeEmail == null || employeeEmail.isEmpty() || !employeeEmail.contains("@")) {
                System.err.println("❌ Invalid employee email address");
                return false;
            }

            String subject = "🔄 Mise à jour: " + demandeTitre;
            String html = buildStatusChangeHtml(employeeName, demandeTitre, oldStatus, newStatus, acteur, comment);

            return sendEmail(employeeEmail, employeeName, subject, html);
        });
    }

    private String buildStatusChangeHtml(String employeeName, String demandeTitre,
                                         String oldStatus, String newStatus, String acteur, String comment) {
        return "<!DOCTYPE html><html><body style='font-family:Segoe UI,Arial,sans-serif;margin:0;padding:0;background:#f4f4f4'>"
                + "<div style='max-width:600px;margin:20px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.1)'>"
                + "  <div style='background:linear-gradient(135deg,#27ae60,#2ecc71);padding:30px;text-align:center'>"
                + "    <h1 style='color:white;margin:0;font-size:24px'>🔄 Mise à jour de votre demande</h1>"
                + "  </div>"
                + "  <div style='padding:30px'>"
                + "    <p style='font-size:16px'>Bonjour <strong>" + employeeName + "</strong>,</p>"
                + "    <p style='color:#555'>Le statut de votre demande a été mis à jour :</p>"
                + "    <div style='text-align:center;margin:30px 0'>"
                + "      <span style='background:#fadbd8;color:#c0392b;padding:10px 20px;border-radius:20px;font-weight:bold;font-size:14px'>" + oldStatus + "</span>"
                + "      <span style='font-size:28px;margin:0 20px;color:#555'>→</span>"
                + "      <span style='background:#d5f5e3;color:#27ae60;padding:10px 20px;border-radius:20px;font-weight:bold;font-size:14px'>" + newStatus + "</span>"
                + "    </div>"
                + "    <table style='width:100%;border-collapse:collapse;margin-top:20px'>"
                + "      <tr><td style='padding:12px;border-bottom:1px solid #eee;color:#888;width:140px'>📌 Demande</td>"
                + "          <td style='padding:12px;border-bottom:1px solid #eee;font-weight:bold'>" + demandeTitre + "</td></tr>"
                + "      <tr><td style='padding:12px;border-bottom:1px solid #eee;color:#888'>👤 Traité par</td>"
                + "          <td style='padding:12px;border-bottom:1px solid #eee'>" + acteur + "</td></tr>"
                + "      <tr><td style='padding:12px;color:#888'>💬 Commentaire</td>"
                + "          <td style='padding:12px'>" + (comment != null && !comment.isEmpty() ? comment : "<em style='color:#999'>Aucun commentaire</em>") + "</td></tr>"
                + "    </table>"
                + "    <div style='margin-top:30px;padding:20px;background:#f8f9fa;border-radius:8px;text-align:center'>"
                + "      <p style='margin:0;color:#666'>Vous pouvez consulter votre demande dans l'application.</p>"
                + "    </div>"
                + "  </div>"
                + "  <div style='background:#f8f9fa;padding:15px;text-align:center;color:#999;font-size:12px'>"
                + "    " + ApiConfig.COMPANY_NAME + " — Système RH | " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date())
                + "  </div>"
                + "</div></body></html>";
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TEST CONNECTION
    // ═══════════════════════════════════════════════════════════════════════

    public void testConnection() {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("📧 SMTP EMAIL CONFIGURATION TEST");
        System.out.println("   Enabled:  " + enabled);
        System.out.println("   Host:     " + ApiConfig.SMTP_HOST);
        System.out.println("   Port:     " + ApiConfig.SMTP_PORT);
        System.out.println("   Username: " + ApiConfig.SMTP_USERNAME);
        System.out.println("   RH Email: " + ApiConfig.RH_EMAIL);
        System.out.println("═══════════════════════════════════════════════");

        if (enabled) {
            System.out.println("\n🔄 Sending test email...");
            boolean ok = sendEmail(
                    ApiConfig.RH_EMAIL,
                    "Test",
                    "✅ Test Email - SMTP Works!",
                    "<div style='font-family:Arial;padding:20px'>" +
                            "<h2 style='color:#27ae60'>✅ Email Service Working!</h2>" +
                            "<p>Timestamp: " + new java.util.Date() + "</p>" +
                            "<p>Your email configuration is correct.</p>" +
                            "</div>"
            );
            System.out.println(ok ? "✅ Test email sent successfully!" : "❌ Test email failed!");
        } else {
            System.out.println("\n⚠️ Email service is disabled. Update ApiConfig.java with valid credentials.");
        }
    }
}