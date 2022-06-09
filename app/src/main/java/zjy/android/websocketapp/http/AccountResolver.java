package zjy.android.websocketapp.http;

import androidx.annotation.NonNull;

public class AccountResolver implements HttpResolver {
    @NonNull
    @Override
    public HttpUrl chain(@NonNull Chain chain) {
        HttpUrl httpUrl = chain.proceed();
        int end = chain.url().indexOf('@');
        if (end != -1) {
            int start = chain.url().indexOf("://") + 3;
            String[] account = chain.url().substring(start, end).split(":");
            if (account.length != 2) {
                throw new IllegalArgumentException("The account info error, check your url: " + chain.url());
            }
            httpUrl.username = account[0];
            httpUrl.password = account[1];
        }
        return httpUrl;
    }
}
