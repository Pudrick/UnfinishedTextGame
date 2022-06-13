package client;

class operate {
    public static final int INVITE = 0;
    public static final int HELP = 1;
    public static final int PLAYERS = 2;

    public static final int PLAYING = 3;

    int type;
    // 0: challenge
    // 1: get help
    // 2: display online players

    String target;
}
