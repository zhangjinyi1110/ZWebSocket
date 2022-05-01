package zjy.android.zwebsocket.writer;

import android.os.Handler;
import android.os.Looper;

public class PingTaskQueue implements Runnable {

    private PingTask pingTask;
    private Handler pingHandler;
    private long pingIntervalMill;
    private int state;

    public static final long MIN_PING_INTERVAL = 10000;

    public void start(PingTask pingTask, long pingIntervalMill) {
        this.pingIntervalMill = Math.max(pingIntervalMill, MIN_PING_INTERVAL);
        this.pingTask = pingTask;
        this.state = 0;
        new Thread(this).start();
    }

    @Override
    public void run() {
        if (state == 0) {
            Looper.prepare();
            pingHandler = new Handler(Looper.myLooper());
            pingHandler.postDelayed(this, pingIntervalMill);
            state = 1;
            Looper.loop();
        } else if (state == 1) {
            this.pingTask.run();
            this.pingHandler.postDelayed(this, pingIntervalMill);
        }
    }

    public void stop() {
        state = 2;
        pingHandler.removeCallbacks(this);
        pingHandler = null;
        pingTask = null;
    }

    public interface PingTask {
        void run();
    }
}
