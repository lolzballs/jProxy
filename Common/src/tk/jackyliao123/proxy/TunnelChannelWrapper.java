package tk.jackyliao123.proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.HashMap;

public class TunnelChannelWrapper extends ChannelWrapper {
    private final HashMap<Integer, ArrayDeque<ByteBuffer>> dataBuffers;
    private final ArrayDeque<Integer> ids;
    private final ArrayDeque<Integer> idsWaiting;

    public TunnelChannelWrapper(SocketChannel channel, SelectionKey key) {
        super(channel, key);
        this.dataBuffers = new HashMap<Integer, ArrayDeque<ByteBuffer>>();
        this.ids = new ArrayDeque<Integer>();
        this.idsWaiting = new ArrayDeque<Integer>();
    }

    public void pushWriteBuffer(int connectionId, ByteBuffer data) {
        ArrayDeque<ByteBuffer> buffers = dataBuffers.get(connectionId);
        if (buffers == null) {
            buffers = new ArrayDeque<ByteBuffer>();
            dataBuffers.put(connectionId, buffers);
        }

        buffers.push(data);

        if (ids.contains(connectionId)) {
            return;
        }
        ids.push(connectionId);
    }

    @Override
    public ByteBuffer getWriteBuffer() {
        int client = ids.pop();
        ids.push(client);
        idsWaiting.push(client);

        super.pushWriteBuffer(dataBuffers.get(client).pop());

        return super.getWriteBuffer();
    }

    @Override
    public ByteBuffer popWriteBuffer() throws IOException {
        int client = idsWaiting.pop();
        ArrayDeque<ByteBuffer> buffers = dataBuffers.get(client);

        if (buffers.isEmpty()) {
            ids.remove(client);
            dataBuffers.remove(client);
        }

        return super.popWriteBuffer();
    }
}
