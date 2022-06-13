package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MySocket {

    Socket socket;
    InputStream input;
    OutputStream output;

    public Socket SocketInit(String IPaddr, int port){
        try{
            socket = new Socket(IPaddr, port);
            input = socket.getInputStream();
            output = socket.getOutputStream();
            return socket;
        }
        catch (UnknownHostException Excep) {
            System.out.println("Could not reach the server.");
            System.out.println("Error is " + Excep);
        } catch (IOException Excep) {
            System.out.println("I/O error.");
            System.out.println("Error is " + Excep);
        }
        return null;
    }

    public void close() {
        try {
            socket.close();
        }
        catch(IOException IOexcep){
            System.out.println("close IOException");
        }
    }
}
