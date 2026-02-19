package service.demande;

import entities.demande.Demande;
import service.InterfaceCRUD;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    // Add these methods at the end of your existing DemandeCRUD.java class

    public int countAll() throws SQLException {
        String req = "SELECT COUNT(*) FROM demande";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countByStatus(String status) throws SQLException {
        String req = "SELECT COUNT(*) FROM demande WHERE status=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, status);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countByPriorite(String priorite) throws SQLException {
        String req = "SELECT COUNT(*) FROM demande WHERE priorite=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, priorite);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countByType(String typeDemande) throws SQLException {
        String req = "SELECT COUNT(*) FROM demande WHERE type_demande=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, typeDemande);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countByCategorie(String categorie) throws SQLException {
        String req = "SELECT COUNT(*) FROM demande WHERE categorie=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, categorie);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public Map<String, Integer> countGroupByStatus() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String req = "SELECT status, COUNT(*) as total FROM demande GROUP BY status";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("total"));
            }
        }
        return map;
    }

    public Map<String, Integer> countGroupByPriorite() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String req = "SELECT priorite, COUNT(*) as total FROM demande GROUP BY priorite";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                map.put(rs.getString("priorite"), rs.getInt("total"));
            }
        }
        return map;
    }

    public Map<String, Integer> countGroupByType() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String req = "SELECT type_demande, COUNT(*) as total FROM demande GROUP BY type_demande";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                map.put(rs.getString("type_demande"), rs.getInt("total"));
            }
        }
        return map;
    }

    public Map<String, Integer> countGroupByCategorie() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String req = "SELECT categorie, COUNT(*) as total FROM demande GROUP BY categorie";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                map.put(rs.getString("categorie"), rs.getInt("total"));
            }
        }
        return map;
    }}