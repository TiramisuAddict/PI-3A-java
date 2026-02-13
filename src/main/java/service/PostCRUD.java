package service;

import entity.Post;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostCRUD implements InterfaceCRUD<Post> {

    Connection conn;

    public PostCRUD() {
        conn = MyDB.getInstance().getConn();
    }

    @Override
    public void ajouter(Post p) throws SQLException {

        String req = """
            INSERT INTO post 
            (titre, contenu, type_post, date_creation, utilisateur_id, active,
             date_evenement, date_fin_evenement, lieu, capacite_max)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        PreparedStatement pst = conn.prepareStatement(req);

        pst.setString(1, p.getTitre());
        pst.setString(2, p.getContenu());
        pst.setInt(3, p.getTypePost());
        pst.setTimestamp(4, Timestamp.valueOf(p.getDateCreation()));
        pst.setInt(5, p.getUtilisateurId());
        pst.setBoolean(6, p.isActive());

        pst.setDate(7, p.getDateEvenement() != null ? Date.valueOf(p.getDateEvenement()) : null);
        pst.setDate(8, p.getDateFinEvenement() != null ? Date.valueOf(p.getDateFinEvenement()) : null);
        pst.setString(9, p.getLieu());
        pst.setObject(10, p.getCapaciteMax());

        pst.executeUpdate();
        System.out.println("Post ajouté");
    }

    @Override
    public void modifier(Post p) throws SQLException {

        String req = """
            UPDATE post SET
            titre = ?, contenu = ?, type_post = ?, active = ?,
            date_evenement = ?, date_fin_evenement = ?, lieu = ?, capacite_max = ?
            WHERE id_post = ?
        """;

        PreparedStatement pst = conn.prepareStatement(req);

        pst.setString(1, p.getTitre());
        pst.setString(2, p.getContenu());
        pst.setInt(3, p.getTypePost());
        pst.setBoolean(4, p.isActive());

        pst.setDate(5, p.getDateEvenement() != null ? Date.valueOf(p.getDateEvenement()) : null);
        pst.setDate(6, p.getDateFinEvenement() != null ? Date.valueOf(p.getDateFinEvenement()) : null);
        pst.setString(7, p.getLieu());
        pst.setObject(8, p.getCapaciteMax());

        pst.setInt(9, p.getIdPost());

        pst.executeUpdate();
        System.out.println("Post modifié");
    }

    @Override
    public void supprimer(int id) throws SQLException {

        String req = "DELETE FROM post WHERE id_post = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();

        System.out.println("Post supprimé");
    }

    @Override
    public List<Post> afficher() throws SQLException {

        List<Post> posts = new ArrayList<>();
        String req = "SELECT * FROM post";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {

            Post p = new Post();
            p.setIdPost(rs.getInt("id_post"));
            p.setTitre(rs.getString("titre"));
            p.setContenu(rs.getString("contenu"));
            p.setTypePost(rs.getInt("type_post"));
            p.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
            p.setUtilisateurId(rs.getInt("utilisateur_id"));
            p.setActive(rs.getBoolean("active"));

            Date d = rs.getDate("date_evenement");
            if (d != null) p.setDateEvenement(d.toLocalDate());

            Date df = rs.getDate("date_fin_evenement");
            if (df != null) p.setDateFinEvenement(df.toLocalDate());

            p.setLieu(rs.getString("lieu"));
            p.setCapaciteMax((Integer) rs.getObject("capacite_max"));

            posts.add(p);
        }

        return posts;
    }
}
