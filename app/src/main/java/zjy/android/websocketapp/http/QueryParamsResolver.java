package zjy.android.websocketapp.http;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class QueryParamsResolver implements HttpResolver {
    @NonNull
    @Override
    public HttpUrl chain(@NonNull Chain chain) {
        HttpUrl httpUrl = chain.proceed();
        int start = chain.url().indexOf('?');
        if (start != -1) {
            int end = chain.url().indexOf('#');
            if (end == -1) {
                end = chain.url().length();
            }
            String[] params = chain.url().substring(start + 1, end).split("&");
            httpUrl.queryParams = Arrays.asList(params);
        }
        return httpUrl;
    }
}
