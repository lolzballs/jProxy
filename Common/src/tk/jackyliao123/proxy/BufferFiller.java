package tk.jackyliao123.proxy;

import tk.jackyliao123.proxy.event.ReadEventListener;

import java.nio.ByteBuffer;

public class BufferFiller {
    public final ByteBuffer buffer;
    public final ReadEventListener listener;
    public final boolean fillToMax;

    public BufferFiller(ByteBuffer buffer, ReadEventListener listener, boolean fillToMax) {
        this.buffer = buffer;
        this.listener = listener;
        this.fillToMax = fillToMax;
    }
}
