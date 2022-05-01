package zjy.android.websocketapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import zjy.android.zwebsocket.IConnectCallback;
import zjy.android.zwebsocket.WebSocket;
import zjy.android.zwebsocket.ZWebSocket;
import zjy.android.zwebsocket.request.Request;

public class MainActivity extends AppCompatActivity {

    WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MockServer.getInstance().start();
        start();

        findViewById(R.id.close).setOnClickListener(v -> new Thread(() -> {
            try {
                webSocket.close(4006, "client apply close");
//                MockServer.getInstance().close(4004, "hhh");
//                Thread.sleep(800);
//                webSocket.reconnect();
            } catch (Exception e) {
                Log.e("TAG", "onCreate: " + e);
            }
        }).start());
        findViewById(R.id.send).setOnClickListener(v -> new Thread(this::send).start());
    }

    private void send() {
        new Thread(() -> {
            try {
                for (int i = 0; i < 1000000; i++) {
                    webSocket.send("msg: " + i);
                }
                webSocket.close(4005, "close");
            } catch (IOException e){
                Log.e("TAG", "send: " + e.toString());
            }
        }).start();
    }

    private void start() {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                String url = MockServer.getInstance().getUrl();
                Log.e("TAG", "onCreate: " + url);
                Request request = new Request.Builder()
                        .setUrl(url)
                        .setPingIntervalMill(10000)
                        .build();
                webSocket = new ZWebSocket();
                webSocket.registerConnectCallback(new IConnectCallback() {
                    final String TAG = "client";
                    @Override
                    public void onConnecting() {
                        Log.e(TAG, "onConnecting: ");
                    }

                    @Override
                    public void onConnected() {
                        Log.e(TAG, "onConnected: ");
                    }

                    @Override
                    public void onClosing(int code, String reason) {
                        Log.e(TAG, "onClosing: " + code + "/" + reason);
                    }

                    @Override
                    public void onClosed(int code, String reason) {
                        Log.e(TAG, "onClosed: " + code + "/" + reason);
                    }

                    @Override
                    public void onConnectFail(int code, String reason) {
                        Log.e(TAG, "onConnectFail: " + code + "/" + reason);
                    }
                });
                webSocket.connect(request);
            } catch (Exception e) {
                Log.e("TAG", "onCreate: " + e);
            }
        }).start();
    }
}