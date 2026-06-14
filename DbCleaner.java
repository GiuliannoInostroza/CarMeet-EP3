import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbCleaner {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=root&password=");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DROP DATABASE IF EXISTS db_analytics");
            System.out.println("Database dropped successfully.");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
