package tk.jackyliao123.proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class TunnelChannelWrapper extends ChannelWrapper {
    private final HashMap<Integer, ArrayDeque<ByteBuffer>> dataBuffers;

    public TunnelChannelWrapper(ChannelWrapper wrapper) {
        super(wrapper.channel, wrapper.selectionKey, wrapper.readBuffers, wrapper.writeBuffers);
        currentTimestamp = wrapper.currentTimestamp;
        connectListener = wrapper.connectListener;
        disconnectListener = wrapper.disconnectListener;
        isConnected = wrapper.isConnected;
        shouldClose = wrapper.shouldClose;
        isClosed = wrapper.isClosed;

        stopWriting = wrapper.stopWriting;
        stopReading = wrapper.stopReading;

        selectionKey.attach(this);

        this.dataBuffers = new HashMap<Integer, ArrayDeque<ByteBuffer>>();
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

        buffers.addLast(data);

        if (buffers.size() >= Constants.MAX_QUEUE) {
            stopReading(connectionId);
            Logger.info("Buffer filled");
        }

        addInterest(SelectionKey.OP_WRITE);
    }

    @Override
    public ByteBuffer getWriteBuffer() {
        if (writeBuffers.isEmpty()) {
            Iterator<Map.Entry<Integer, ArrayDeque<ByteBuffer>>> iterator = dataBuffers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, ArrayDeque<ByteBuffer>> entry = iterator.next();
                ArrayDeque<ByteBuffer> clientBuffers = entry.getValue();
                ByteBuffer buffer = clientBuffers.removeFirst();
                if (clientBuffers.isEmpty()) {
                    iterator.remove();
                }
                if (clientBuffers.size() < Constants.MAX_QUEUE) {
                    startReading(entry.getKey());
                }
                super.pushWriteBuffer(buffer);
            }
        }

        return super.getWriteBuffer();
    }

    @Override
    public ByteBuffer popWriteBuffer() throws IOException {
        ByteBuffer b = writeBuffers.removeFirst();
        if (writeBuffers.isEmpty() && dataBuffers.isEmpty()) {
            removeInterest(SelectionKey.OP_WRITE);
        }
        return b;
    }

    public abstract void stopReading(int connectionId);

    public abstract void startReading(int connectionId);

    public abstract void cleanup();

}
