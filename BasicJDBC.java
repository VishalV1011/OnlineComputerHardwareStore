import java.sql.*;

public class BasicJDBC {
    private String driver = "com.mysql.cj.jdbc.Driver";
    private String dbName = "ochs";
    private String connectionURL = "jdbc:mysql://localhost:3306/";
    private String username = "root";
    private String password = "";
    private Connection conn;

    public BasicJDBC() throws Exception {
        Class.forName(driver);
        conn = DriverManager.getConnection(connectionURL + dbName, username, password);
    }

    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    public int executeUpdate(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        return stmt.executeUpdate(query);
    }

    public PreparedStatement prepareStatement(String query) throws SQLException {
        return conn.prepareStatement(query);
    }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }
}
