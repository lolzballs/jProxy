package tk.jackyliao123.proxy;

import tk.jackyliao123.proxy.event.ConnectEventListener;
import tk.jackyliao123.proxy.event.DisconnectEventListener;
import tk.jackyliao123.proxy.event.ReadEventListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayDeque;

public class ChannelWrapper {
    public final AbstractSelectableChannel channel;
    public final SelectionKey selectionKey;
    private final ArrayDeque<BufferFiller> readBuffers;
    private final ArrayDeque<ByteBuffer> writeBuffers;

    public long currentTimestamp = System.currentTimeMillis();

    public boolean isConnected = false;
    public ConnectEventListener connectListener;
    public DisconnectEventListener disconnectListener;
    private boolean shouldClose = false;

    public ChannelWrapper(SocketChannel channel, SelectionKey key) {
        this.channel = channel;
        this.selectionKey = key;
        this.readBuffers = new ArrayDeque<BufferFiller>();
        this.writeBuffers = new ArrayDeque<ByteBuffer>();
    }

    public ChannelWrapper(DatagramChannel channel, SelectionKey key) {
        this.channel = channel;
        this.selectionKey = key;
        this.readBuffers = new ArrayDeque<BufferFiller>();
        this.writeBuffers = new ArrayDeque<ByteBuffer>();
    }

    public int read(ByteBuffer buffer) throws IOException {
        return ((ByteChannel) channel).read(buffer);
    }

    public int write(ByteBuffer buffer) throws IOException {
        return ((ByteChannel) channel).write(buffer);
    }

    public void close() throws IOException {
        try {
            if (disconnectListener != null) {
                disconnectListener.onDisconnect(this);
            }
        } catch (Exception e) {
            System.err.println("onDisconnect has experienced an error");
            e.printStackTrace();
        }
        channel.close();
    }

    public void closeOnFinishData() {
        shouldClose = true;
    }

    public void addInterest(int op) {
        selectionKey.interestOps(selectionKey.interestOps() | op);
    }

    public void removeInterest(int op) {
        selectionKey.interestOps(selectionKey.interestOps() & (~op));
    }

    public void pushDumpReadBuffer(ReadEventListener listener) {
        readBuffers.addLast(new BufferFiller(ByteBuffer.allocate(Constants.BUFFER_SIZE), listener, false));
        addInterest(SelectionKey.OP_READ);
    }

    public void pushFillReadBuffer(ByteBuffer bytes, ReadEventListener listener) {
        readBuffers.addLast(new BufferFiller(bytes, listener, true));
        addInterest(SelectionKey.OP_READ);
    }

    public BufferFiller popReadBuffer() throws IOException {
        BufferFiller b = readBuffers.removeFirst();
        if (readBuffers.isEmpty()) {
            removeInterest(SelectionKey.OP_READ);
        }
        return b;
    }

    public BufferFiller getReadBuffer() {
        if (!readBuffers.isEmpty()) {
            return readBuffers.getFirst();
        }
        return null;
    }

    public boolean isFullyRead() {
        ByteBuffer b = readBuffers.peekFirst().buffer;
        return b.position() == b.capacity();
    }

    public void pushWriteBuffer(ByteBuffer bytes) {
        writeBuffers.addLast(bytes);
        addInterest(SelectionKey.OP_WRITE);
    }

    public ByteBuffer popWriteBuffer() throws IOException {
        ByteBuffer b = writeBuffers.removeFirst();
        if (writeBuffers.isEmpty()) {
            removeInterest(SelectionKey.OP_WRITE);
            if (shouldClose) {
                close();
            }
        }
        return b;
    }

    public ByteBuffer getWriteBuffer() {
        if (!writeBuffers.isEmpty()) {
            return writeBuffers.getFirst();
        }
        return null;
    }

    public boolean isFullyWritten() {
        ByteBuffer b = writeBuffers.peekFirst();
        return b.position() == b.capacity();
    }
}
