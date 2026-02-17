package service.demande;

import entites.Demande;
import service.InterfaceCRUD;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DemandeCRUD implements InterfaceCRUD<Demande> {
    private final Connection conn;

    public DemandeCRUD() {
        conn = MyDB.getInstance().getConn();
    }

    @Override
    public void ajouter(Demande demande) throws SQLException {
        String req = "INSERT INTO demande (categorie, titre, description, priorite, status, date_creation, type_demande) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            setDemandeParameters(pst, demande);
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                demande.setIdDemande(rs.getInt(1));
            }
        }
    }

    @Override
    public void modifier(Demande demande) throws SQLException {
        String req = "UPDATE demande SET categorie=?, titre=?, description=?, priorite=?, status=?, date_creation=?, type_demande=? WHERE id_demande=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            setDemandeParameters(pst, demande);
            pst.setInt(8, demande.getIdDemande());
            pst.executeUpdate();
        }
    }

    @Override
    public void supprimer(int idDemande) throws SQLException {
        String req = "DELETE FROM demande WHERE id_demande=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, idDemande);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Demande> afficher() throws SQLException {
        List<Demande> demandes = new ArrayList<>();
        String req = "SELECT * FROM demande";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Demande d = new Demande();
                d.setIdDemande(rs.getInt("id_demande"));
                d.setCategorie(rs.getString("categorie"));
                d.setTitre(rs.getString("titre"));
                d.setDescription(rs.getString("description"));
                d.setPriorite(rs.getString("priorite"));
                d.setStatus(rs.getString("status"));
                d.setDateCreation(rs.getDate("date_creation"));
                d.setTypeDemande(rs.getString("type_demande"));
                demandes.add(d);
            }
        }
        return demandes;
    }

    private void setDemandeParameters(PreparedStatement pst, Demande demande) throws SQLException {
        pst.setString(1, demande.getCategorie());
        pst.setString(2, demande.getTitre());
        pst.setString(3, demande.getDescription());
        pst.setString(4, demande.getPriorite());
        pst.setString(5, demande.getStatus());
        pst.setDate(6, new java.sql.Date(demande.getDateCreation().getTime()));
        pst.setString(7, demande.getTypeDemande());
    }
}