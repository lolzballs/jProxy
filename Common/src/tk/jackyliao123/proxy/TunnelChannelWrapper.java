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

    @Override
    public void pushWriteBuffer(ByteBuffer data) {
        pushWriteBuffer(Constants.MAX_CONNECTIONS, data);
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

        System.out.println(ids + "!");

        addInterest(SelectionKey.OP_WRITE);
        System.out.println("Interest added");
    }

    @Override
    public ByteBuffer getWriteBuffer() {
        if (ids.isEmpty()) {
            removeInterest(SelectionKey.OP_WRITE);
            System.out.println("get buffer null");
            return null;
        }

        int client = ids.pop();

        ids.push(client);
        idsWaiting.push(client);

        ArrayDeque<ByteBuffer> clientBuffers = dataBuffers.get(client);
        if (clientBuffers.isEmpty()) {
            removeInterest(SelectionKey.OP_WRITE);
            System.out.println("get buffer null");
            return null;
        }
        super.pushWriteBuffer(clientBuffers.pop());

        System.out.println(ids + ", " + idsWaiting);
        return super.getWriteBuffer();
    }

    @Override
    public ByteBuffer popWriteBuffer() throws IOException {
        System.out.println(idsWaiting);

        int client = idsWaiting.pop();
        System.out.println("Pop " + client);
        ArrayDeque<ByteBuffer> buffers = dataBuffers.get(client);

        if (buffers.isEmpty()) {
            ids.remove(client);
            dataBuffers.remove(client);
        }

        ByteBuffer b = writeBuffers.removeFirst();
        if (dataBuffers.isEmpty()) {
            removeInterest(SelectionKey.OP_WRITE);
        }
        return b;
    }
}
