package utils;

/**
 * Singleton class to manage the current user session.
 * Stores the logged-in employee's information and role.
 */
public class UserSession {

    private static UserSession instance;

    private int userId;
    private String nom;
    private String prenom;
    private String role; // "rh", "responsable", "employee"

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUser(int userId, String nom, String prenom, String role) {
        this.userId = userId;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role != null ? role.toLowerCase() : "employee";
    }

    public void clearSession() {
        this.userId = 0;
        this.nom = null;
        this.prenom = null;
        this.role = null;
    }

    public int getUserId() {
        return userId;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getFullName() {
        return (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");
    }

    public String getRole() {
        return role;
    }

    public boolean isLoggedIn() {
        return userId > 0 && role != null;
    }

    /**
     * Check if current user can manage projects (create, update, delete)
     * Only RH and Responsable can fully manage projects
     */
    public boolean canManageProjects() {
        return "rh".equals(role) || "responsable".equals(role);
    }

    /**
     * Check if current user can manage tasks (create, update, delete)
     * Only RH and Responsable can fully manage tasks
     */
    public boolean canManageTasks() {
        return "rh".equals(role) || "responsable".equals(role);
    }

    /**
     * Check if current user is a regular employee
     */
    public boolean isEmployee() {
        return "employee".equals(role);
    }
}

