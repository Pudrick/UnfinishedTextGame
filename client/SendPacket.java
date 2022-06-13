package client;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SendPacket {
    MySocket socket;

    byte[] pktContent;

    public SendPacket() {
        socket = Client.serverSocket;
    }

    public void CreatePacket(String content, byte state) {
        int minuslen = content.length();
        byte[] res = new byte[minuslen + 2];
        res[0] = state;
        res[1] = (byte)minuslen;
        byte[] stringbytes = content.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(stringbytes, 0, res, 2, stringbytes.length);
        pktContent = res;
    }

    public void CreatePacket(byte state) {
        byte[] res = new byte[1];
        res[0] = state;
        pktContent = res;
    }

    public void Send() {
        try {
            socket.output.write(pktContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
