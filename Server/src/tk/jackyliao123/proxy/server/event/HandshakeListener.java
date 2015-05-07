package tk.jackyliao123.proxy.server.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.event.ReadEventListener;
import tk.jackyliao123.proxy.server.Server;

import java.io.IOException;
import java.nio.ByteBuffer;

public class HandshakeListener implements ReadEventListener {
    public final Server server;

    public HandshakeListener(Server server) {
        this.server = server;
    }

    public void sendResponse(ChannelWrapper channel, byte responseCode) {
        ByteBuffer b = ByteBuffer.allocate(7);
        b.put(Constants.MAGIC);
        b.put(responseCode);
        channel.pushWriteBuffer((ByteBuffer) b.flip());
    }

    @Override
    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        if (!Util.bseq(array, 0, Constants.MAGIC_LENGTH, Constants.MAGIC, 0, Constants.MAGIC_LENGTH)) {
            sendResponse(channel, Constants.INIT_UNSUPPORTEDVERSION);
            channel.closeOnFinishData();
            return;
        }
        if (array[Constants.MAGIC_LENGTH] != Constants.MAJOR) {
            sendResponse(channel, Constants.INIT_UNSUPPORTEDVERSION);
            channel.closeOnFinishData();
            return;
        }
        if (array[Constants.MAGIC_LENGTH + 1] != Constants.MINOR) {
            System.err.println("WARNING: MINOR VERSION MISMATCH: Received " + array[Constants.MAGIC_LENGTH] + ", Expected " + Constants.MINOR);
        }
        sendResponse(channel, Constants.INIT_SUCCESSFUL);
        channel.pushFillReadBuffer(ByteBuffer.allocate(Constants.RSA_PUBLICKEYSIZE_BYTES + Constants.HASH_SIZE), server.authenticateListener);
    }
}
