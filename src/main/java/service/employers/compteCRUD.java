package service.employers;

import entities.employers.compte;
import utils.MyDB;

import java.sql.*;
import java.util.List;

public class compteCRUD  {

    private Connection conn;

    public compteCRUD() throws SQLException {
        conn = MyDB.getInstance().getConn();
    }

    public void ajouter(compte c) throws SQLException {
        String sql = "INSERT INTO compte(e_mail, mot_de_passe, id_employe) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getE_mail());
            String hashed = hachageMotDePasse.hashPassword(c.getPassword());
            ps.setString(2, hashed);
            ps.setInt(3, c.getId_employe());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<compte> afficher() throws SQLException {
        return List.of();
    }

    public void modifierMotDePasse(int idCompte, String nouveauMotDePasseHashe) throws SQLException {
        String sql = "UPDATE compte SET mot_de_passe = ? WHERE id_compte = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nouveauMotDePasseHashe);
            ps.setInt(2, idCompte);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Aucun compte trouvé avec l'id " + idCompte);
            }
        }
    }
    public void supprimer(int idEmploye) throws SQLException {
        String sql = "DELETE FROM compte WHERE id_employe = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idEmploye);
        ps.executeUpdate();
    }

    public compte findByEmail(String e_mail) throws SQLException {
        String sql1 = "select * from compte where e_mail=?";
        PreparedStatement ps1 = conn.prepareStatement(sql1);
        ps1.setString(1, e_mail);
        ResultSet rs = ps1.executeQuery();
        if (rs.next()) {
            compte c = new compte();
            c.setId(rs.getInt("id_compte"));
            c.setE_mail(rs.getString("e_mail"));
            c.setPassword(rs.getString("mot_de_passe"));
            c.setId_employe(rs.getInt("id_employe"));
            return c;

        }
        return null;


    }
    public compte authentifier(String email, String password) {
        String sql = "SELECT * FROM compte WHERE e_mail = ? AND mot_de_passe = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, hachageMotDePasse.hashPassword(password));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    compte c = new compte();
                    c.setId(rs.getInt("id_compte"));
                    c.setE_mail(rs.getString("e_mail"));
                    c.setPassword(rs.getString("mot_de_passe"));
                    c.setId_employe(rs.getInt("id_employe"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
