package zjy.android.websocketapp.http;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class PathResolver implements HttpResolver {
    @NonNull
    @Override
    public HttpUrl chain(@NonNull Chain chain) {
        HttpUrl httpUrl = chain.proceed();
        int proIndex = chain.url().indexOf("://") + 3;
        int start = chain.url().indexOf("/", proIndex);
        if (start != -1) {
            int end = chain.url().indexOf("?", start);
            if (end == -1) {
                end = chain.url().indexOf('#', start);
                if (end == -1) {
                    end = chain.url().length();
                }
            }
            String[] paths = chain.url().substring(start + 1, end).split("/");
            httpUrl.paths = Arrays.asList(paths);
        }
        return httpUrl;
    }
}
