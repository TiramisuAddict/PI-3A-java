package service;

import entities.compte;
import entities.entreprise;
import entities.role;
import entities.statut;
import utils.MyDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class entrepriseCRUD implements InterfaceCRUD<entreprise> {
    private compteCRUD compteCRUD;
    private Connection conn;
    public entrepriseCRUD() throws SQLException {
        compteCRUD = new compteCRUD();
        conn= MyDB.getInstance().getConn();
    }

    @Override
    public void ajouter(entreprise entreprise) throws SQLException {
        String sql = "insert into entreprise(nom_entreprise, pays, ville, nom, prenom, matricule_fiscale, telephone, e_mail, statut, date_demande) values (?, ?, ?, ?, ?, ?, ?, ?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, entreprise.getNom_entreprise());
        ps.setString(2, entreprise.getPays());
        ps.setString(3, entreprise.getVille());
        ps.setString(4, entreprise.getNom());
        ps.setString(5, entreprise.getPrenom());
        ps.setString(6, entreprise.getMatricule_fiscale());
        ps.setString(7, String.valueOf(entreprise.getTelephone()));
        ps.setString(8, entreprise.getE_mail());
        ps.setString(9, entreprise.getStatut().getLibelle());
        LocalDate dateDemande = entreprise.getDate_demande() != null
                ? entreprise.getDate_demande()
                : LocalDate.now();
        ps.setDate(10, Date.valueOf(dateDemande));
        ps.executeUpdate();
    }

    @Override
    public List<entreprise> afficher() throws SQLException {
        String sql = "select * from entreprise";
        Statement statement=conn.createStatement();
        ResultSet resultSet=statement.executeQuery(sql);
        List<entreprise> entreprises=new ArrayList<entreprise>();
        while(resultSet.next()){
            entreprise e=new entreprise();
            e.setId(resultSet.getInt("id_entreprise"));
            e.setNom_entreprise(resultSet.getString("nom_entreprise"));
            e.setPays(resultSet.getString("pays"));
            e.setVille(resultSet.getString("ville"));
            e.setNom(resultSet.getString("nom"));
            e.setPrenom(resultSet.getString("prenom"));
            e.setMatricule_fiscale(resultSet.getString("matricule_fiscale"));
            e.setTelephone(resultSet.getInt("telephone"));
            e.setE_mail(resultSet.getString("e_mail"));
            String statutString = resultSet.getString("statut");
            e.setStatut(statut.fromString(statutString));
            e.setStatut(statut.fromString(statutString));
            e.setDate_demande(resultSet.getDate("date_demande").toLocalDate());

            entreprises.add(e);
        }
        return entreprises;
    }

    @Override
    public void modifier(entreprise entreprise) throws SQLException {
    String sql="update entreprise set nom_entreprise=? ,pays=? ,ville=?,nom=?,prenom=?,matricule_fiscale=? ,telephone=?,e_mail=?,statut=? where id_entreprise=?";
    PreparedStatement statement=conn.prepareStatement(sql);
    statement.setString(1,entreprise.getNom());
    statement.setString(2,entreprise.getPays());
    statement.setString(3,entreprise.getVille());
    statement.setString(4,entreprise.getMatricule_fiscale());
    statement.setInt(5,entreprise.getTelephone());
    statement.setString(6,entreprise.getE_mail());
    statement.setString(9, entreprise.getStatut().getLibelle());
    statement.setInt(8,entreprise.getId());
    statement.executeUpdate();

    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql1 = "delete from compte where id_employe in (select id_employe from employé where id_entreprise=?)";
        String sql2 = "delete from employé where id_entreprise=?";
        String sql3 = "delete from entreprise where id_entreprise=?";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sql3)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            System.err.println(e.getMessage());
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    public void changerStatut(int id, statut nouveauStatut) throws SQLException {
        String sql = "UPDATE entreprise SET statut=? WHERE id_entreprise=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nouveauStatut.getLibelle());
            ps.setInt(2, id);
            ps.executeUpdate();

        }

    }
    public void accepterEntreprise(entreprise e) throws SQLException {
        String sqlStatut = "update entreprise set statut=? where id_entreprise=?";
        String sqlEmploye = "insert into employé(nom, prenom, e_mail, telephone, poste, role, date_embauche, id_entreprise) values (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlStatut)) {
                ps1.setString(1, statut.acceptee.getLibelle());
                ps1.setInt(2, e.getId());
                ps1.executeUpdate();
            }
            int idEmploye;
            try (PreparedStatement ps2 = conn.prepareStatement(sqlEmploye, Statement.RETURN_GENERATED_KEYS)) {
                ps2.setString(1, e.getNom());
                ps2.setString(2, e.getPrenom());
                ps2.setString(3, e.getE_mail());
                ps2.setInt(4, e.getTelephone());
                ps2.setString(5, "CEO");
                ps2.setString(6, role.ADMINISTRATEUR_ENTREPRISE.getLibelle());
                ps2.setNull(7, Types.DATE);
                ps2.setInt(8, e.getId());
                ps2.executeUpdate();
                try (ResultSet rs = ps2.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("Impossible de récupérer l'ID de l'employé admin créé");
                    }
                    idEmploye = rs.getInt(1);
                }
            }
            String motDePasse = generationMotDePasse.generer();
            compte c = new compte(e.getE_mail(), motDePasse, idEmploye);
            compteCRUD.ajouter(c);
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    }

