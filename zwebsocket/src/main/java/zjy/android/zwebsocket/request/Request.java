package zjy.android.zwebsocket.request;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import zjy.android.zwebsocket.header.Header;
import zjy.android.zwebsocket.request.resolver.AccountResolver;
import zjy.android.zwebsocket.request.resolver.ChainImpl;
import zjy.android.zwebsocket.request.resolver.FragmentResolver;
import zjy.android.zwebsocket.request.resolver.HostResolver;
import zjy.android.zwebsocket.request.resolver.PathResolver;
import zjy.android.zwebsocket.request.resolver.PortResolver;
import zjy.android.zwebsocket.request.resolver.ProtocolResolver;
import zjy.android.zwebsocket.request.resolver.QueryParamsResolver;
import zjy.android.zwebsocket.request.resolver.Resolver;

public class Request {

    private String url;
    private long pingIntervalMill;
    private String username;
    private String password;
    private String host;
    private int port;
    private List<String> pathSegments;
    private List<String> queryParams;
    private String fragment;
    private List<Header> headers;

    private Request() {
    }

    public long getPingIntervalMill() {
        return pingIntervalMill;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<String> getPathSegments() {
        return pathSegments;
    }

    public void setPathSegments(List<String> pathSegments) {
        this.pathSegments = pathSegments;
    }

    public String getPath() {
        if (pathSegments == null) return "/";
        StringBuilder builder = new StringBuilder();
        for (String path : pathSegments) {
            builder.append('/').append(path);
        }
        return builder.toString();
    }

    public List<String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<String> queryParams) {
        this.queryParams = queryParams;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    private void resolveUrl(String url) {
        if (url.startsWith("ws:")) {
            url = "http" + url.substring(2);
        } else if (url.startsWith("wss:")) {
            url = "https" + url.substring(3);
        }
        List<Resolver> resolverList = new ArrayList<>();
        resolverList.add(new ProtocolResolver());
        resolverList.add(new AccountResolver());
        resolverList.add(new HostResolver());
        resolverList.add(new PortResolver());
        resolverList.add(new PathResolver());
        resolverList.add(new QueryParamsResolver());
        resolverList.add(new FragmentResolver());
        Resolver.Chain chain = new ChainImpl(this, url, resolverList);
        chain.proceed();
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
                request.resolveUrl(url);
            }
            return request;
        }
    }
}
