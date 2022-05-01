package zjy.android.zwebsocket.request;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import zjy.android.zwebsocket.header.Header;

public class Request {

    private String url;
    private long pingIntervalMill;
    private String host;
    private int port;
    private List<Header> headers;

    private Request() {}

    public long getPingIntervalMill() {
        return pingIntervalMill;
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public static class Builder {

        private String url;
        private long pingIntervalMill = 60000;
        private String host;
        private int port;
        private final List<Header> headers = new ArrayList<>();

        public Builder setPingIntervalMill(long pingIntervalMill) {
            this.pingIntervalMill = pingIntervalMill;
            return this;
        }

        public Builder addHeaders(List<Header> headers) {
            this.headers.addAll(headers);
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder addHeader(String key, String value) {
            return addHeader(new Header(key, value));
        }

        public Builder addHeader(Header header) {
            headers.add(header);
            return this;
        }

        public Request build() {
            Request request = new Request();
            request.headers = headers;
            request.pingIntervalMill = pingIntervalMill;
            if (TextUtils.isEmpty(url)) {
                request.host = host;
                request.port = port;
            } else {
                if (url.startsWith("ws:")) {
                    url = "http" + url.substring(2);
                } else if (url.startsWith("wss:")) {
                    url = "https" + url.substring(3);
                }
                request.url = url;
                int pos = 0;
                if (url.startsWith("http:")) {
                    pos = 7;
                    request.port = 80;
                } else if (url.startsWith("https:")) {
                    pos = 8;
                    request.port = 443;
                }
                int end = url.substring(pos).indexOf(":");
                if (end == -1) {
                    request.host = url.substring(pos);
                } else {
                    request.host = url.substring(pos, end);
                    StringBuilder builder = new StringBuilder();
                    for (char c : url.substring(end + 1).toCharArray()) {
                        if (c == '?' || c == '#' || c == '@' || c == '&') {
                            break;
                        }
                        builder.append(c);
                    }
                    request.port = Integer.parseInt(builder.toString());
                }
            }
            return request;
        }
    }
}
