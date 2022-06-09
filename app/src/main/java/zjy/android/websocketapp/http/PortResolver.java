package zjy.android.websocketapp.http;

import androidx.annotation.NonNull;

public class PortResolver implements HttpResolver {
    @NonNull
    @Override
    public HttpUrl chain(@NonNull Chain chain) {
        HttpUrl httpUrl = chain.proceed();
        int proIndex = chain.url().indexOf("://") + 3;
        int accIndex = chain.url().indexOf("@") + 1;
        int start, end;
        int index = accIndex == 0 ? proIndex : accIndex;
        start = chain.url().indexOf(":", index) + 1;
        if (start == 0) {
            if (httpUrl.url.startsWith("http://")) {
                httpUrl.port = 80;
            } else if (httpUrl.url.startsWith("https://")) {
                httpUrl.port = 443;
            }
        } else {
            end = chain.url().indexOf('/', index);
            if (end == -1) {
                end = chain.url().indexOf('?', index);
                if (end == -1) {
                    end = chain.url().indexOf('#', index);
                }
            }
            if (end == -1) {
                end = chain.url().length();
            }
            if (start >= end) {
                throw new IllegalArgumentException("The port error, check your url: " + chain.url());
            }
            try {
                httpUrl.port = Integer.parseInt(chain.url().substring(start, end));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("The port error, error is " + exception);
            }
        }
        return httpUrl;
    }
}
