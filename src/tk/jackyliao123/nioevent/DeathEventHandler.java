package tk.jackyliao123.nioevent;

import java.io.IOException;

public interface DeathEventHandler {
    void action(EventProcess event) throws IOException;
}
