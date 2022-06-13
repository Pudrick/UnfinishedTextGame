package client;

import java.util.ArrayList;

public class InteractState {

    public static int inviteFlag = 0;
    // maybe need some mutex lock.
    // 0: nothing
    // 1: accepted
    // 2: rejected

    public static int registerFlag = 0;
    // 0: nothing
    // 1: register success.
    // 2: register fail.

    public static int playerCount = -1;

    static class playerState {
        String name;
        String State;
    }

    public static ArrayList<playerState> playerlist = new ArrayList<>();
}


