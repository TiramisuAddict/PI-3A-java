package service.demande;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDBConnection {
    private static final String URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static Connection conn;

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            createTables();
        }
        return conn;
    }

    private static void createTables() throws SQLException {
        try (Statement st = conn.createStatement()) {
            // Create demande table
            st.execute(
                    "CREATE TABLE IF NOT EXISTS demande (" +
                            "   id_demande INT AUTO_INCREMENT PRIMARY KEY," +
                            "   categorie VARCHAR(50) NOT NULL," +
                            "   titre VARCHAR(50) NOT NULL," +
                            "   description VARCHAR(50) NOT NULL," +
                            "   priorite VARCHAR(20) NOT NULL," +
                            "   status VARCHAR(50) NOT NULL," +
                            "   date_creation DATE NOT NULL," +
                            "   type_demande VARCHAR(50) NOT NULL" +
                            ")"
            );

            // Create demande_details table
            st.execute(
                    "CREATE TABLE IF NOT EXISTS demande_details (" +
                            "   id_details INT AUTO_INCREMENT PRIMARY KEY," +
                            "   id_demande INT NOT NULL," +
                            "   details CLOB NOT NULL," +
                            "   FOREIGN KEY (id_demande) REFERENCES demande(id_demande)" +
                            ")"
            );

            // Create historique_demande table
            st.execute(
                    "CREATE TABLE IF NOT EXISTS historique_demande (" +
                            "   id_historique INT AUTO_INCREMENT PRIMARY KEY," +
                            "   id_demande INT NOT NULL," +
                            "   ancien_statut VARCHAR(50) NOT NULL," +
                            "   nouveau_statut VARCHAR(50) NOT NULL," +
                            "   date_action TIMESTAMP NOT NULL," +
                            "   acteur VARCHAR(20) NOT NULL," +
                            "   commentaire TEXT NOT NULL," +
                            "   FOREIGN KEY (id_demande) REFERENCES demande(id_demande)" +
                            ")"
            );
        }
    }

    public static void clearTables() throws SQLException {
        try (Statement st = getConnection().createStatement()) {
            st.execute("DELETE FROM historique_demande");
            st.execute("DELETE FROM demande_details");
            st.execute("DELETE FROM demande");
        }
    }
}