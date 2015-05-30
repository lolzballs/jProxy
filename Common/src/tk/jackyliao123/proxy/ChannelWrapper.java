package tk.jackyliao123.proxy;

import tk.jackyliao123.proxy.event.ConnectEventListener;
import tk.jackyliao123.proxy.event.DisconnectEventListener;
import tk.jackyliao123.proxy.event.ReadEventListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayDeque;

public class ChannelWrapper {
    public final AbstractSelectableChannel channel;
    public final SelectionKey selectionKey;
    protected final ArrayDeque<BufferFiller> readBuffers;
    protected final ArrayDeque<ByteBuffer> writeBuffers;

    public long currentTimestamp = System.currentTimeMillis();

    public boolean isConnected = false;
    public ConnectEventListener connectListener;
    public DisconnectEventListener disconnectListener;
    protected boolean shouldClose = false;
    protected boolean isClosed = false;

    public boolean stopReading = false;
    public boolean stopWriting = false;

    protected ChannelWrapper(AbstractSelectableChannel channel, SelectionKey key, ArrayDeque<BufferFiller> readBuffer, ArrayDeque<ByteBuffer> writeBuffer) {
        this.channel = channel;
        this.selectionKey = key;
        this.readBuffers = readBuffer;
        this.writeBuffers = writeBuffer;
    }

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

    public void close() {
        if (!isClosed) {
            isClosed = true;
            try {
                if (disconnectListener != null) {
                    disconnectListener.onDisconnect(this);
                }
            } catch (Exception e) {
                Logger.error("onDisconnect has experienced an error");
                Logger.error(e);
            }
            try {
                channel.close();
            } catch (Exception e) {
                Logger.error("channel.close() has experienced an error");
                Logger.error(e);
            }
        }
    }

    public void closeOnFinishData() {
        shouldClose = true;
        removeInterest(SelectionKey.OP_READ);
        if (writeBuffers.isEmpty()) {
            close();
        }
    }

    public boolean shouldClose() {
        return shouldClose;
    }

    public void addInterest(int op) {
        try {
            selectionKey.interestOps(selectionKey.interestOps() | op);
        } catch (CancelledKeyException e) {
            Logger.warning("Cancelled key exception");
            close();
        }
    }

    public void removeInterest(int op) {
        try {
            selectionKey.interestOps(selectionKey.interestOps() & (~op));
        } catch (CancelledKeyException e) {
            Logger.warning("Cancelled key exception");
            close();
        }
    }

    public void pushDumpReadBuffer(ReadEventListener listener) {
        if (!shouldClose) {
            readBuffers.addLast(new BufferFiller(ByteBuffer.allocate(Constants.BUFFER_SIZE), listener, false));
            if (!stopReading) {
                addInterest(SelectionKey.OP_READ);
            }
        }
    }

    public void pushFillReadBuffer(ByteBuffer bytes, ReadEventListener listener) {
        if (!shouldClose) {
            readBuffers.addLast(new BufferFiller(bytes, listener, true));
            if (!stopReading) {
                addInterest(SelectionKey.OP_READ);
            }
        }
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
        removeInterest(SelectionKey.OP_READ);
        return null;
    }

    public boolean isFullyRead() {
        ByteBuffer b = readBuffers.peekFirst().buffer;
        return b.position() == b.capacity();
    }

    public void pushWriteBuffer(ByteBuffer bytes) {
        if (!shouldClose) {
            writeBuffers.addLast(bytes);
            if (!stopWriting) {
                addInterest(SelectionKey.OP_WRITE);
            }
        }
    }

    public ByteBuffer popWriteBuffer() throws IOException {
        ByteBuffer b = writeBuffers.removeFirst();
        if (writeBuffers.isEmpty()) {
            removeInterest(SelectionKey.OP_WRITE);
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
