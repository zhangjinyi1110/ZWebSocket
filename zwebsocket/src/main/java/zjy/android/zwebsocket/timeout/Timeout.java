package zjy.android.zwebsocket.timeout;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Timeout {

    private static TimeHandler handler;
    private static final ArrayList<Message> messageList = new ArrayList<>(5);

    public synchronized static void timeout(int what, long time, Runnable runnable) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = runnable;
        message.arg1 = (int) time;
        if (handler == null) {
            messageList.add(message);
            new Thread(() -> {
                Looper.prepare();
                handler = new TimeHandler();
                for (Message msg : messageList) {
                    handler.sendMessageDelayed(msg, msg.arg1);
                }
                Looper.loop();
            }).start();
        } else {
            handler.sendMessageDelayed(message, time);
        }
    }

    public synchronized static void cancel(int what) {
        if (handler == null) {
            for (Message msg : messageList) {
                if (msg.what == what) {
                    messageList.remove(msg);
                    return;
                }
            }
        } else {
            handler.removeMessages(what);
        }
    }

    private static final class TimeHandler extends Handler {

        TimeHandler() {
            super(Looper.myLooper());
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Runnable run = (Runnable) msg.obj;
            run.run();
        }
    }

}
