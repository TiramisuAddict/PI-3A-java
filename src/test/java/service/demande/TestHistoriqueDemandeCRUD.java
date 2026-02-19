package service.demande;

import entities.demande.HistoriqueDemande;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestHistoriqueDemandeCRUD {
    private final Connection conn;

    public TestHistoriqueDemandeCRUD() {
        try {
            this.conn = TestDBConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get test connection", e);
        }
    }

    public void ajouter(HistoriqueDemande h) throws SQLException {
        String req = "INSERT INTO historique_demande (id_demande, ancien_statut, nouveau_statut, date_action, acteur, commentaire) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, h.getIdDemande());
            pst.setString(2, h.getAncienStatut());
            pst.setString(3, h.getNouveauStatut());
            pst.setTimestamp(4, new Timestamp(h.getDateAction().getTime()));
            pst.setString(5, h.getActeur());
            pst.setString(6, h.getCommentaire());
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                h.setIdHistorique(rs.getInt(1));
            }
        }
    }

    public List<HistoriqueDemande> getByDemande(int idDemande) throws SQLException {
        List<HistoriqueDemande> list = new ArrayList<>();
        String req = "SELECT * FROM historique_demande WHERE id_demande=? ORDER BY date_action DESC";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, idDemande);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                HistoriqueDemande h = new HistoriqueDemande();
                h.setIdHistorique(rs.getInt("id_historique"));
                h.setIdDemande(rs.getInt("id_demande"));
                h.setAncienStatut(rs.getString("ancien_statut"));
                h.setNouveauStatut(rs.getString("nouveau_statut"));
                h.setDateAction(rs.getTimestamp("date_action"));
                h.setActeur(rs.getString("acteur"));
                h.setCommentaire(rs.getString("commentaire"));
                list.add(h);
            }
        }
        return list;
    }

    public void supprimerByDemande(int idDemande) throws SQLException {
        String req = "DELETE FROM historique_demande WHERE id_demande=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, idDemande);
            pst.executeUpdate();
        }
    }
}