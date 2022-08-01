package server;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerThread extends Thread {
    public int port;

    public ServerThread(int port) {
        this.port = port;
        start();
    }

    @Override
    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            CristoServer.ui.consolePrint("listening on port " + port);
            while (!serverSocket.isClosed())CristoServer.sockets.add(new SocketThread(serverSocket.accept()));
        } catch (IOException e) {
            CristoServer.ui.consolePrint("could not listen on port " + port);
        }
    }
}
