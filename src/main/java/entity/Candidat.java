package entity;

import java.sql.Date;
import java.util.Arrays;

public class Candidat {
    private int id;
    private String codeCandidature;
    private String nom;
    private String prenom;
    private String email;
    private String numTel;
    private String cvNom;
    private byte[] cvData;
    private String lettreMotivationNom;
    private byte[] lettreMotivationData;
    private String etat;
    private String note;
    private Date dateCandidature;
    private int idOffre;

    public Candidat() {}

    public Candidat (String codeCandidature, String nom, String prenom, String email, String numTel, String cvNom, byte[] cvData, String lettreMotivationNom, byte[] lettreMotivationData, String etat, String note, Date dateCandidature, int idOffre) {
        this.codeCandidature = codeCandidature;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.numTel = numTel;
        this.cvNom = cvNom;
        this.cvData = cvData;
        this.lettreMotivationNom = lettreMotivationNom;
        this.lettreMotivationData = lettreMotivationData;
        this.etat = etat;
        this.note = note;
        this.dateCandidature = dateCandidature;
        this.idOffre = idOffre;
    }

    public Candidat (String codeCandidature, String nom, String prenom, String email, String numTel, String etat, String note, Date dateCandidature, int idOffre) {
        this.codeCandidature = codeCandidature;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.numTel = numTel;
        this.etat = etat;
        this.note = note;
        this.dateCandidature = dateCandidature;
        this.idOffre = idOffre;
    }

    public int getId() {
        return id;
    }

    public String getCodeCandidature() {
        return codeCandidature;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getNumTel() {
        return numTel;
    }

    public String getCvNom() {
        return cvNom;
    }

    public byte[] getCvData() {
        return cvData;
    }

    public String getLettreMotivationNom() {
        return lettreMotivationNom;
    }

    public byte[] getLettreMotivationData() {
        return lettreMotivationData;
    }

    public String getEtat() {
        return etat;
    }

    public String getNote() {
        return note;
    }

    public Date getDateCandidature() {
        return dateCandidature;
    }

    public int getIdOffre() {
        return idOffre;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCodeCandidature(String codeCandidature) {
        this.codeCandidature = codeCandidature;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public void setCvNom(String cvNom) {
        this.cvNom = cvNom;
    }

    public void setCvData(byte[] cvData) {
        this.cvData = cvData;
    }

    public void setLettreMotivationNom(String lettreMotivationNom) {
        this.lettreMotivationNom = lettreMotivationNom;
    }

    public void setLettreMotivationData(byte[] lettreMotivationData) {
        this.lettreMotivationData = lettreMotivationData;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setDateCandidature(Date dateCandidature) {
        this.dateCandidature = dateCandidature;
    }

    public void setIdOffre(int idOffre) {
        this.idOffre = idOffre;
    }

    @Override
    public String toString() {
        return "Candidat{" +
                "id=" + id +
                ", codeCandidature='" + codeCandidature + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", numTel='" + numTel + '\'' +
                ", cvNom='" + cvNom + '\'' +
                ", cvData=" + Arrays.toString(cvData) +
                ", lettreMotivationNom='" + lettreMotivationNom + '\'' +
                ", lettreMotivationData=" + Arrays.toString(lettreMotivationData) +
                ", etat='" + etat + '\'' +
                ", note='" + note + '\'' +
                ", dateCandidature=" + dateCandidature +
                ", idOffre=" + idOffre +
                '}';
    }
}