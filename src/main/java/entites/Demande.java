package entites;

import java.util.Date;

public class Demande {
    private int idDemande;
    private String categorie;
    private String titre;
    private String description;
    private String priorite;
    private String status;
    private Date dateCreation;
    private String typeDemande;
    private String details;

    public Demande() {}

    public Demande(int idDemande, String categorie, String titre, String description,
                   String priorite, String status, Date dateCreation, String typeDemande) {
        this.idDemande = idDemande;
        this.categorie = categorie;
        this.titre = titre;
        this.description = description;
        this.priorite = priorite;
        this.status = status;
        this.dateCreation = dateCreation;
        this.typeDemande = typeDemande;
    }

    public int getIdDemande() { return idDemande; }
    public void setIdDemande(int idDemande) { this.idDemande = idDemande; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

    public String getTypeDemande() { return typeDemande; }
    public void setTypeDemande(String typeDemande) { this.typeDemande = typeDemande; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}