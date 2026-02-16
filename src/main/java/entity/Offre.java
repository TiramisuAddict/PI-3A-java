package entity;

import java.sql.Date;

public class Offre {
    private Integer id;
    private String codeOffre;
    private Integer idEmployer;
    private String titrePoste;
    private TypeContrat typeContrat;
    private Date dateLimite;
    private EtatOffre etat;

    public Offre() {}

    public Offre (Integer id , String codeOffre, Integer idEmployer, String titrePoste, TypeContrat typeContrat, Date dateLimite, EtatOffre etat) {
        this.id = id;
        this.codeOffre = codeOffre;
        this.idEmployer = idEmployer;
        this.titrePoste = titrePoste;
        this.typeContrat = typeContrat;
        this.dateLimite = dateLimite;
        this.etat = etat;
    }

    public Offre (String codeOffre, Integer idEmployer, String titrePoste, TypeContrat typeContrat, Date dateLimite, EtatOffre etat) {
        this.codeOffre = codeOffre;
        this.idEmployer = idEmployer;
        this.titrePoste = titrePoste;
        this.typeContrat = typeContrat;
        this.dateLimite = dateLimite;
        this.etat = etat;
    }

    public Integer getId() {
        return id;
    }

    public String getCodeOffre() {
        return codeOffre;
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

    public void setId(Integer id) {
        this.id = id;
    }

    public void setCodeOffre(String codeOffre) {
        this.codeOffre = codeOffre;
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

    @Override
    public String toString() {
        return "Offre{" +
//                "id=" + id +
                ", codeOffre='" + codeOffre + '\'' +
                ", idEmployer=" + idEmployer +
                ", titrePoste='" + titrePoste + '\'' +
                ", typeContrat='" + typeContrat.getDisplayName() + '\'' +
                ", dateLimite=" + dateLimite +
                ", etat='" + etat.getDisplayName() + '\'' +
                '}';
    }
}
