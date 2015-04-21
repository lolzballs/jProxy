package tk.jackyliao123.nioevent;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Objects;

public class EventProcess {
    public SelectionKey key;
    public ReadEventHandler read;
    public DeathEventHandler death;
    public ByteBuffer buffer;
    public boolean notifyOnRead;
    public long lastTime;
    public final long timeout;
    public Object info;

    public EventProcess(SelectionKey key, ReadEventHandler read, DeathEventHandler death, ByteBuffer buffer, boolean notifyOnRead, long timeout) {
        this.key = key;
        this.read = read;
        this.death = death;
        this.buffer = buffer;
        this.notifyOnRead = notifyOnRead;
        this.timeout = timeout;
        this.info = null;
    }

    public EventProcess(SelectionKey key, ReadEventHandler read, DeathEventHandler death, ByteBuffer buffer, boolean notifyOnRead, long timeout, Object info) {
        this.key = key;
        this.read = read;
        this.death = death;
        this.buffer = buffer;
        this.notifyOnRead = notifyOnRead;
        this.timeout = timeout;
        this.info = info;
    }
}
