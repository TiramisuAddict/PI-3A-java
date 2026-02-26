package service;

import Models.Projet;
import Models.Tache;
import Models.priority;
import Models.statut_t;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
/**
 * AI Service powered by Google Gemini API (FREE TIER).
 * Free tier: 60 req/min, 1 million tokens/month, no credit card needed.
 * Configuration: set GEMINI_API_KEY=AIza... in the .env file at project root.
 */
public class OpenAIService {
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private String apiKey;
    private static OpenAIService instance;
    private OpenAIService() {
        this.apiKey = loadApiKey();
    }
    public static OpenAIService getInstance() {
        if (instance == null) {
            instance = new OpenAIService();
        }
        return instance;
    }
    private String loadApiKey() {
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            String key = dotenv.get("GEMINI_API_KEY");
            if (key != null && !key.isBlank() && !key.equals("your_gemini_api_key_here")) {
                return key;
            }
        } catch (DotenvException ignored) { }
        String envKey = System.getenv("GEMINI_API_KEY");
        return (envKey != null && !envKey.isBlank()) ? envKey : "";
    }
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
    public static String getApiKeyInstructions() {
        return "Pour obtenir une cle API Gemini GRATUITE :\n"
             + "1. Allez sur : https://makersuite.google.com/app/apikey\n"
             + "2. Connectez-vous avec votre compte Google\n"
             + "3. Cliquez sur Create API Key\n"
             + "4. Collez la cle dans le fichier .env :\n"
             + "   GEMINI_API_KEY=AIza...\n\n"
             + "Gratuit : 60 req/min, 1 million tokens/mois";
    }
    public String generateTaskDescription(String taskTitle, String projectName) throws IOException {
        requireKey();
        String prompt = "Tu es un assistant de gestion de projet. Genere une description concise (2-3 phrases) "
            + "pour la tache \"" + taskTitle + "\" dans le projet \"" + projectName + "\". "
            + "Reponds uniquement avec la description.";
        return callGemini(prompt);
    }
    public priority suggestPriority(String taskTitle, String taskDescription) throws IOException {
        requireKey();
        String desc = (taskDescription != null && !taskDescription.isBlank()) ? taskDescription : "Pas de description";
        String prompt = "Analyse cette tache et reponds avec UN mot : HAUTE, MOYENNE ou BASSE.\n"
            + "Titre: " + taskTitle + "\nDescription: " + desc;
        String response = callGemini(prompt).toUpperCase().trim();
        if (response.contains("HAUTE")) return priority.HAUTE;
        if (response.contains("BASSE")) return priority.BASSE;
        return priority.MOYENNE;
    }
    public String generateProjectPlanningSuggestions(Projet project, List<Tache> tasks) throws IOException {
        requireKey();
        int todo = 0, inProgress = 0, blocked = 0, done = 0;
        for (Tache t : tasks) {
            switch (t.getStatut_tache()) {
                case A_FAIRE  -> todo++;
                case EN_COURS -> inProgress++;
                case BLOCQUEE -> blocked++;
                case TERMINEE -> done++;
            }
        }
        String prompt = "Donne 3-4 suggestions pour ameliorer le projet \"" + project.getNom() + "\".\n"
            + "Taches: a faire=" + todo + ", en cours=" + inProgress + ", bloquees=" + blocked + ", terminees=" + done + "\n"
            + "Format : liste numerotee.";
        return callGemini(prompt);
    }
    public String summarizeProjectStatus(Projet project, List<Tache> tasks) throws IOException {
        requireKey();
        int highPriority = 0, overdue = 0;
        for (Tache t : tasks) {
            if (t.getPriority_tache() == priority.HAUTE && t.getStatut_tache() != statut_t.TERMINEE) highPriority++;
            if (t.getDate_limite() != null && t.getDate_limite().isBefore(java.time.LocalDate.now())
                    && t.getStatut_tache() != statut_t.TERMINEE) overdue++;
        }
        int total = tasks.size();
        long completed = tasks.stream().filter(t -> t.getStatut_tache() == statut_t.TERMINEE).count();
        int pct = total > 0 ? (int)(completed * 100 / total) : 0;
        String prompt = "Resume executif (4-5 phrases) du projet \"" + project.getNom() + "\".\n"
            + "Progression: " + pct + "% (" + completed + "/" + total + " terminees)\n"
            + "Haute priorite en attente: " + highPriority + ", En retard: " + overdue;
        return callGemini(prompt);
    }
    public String suggestSubtasks(String taskTitle, String taskDescription) throws IOException {
        requireKey();
        String desc = (taskDescription != null && !taskDescription.isBlank()) ? taskDescription : "Pas de description";
        String prompt = "Suggere 3-5 sous-taches pour : \"" + taskTitle + "\"\nDescription: " + desc + "\nFormat : liste numerotee.";
        return callGemini(prompt);
    }
    private void requireKey() throws IOException {
        if (!isConfigured()) throw new IOException("Cle API non configuree.\n\n" + getApiKeyInstructions());
    }
    private String callGemini(String prompt) throws IOException {
        var url = URI.create(GEMINI_API_URL + "?key=" + apiKey).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(30_000);
        String body = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapeJson(prompt)
                + "\"}]}],\"generationConfig\":{\"temperature\":0.7,\"maxOutputTokens\":500}}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        int code = conn.getResponseCode();
        if (code == HttpURLConnection.HTTP_OK) return parseGemini(readStream(conn.getInputStream()));
        String error = readStream(conn.getErrorStream());
        if (error.contains("API_KEY_INVALID")) throw new IOException("Cle API invalide. Verifiez le fichier .env");
        if (error.contains("QUOTA_EXCEEDED"))  throw new IOException("Quota depasse. Reessayez plus tard.");
        if (code == 403) throw new IOException("Acces refuse. Activez l API Gemini pour votre cle.");
        throw new IOException("Erreur API Gemini (" + code + "): " + error);
    }
    private String readStream(java.io.InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }
    private String parseGemini(String json) {
        try {
            int start = json.indexOf("\"text\":") + 7;
            start = json.indexOf("\"", start) + 1;
            int end = start;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
                end++;
            }
            return json.substring(start, end)
                       .replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\").trim();
        } catch (Exception e) {
            return "Impossible de parser la reponse : " + e.getMessage();
        }
    }
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"")
                   .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}