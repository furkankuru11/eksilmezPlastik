package eksilmezPlastik;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/eksilmezplastik";
    private static final String USER = "root";
    private static final String PASSWORD = "Fk7153004."; // MySQL şifrenizi burada belirtin

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }


}
