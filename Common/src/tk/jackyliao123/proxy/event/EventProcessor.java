package tk.jackyliao123.proxy.event;

import tk.jackyliao123.proxy.ArrayFiller;
import tk.jackyliao123.proxy.ArrayFillingManager;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;

public class EventProcessor {
    protected Selector selector;

    public EventProcessor() throws IOException {
        this.selector = Selector.open();
    }

    public void registerChannel(AbstractSelectableChannel channel, Object channelData) throws IOException {
        channel.register(selector, SelectionKey.OP_READ, new ArrayFillingManager(channelData));
    }

    public void fillArray(AbstractSelectableChannel channel, ReadEventListener listener, int arraySize) throws IOException {
        new ArrayFiller(new byte[arraySize]);
    }

    public void process(long timeout) throws IOException {
        selector.select(timeout);

    }
}
