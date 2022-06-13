package server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GameService extends Thread{
    int maxhp = 3;

    class player {
        String name;
        int hp = maxhp;
        ClientState client;
        int move = -1;
    }
    public static player p1;
    public static player p2;

    public GameService(String p1Str, String p2Str) {
        System.out.println("p1str p2str" + p1Str + " " + p2Str);
        p1 = new player();
        p1.name = p1Str;
        p2 = new player();
        p2.name = p2Str;
        for(ClientState temp : Server.clients) {
            System.out.println("temp name is " + temp.username);
            if(temp.username.equals(p1Str))
                p1.client = temp;
            if(temp.username.equals(p2Str))
                p2.client = temp;
        }
    }

    public player WhichPlayer(String judgeName) {
        if(p1.name == judgeName)
            return p1;
        if(p2.name == judgeName)
            return p2;
        return null;
    }


    void JudgeWinner() {
        System.out.println("Judge blocking.");

//        while(true) {
//            if(p1.move < 0 || p2.move < 0) {
//                try {
//                    TimeUnit.MILLISECONDS.sleep(300);
//                    continue;
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            else
//                break;
//        };

        while(p1.move < 0 || p2.move < 0) {
            try {
//                    System.out.println("into or loop.");
                    TimeUnit.MILLISECONDS.sleep(300);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
            }
        }
        System.out.println("Start judge: p1 is " + p1.move + " p2 is " + p2.move);
        if(p1.move == 0) {
            if(p2.move == 0) {
                // no win.
                SendResult(3);
            } else if(p2.move == 1) {
                // p1 win
                SendResult(1);
                p2.hp--;
            } else if(p2.move == 2) {
                // p1 lose
                SendResult(2);
                p1.hp--;
            }
        } else if(p1.move == 1) {
            if(p2.move == 0) {
                // p1 lose
                SendResult(2);
                p1.hp--;
            } else if(p2.move == 1) {
                SendResult(3);
            } else if(p2.move == 2) {
                SendResult(1);
                p2.hp--;
            }
        } else if(p1.move == 2) {
            if(p2.move == 0) {
                // p1 win
                SendResult(1);
                p2.hp--;
            } else if(p2.move == 1) {
                SendResult(2);
                p1.hp--;
            } else if(p2.move == 2) {
                SendResult(3);
            }
        }
    }

    void SendResult(int res) {
        // 1: p1win
        // 2: p2win
        // 3: no win
        try {
            byte[] winner = new byte[2];
            winner[0] = 0x14;
            byte[] loser = new byte[2];
            loser[0] = 0x14;
            winner[1] = (byte) 2;
            loser[1] = (byte) 1;
            byte[] equal = new byte[2];
            equal[0] = 0x14;
            equal[1] = (byte)0;
            switch(res) {
                case 1:
                    p1.client.socket.getOutputStream().write(winner);
                    p2.client.socket.getOutputStream().write(loser);
                    break;
                case 2:
                    p2.client.socket.getOutputStream().write(winner);
                    p1.client.socket.getOutputStream().write(loser);
                    break;
                case 3:
                    p1.client.socket.getOutputStream().write(equal);
                    p2.client.socket.getOutputStream().write(equal);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        p1.move = -1;
        p2.move = -1;
    }

    void SendGameResult() {
        try {
            byte[] res = new byte[2];
            res[0] = 0x15;
            if (p1.hp == 0) {
                res[1] = 0x01;
                p1.client.socket.getOutputStream().write(res);
                res[1] = 0x00;
                p2.client.socket.getOutputStream().write(res);
            } else if(p2.hp == 0) {
                res[1] = 0x01;
                p2.client.socket.getOutputStream().write(res);
                res[1] = 0x00;
                p1.client.socket.getOutputStream().write(res);
            }
            p1.client.socket.getOutputStream().write(0x16);
            p2.client.socket.getOutputStream().write(0x16);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        while(p1.hp > 0 && p2.hp > 0)
            JudgeWinner();
        SendGameResult();
        try {
            for(ClientState temp : Server.clients) {
                if(temp.username.equals(p1.name)) {
                    Server.clients.remove(temp);
                }
                if(temp.username.equals(p2.name)) {
                    Server.clients.remove(temp);
                }
            }
            p1.client.socket.close();
            p2.client.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("server game halt.");
        return;
    }
}
