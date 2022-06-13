package client;

import java.util.Scanner;

public class ClientGame {

    public static int gameFlag = 0;
    public void PlayGame() {
        DisplayHelp();
        while(true) {
            if(gameFlag == 1) continue;
            operate userOperate = ChooseOperation();
            ExecuteOperation(userOperate);
            RecvPacket.needRead = 1;
            return;
        }
    }

    void DisplayHelp() {
        System.out.println("Operation Tutorial:");
        System.out.println("input \"intite\" ID to invite other player to play. Example: invite player1");
//        System.out.println("input \"accept\" to accept others' invitation.");
//        System.out.println("input \"reject\" to reject invitation.");
//        System.out.println("input \"online\" to see players online.");
        System.out.println("Ingame Tutorial:");
        System.out.println("input rock/paper/scissors to send your turn.");
        System.out.println("at any time, input exit to exit game.");
    }

    operate ChooseOperation() {
        Scanner keyboard = new Scanner(System.in);
        String command = keyboard.nextLine();
        if(gameFlag == 1) {
            operate playing = new operate();
            playing.type = operate.PLAYING;
            return playing;
        }
        String[] splitCommands = command.split(" ");
        operate userOperate = new operate();
        switch(splitCommands[0]) {
            case "invite":
                System.out.println("switched invite.");
                InteractState.inviteFlag = 0;
                System.out.println("length is " + splitCommands.length);
                if(splitCommands.length < 2) {
                    System.out.println("Input error.");
                    userOperate = ChooseOperation();
                    return userOperate;
                }
                userOperate.type = operate.INVITE;
                userOperate.target = splitCommands[1];
                break;
            case "online":
                userOperate.type = operate.PLAYING;
                break;
            case "exit":
                System.exit(0);
                break;
        }
        return userOperate;
    }

    void ExecuteOperation(operate userOperate) {
        System.out.println("start execute.");
        switch(userOperate.type) {
            case operate.INVITE:
                System.out.println("Inviting player...");
                SendPacket invitePkt = new SendPacket();
                invitePkt.CreatePacket(userOperate.target, (byte)0x04);
                invitePkt.Send();
                System.out.println("invite sent.waiting for flag.");
                while(true) {
//                    System.out.println("flag detected as " + InteractState.inviteFlag);
                    System.out.printf("");
                    if(InteractState.inviteFlag == 0)
                        continue;
                    else
                        break;
                }
                if(InteractState.inviteFlag == 2) {
                    System.out.println("Your invitation is rejected.");
                } else if(InteractState.inviteFlag == 1) {
                    System.out.println("Game is starting...");
                    System.out.println("game flag is " + gameFlag);
                    gameFlag = 1;
                    RecvPacket.Game = new RunningGame(userOperate.target);
                    RecvPacket.Game.start();
                }
                break;
            case operate.PLAYING:
                SendPacket pkt = new SendPacket();
                pkt.CreatePacket((byte) 0x20);
                pkt.Send();
                return;
        }
    }
}

