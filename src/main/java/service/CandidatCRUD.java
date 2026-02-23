package service;

import entity.Candidat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import utils.MyDB;

public class CandidatCRUD implements InterfaceCRUD<Candidat> {

    Connection conn;
    public CandidatCRUD() {conn = MyDB.getInstance().getConn();}

    public void ajouter(Candidat c) throws SQLException {

        String req = "INSERT INTO candidat (" +
                "code_candidature, nom, prenom, email, num_tel, " +
                "cv_nom, cv_data, lettre_motivation_nom, lettre_motivation_data, " +
                "etat, note, date_candidature, id_offre) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(req);

        ps.setString(1, c.getCodeCandidature());
        ps.setString(2, c.getNom());
        ps.setString(3, c.getPrenom());
        ps.setString(4, c.getEmail());
        ps.setString(5, c.getNumTel());

        ps.setString(6, c.getCvNom());
        ps.setBytes(7, c.getCvData());

        ps.setString(8, c.getLettreMotivationNom());
        ps.setBytes(9, c.getLettreMotivationData());

        ps.setString(10, c.getEtat());
        ps.setString(11, c.getNote());
        ps.setDate(12, c.getDateCandidature());
        ps.setInt(13, c.getIdOffre());

        ps.executeUpdate();
        System.out.println("Candidat ajouté avec fichiers !");
    }


    @Override
    public void modifier(Candidat c) throws SQLException {
        String req = "UPDATE Candidat SET etat = ?, note = ? WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, c.getEtat());
        ps.setString(2, c.getNote());
        ps.setInt(3, c.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req="DELETE FROM personne WHERE id=?";

        PreparedStatement ps = conn.prepareStatement(req);

        ps.setInt(1, id);

        ps.executeUpdate();
        System.out.println("Candidat supprimer");
    }

    @Override
    public List<Candidat> afficher() throws SQLException {
        String req = "SELECT * FROM Candidat";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        List<Candidat> listeCandidats = new ArrayList<Candidat>();

        while (rs.next()) {
            Candidat c = new Candidat();
            c.setId(rs.getInt("id"));
            c.setCodeCandidature(rs.getString("code_candidature"));
            c.setNom(rs.getString("nom"));
            c.setPrenom(rs.getString("prenom"));
            c.setEmail(rs.getString("email"));
            c.setNumTel(rs.getString("num_tel"));
            c.setEtat(rs.getString("etat"));
            c.setNote(rs.getString("note"));
            c.setDateCandidature(rs.getDate("date_candidature"));
            c.setIdOffre(rs.getInt("id_offre"));

            c.setCvNom(rs.getString("cv_nom"));
            c.setLettreMotivationNom(rs.getString("lettre_motivation_nom"));

            c.setCvData(rs.getBytes("cv_data"));
            c.setLettreMotivationData(rs.getBytes("lettre_motivation_data"));

            listeCandidats.add(c);
        }
        return listeCandidats;
    }
}
