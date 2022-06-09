package zjy.android.websocketapp.http;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class HttpUrl {

    protected String url;
    protected String username;
    protected String password;
    protected String host;
    protected int port;
    protected List<String> paths;
    protected List<String> queryParams;
    protected String fragment;

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public List<String> getPaths() {
        return paths;
    }

    public List<String> getQueryParams() {
        return queryParams;
    }

    public String getFragment() {
        return fragment;
    }

    public static HttpUrl resolve(String url) {
        List<HttpResolver> resolvers = new ArrayList<>();
        resolvers.add(new ProtocolResolver());
        resolvers.add(new AccountResolver());
        resolvers.add(new HostResolver());
        resolvers.add(new PortResolver());
        resolvers.add(new PathResolver());
        resolvers.add(new QueryParamsResolver());
        resolvers.add(new FragmentResolver());
        ChainImpl chain = new ChainImpl(url, resolvers);
        return chain.proceed();
    }

    @NonNull
    @Override
    public String toString() {
        return "HttpUrl{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", paths=" + paths +
                ", queryParams=" + queryParams +
                ", fragment='" + fragment + '\'' +
                '}';
    }
}
