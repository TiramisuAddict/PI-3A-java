package service;

import entity.Commentaire;
import utils.MyDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentaireCRUD implements service.InterfaceCRUD<Commentaire> {

    Connection conn;

    public CommentaireCRUD() {
        conn = MyDB.getInstance().getConn();
    }

    @Override
    public void ajouter(Commentaire c) throws SQLException {
        String sql = "INSERT INTO commentaire (contenu, date_commentaire, utilisateur_id, post_id) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, c.getContenu());
        pst.setTimestamp(2, Timestamp.valueOf(c.getDateCommentaire()));
        pst.setInt(3, c.getUtilisateurId());
        pst.setInt(4, c.getPostId());

        pst.executeUpdate();
    }

    @Override
    public void modifier(Commentaire c) throws SQLException {
        String sql = "UPDATE commentaire SET contenu=? WHERE id_commentaire=?";

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, c.getContenu());
        pst.setInt(2, c.getIdCommentaire());

        pst.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM commentaire WHERE id_commentaire=?";

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    @Override
    public List<Commentaire> afficher() throws SQLException {
        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Commentaire c = new Commentaire();
            c.setIdCommentaire(rs.getInt("id_commentaire"));
            c.setContenu(rs.getString("contenu"));
            c.setDateCommentaire(rs.getTimestamp("date_commentaire").toLocalDateTime());
            c.setUtilisateurId(rs.getInt("utilisateur_id"));
            c.setPostId(rs.getInt("post_id"));

            list.add(c);
        }
        return list;
    }
}
