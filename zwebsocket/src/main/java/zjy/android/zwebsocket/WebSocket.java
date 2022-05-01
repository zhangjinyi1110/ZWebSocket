package zjy.android.zwebsocket;

import java.io.IOException;

import okio.ByteString;
import zjy.android.zwebsocket.request.Request;

public interface WebSocket {

    void connect(Request request) throws IOException;

    void reconnect() throws IOException;

    void close(int code, String reason) throws IOException;

    void send(String message) throws IOException;

    void send(ByteString message) throws IOException;

    void registerReadCallback(IReadCallback callback);

    void unregisterReadCallback(IReadCallback callback);

    void registerConnectCallback(IConnectCallback callback);

    void unregisterConnectCallback(IConnectCallback callback);

}
