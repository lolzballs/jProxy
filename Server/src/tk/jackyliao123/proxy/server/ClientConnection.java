package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.cipher.AESCipher;
import tk.jackyliao123.proxy.server.event.EncryptedPacketLengthListener;
import tk.jackyliao123.proxy.server.event.EncryptedPacketListener;

public class ClientConnection {
    private Server server;
    private ChannelWrapper channel;

    public AESCipher cipher;
    public EncryptedPacketLengthListener packetLengthListener;
    public EncryptedPacketListener packetListener;

    public ClientConnection(Server server, ChannelWrapper channel, AESCipher cipher) {
        this.server = server;
        this.channel = channel;
        this.cipher = cipher;

        this.packetLengthListener = new EncryptedPacketLengthListener(this);
        this.packetListener = new EncryptedPacketListener(this);
    }

    public void processPacket(byte[] data) {
        System.out.println(Util.bs2str(data));

        switch(data[0]) {
            case Constants.TCP_CONNECT:
                break;
            case Constants.TCP_PACKET:
                break;
            case Constants.TCP_DISCONNECT:
                break;
        }
    }
}
