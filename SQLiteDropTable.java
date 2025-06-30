package nl.saxion.ptbc.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDropTable {
    public static void main(String[] args){
        String url = "jdbc:sqlite:sasa.db";

        String sql = "DROP TABLE IF EXISTS mission_logs";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();

            stmt.execute(sql);
            System.out.println("Table dropped");
            

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
