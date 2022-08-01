package server;

import model.User;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class SocketThread extends Thread {
    public ServerProtocol protocol;
    public User user = null;
    public Socket socket;
    public final PrintWriter out;
    public final BufferedReader in;

    public SocketThread(Socket socket) throws IOException {
        setName(UUID.randomUUID().toString().replace("-", "").substring(0, 5));
        this.socket = socket;
        protocol = new ServerProtocol(this);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        start();
    }

    public void sendAndPrintData(String data) {
        signedPrint("<- " + data);
        sendData(data);
    }

    public void sendData(String data) {
        out.println(data);
    }

    public void signedPrint(String data) {
        CristoServer.ui.consolePrint("[" + ((user != null) ? getName() + " " + user.getUsername() : getName()) + "] " + data);
    }

    @Override
    public void run() {
        CristoServer.ui.consolePrint("[socket " + getName() + "] connected");
        CristoServer.ui.updateClientsTable();
        String inputLine;
        ServerProtocol protocol = new ServerProtocol(this);
        while (!socket.isClosed()) {
            try {
                if ((inputLine = in.readLine()) != null)
                    protocol.processInput(inputLine);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
