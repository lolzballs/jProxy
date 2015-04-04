package tk.jackyliao123.nioevent;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class EventProcess {
    public SelectionKey key;
    public ReadEventHandler read;
    public DeathEventHandler death;
    public ByteBuffer buffer;
    public boolean notifyOnRead;
    public long lastTime;
    public final long timeout;
    public int id;

    public EventProcess(SelectionKey key, ReadEventHandler read, DeathEventHandler death, ByteBuffer buffer, boolean notifyOnRead, long timeout) {
        this.key = key;
        this.read = read;
        this.death = death;
        this.buffer = buffer;
        this.notifyOnRead = notifyOnRead;
        this.timeout = timeout;
        this.id = -1;
    }

    public EventProcess(SelectionKey key, ReadEventHandler read, DeathEventHandler death, ByteBuffer buffer, boolean notifyOnRead, long timeout, int id) {
        this.key = key;
        this.read = read;
        this.death = death;
        this.buffer = buffer;
        this.notifyOnRead = notifyOnRead;
        this.timeout = timeout;
        this.id = id;
    }
}
