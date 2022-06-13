package client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class RecvPacket extends Thread{
    public MySocket listenSocket;

    final int readBufSize = 128;

    static int needRead = 1;
    byte[] readBuffer = new byte[readBufSize];

    static RunningGame Game;

    public RecvPacket(MySocket socket) {
        listenSocket = socket;
    }

    public void run() {
        while(true) {
            try {
//                System.out.println("start read buffer.");
                listenSocket.input.read(readBuffer);
//                System.out.println("read finish. start handle.");
                HandleReadMsg();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void HandleReadMsg() {
        while(readBuffer[0] == 0) {
//            System.out.println("buffer[0] is 0");
            readBuffer = Arrays.copyOfRange(readBuffer, 1, readBuffer.length);
        }
//        System.out.println("start switch.");
        switch(readBuffer[0]) {
            case 0x08:
                System.out.println("0x08 recv.");
                InteractState.registerFlag = 2;
                break;
            case 0x09:
                System.out.println("0x09 recv.");
                InteractState.registerFlag = 1;
                break;
            case 0x02:
                System.out.println("0x02 recv.");
                InteractState.playerCount = readBuffer[1];
//                int offset = 1;
//                for(int i=0; i < InteractState.playerCount;i++) {
//                    int nameLen = readBuffer[offset];
//                    byte[] tempByteName = new byte[nameLen];
//                    System.arraycopy(readBuffer, offset + 1, tempByteName, 0, nameLen);
//                    String playerName = new String(tempByteName, StandardCharsets.UTF_8);
//                    InteractState.playerState tempState = new InteractState.playerState();
//                    tempState.name = playerName;
//                    int state = readBuffer[offset + nameLen];
//                    if(state == 1)
//                        tempState.State = "online";
//                    else
//                        tempState.State = "playing";
//                    InteractState.playerlist.add(tempState);
//                    offset += (nameLen + 2);
//                }
                break;
            case 0x03:
                System.out.println("0x03 recv.");
                int readoffset = 0;
                byte[] rawMsg = Arrays.copyOfRange(readBuffer, 1, readBuffer.length);
                for(int i=0;i<InteractState.playerCount;i++) {
                    int nameLen = rawMsg[readoffset + 0];
                    byte[] playerName = Arrays.copyOfRange(rawMsg, readoffset + 1, readoffset + nameLen + 1);
                    byte playerState = rawMsg[readoffset + nameLen + 1];
                    InteractState.playerState tempPlayer = new InteractState.playerState();
                    tempPlayer.name = new String(playerName, StandardCharsets.UTF_8);
                    if(playerState == 0x01) {
                        tempPlayer.State = "online";
                    }
                    if(playerState == 0x02) {
                        tempPlayer.State = "playing";
                    }
                    InteractState.playerlist.add(tempPlayer);
                    readoffset += (2 + playerName.length);
                }
                break;
            case 0x05:
                System.out.println("0x05 recv.");
                ClientGame.gameFlag = 1;
                int sourceLen = readBuffer[1];
                byte[] tempsourceName = new byte[sourceLen];
                System.arraycopy(readBuffer, 2, tempsourceName, 0, sourceLen);
                String sourceName = new String(tempsourceName, StandardCharsets.UTF_8);
                System.out.println(sourceName + " invite you to play. Accept?(y/n)");
                Scanner keyboard = new Scanner(System.in);
                String ifacc = keyboard.nextLine();
                System.out.println(ifacc);
                if(ifacc.equals("n")) {
                    // rejected.
                    System.out.println("n pressed");
                    byte[] rejPkt = readBuffer;
                    rejPkt[0] = 0x07;
                    try {
                        listenSocket.output.write(readBuffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Rejected.");
                } else if(ifacc.equals("y")) {
                    // accepted.
                    System.out.println("y pressed.");
                    byte[] accPkt = readBuffer;
                    accPkt[0] = 0x06;
                    try {
                        listenSocket.output.write(readBuffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Accepted.");
                    Game = new RunningGame(sourceName);
                    Game.start();
                }
                break;
            case 0x10:
                System.out.println("0x10 recv.");
//                System.out.println("Your invitation is rejected.");
                InteractState.inviteFlag = 2;
                break;
            case 0x11:
                System.out.println("0x11 received.");
                InteractState.inviteFlag = 1;
                System.out.println("flag changed to 1");
                break;
            case 0x14:
                System.out.println("0x14 recv.");
                switch(readBuffer[1]) {
                    case 0x1:
                        Game.selfInfo.hp--;
                        System.out.println("You lose!");
                        break;
                    case 0x2:
                        Game.targetInfo.hp--;
                        System.out.println("You win!");
                        break;
                    case 0x3:
                        System.out.println("Draw!");
                        break;

                }
                System.exit(0);
                Game.moveFlag = 0;
                needRead = 0;
                break;
            case 0x15:
                System.out.println("0x15 recv.");
                Game.EndFlag = 1;
                if(readBuffer[1] == 0x01) {
                    System.out.println("Game over! You win.");
                } else {
                    System.out.println("Game over! You lose.");
                }
                ClientGame.gameFlag = 0;
                break;
            case 0x16:
                Game.moveFlag = 0;
                System.out.println("get 0x16");
                break;
            case 0x17:
                int len = readBuffer[1];
                byte[] bytemsg = Arrays.copyOfRange(readBuffer, 2, len + 1);
                String broad = new String(bytemsg, StandardCharsets.UTF_8);
                System.out.println(broad);
                break;
        }
    }
}
