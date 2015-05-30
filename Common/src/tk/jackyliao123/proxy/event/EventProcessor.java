package tk.jackyliao123.proxy.event;

import tk.jackyliao123.proxy.BufferFiller;
import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.TunnelChannelWrapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class EventProcessor {
    protected Selector selector;

    public EventProcessor() throws IOException {
        this.selector = Selector.open();
    }

    public Selector getSelector() {
        return selector;
    }

    public void process(long timeout) throws IOException {
        selector.select(timeout);

        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            try {
                if (!key.isValid()) {
                    kill((ChannelWrapper) key.attachment());
                    keys.remove();
                    continue;
                }

                if (key.isReadable()) {
                    Object attachment = key.attachment();
                    if (attachment != null && attachment instanceof ChannelWrapper) {
                        ChannelWrapper channel = (ChannelWrapper) attachment;
                        BufferFiller b = channel.getReadBuffer();
                        if (b != null) {
                            int read = channel.read(b.buffer);
                            if (read == -1) {
                                kill(channel);
                                keys.remove();
                                continue;
                            }

                            if (channel.isFullyRead()) {
                                channel.popReadBuffer().listener.onRead(channel, b.buffer.array());
                            } else if (!b.fillToMax) {
                                byte[] array = new byte[b.buffer.position()];
                                System.arraycopy(b.buffer.array(), 0, array, 0, b.buffer.position());
                                channel.popReadBuffer().listener.onRead(channel, array);
                            }
                        } else {
                            Logger.error("Error, ignored bytes, buffered. Performance bug. This should not happen.");
                        }
                    }
                }

                if (!key.isValid()) {
                    kill((ChannelWrapper) key.attachment());
                    keys.remove();
                    continue;
                }

                if (key.isWritable()) {
                    Object attachment = key.attachment();
                    if (attachment != null && attachment instanceof ChannelWrapper) {
                        ChannelWrapper channel = (ChannelWrapper) attachment;
                        ByteBuffer b = channel.getWriteBuffer();
                        if (b != null) {
                            int write = channel.write(b);

                            if (channel.isFullyWritten()) {
                                channel.popWriteBuffer();
                                if (channel.getWriteBuffer() == null && channel.shouldClose()) {
                                    channel.close();
                                }
                            }
                        }
                    }
                }

                if (!key.isValid()) {
                    kill((ChannelWrapper) key.attachment());
                    keys.remove();
                    continue;
                }

                if (key.isAcceptable()) {
                    Object attachment = key.attachment();
                    if (attachment != null && attachment instanceof AcceptEventListener) {
                        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                        channel.configureBlocking(false);
                        SelectionKey nKey = channel.register(selector, 0);
                        ChannelWrapper wrapper = new ChannelWrapper(channel, nKey);
                        nKey.attach(wrapper);
                        ((AcceptEventListener) attachment).onAccept(wrapper);
                    }
                }

                if (!key.isValid()) {
                    kill((ChannelWrapper) key.attachment());
                    keys.remove();
                    continue;
                }

                if (key.isConnectable()) {
                    Object attachment = key.attachment();
                    if (attachment != null && attachment instanceof ChannelWrapper) {
                        ConnectEventListener listener = ((ChannelWrapper) attachment).connectListener;
                        ((ChannelWrapper) attachment).removeInterest(SelectionKey.OP_CONNECT);
                        if (listener != null) {
                            boolean b = listener.onConnect((ChannelWrapper) attachment);
                            if (b) {
                                ((ChannelWrapper) attachment).isConnected = true;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                if (e.getMessage().equals("Connection reset by peer")) {
                    Logger.info("Connection Reset: " + key.channel());
                } else {
                    Logger.warning("IO error happened");
                }
                kill((ChannelWrapper) key.attachment());
            } catch (Exception e) {
                Logger.error("Event processing has experienced an error on " + key.channel());
                Logger.error(e);
                kill((ChannelWrapper) key.attachment());
            } catch (Throwable t) {
                Logger.error("Critical Error, Throwable caught");
                Logger.error(t);
                ChannelWrapper wrapper = (ChannelWrapper) key.attachment();
                if (wrapper instanceof TunnelChannelWrapper) {
                    ((TunnelChannelWrapper) wrapper).cleanup();
                }
                kill(wrapper);
                System.gc();
            }

            keys.remove();
        }
    }

    public void registerServerChannel(ServerSocketChannel channel, AcceptEventListener listener) throws IOException {
        channel.register(selector, SelectionKey.OP_ACCEPT, listener);
    }

    public ChannelWrapper registerSocketChannel(SocketChannel channel, ConnectEventListener listener) throws IOException {
        SelectionKey key = channel.register(selector, SelectionKey.OP_CONNECT);
        ChannelWrapper wrapper = new ChannelWrapper(channel, key);
        wrapper.connectListener = listener;
        key.attach(wrapper);
        return wrapper;
    }

    public void kill(ChannelWrapper channel) {
        Logger.info("Connection killed: " + channel.channel);
        channel.close();
    }
}
