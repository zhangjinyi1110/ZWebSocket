package zjy.android.zwebsocket.response;

import static zjy.android.zwebsocket.Contract.ACCEPT_MAGIC;

import java.util.ArrayList;
import java.util.List;

import okio.ByteString;
import zjy.android.zwebsocket.header.Header;

public class Response {
    private final int code;
    private final List<Header> headers = new ArrayList<>();

    public Response(String content) {
        String[] temp = content.split("\r\n");
        String str = temp[0];
        int codeStartIndex = str.indexOf(" ");
        int codeEndIndex = str.indexOf(" ", codeStartIndex + 1);
        String codeStr = str.substring(codeStartIndex + 1, codeEndIndex);
        code = Integer.parseInt(codeStr);
        int size = temp.length;
        for (int i = 1; i < size; i++) {
            String[] header = temp[i].split(": ");
            headers.add(new Header(header[0], header[1]));
        }
    }

    public int getCode() {
        return code;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        for (Header header : headers) {
            if (header.getKey().equalsIgnoreCase(key)) {
                return header.getValue();
            }
        }
        return null;
    }

    public boolean checkUpgrade(String key) {
        if (code != 101) {
            return false;
        }
        if (!"Upgrade".equalsIgnoreCase(getHeader("Connection"))) {
            return false;
        }
        if (!"WebSocket".equalsIgnoreCase(getHeader("Upgrade"))) {
            return false;
        }
        String accept = getHeader("Sec-Websocket-Accept");
        String code64 = ByteString.encodeUtf8(key + ACCEPT_MAGIC).sha1().base64();
        return code64.equals(accept);
    }

}
