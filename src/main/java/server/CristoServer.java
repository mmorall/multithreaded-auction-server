package server;

import model.DB;
import view.ServerUI;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class CristoServer {
    public static ServerThread server;
    public static ArrayList<SocketThread> sockets = new ArrayList<>();
    public static ServerUI ui;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(ServerProtocol::serverShutdown));
        setUsersDisconnected();
        SwingUtilities.invokeLater(ui = new ServerUI("CristoServer"));
    }

    public static void runServer(String port) {
        if (!port.isBlank() && port.matches("\\d*")) {
            server = new ServerThread(Integer.parseInt(port));
            ui.goToClientsPane();
        } else {
            ui.consolePrint("invalid port");
            JOptionPane.showMessageDialog(null, "Invalid port");
        }
    }

    public static void setUsersDisconnected() {
        String sqlString = "UPDATE usuario SET connected=0";
        try {
            DB.sem.acquire();
            Connection con = DB.getConnection();
            con.createStatement().execute(sqlString);
            con.close();
            DB.sem.release();
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

