package zjy.android.websocketapp;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

import okio.Buffer;
import okio.ByteString;
import zjy.android.zwebsocket.IConnectCallback;
import zjy.android.zwebsocket.IReadCallback;
import zjy.android.zwebsocket.WebSocket;
import zjy.android.zwebsocket.ZWebSocket;
import zjy.android.zwebsocket.request.Request;

public class MainActivity extends AppCompatActivity {

    WebSocket webSocket;
    NestedScrollView scrollView;
    TextView content;
    int scrollY;
    boolean scrollState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start();
        content = findViewById(R.id.content);
        scrollView = findViewById(R.id.scroll_view);
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX,
                                                                                        oldScrollY) -> {
            MainActivity.this.scrollY = scrollY;
            scrollState = scrollView.getHeight() + scrollY >= content.getBottom();
        });
    }

    private void send() {
        new Thread(() -> {
            try {
                int headLen = 16;

                Map<String, Object> map = new HashMap<>();
                map.put("uid", 0);
                map.put("roomid", 13550856);
                map.put("protover", 1);
                map.put("platform", "android");
                map.put("clientver", "1.4.0");
                String json = new Gson().toJson(map);
                byte[] data = json.getBytes(StandardCharsets.UTF_8);

                Buffer buffer = new Buffer();
                buffer.writeInt(headLen + data.length);
                buffer.writeShort(headLen);
                buffer.writeShort(1);
                buffer.writeInt(7);
                buffer.writeInt(1);
                buffer.write(data);
//                Log.e("TAG", "send: " + buffer.readByteString());
                webSocket.send(buffer.readByteString());
            } catch (IOException e) {
                Log.e("TAG", "send: " + e);
            }
        }).start();
    }

    long firstTime = 0;
    String json;
    Set<String> cmdList = new HashSet<>();

    private void start() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .setUrl("ws://broadcastlv.chat.bilibili.com:2244/sub")
                        .setPingIntervalMill(300000)
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
                        send();
                        ping();
                    }

                    @Override
                    public void onClosing(int code, String reason) {
                        Log.e(TAG, "onClosing: " + code + "/" + reason);
                        Log.e(TAG, "onClosing: " + (System.currentTimeMillis() - firstTime));
                    }

                    @Override
                    public void onClosed(int code, String reason) {
                        Log.e(TAG, "onClosed: " + code + "/" + reason);
                    }

                    @Override
                    public void onConnectFail(int code, String reason) {
                        Log.e(TAG, "onConnectFail: " + code + "/" + reason);
                        Log.e(TAG, "onConnectFail: " + (System.currentTimeMillis() - firstTime));
                    }
                });
                webSocket.registerReadCallback(new IReadCallback() {
                    @Override
                    public void onRead(String message) {
                        Log.e("TAG-string", "onRead: " + message);
                    }

                    @Override
                    public void onRead(ByteString message) {
                        if (firstTime == 0) {
                            firstTime = System.currentTimeMillis();
                        }
                        Log.e("TAG-byte", "onRead: " + message);
                        Buffer buffer = new Buffer();
                        buffer.write(message);
                        try {
                            int total = buffer.readInt();
                            Log.e("TAG", "onRead: total = " + total);
                            int headLen = buffer.readShort();
                            Log.e("TAG", "onRead: headLen = " + headLen);
                            int pVersion = buffer.readShort();
                            Log.e("TAG", "onRead: pVersion = " + pVersion);
                            int opcode = buffer.readInt();
                            Log.e("TAG", "onRead: opcode = " + opcode);
                            int sequence = buffer.readInt();
                            Log.e("TAG", "onRead: sequence = " + sequence);
                            if (opcode == 5) {
                                if (pVersion <= 1) {
                                    Log.e("TAG", "onRead: data = " + buffer.readUtf8());
                                } else {
                                    String tempData = new String(decompress(buffer.readByteArray(total - headLen)),
                                            StandardCharsets.UTF_8);
                                    String regEx = "[\\x00-\\x1f]+";
                                    Pattern p = Pattern.compile(regEx);
                                    Matcher m = p.matcher(tempData);
                                    String data = m.replaceAll("===").trim();
                                    String[] jsonList = data.split("===");
                                    for (String item : jsonList) {
                                        if (item.startsWith("{\"")) {
                                            json = item;
                                            Map<String, Object> map = new Gson().fromJson(item, new TypeToken<Map<String,
                                                    Object>>() {
                                            }.getType());
                                            String cmd = (String) map.get("cmd");
                                            cmdList.add(cmd);
                                            if ("DANMU_MSG".equals(cmd)) {
                                                handleDanMu((List<Object>) map.get("info"));
                                            } else if ("COMBO_SEND".equals(cmd)) {
//                                                handleCombo();
                                                Log.e("TAG", "onRead1: COMBO_SEND " + map);
                                            } else if ("SEND_GIFT".equals(cmd)) {
                                                Log.e("TAG", "onRead2: COMBO_SEND " + map);
                                            }
                                        }
                                    }
                                    Log.e("TAG", "onRead: cmdList = " + cmdList);
                                }
                            } else if (opcode == 3) {
                                Log.e("TAG", "onRead: wow===wow===wow" + buffer.readInt());
                            } else {
                                Log.e("TAG", "onRead: ==========================" + opcode);
                            }
                        } catch (EOFException e) {
                            e.printStackTrace();
                            Log.e("TAG", "onRead: " + e);
                        }
                    }
                });
                webSocket.connect(request);
            } catch (Exception e) {
                Log.e("TAG", "onCreate: " + e);
                Log.e("TAG", "onCreate: json =" + json);
            }
        }).start();
    }

    private void handleDanMu(List<Object> info) {
        String danMu = (String) info.get(1);
        List<Object> user = (List<Object>) info.get(2);
        String username = (String) user.get(1);
        Log.e("TAG", "handleDanMu: " + info);
        runOnUiThread(() -> {
            SpannableString string = new SpannableString(username);
            string.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, string.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            content.append(string);
            content.append(": " + danMu + "\n");
            content.post(() -> {
                if (scrollState && scrollView.getHeight() + scrollY < content.getBottom()) {
                    scrollView.scrollTo(0, content.getBottom());
                }
            });
        });
    }

    private void ping() {
        new Thread(() -> {
            try {
                Thread.sleep(30000);
                while (webSocket != null) {
                    Buffer buffer = new Buffer();
                    buffer.writeInt(16);
                    buffer.writeShort(16);
                    buffer.writeShort(1);
                    buffer.writeInt(2);
                    buffer.writeInt(1);
                    webSocket.send(buffer.readByteString());
                    Thread.sleep(30000);
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private byte[] decompress(byte[] data) {
        byte[] output;

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }
}