package models.employe;

import java.time.LocalDate;

public class employe {
    private int id_employé;
    private String nom;
    private String prenom;
    private String e_mail;
    private int telephone;
    private String poste;
    private String role;
    private LocalDate date_embauche;
    private int idEntreprise;

    public employe(){}

    public employe(String nom, String prenom, String e_mail, int telephone, String poste, String role, LocalDate date_embauche) {
        this.nom = nom;
        this.prenom = prenom;
        this.e_mail = e_mail;
        this.telephone = telephone;
        this.poste = poste;
        this.role = role;
        this.date_embauche = date_embauche;
    }

    public int getId_employé() {
        return id_employé;
    }

    public void setId_employé(int id_employé) {
        this.id_employé = id_employé;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getE_mail() {
        return e_mail;
    }

    public void setE_mail(String e_mail) {
        this.e_mail = e_mail;
    }

    public int getTelephone() {
        return telephone;
    }

    public void setTelephone(int telephone) {
        this.telephone = telephone;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getDate_embauche() {
        return date_embauche;
    }

    public void setDate_embauche(LocalDate date_embauche) {
        this.date_embauche = date_embauche;
    }

    public int getIdEntreprise() {
        return idEntreprise;
    }

    public void setIdEntreprise(int idEntreprise) {
        this.idEntreprise = idEntreprise;
    }
}
