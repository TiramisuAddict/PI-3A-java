package service;

import entity.EtatOffre;
import entity.Offre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import entity.TypeContrat;
import utils.MyDB;

public class OffreCRUD implements InterfaceCRUD <Offre>{

    Connection conn;
    public OffreCRUD() { conn = MyDB.getInstance().getConn(); }

    public int getIdByCodeOffre(String code) throws SQLException {
        String req = "SELECT id FROM offre WHERE code_offre=?";

        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("id");
        }
        throw new SQLException("Offre with code '" + code + "' not found");
    }

    @Override
    public void ajouter(Offre o) throws SQLException {
        String req = "INSERT INTO offre (code_offre, id_employer, titre_poste, type_contrat, date_limite, etat) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(req);

        ps.setString(1, o.getCodeOffre());
        ps.setInt(2, o.getIdEmployer());
        ps.setString(3, o.getTitrePoste());
        ps.setString(4, o.getTypeContrat().getDisplayName());
        ps.setDate(5, o.getDateLimite());
        ps.setString(6, o.getEtat().getDisplayName());

        ps.executeUpdate();
        System.out.println("Offre ajoutée !");
    }

    @Override
    public void modifier(Offre o) throws SQLException {
        String req="UPDATE offre SET code_offre=?,titre_poste=?,type_contrat=?,date_limite=?,etat=? WHERE id=?";

        PreparedStatement ps = conn.prepareStatement(req);

        ps.setString(1, o.getCodeOffre());
        ps.setString(2, o.getTitrePoste());
        ps.setString(3, o.getTypeContrat().getDisplayName());
        ps.setDate(4, o.getDateLimite());
        ps.setString(5, o.getEtat().getDisplayName());

        ps.setInt(6, o.getId());

        ps.executeUpdate();
        System.out.println("Offre modifiée");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req="DELETE FROM offre WHERE id=?";

        PreparedStatement ps = conn.prepareStatement(req);

        ps.setInt(1, id);

        ps.executeUpdate();
        System.out.println("Offre supprimer");
    }

    @Override
    public List<Offre> afficher() throws SQLException {
        String req = "SELECT * FROM offre";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        List<Offre> listeOffres = new ArrayList<Offre>();

        while (rs.next()) {
            Offre o = new Offre();

            o.setId(rs.getInt("id"));
            o.setCodeOffre(rs.getString("code_offre"));
            o.setIdEmployer(rs.getInt("id_employer"));
            o.setTitrePoste(rs.getString("titre_poste"));
            o.setTypeContrat(TypeContrat.fromDisplayName(rs.getString("type_contrat")));
            o.setDateLimite(rs.getDate("date_limite"));
            o.setEtat(EtatOffre.fromDisplayName(rs.getString("etat")));

            listeOffres.add(o);
        }

        return listeOffres;
    }

    public Offre getById(int idOffre) {
        String req = "SELECT * FROM offre WHERE id=?";

        try {
            PreparedStatement ps = conn.prepareStatement(req);
            ps.setInt(1, idOffre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Offre o = new Offre();

                o.setId(rs.getInt("id"));
                o.setCodeOffre(rs.getString("code_offre"));
                o.setIdEmployer(rs.getInt("id_employer"));
                o.setTitrePoste(rs.getString("titre_poste"));
                o.setTypeContrat(TypeContrat.fromDisplayName(rs.getString("type_contrat")));
                o.setDateLimite(rs.getDate("date_limite"));
                o.setEtat(EtatOffre.fromDisplayName(rs.getString("etat")));

                return o;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
