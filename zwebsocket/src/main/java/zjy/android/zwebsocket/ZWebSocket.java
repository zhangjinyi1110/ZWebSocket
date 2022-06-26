package zjy.android.zwebsocket;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import okio.ByteString;
import zjy.android.zwebsocket.call.WebSocketCall;
import zjy.android.zwebsocket.reader.Reader;
import zjy.android.zwebsocket.request.Request;
import zjy.android.zwebsocket.timeout.Timeout;
import zjy.android.zwebsocket.writer.PingTaskQueue;
import zjy.android.zwebsocket.writer.Writer;

public class ZWebSocket implements WebSocket, Reader.FrameCallback {

    private final List<IReadCallback> readCallbackList = new ArrayList<>();
    private final List<IConnectCallback> connectCallbackList = new ArrayList<>();

    private Request request;

    private Writer writer;
    private Reader reader;
    private Socket socket;

    private PingTaskQueue pingTaskQueue;

    private boolean clientClose = false;

    public static int MAX_WAIT_PONG_COUNT = 10;
    private int pingCount = 0;
    private boolean waitPong = false;

    @Override
    public void connect(Request request) throws IOException {
        this.request = request;
        changedConnectState(0, 0, null);
        WebSocketCall webSocketCall = new WebSocketCall();
        webSocketCall.call(request, new WebSocketCall.Callback() {
            @Override
            public void onSuccess(Socket socket) {
                try {
                    ZWebSocket.this.socket = socket;
                    writer = new Writer(socket.getOutputStream());
                    reader = new Reader(socket.getInputStream(), ZWebSocket.this);
                    changedConnectState(1, 0, null);
                    pingTaskQueue = new PingTaskQueue();
                    pingTaskQueue.start(() -> {
                        try {
                            if (waitPong && pingCount > MAX_WAIT_PONG_COUNT) {
                                throw new IOException("wait pong count greater than " + MAX_WAIT_PONG_COUNT + ", read timeout.");
                            }
                            writer.writePingFrame(ByteString.EMPTY);
                            pingCount++;
                            waitPong = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                close(4001, e.toString());
                            } catch (IOException exception) {
                                exception.printStackTrace();
                                onFailure(4003, exception.toString());
                            }
                        }
                    }, ZWebSocket.this.request.getPingIntervalMill());
                    reader.loopRead();
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        close(4002, e.toString());
                    } catch (IOException exception) {
                        exception.printStackTrace();
                        onFailure(4004, exception.toString());
                    }
                }
            }

            @Override
            public void onFailure(int code, String reason) {
                changedConnectState(4, code, reason);
            }
        });
    }

    @Override
    public void reconnect() throws IOException {
        if (socket != null) {
            throw new IOException("original socket is live");
        }
        clientClose = false;
        pingCount = 0;
        waitPong = false;
        connect(request);
    }

    @Override
    public void close(int code, String reason) throws IOException {
        changedConnectState(2, code, reason);
        writer.writeCloseFrame(code, ByteString.encodeUtf8(reason));
        clientClose = true;
        if (code == 4002) {
            close();
        } else {
            Timeout.timeout(1000, 10000, () -> {
                try {
                    close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    changedConnectState(4, 4005, "server close timeout");
                }
            });
        }
    }

    @Override
    public void send(String message) throws IOException {
        if (writer != null) {
            writer.writeMessageFrame(message);
        }
    }

    @Override
    public void send(ByteString message) throws IOException {
        if (writer != null) {
            writer.writeMessageFrame(message);
        }
    }

    @Override
    public void registerReadCallback(IReadCallback callback) {
        if (!readCallbackList.contains(callback)) {
            readCallbackList.add(callback);
        }
    }

    @Override
    public void unregisterReadCallback(IReadCallback callback) {
        readCallbackList.remove(callback);
    }

    @Override
    public void registerConnectCallback(IConnectCallback callback) {
        if (!connectCallbackList.contains(callback)) {
            connectCallbackList.add(callback);
        }
    }

    @Override
    public void unregisterConnectCallback(IConnectCallback callback) {
        connectCallbackList.remove(callback);
    }

    private void close() throws IOException {
        pingTaskQueue.stop();
        pingTaskQueue = null;
        if (writer != null) {
            writer.close();
            writer = null;
        }
        if (reader != null) {
            reader.close();
            reader = null;
        }
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    private void changedConnectState(int state, int code, String reason) {
        switch (state) {
            case 0:
                for (IConnectCallback callback : connectCallbackList) {
                    callback.onConnecting();
                }
                break;
            case 1:
                for (IConnectCallback callback : connectCallbackList) {
                    callback.onConnected();
                }
                break;
            case 2:
                for (IConnectCallback callback : connectCallbackList) {
                    callback.onClosing(code, reason);
                }
                break;
            case 3:
                for (IConnectCallback callback : connectCallbackList) {
                    callback.onClosed(code, reason);
                }
                break;
            case 4:
                for (IConnectCallback callback : connectCallbackList) {
                    callback.onConnectFail(code, reason);
                }
                break;
        }
    }

    @Override
    public void onMessage(String message) {
        for (IReadCallback callback : readCallbackList) {
            callback.onRead(message);
        }
    }

    @Override
    public void onMessage(ByteString message) {
        for (IReadCallback callback : readCallbackList) {
            callback.onRead(message);
        }
    }

    @Override
    public void onClose(int code, String reason) throws IOException {
        if (!clientClose) {
            changedConnectState(2, code, reason);
            writer.writeCloseFrame(code, ByteString.EMPTY);
        }
        Timeout.cancel(1000);
        close();
        changedConnectState(3, code, reason);
    }

    @Override
    public void onPing(ByteString payload) throws IOException {
        writer.writePongFrame(payload);
    }

    @Override
    public void onPong(ByteString payload) {
        pingCount = 0;
        waitPong = false;
    }
}
