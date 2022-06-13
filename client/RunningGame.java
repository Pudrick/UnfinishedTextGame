package client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RunningGame extends Thread{

    static int moveFlag = 0;

    static int EndFlag = 0;
    ArrayList<String> validInput = new ArrayList<>(List.of(
            "paper", "rock", "scissors"
    ));

    MySocket selfsocket;
    int maxhp = 3;

    class playerinfo {
        String name;
        int hp = maxhp;
    }
    playerinfo selfInfo;
    playerinfo targetInfo;
    public RunningGame(String targetName) {
        selfsocket = Client.serverSocket;
        selfInfo = new playerinfo();
        selfInfo.name = Client.selfName;
        targetInfo = new playerinfo();
        targetInfo.name = targetName;
    }

    public void run() {
        DisplayInfo();
        while(EndFlag == 0) {
//            System.out.println("input into loop.");
            while(moveFlag == 1);
            String move = GetMoveInput();
            SendMove(move);
            moveFlag = 1;
        }
        return;
    }

    void DisplayInfo() {
        System.out.print("\033\143");// flush the console
        System.out.println("Game has start! input your move: rock/scissors/paper");
    }

    String GetMoveInput() {
        Scanner keyboard = new Scanner(System.in);
        while(true) {
            String move = keyboard.nextLine();
            for(String input : validInput) {
                if(input.equals(move))
                    return input;
            }
            System.out.println("Invalid input!");
        }
    }

    void SendMove(String move) {
        int nameLen = Client.selfName.length();
        byte[] movepkt = new byte[3 + nameLen];
        movepkt[0] = 0x12;
        switch(move) {
            case "rock":
                movepkt[1] = 0x0;
                break;
            case "scissors":
                movepkt[1] = 0x1;
                break;
            case "paper":
                movepkt[1] = 0x2;
                break;
        }
        movepkt[2] = (byte) nameLen;
        byte[] nameByte = Client.selfName.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(nameByte, 0, movepkt, 3, nameByte.length);
        try {
            selfsocket.output.write(movepkt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
