package tk.jackyliao123.nioevent;

public class ConnectionProcess {
    public final ConnectEventHandler handler;
    public final long startTime;
    public final long timeout;

    public ConnectionProcess(ConnectEventHandler handler, long startTime, long timeout) {
        this.handler = handler;
        this.startTime = startTime;
        this.timeout = timeout;
    }
}
