package tk.jackyliao123.proxy;

import java.util.ArrayDeque;

public class Logger {
    public static final byte DEBUG = 0;
    public static final byte VERBOSE = 1;
    public static final byte INFO = 2;
    public static final byte WARNING = 3;
    public static final byte ERROR = 4;
    public static final String[] messages = new String[]{"DEBUG", "VERBOSE", "INFO", "WARNING", "ERROR"};
    private static final ArrayDeque<Message> queue = new ArrayDeque<Message>();
    private static final LogThread thread = new LogThread();
    private static byte level;

    public static void init(byte level) {
        Logger.level = level;
        thread.start();
    }

    public static void stop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static byte getLoggingLevel() {
        return level;
    }

    public static void setLoggingLevel(byte level) {
        Logger.level = level;
    }

    public static void error(String s) {
        synchronized (queue) {
            queue.addLast(new Message(ERROR, s));
        }
        thread.interrupt();
    }

    public static void error(Throwable t) {
        synchronized (queue) {
            queue.addLast(new Message(ERROR, t));
        }
        thread.interrupt();
    }

    public static void warning(String s) {
        synchronized (queue) {
            queue.addLast(new Message(WARNING, s));
        }
        thread.interrupt();
    }

    public static void info(String s) {
        synchronized (queue) {
            queue.addLast(new Message(INFO, s));
        }
        thread.interrupt();
    }

    public static void verbose(String s) {
        synchronized (queue) {
            queue.addLast(new Message(VERBOSE, s));
        }
        thread.interrupt();
    }

    public static void debug(String s) {
        synchronized (queue) {
            queue.addLast(new Message(DEBUG, s));
        }
        thread.interrupt();
    }

    private static class Message {
        private byte level;
        private String message;
        private Throwable t;

        private Message(byte level, String message) {
            this.level = level;
            this.message = message;
        }

        private Message(byte level, Throwable t) {
            this.level = level;
            this.t = t;
        }
    }

    public static class LogThread extends Thread {
        public LogThread() {
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException ignored) {
                }
                synchronized (queue) {
                    while (queue.size() > 0) {
                        Message m = queue.pop();
                        if (m.level >= level) {
                            if (m.t != null) {
                                m.t.printStackTrace(System.out);
                            }
                            if (m.message != null) {
                                System.out.println("[" + messages[m.level] + "] " + m.message);
                            }
                        }
                    }
                }
            }
        }
    }
}
