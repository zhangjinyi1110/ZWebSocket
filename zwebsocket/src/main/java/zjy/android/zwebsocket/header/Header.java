package zjy.android.zwebsocket.header;

public class Header {

    private final String key;
    private final String value;

    public Header(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }
}
