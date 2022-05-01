package zjy.android.zwebsocket;

public interface IConnectCallback {

    void onConnecting();

    void onConnected();

    void onClosing(int code , String reason);

    void onClosed(int code , String reason);

    void onConnectFail(int code , String reason);

}
