package service.employers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entities.employers.competences_employe;
import entities.employers.compte;
import entities.employers.employe;
import entities.employers.role;
import service.employers.serviceEmail;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class employeCRUD {
    private Connection conn;
    private compteCRUD compteCRUD;
    private competence_employeCRUD competenceCRUD;

    public employeCRUD() {
        try {
            conn = MyDB.getInstance().getConn();
            compteCRUD = new compteCRUD();
            competenceCRUD = new competence_employeCRUD();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public int add(employe employe) throws SQLException {
        String sql = "insert into employé(nom, prenom, e_mail, telephone, poste, role, date_embauche,image_profil, id_entreprise, cv_data, cv_nom, id_candidat) values(?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?,?)";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, employe.getNom());
        ps.setString(2, employe.getPrenom());
        ps.setString(3, employe.getE_mail());
        ps.setInt(4, employe.getTelephone());
        ps.setString(5, employe.getPoste());
        ps.setString(6, employe.getRole().getLibelle());
        if (employe.getDate_embauche() != null) {
            ps.setDate(7, Date.valueOf(employe.getDate_embauche()));
        } else {
            ps.setNull(7, Types.DATE);
        }
        ps.setString(8, employe.DEFAULT_IMAGE);
        ps.setInt(9, employe.getIdEntreprise());
        ps.setBytes(10, employe.getCv_data());
        ps.setString(11, employe.getCv_nom());
        if (employe.getIdCandidat() != null) {
            ps.setInt(12, employe.getIdCandidat());
        } else {
            ps.setNull(12, Types.INTEGER);
        }
        ps.executeUpdate();
        if (employe.hasCv() && employe.getCv_data() != null) {
            extraireCompetencesCV(employe);
        }
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

        serviceEmail.envoyer(employe.getE_mail(), sujet, corps);
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
        String sql = "update employé set nom=?, prenom=?,e_mail=?, telephone=?, poste=?, role=?, date_embauche=?,image_profil=?, cv_data=?, cv_nom=?, id_candidat=?  where id_employe=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, employe.getNom());
        ps.setString(2, employe.getPrenom());
        ps.setString(3, employe.getE_mail());
        ps.setInt(4, employe.getTelephone());
        ps.setString(5, employe.getPoste());
        ps.setString(6, employe.getRole().getLibelle());
        if (employe.getDate_embauche() != null) {
            ps.setDate(7, Date.valueOf(employe.getDate_embauche()));
        } else {
            ps.setNull(7, Types.DATE);
        }
        if (employe.getImageProfil() != null && !employe.getImageProfil().isBlank()) {
            ps.setString(8, employe.getImageProfil());
        } else {
            ps.setNull(8, Types.VARCHAR);
        }
        ps.setBytes(9, employe.getCv_data());
        ps.setString(10, employe.getCv_nom());
        if (employe.getIdCandidat() != null) {
            ps.setInt(11, employe.getIdCandidat());
        } else {
            ps.setNull(11, Types.INTEGER);
        }
        ps.setInt(12, employe.getId_employé());
        ps.executeUpdate();
        if (employe.hasCv() && employe.getCv_data() != null) {
            extraireCompetencesCV(employe);
        }
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
        e.setRole(role.fromString(rs.getString("role")));

        Date dateEmbauche = rs.getDate("date_embauche");
        if (dateEmbauche != null) {
            e.setDate_embauche(dateEmbauche.toLocalDate());
        }
        e.setImageProfil(rs.getString("image_profil"));
        e.setIdEntreprise(rs.getInt("id_entreprise"));
        e.setCv_data(rs.getBytes("cv_data"));
        e.setCv_nom(rs.getString("cv_nom"));
        int idCand = rs.getInt("id_candidat");
        if (!rs.wasNull()) {
            e.setIdCandidat(idCand);
        } else {
            e.setIdCandidat(null);
        }

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

    private void extraireCompetencesCV(employe e) {
        competence_employeCRUD crud = this.competenceCRUD;

        Thread thread = new Thread(() -> {
            try {
                String jsonResult = extract_CV_data.extraireDepuisCV(e.getCv_data());
                jsonResult = reparerJSON(jsonResult);
                Gson gson = new Gson();
                JsonObject root = gson.fromJson(jsonResult, JsonObject.class);

                String skills = root.has("skills") ? gson.toJson(root.get("skills")) : "[]";
                String formations = root.has("formations") ? gson.toJson(root.get("formations")) : "[]";
                String experience = root.has("experience") ? gson.toJson(root.get("experience")) : "[]";

                competences_employe comp = new competences_employe(e.getId_employé(), skills, formations, experience);

                crud.ajouter(comp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String reparerJSON(String json) {
        if (json == null || json.isEmpty()) return "{}";

        int accolades = 0;
        int crochets = 0;
        boolean dansString = false;
        boolean escape = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escape) {
                escape = false;
                continue;
            }

            if (c == '\\') {
                escape = true;
                continue;
            }

            if (c == '"') {
                dansString = !dansString;
                continue;
            }

            if (!dansString) {
                if (c == '{') accolades++;
                else if (c == '}') accolades--;
                else if (c == '[') crochets++;
                else if (c == ']') crochets--;
            }
        }

        StringBuilder sb = new StringBuilder(json);
        if (dansString) {
            sb.append('"');
        }
        String temp = sb.toString().trim();
        while (temp.endsWith(",") || temp.endsWith(":")) {
            temp = temp.substring(0, temp.length() - 1).trim();
        }
        if (temp.matches(".*,\\s*\"[^\"]*\"\\s*$")) {
            temp = temp.substring(0, temp.lastIndexOf(",")).trim();
        }

        sb = new StringBuilder(temp);
        accolades = 0;
        crochets = 0;
        dansString = false;
        escape = false;

        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"') {
                dansString = !dansString;
                continue;
            }
            if (!dansString) {
                if (c == '{') accolades++;
                else if (c == '}') accolades--;
                else if (c == '[') crochets++;
                else if (c == ']') crochets--;
            }
        }
        for (int i = 0; i < crochets; i++) {
            sb.append(']');
        }
        for (int i = 0; i < accolades; i++) {
            sb.append('}');
        }

        return sb.toString();
    }

    public record EmployeeInfo(int id, String nom, String prenom, String role) {
        public String getFullName() {
            return nom + " " + prenom;
        }

        @Override
        public String toString() {
            return nom + " " + prenom;
        }
    }
    public EmployeeInfo getEmployeeInfoByEmail(String email) throws SQLException {
        String sql = "SELECT id_employe, nom, prenom, role FROM employé WHERE e_mail = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new EmployeeInfo(
                            rs.getInt("id_employe"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("role")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Get all employees
     */
    public List<EmployeeInfo> getAllEmployees() throws SQLException {
        String sql = "SELECT id_employe, nom, prenom, role FROM employé";
        List<EmployeeInfo> employees = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                employees.add(new EmployeeInfo(
                        rs.getInt("id_employe"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("role")
                ));
            }
        }
        return employees;
    }

    /**
     * Get employees by role
     */
    public List<EmployeeInfo> getEmployeesByRole(String role) throws SQLException {
        String sql = "SELECT id_employe, nom, prenom, role FROM employé WHERE role = ?";
        List<EmployeeInfo> employees = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    employees.add(new EmployeeInfo(
                            rs.getInt("id_employe"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("role")
                    ));
                }
            }
        }
        return employees;
    }

    /**
     * Get employees with chef projet role (responsables)
     */
    public List<EmployeeInfo> getResponsables() throws SQLException {
        return getEmployeesByRole("chef projet");
    }
}