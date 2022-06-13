package server;

import client.Client;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerService extends Thread{
    public Socket clientSocket;
    int initReadBufSize = 128;

    static ClientState currentPlayer;

    InputStream input;
    OutputStream output;

    GameService newGame;

    public ServerService(Socket clientskt) {
        clientSocket = clientskt;
        System.out.println("New thread for coming client.");
    }

    public void run() {
        try {
            input = clientSocket.getInputStream();
            output = clientSocket.getOutputStream();
            while(true) {
                byte[] recvBuf = new byte[initReadBufSize];
                input.read(recvBuf);
                System.out.println("begin handle msg");
                int res = HandleMsg(recvBuf);
                if(res == -1) {
                    System.out.println("Add error.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int HandleMsg(byte[] rawMsg) {
        switch (rawMsg[0]) {
            case 0x01:
                System.out.println("0x01 from ");
                // user login
                if(AddNewPlayer(rawMsg) == 1)
                    return -1;
                for(ClientState temp : Server.clients)
                    if (temp.username == currentPlayer.username)
                        temp.state = 1;
                SendPlayerList();
                break;
            case 0x04:
                System.out.println("0x04 from ");
                if(currentPlayer.state == 2) {
                    // target is playing.
                    try {
                        clientSocket.getOutputStream().write(0x10);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                // invite other player
                int nameLen = rawMsg[1];
                byte[] targetByteName = Arrays.copyOfRange(rawMsg, 2, nameLen + 2);
                String targetName = new String(targetByteName, StandardCharsets.UTF_8);
                currentPlayer.inviteTarget = targetName;
                SendInvitePacket(targetName, currentPlayer.username);
                break;
            case 0x06:
                System.out.println("0x06 from ");
                SendAcceptPack(rawMsg);
                currentPlayer.state = 2;
                for(ClientState temp : Server.clients) {
                    if(temp.username.equals(currentPlayer.inviteTarget))
                        temp.state = 2;
                    if(temp.username.equals(currentPlayer.username))
                        temp.state = 2;
                }
                newGame = new GameService(currentPlayer.username, currentPlayer.inviteTarget);
                newGame.start();
                System.out.println("new game running.");
                break;
            case 0x07:
                System.out.println("0x07 from ");
                // reject game invitation.
                SendRejectPack(rawMsg);
                break;
            case 0x12:
                System.out.println("0x12 from ");
                int fromnameLen = rawMsg[2];
                byte[] nameByte = Arrays.copyOfRange(rawMsg, 3, fromnameLen + 3);
                String fromName = new String(nameByte, StandardCharsets.UTF_8);
                // send move.
                //GameService.player selfPlayer = newGame.WhichPlayer(currentPlayer.username);
                GameService.player selfPlayer;
                System.out.println("0x12, from name is " + fromName);
                if(fromName.equals(newGame.p1.name)) {
                    System.out.println("equal p1name.");
                    selfPlayer = newGame.p1;
                }
                else
                    selfPlayer = newGame.p2;
                selfPlayer.move = rawMsg[1];
                System.out.println("p1.move is "+newGame.p1.move + " p2 move is " + newGame.p2.move);
                break;
            case 0x20:
                SendPlayerList();
                break;
        }
        return 0;
    }

    void SendAcceptPack(byte[] rawMsg) {
        // received order: cource target.
        int sourceLen = rawMsg[1];
        byte[] sourceNameByte = new byte[sourceLen];
        System.arraycopy(rawMsg, 2, sourceNameByte, 0, sourceLen);
        String sourceName = new String(sourceNameByte, StandardCharsets.UTF_8);
        try {
            for(ClientState temp : Server.clients) {
                if(temp.username.equals(sourceName)) {
                    temp.socket.getOutputStream().write(0x11);
                    return;
                }
            }
            System.out.println("Send accept failed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void SendRejectPack(byte[] rawMsg) {
        // received order: source target.
        int sourceLen = rawMsg[1];
        byte[] sourceNameByte = new byte[sourceLen];
        System.arraycopy(rawMsg, 2, sourceNameByte, 0, sourceLen);
        String sourceName = new String(sourceNameByte, StandardCharsets.UTF_8);
        try {
            for(ClientState temp : Server.clients) {
                if(temp.username == sourceName) {
                    temp.socket.getOutputStream().write(0x10);
                    return;
                }
            }
            System.out.println("Send reject failed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void SendInvitePacket(String targetName, String sourceName) {
        int byteLength = targetName.length() + sourceName.length() + 2;
        byte[] invitepkt = new byte[byteLength];
        invitepkt[0] = 0x05;
        invitepkt[1] = (byte)sourceName.length();
        byte[] targetByte = targetName.getBytes(StandardCharsets.UTF_8);
        byte[] sourceByte = sourceName.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(sourceByte, 0, invitepkt, 2, sourceByte.length);
        System.arraycopy(targetByte, 0, invitepkt, 2 + sourceByte.length, targetByte.length);
        try {
            for(ClientState tempclient : Server.clients) {
                if(tempclient.username.equals(targetName)) {
                    tempclient.socket.getOutputStream().write(invitepkt);
                    return;
                }
            }
            System.out.println("Send invite error.");
            try {
                clientSocket.getOutputStream().write(0x10);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int AddNewPlayer(byte[] rawMsg) {
        int nameLen = rawMsg[1];
        byte[] nameBytes = Arrays.copyOfRange(rawMsg, 2, rawMsg.length);
        String name = new String(nameBytes, StandardCharsets.UTF_8);
        name = name.substring(0, 0 + nameLen);
        if(Server.clients.isEmpty() != true)
            for (ClientState recur : Server.clients)
                if (recur.username == name) {
                    // replicated name.
                    SendRegisterFail();
                    return 1;
                }
        ClientState newClient = new ClientState();
        newClient.username = name;
        try {
            for(ClientState temp : Server.clients) {
                String msg = name + " has login.";
                byte[] broadmsg = new byte[msg.length() + 2];
                byte[] bytemsg = msg.getBytes(StandardCharsets.UTF_8);
                System.arraycopy(bytemsg, 0, broadmsg, 2, bytemsg.length);
                broadmsg[0] = 0x17;
                broadmsg[1] = (byte) bytemsg.length;
                temp.socket.getOutputStream().write(broadmsg);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(name + "added.");
        newClient.socket = clientSocket;
        newClient.state = 1;
        currentPlayer = newClient;
        Server.clients.add(newClient);
        SendRegisterSuccess();
        return 0;
    }

    void SendRegisterFail() {
        byte[] repName= new byte[1];
        repName[0] = 0x08;
        try{
            clientSocket.getOutputStream().write(repName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void SendRegisterSuccess() {
        byte[] repName= new byte[1];
        repName[0] = 0x09;
        try{
            clientSocket.getOutputStream().write(repName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void SendPlayerList() {
        int playerCount = Server.clients.size();
        byte[] countbyte = new byte[2];
        countbyte[0] = 0x02;
        countbyte[1] = (byte) playerCount;
        try {
            output.write(countbyte);
            byte[] playersheet = new byte[128];
            playersheet[0] = 0x03;
            int writeoffset = 1;
            for(int i = 0; i < playerCount; i++) {
                String name = Server.clients.get(i).username;
                int nameLen = name.length();
                playersheet[writeoffset] = (byte)nameLen;
                byte[] nameinByte = Server.clients.get(i).username.getBytes(StandardCharsets.UTF_8);
                System.arraycopy(nameinByte, 0, playersheet, writeoffset + 1, nameinByte.length);
                playersheet[writeoffset + 1 + nameinByte.length] = (byte)Server.clients.get(i).state;
                writeoffset += (nameinByte.length + 2);
                if(playersheet[0] == 0) {
                    System.out.println("0 is 0");
                }
            }
            output.write(playersheet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
