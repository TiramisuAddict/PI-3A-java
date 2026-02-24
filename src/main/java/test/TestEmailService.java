package test;

import service.api.EmailService;
import service.api.ApiConfig;

public class TestEmailService {
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("           EMAIL SERVICE TEST");
        System.out.println("═══════════════════════════════════════════════");

        // Test 1: Check configuration
        System.out.println("\n📋 Configuration Check:");
        System.out.println("   SMTP Host: " + ApiConfig.SMTP_HOST);
        System.out.println("   SMTP Port: " + ApiConfig.SMTP_PORT);
        System.out.println("   Username: " + (ApiConfig.SMTP_USERNAME != null ? "✅ Set" : "❌ Not set"));
        System.out.println("   Password: " + (ApiConfig.SMTP_PASSWORD != null && !ApiConfig.SMTP_PASSWORD.isEmpty() ? "✅ Set" : "❌ Not set"));

        // Test 2: Initialize service
        System.out.println("\n📋 Initializing Email Service...");
        EmailService emailService = new EmailService();

        // Test 3: Test connection
        System.out.println("\n📋 Testing Connection...");
        emailService.testConnection();

        // Test 4: Send test status change email
        if (emailService.isEnabled()) {
            System.out.println("\n📋 Sending Test Status Change Email...");
            emailService.sendStatusChangeToEmployee(
                    "Test User",
                    ApiConfig.RH_EMAIL, // Send to yourself for testing
                    "Test Demande",
                    "En cours",
                    "Résolue",
                    "Admin (RH)",
                    "This is a test email from the application."
            ).thenAccept(success -> {
                System.out.println(success ? "✅ Test email sent!" : "❌ Test email failed!");
            }).join(); // Wait for completion
        }

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("           TEST COMPLETE");
        System.out.println("═══════════════════════════════════════════════");
    }
}