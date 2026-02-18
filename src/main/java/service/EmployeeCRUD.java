package service;

import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeCRUD {
    private final Connection cnx = MyDB.getInstance().getConn();


    public record EmployeeInfo(int id, String nom, String prenom, String role) {
        public String getFullName() {
            return nom + " " + prenom;
        }

        @Override
        public String toString() {
            return nom + " " + prenom;
        }
    }

    public List<EmployeeInfo> getAllEmployees() throws SQLException {
        String sql = "SELECT id_emp, nom, prenom, role FROM employee ORDER BY nom, prenom";
        List<EmployeeInfo> list = new ArrayList<>();

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new EmployeeInfo(
                        rs.getInt("id_emp"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("role")
                ));
            }
        }
        return list;
    }


    public EmployeeInfo getEmployeeById(int id) throws SQLException {
        String sql = "SELECT id_emp, nom, prenom, role FROM employee WHERE id_emp = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new EmployeeInfo(
                            rs.getInt("id_emp"),
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
     * Authenticate employee by ID and retrieve their info including role
     * @param employeeId The employee ID to login with
     * @return EmployeeInfo if found, null otherwise
     */
    public EmployeeInfo authenticateById(int employeeId) throws SQLException {
        return getEmployeeById(employeeId);
    }
}
