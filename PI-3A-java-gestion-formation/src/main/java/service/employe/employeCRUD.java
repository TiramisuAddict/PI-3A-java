package service.employe;

import models.employe.employe;
import models.employe.compte;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class employeCRUD {
    private Connection conn;
    private compteCRUD compteCRUD;

    public employeCRUD() {
        try {
            conn = MyDB.getInstance().getConn();
            compteCRUD = new compteCRUD();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public int add(employe employe) throws SQLException {
        String sql = "insert into employé(nom, prenom, e_mail, telephone, poste, role, date_embauche, id_entreprise) values(?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, employe.getNom());
        ps.setString(2, employe.getPrenom());
        ps.setString(3, employe.getE_mail());
        ps.setInt(4, employe.getTelephone());
        ps.setString(5, employe.getPoste());
        ps.setString(6, employe.getRole());
        if (employe.getDate_embauche() != null) {
            ps.setDate(7, Date.valueOf(employe.getDate_embauche()));
        } else {
            ps.setNull(7, Types.DATE);
        }
        ps.setInt(8, employe.getIdEntreprise());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        int idEmploye = rs.getInt(1);
        employe.setId_employé(idEmploye);
        String motDePasse = generationMotDePasse.generer();
        compte c = new compte(employe.getE_mail(), motDePasse, idEmploye);
        compteCRUD.ajouter(c);
        String sujet = "Création de votre compte employé";
        String corps = "Bonjour " + employe.getPrenom() + ",\n\n"
                + "Un compte a été créé pour vous.\n"
                + "Email : " + employe.getE_mail() + "\n"
                + "Mot de passe : " + motDePasse + "\n\n"
                + "Bonne journée.";

        try {
            serviceEmail.envoyer(employe.getE_mail(), sujet, corps);
        } catch (Exception e) {
            System.err.println("⚠️ Avertissement : Impossible d'envoyer l'email, mais l'employé a été créé avec succès : " + e.getMessage());
        }
        return idEmploye;
    }

    public List<employe> afficher(int idEntreprise) throws SQLException {
        String sql = "select * from employé where id_entreprise = ?";
        List<employe> employes = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idEntreprise);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            employe e = creerEmployeDepuisResultSet(rs);
            employes.add(e);
        }
        return employes;
    }
    public void modifier(employe employe) throws SQLException {
        String sql = "update employé set nom=?, prenom=?,e_mail=?, telephone=?, poste=?, role=?, date_embauche=? where id_employe=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, employe.getNom());
        ps.setString(2, employe.getPrenom());
        ps.setString(3, employe.getE_mail());
        ps.setInt(4, employe.getTelephone());
        ps.setString(5, employe.getPoste());
        ps.setString(6, employe.getRole());
        if (employe.getDate_embauche() != null) {
            ps.setDate(7, Date.valueOf(employe.getDate_embauche()));
        } else {
            ps.setNull(7, Types.DATE);
        }
        ps.setInt(8, employe.getId_employé());
        ps.executeUpdate();
    }
    public void supprimer(int id) throws SQLException {
        try {
            conn.setAutoCommit(false);
            compteCRUD.supprimer(id);
            String sql = "delete from employé where id_employe = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    private employe creerEmployeDepuisResultSet(ResultSet rs) throws SQLException {
        employe e = new employe();
        e.setId_employé(rs.getInt("id_employe"));
        e.setNom(rs.getString("nom"));
        e.setPrenom(rs.getString("prenom"));
        e.setE_mail(rs.getString("e_mail"));
        e.setTelephone(rs.getInt("telephone"));
        e.setPoste(rs.getString("poste"));
        e.setRole(rs.getString("role"));

        Date dateEmbauche = rs.getDate("date_embauche");
        if (dateEmbauche != null) {
            e.setDate_embauche(dateEmbauche.toLocalDate());
        }

        e.setIdEntreprise(rs.getInt("id_entreprise"));

        return e;
    }
    public employe getById(int id) throws SQLException {
        String sql = "select * FROM employé where id_employe = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return creerEmployeDepuisResultSet(rs);
        }
        return null;
    }
}