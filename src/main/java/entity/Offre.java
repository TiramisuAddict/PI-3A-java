package entity;

import java.sql.Date;

public class Offre {
    private Integer id;
    private Integer idEmployer;
    private String titrePoste;
    private TypeContrat typeContrat;
    private Date dateLimite;
    private EtatOffre etat;
    private String description;

    public Offre() {}

    public Offre (Integer id, Integer idEmployer, String titrePoste, TypeContrat typeContrat, Date dateLimite, EtatOffre etat, String description) {
        this.id = id;
        this.idEmployer = idEmployer;
        this.titrePoste = titrePoste;
        this.typeContrat = typeContrat;
        this.dateLimite = dateLimite;
        this.etat = etat;
        this.description = description;
    }

    public Offre (Integer idEmployer, String titrePoste, TypeContrat typeContrat, Date dateLimite, EtatOffre etat, String description) {
        this.idEmployer = idEmployer;
        this.titrePoste = titrePoste;
        this.typeContrat = typeContrat;
        this.dateLimite = dateLimite;
        this.etat = etat;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public Integer getIdEmployer() {
        return idEmployer;
    }

    public String getTitrePoste() {
        return titrePoste;
    }

    public TypeContrat getTypeContrat() {
        return typeContrat;
    }

    public Date getDateLimite() {
        return dateLimite;
    }

    public EtatOffre getEtat() {
        return etat;
    }

    public String getDescription() {return description;}

    public void setId(Integer id) {
        this.id = id;
    }

    public void setIdEmployer(Integer idEmployer) {
        this.idEmployer = idEmployer;
    }

    public void setTitrePoste(String titrePoste) {
        this.titrePoste = titrePoste;
    }

    public void setTypeContrat(TypeContrat typeContrat) {
        this.typeContrat = typeContrat;
    }

    public void setDateLimite(Date dateLimite) {
        this.dateLimite = dateLimite;
    }

    public void setEtat(EtatOffre etat) {
        this.etat = etat;
    }

    public void setDescription(String description) {this.description = description;}

    @Override
    public String toString() {
        return "Offre{" +
                ", idEmployer=" + idEmployer +
                ", titrePoste='" + titrePoste + '\'' +
                ", typeContrat='" + typeContrat.getDisplayName() + '\'' +
                ", dateLimite=" + dateLimite +
                ", etat='" + etat.getDisplayName() + '\'' +
                '}';
    }
}
