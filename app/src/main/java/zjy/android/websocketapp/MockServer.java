package zjy.android.websocketapp;

import static zjy.android.zwebsocket.Contract.ACCEPT_MAGIC;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.ByteString;

public class MockServer {

    private final static MockServer server = new MockServer();

    public static MockServer getInstance() {
        return server;
    }

    public void start() {
        new Thread(this::initServer).start();
    }

    private String host;
    private int port;
    private ServerWebSocketListener webSocketListener;

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getUrl() {
        return getHost() + ":" + getPort();
    }

    public void close(int code, String reason) {
        webSocketListener.webSocket.close(code, reason);
    }

    public void send(String message) {
        webSocketListener.webSocket.send(message);
    }

    private void initServer() {
        try {
            Log.e("TAG", "initServer: start");
            MockWebServer mockWebServer = new MockWebServer();
            mockWebServer.setDispatcher(dispatcher);
            mockWebServer.start();
            host = mockWebServer.getHostName();
            port = mockWebServer.getPort();
            Log.e("TAG", "initServer: success");
        } catch (IOException e) {
            Log.e("TAG", "initServer: " + e);
        }
    }

    private final Dispatcher dispatcher = new Dispatcher() {
        @NonNull
        @Override
        public MockResponse dispatch(@NonNull RecordedRequest recordedRequest) {
            String path = recordedRequest.getPath();
            if ("/".equals(path)) {
                String key = recordedRequest.getHeader("Sec-Websocket-Key");
                String accept = ByteString.encodeUtf8(key + ACCEPT_MAGIC).sha1().base64();
                webSocketListener = new ServerWebSocketListener();
                return new MockResponse()
                        .withWebSocketUpgrade(webSocketListener)
                        .addHeader("Sec-Websocket-Accept", accept);
            }
            return new MockResponse();
        }
    };

    private static final class ServerWebSocketListener extends WebSocketListener {

        private final String TAG = "MockServer";
        WebSocket webSocket;

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosed(webSocket, code, reason);
            this.webSocket = webSocket;
            Log.e(TAG, "onClosed: code = " + code + ", reason = " + reason);
        }

        @Override
        public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosing(webSocket, code, reason);
            this.webSocket = webSocket;
            Log.e(TAG, "onClosing: code = " + code + ", reason = " + reason);
            webSocket.close(code, reason);
        }

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            this.webSocket = webSocket;
            Log.e(TAG, "onFailure: throwable = " + t + ", response = " + response);
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            super.onMessage(webSocket, text);
            this.webSocket = webSocket;
            Log.e(TAG, "onMessage: text = " + text);
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
            this.webSocket = webSocket;
            Log.e(TAG, "onMessage: bytes = " + Arrays.toString(bytes.toByteArray()));
            Log.e(TAG, "onMessage: bytes to text = " + bytes.utf8());
        }

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            super.onOpen(webSocket, response);
            this.webSocket = webSocket;
            Log.e(TAG, "onOpen: response = " + response);
        }
    }

}
