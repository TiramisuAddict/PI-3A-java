package utils;

import javafx.scene.control.Label;

public class BadgeFactory {

    public static Label createBadge(String status) {
        if (status == null) status = "Inconnu";

        Label badge = new Label(status.toUpperCase());

        String baseStyle = "-fx-padding: 2 10; -fx-background-radius: 12; -fx-font-size: 10; -fx-font-weight: bold;";
        String colorStyle = "";

        switch (status.toLowerCase()) {
            // Recrutement (Candidat)
            case "présélectionné":
            case "ouvert":
                colorStyle = "-fx-background-color: -color-accent-subtle; -fx-text-fill: -color-accent-fg;";
                break;

            case "accepté":
            case "publiée":
                colorStyle = "-fx-background-color: -color-success-subtle; -fx-text-fill: -color-success-fg;";
                break;

            case "refusé":
            case "fermé":
                colorStyle = "-fx-background-color: -color-danger-subtle; -fx-text-fill: -color-danger-fg;";
                break;

            case "entretien":
                colorStyle = "-fx-background-color: -color-info-subtle; -fx-text-fill: -color-info-fg;";
                break;

            case "en attente":
            case "en cours":
            default:
                colorStyle = "-fx-background-color: -color-base-2; -fx-text-fill: -color-fg-muted;";
                break;
        }

        badge.setStyle(baseStyle + colorStyle);
        return badge;
    }
}