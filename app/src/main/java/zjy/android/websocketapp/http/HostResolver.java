package zjy.android.websocketapp.http;

import androidx.annotation.NonNull;

public class HostResolver implements HttpResolver {
    @NonNull
    @Override
    public HttpUrl chain(@NonNull Chain chain) {
        HttpUrl httpUrl = chain.proceed();
        int proIndex = chain.url().indexOf("://") + 3;
        int start = chain.url().indexOf('@') + 1;
        if (start == 0) {
            start = proIndex;
        }
        int end = chain.url().indexOf(":", start);
        if (end == -1) {
            end = chain.url().indexOf("/", start);
            if (end == -1) {
                end = chain.url().indexOf("?", start);
                if (end == -1) {
                    end = chain.url().indexOf("#", start);
                }
            }
        }
        if (end == -1) {
            end = chain.url().length();
        }
        if (start >= end) {
            throw new IllegalArgumentException("The host can not null, check your url: " + chain.url());
        }
        httpUrl.host = chain.url().substring(start, end);
        return httpUrl;
    }
}
