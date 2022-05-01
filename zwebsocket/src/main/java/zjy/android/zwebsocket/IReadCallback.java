package zjy.android.zwebsocket;

import okio.ByteString;

public interface IReadCallback {

    void onRead(String message);

    void onRead(ByteString message);

}
