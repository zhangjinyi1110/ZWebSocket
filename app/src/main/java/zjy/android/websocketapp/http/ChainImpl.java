package zjy.android.websocketapp.http;

import androidx.annotation.NonNull;

import java.util.List;

import zjy.android.websocketapp.http.HttpResolver;
import zjy.android.websocketapp.http.HttpUrl;

public class ChainImpl implements HttpResolver.Chain {

    private final String url;
    private final List<HttpResolver> resolvers;
    private int index;

    public ChainImpl(@NonNull String url, List<HttpResolver> resolvers) {
        this.url = url;
        this.resolvers = resolvers;
        this.index = resolvers.size();
    }

    @NonNull
    @Override
    public String url() {
        return url;
    }

    @NonNull
    @Override
    public HttpUrl proceed() {
        if (0 == index) return new HttpUrl();
        return resolvers.get(--index).chain(this);
    }
}
