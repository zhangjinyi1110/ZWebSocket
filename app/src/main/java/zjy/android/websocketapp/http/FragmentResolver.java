package zjy.android.websocketapp.http;

import androidx.annotation.NonNull;

public class FragmentResolver implements HttpResolver {
    @NonNull
    @Override
    public HttpUrl chain(@NonNull Chain chain) {
        HttpUrl httpUrl = chain.proceed();
        int start = chain.url().indexOf('#');
        if (start != -1) {
            httpUrl.fragment = chain.url().substring(start + 1);
        }
        return httpUrl;
    }
}
