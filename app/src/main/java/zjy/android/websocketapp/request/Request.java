package zjy.android.websocketapp.request;

import zjy.android.websocketapp.http.HttpUrl;

public class Request {

    public HttpUrl httpUrl;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String url;

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Request build() {
            Request request = new Request();
            request.httpUrl = HttpUrl.resolve(url);
            return request;
        }
    }

}
