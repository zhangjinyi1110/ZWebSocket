package zjy.android.websocketapp.http;

import androidx.annotation.NonNull;

public interface HttpResolver {

    @NonNull
    HttpUrl chain(@NonNull Chain chain);

    interface Chain {
        @NonNull
        String url();

        @NonNull
        HttpUrl proceed();
    }

}
