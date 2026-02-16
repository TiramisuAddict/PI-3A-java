package entites;

import java.util.Date;

public class HistoriqueDemande {
    private int idHistorique;
    private int idDemande;
    private String ancienStatut;
    private String nouveauStatut;
    private Date dateAction;
    private String acteur;
    private String commentaire;

    public HistoriqueDemande() {}

    public int getIdHistorique() { return idHistorique; }
    public void setIdHistorique(int idHistorique) { this.idHistorique = idHistorique; }

    public int getIdDemande() { return idDemande; }
    public void setIdDemande(int idDemande) { this.idDemande = idDemande; }

    public String getAncienStatut() { return ancienStatut; }
    public void setAncienStatut(String ancienStatut) { this.ancienStatut = ancienStatut; }

    public String getNouveauStatut() { return nouveauStatut; }
    public void setNouveauStatut(String nouveauStatut) { this.nouveauStatut = nouveauStatut; }

    public Date getDateAction() { return dateAction; }
    public void setDateAction(Date dateAction) { this.dateAction = dateAction; }

    public String getActeur() { return acteur; }
    public void setActeur(String acteur) { this.acteur = acteur; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}