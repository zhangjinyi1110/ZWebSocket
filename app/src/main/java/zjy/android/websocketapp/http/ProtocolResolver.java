package zjy.android.websocketapp.http;

import androidx.annotation.NonNull;

public class ProtocolResolver implements HttpResolver {
    @NonNull
    @Override
    public HttpUrl chain(@NonNull Chain chain) {
        HttpUrl httpUrl = chain.proceed();
        if (chain.url().startsWith("ws://")) {
            httpUrl.url = chain.url().replace("ws", "http");
        } else if (chain.url().startsWith("wss://")) {
            httpUrl.url = chain.url().replace("wss", "https");
        } else if (chain.url().startsWith("http://") || chain.url().startsWith("https://")) {
            httpUrl.url = chain.url();
        } else {
            throw new IllegalArgumentException("The url error, start must be 'http' or 'https' or" +
                    " 'ws' or 'wss', your url is " + chain.url());
        }
        return httpUrl;
    }
}
