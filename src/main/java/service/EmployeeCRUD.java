package service;

import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeCRUD {
    private final Connection cnx = MyDB.getInstance().getConn();

    /**
     * Simple record to hold employee info for display in UI
     */
    public record EmployeeInfo(int id, String nom, String prenom) {
        public String getFullName() {
            return nom + " " + prenom;
        }

        @Override
        public String toString() {
            return nom + " " + prenom;
        }
    }

    /**
     * Get all employees from the database
     */
    public List<EmployeeInfo> getAllEmployees() throws SQLException {
        String sql = "SELECT id_emp, nom, prenom FROM employee ORDER BY nom, prenom";
        List<EmployeeInfo> list = new ArrayList<>();

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new EmployeeInfo(
                        rs.getInt("id_emp"),
                        rs.getString("nom"),
                        rs.getString("prenom")
                ));
            }
        }
        return list;
    }

    /**
     * Get employee by ID
     */
    public EmployeeInfo getEmployeeById(int id) throws SQLException {
        String sql = "SELECT id_emp, nom, prenom FROM employee WHERE id_emp = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new EmployeeInfo(
                            rs.getInt("id_emp"),
                            rs.getString("nom"),
                            rs.getString("prenom")
                    );
                }
            }
        }
        return null;
    }
}

