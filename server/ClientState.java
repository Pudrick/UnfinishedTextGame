package server;

import java.net.Socket;

public class ClientState {
    public String username;
    public Socket socket;

    public String inviteTarget;

    public String invitor;
    public int state;
    // 1 for online, 2 for playing.
}
