package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static ArrayList<ClientState> clients;

    public static void main(String[] argv) {
        System.out.print("\033\143");// flush the console
        try {
            clients = new ArrayList<ClientState>();
            ServerSocket socket = new ServerSocket(9876);
            System.out.println("listening...");
            for(;;) {
                Socket s = socket.accept();
                ServerService newClient = new ServerService(s);
                newClient.start();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}


