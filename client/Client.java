package client;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {

    static MySocket serverSocket;

    static String selfName;

    public static void main(String[] argv) {
        System.out.print("\033\143");// flush the console
        MySocket skt = InitSocket();
        RecvPacket recv = new RecvPacket(skt);
        recv.start();
        while(true) {
            String username = InitUsername();
            if(LoginToServer(username, skt) == 0)
                break;
        }
        DisplayPlayerList(skt);
        ClientGame newGame = new ClientGame();
        newGame.PlayGame();
    }


    //return the socket
    static MySocket InitSocket() {
        String IPaddr = "127.0.0.1";
        int port = 9876;
        MySocket sokt = new MySocket();
        sokt.SocketInit(IPaddr, port);
        serverSocket = sokt;
        return sokt;
    }

    // return username.
    static String InitUsername() {
        System.out.print("\033\143");// flush the console
        System.out.println("Welcome! Please input your Username to Login, whose length less than 16: ");
        Scanner keyboard = new Scanner(System.in);
        String username;
        for(;;) {
            username = keyboard.nextLine();
            if(username.length() > 16) {
                System.out.println("Invalid username. Please Retry.");
                continue;
            } else {
                break;
            }
        }
        selfName = username;
        return username;
    }


    static int LoginToServer(String username, MySocket socket) {
        SendPacket pkt = new SendPacket();
        pkt.CreatePacket(username, (byte)0x01);
        pkt.Send();
        while(InteractState.registerFlag != 1) {
            if(InteractState.registerFlag == 0)
                continue;
            else if(InteractState.registerFlag == 2) {
                return 1;
            }
        }
        return 0;
    }

    static void DisplayPlayerList(MySocket socket) {
        System.out.print("\033\143");// flush the console
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Players online presently: ");
        int count = InteractState.playerCount;
        if(count <= 0) {
            System.out.println("Player count error.");
        }
        System.out.println("list len:" + InteractState.playerlist.size());
        for(int i = 0; i < count; i++) {
            System.out.println(InteractState.playerlist.get(i).name + "-" +
                    InteractState.playerlist.get(i).State);
        }
    }


}
