package service;

import entity.Participation;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationCRUD implements InterfaceCRUD<Participation> {

    Connection conn;

    public ParticipationCRUD() {
        conn = MyDB.getInstance().getConn();
    }

    @Override
    public void ajouter(Participation p) throws SQLException {
        String sql = "INSERT INTO participation (utilisateur_id, post_id, statut, date_action) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, p.getUtilisateurId());
        pst.setInt(2, p.getpostId());
        pst.setString(3, p.getStatutParticipation());
        pst.setTimestamp(4, Timestamp.valueOf(p.getDateAction()));

        pst.executeUpdate();
    }

    @Override
    public void modifier(Participation p) throws SQLException {
        String sql = "UPDATE participation SET statut=?, date_action=? WHERE id_participation=?";

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, p.getStatutParticipation());
        pst.setTimestamp(2, Timestamp.valueOf(p.getDateAction()));
        pst.setInt(3, p.getIdParticipation());

        pst.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM participation WHERE id_participation=?";

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    @Override
    public List<Participation> afficher() throws SQLException {
        List<Participation> list = new ArrayList<>();
        String sql = "SELECT * FROM participation";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Participation p = new Participation();
            p.setIdParticipation(rs.getInt("id_participation"));
            p.setUtilisateurId(rs.getInt("utilisateur_id"));
            p.setpostId(rs.getInt("post_id"));
            p.setStatutParticipation(rs.getString("statut"));
            p.setDateAction(rs.getTimestamp("date_action").toLocalDateTime());

            list.add(p);
        }
        return list;
    }
}
