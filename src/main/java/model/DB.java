package model;

import java.sql.*;
import java.util.concurrent.Semaphore;

public abstract class DB {

    public static Semaphore sem = new Semaphore(1, true);
    private static final String
            url = "jdbc:mysql://localhost:3306/cristobay_db",
            user = "root",
            password = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}