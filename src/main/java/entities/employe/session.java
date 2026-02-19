package entities.employe;

public class session {
    private static compte currentCompte;
    private static administrateur_systeme currentAdmin;
    private static employe currentEmploye;

    // Compte
    public static void setCompte(compte c) {
        currentCompte = c;
    }

    public static compte getCompte() {
        return currentCompte;
    }

    // Admin système
    public static void setAdmin(administrateur_systeme admin) {
        currentAdmin = admin;
    }
    public static administrateur_systeme getAdmin() {
        return currentAdmin;
    }
    public static void setEmploye(employe emp) {
        currentEmploye = emp;
    }
    public static employe getEmploye() {
        return currentEmploye;
    }
    public static int getIdEntreprise() {
        if (currentEmploye != null) {
            return currentEmploye.getIdEntreprise();
        }
        return -1;
    }
    public static void logout() {
        currentCompte = null;
        currentAdmin = null;
        currentEmploye = null;
    }
}