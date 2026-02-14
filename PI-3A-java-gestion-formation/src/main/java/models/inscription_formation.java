package models;

public class inscription_formation {

    private int id_inscription;
    private int id_formation;
    private int id_user;
    private StatutInscription statut;

    public inscription_formation() {
    }

    public inscription_formation(int id_formation, int id_user, StatutInscription statut) {
        this.id_formation = id_formation;
        this.id_user = id_user;
        this.statut = statut;
    }

    public inscription_formation(int id_inscription, int id_formation, int id_user, StatutInscription statut) {
        this.id_inscription = id_inscription;
        this.id_formation = id_formation;
        this.id_user = id_user;
        this.statut = statut;
    }

    // ===== Getters & Setters =====

    public int getId_inscription() {
        return id_inscription;
    }

    public void setId_inscription(int id_inscription) {
        this.id_inscription = id_inscription;
    }

    public int getId_formation() {
        return id_formation;
    }

    public void setId_formation(int id_formation) {
        this.id_formation = id_formation;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public StatutInscription getStatut() {
        return statut;
    }

    public void setStatut(StatutInscription statut) {
        this.statut = statut;
    }
}
