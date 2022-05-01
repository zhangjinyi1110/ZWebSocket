package zjy.android.zwebsocket.call;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import okio.Buffer;
import okio.ByteString;
import zjy.android.zwebsocket.header.Header;
import zjy.android.zwebsocket.request.Request;
import zjy.android.zwebsocket.response.Response;

public class WebSocketCall {

    private final String key;

    public WebSocketCall() {
        byte[] bytes = new byte[16];
        new Random().nextBytes(bytes);
        key = ByteString.of(bytes).base64();
    }

    public void call(Request request, Callback callback) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(request.getHost(), request.getPort()));

        socket.getOutputStream().write(requestHeader(request));

        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[1024];
        Buffer buffer = new Buffer();
        int len;
        while ((len = inputStream.read(bytes)) > 0) {
            buffer.write(bytes, 0, len);
            if (inputStream.available() <= 0) {
                break;
            }
        }
        Response response = new Response(buffer.readUtf8());
        if (response.checkUpgrade(key)) {
            callback.onSuccess(socket);
        } else {
            socket.close();
            callback.onFailure(4000, "check upgrade fail");
        }
    }

    private byte[] requestHeader(Request request) {
        StringBuilder builder = new StringBuilder();
        builder.append("GET / HTTP/1.1").append("\r\n")
                .append("Upgrade: websocket").append("\r\n")
                .append("Connection: Upgrade").append("\r\n")
                .append("Sec-WebSocket-Key: ").append(key).append("\r\n")
                .append("Sec-WebSocket-Version: 13").append("\r\n")
                .append("Host: ").append(request.getHost()).append("\r\n");
        for (Header header : request.getHeaders()) {
            builder.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        builder.append("\r\n");
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public interface Callback {
        void onSuccess(Socket socket);
        void onFailure(int code, String reason);
    }

}
