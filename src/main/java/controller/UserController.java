package controller;

import model.DB;
import model.User;

import java.sql.*;
import java.util.ArrayList;

public class UserController {

    public static User getUserByCredentials(String username, String password) {
        User user = null;
        String sqlString = "SELECT * from usuario WHERE login=? AND clave=?";
        try {
            DB.sem.acquire();
            Connection con = DB.getConnection();
            PreparedStatement statement = con.prepareStatement(sqlString);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) user = getUser(rs);
            con.close();
            DB.sem.release();
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    public static User getUserByUsername(String username) {
        User user = null;
        String sqlString = "SELECT * from usuario WHERE login=?";
        try {
            DB.sem.acquire();
            Connection con = DB.getConnection();
            PreparedStatement statement = con.prepareStatement(sqlString);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) user = getUser(rs);
            con.close();
            DB.sem.release();
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    private static void changeUserStatus(User user, Boolean connected) {
        String sqlString = "UPDATE usuario SET connected=? WHERE login=?";
        try {
            DB.sem.acquire();
            Connection con = DB.getConnection();
            PreparedStatement statement = con.prepareStatement(sqlString);
            statement.setBoolean(1, connected);
            statement.setString(2, user.getUsername());
            statement.execute();
            con.close();
            DB.sem.release();
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static void connect(User user) {
        changeUserStatus(user, true);
    }

    public static void disconnect(User user) {
        changeUserStatus(user, false);
    }

    public static User getUserById(int id) {
        User user = null;
        try {
            DB.sem.acquire();
            Connection con = DB.getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * from usuario WHERE id_usuario=?");
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) user = getUser(rs);
            con.close();
            DB.sem.release();
        } catch (InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    public static ArrayList<User> getConnectedUsers() {
        ArrayList<User> users = null;
        String sqlString = "SELECT * from usuario WHERE connected=1";
        try {
            DB.sem.acquire();
            Connection con = DB.getConnection();
            ResultSet rs = con.createStatement().executeQuery(sqlString);
            users = new ArrayList<>();
            while (rs.next()) {
                User user = getUser(rs);
                users.add(user);
            }
            con.close();
            DB.sem.release();
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    private static User getUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id_usuario"));
        user.setUsername(rs.getString("login"));
        user.setPassword(rs.getString("clave"));
        user.setBalance(rs.getInt("monedero"));
        user.setName(rs.getString("nombre"));
        user.setSurname(rs.getString("apellido_1"));
        user.setLastname(rs.getString("apellido_2"));
        user.setEmail(rs.getString("correo"));
        user.setConnected(rs.getBoolean("connected"));
        return user;
    }
}
