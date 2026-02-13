package service;

import entity.Like;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LikeCRUD implements service.InterfaceCRUD<Like> {

    Connection conn;

    public LikeCRUD() {
        conn = MyDB.getInstance().getConn();
    }

    @Override
    public void ajouter(Like l) throws SQLException {
        String sql = "INSERT INTO like_post (utilisateur_id, post_id, date_like) VALUES (?, ?, ?)";

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, l.getUtilisateurId());
        pst.setInt(2, l.getpostId());
        pst.setTimestamp(3, Timestamp.valueOf(l.getDateLike()));

        pst.executeUpdate();
    }

    @Override
    public void modifier(Like l) throws SQLException {
        throw new UnsupportedOperationException("Un like ne se modifie pas");
    }

    @Override
    public void supprimer(int idLike) throws SQLException {
        String sql = "DELETE FROM like_post WHERE id_like=?";

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, idLike);
        pst.executeUpdate();
    }

    @Override
    public List<Like> afficher() throws SQLException {
        List<Like> list = new ArrayList<>();
        String sql = "SELECT * FROM like_post";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Like l = new Like();
            l.setIdLike(rs.getInt("id_like"));
            l.setUtilisateurId(rs.getInt("utilisateur_id"));
            l.setpostId(rs.getInt("post_id"));
            l.setDateLike(rs.getTimestamp("date_like").toLocalDateTime());

            list.add(l);
        }
        return list;
    }
}
