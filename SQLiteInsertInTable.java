package nl.saxion.ptbc.database;

import java.sql.PreparedStatement;
import java.sql.*;

public class SQLiteInsertInTable {
    public static void main(String[] args) {

        String url = "jdbc:sqlite:sasa.db";

    String sql = "INSERT INTO " +
            "mission_logs(command) " +
            "VALUES (?)";

        Connection conn;

        try {
            conn = DriverManager.getConnection(url);
            Statement statement = conn.createStatement();

            PreparedStatement stm = conn.prepareStatement(sql);
            stm.setString(1, "DRIVE 0 0 0 (test command)");

            int rowsInserted = stm.executeUpdate();
            if (rowsInserted > 0){
                System.out.println("Statement executed");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}

