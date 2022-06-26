package zjy.android.zwebsocket.reader;

import static zjy.android.zwebsocket.Contract.B0_FLAG_FIN;
import static zjy.android.zwebsocket.Contract.B0_FLAG_RSV1;
import static zjy.android.zwebsocket.Contract.B0_FLAG_RSV2;
import static zjy.android.zwebsocket.Contract.B0_FLAG_RSV3;
import static zjy.android.zwebsocket.Contract.B0_OPCODE;
import static zjy.android.zwebsocket.Contract.B1_FLAG_MASK;
import static zjy.android.zwebsocket.Contract.B1_PAYLOAD_LENGTH;
import static zjy.android.zwebsocket.Contract.OPCODE_BINARY;
import static zjy.android.zwebsocket.Contract.OPCODE_CLOSE;
import static zjy.android.zwebsocket.Contract.OPCODE_CONTINUATION;
import static zjy.android.zwebsocket.Contract.OPCODE_FLAG_CONTROL;
import static zjy.android.zwebsocket.Contract.OPCODE_PING;
import static zjy.android.zwebsocket.Contract.OPCODE_PONG;
import static zjy.android.zwebsocket.Contract.OPCODE_TEXT;
import static zjy.android.zwebsocket.Contract.PAYLOAD_BYTE_MAX;
import static zjy.android.zwebsocket.Contract.PAYLOAD_LONG;
import static zjy.android.zwebsocket.Contract.PAYLOAD_SHORT;
import static zjy.android.zwebsocket.Contract.PAYLOAD_SHORT_MAX;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

public class Reader {

    private final BufferedSource source;
    private final FrameCallback frameCallback;

    private boolean isControllerFrame;
    private boolean isFinalFrame;
    private int opcode;
    private long frameLength;
    private final Buffer messageBuffer = new Buffer();
    private final Buffer controlBuffer = new Buffer();

    private boolean close = false;

    public Reader(InputStream inputStream, FrameCallback frameCallback) {
        this.source = Okio.buffer(Okio.source(inputStream));
        this.frameCallback = frameCallback;
    }

    public void loopRead() throws IOException {
        while (!close) {
            readNextFrame();
        }
    }

    private void readNextFrame() throws IOException {
        try {
            Log.e("TAG", "readNextFrame: 1");
            readHeader();
        } catch (IOException e) {
            Log.e("TAG", "readNextFrame: " + 5000);
            throw new IOException("5000");
        }
        if (isControllerFrame) {
            try {
                readControl();
            } catch (IOException e) {
                Log.e("TAG", "readNextFrame: " + 5100);
                throw new IOException("5100");
            }
        } else {
            try {
                readMessage();
            } catch (IOException e) {
                Log.e("TAG", "readNextFrame: " + 5202);
                throw new IOException("5200");
            }
        }
    }

    private synchronized void readMessage() throws IOException {
        if (close) throw new IOException("reader closed");
        switch (opcode) {
            case OPCODE_BINARY:
            case OPCODE_TEXT:
                break;
            default:
                throw new IOException("unknown message opcode is " + opcode);
        }

        while (true) {
            if (frameLength > 0)
                messageBuffer.write(source, frameLength);
            if (isFinalFrame) break;
            while (!close) {
                Log.e("TAG", "readMessage: 2");
                readHeader();
                if (!isControllerFrame) {
                    break;
                }
                readControl();
            }
            if (opcode != OPCODE_CONTINUATION)
                throw new IOException("message not final must is continuation");
        }

        if (opcode == OPCODE_TEXT) {
            String message = messageBuffer.readUtf8();
            frameCallback.onMessage(message);
        } else if (opcode == OPCODE_BINARY) {
            ByteString byteString = messageBuffer.readByteString();
            frameCallback.onMessage(byteString);
        }
    }

    private synchronized void readControl() throws IOException {
        if (close) throw new IOException("reader closed");
        controlBuffer.write(source, frameLength);
        switch (opcode) {
            case OPCODE_CLOSE:
                int code = controlBuffer.readShort();
                String reason = controlBuffer.readUtf8();
                frameCallback.onClose(code, reason);
                break;
            case OPCODE_PING:
                frameCallback.onPing(controlBuffer.readByteString());
                break;
            case OPCODE_PONG:
                frameCallback.onPong(controlBuffer.readByteString());
                break;
            default:
                throw new IOException("unknown control opcode = " + opcode);
        }
    }

    private synchronized void readHeader() throws IOException {
        if (close) throw new IOException("reader closed");

        Log.e("TAG", "readHeader1: b0");
        int b0;
        try {
            b0 = source.readByte();
        } catch (IOException e) {
            Log.e("TAG", "readHeader: " + e);
            Log.e("TAG", "readHeader: " + Arrays.toString(source.readByteArray()));
            throw new IOException("5101");
        }
        Log.e("TAG", "readHeader2: b0 = " + b0);
        isFinalFrame = (b0 & B0_FLAG_FIN) != 0;
        Log.e("TAG", "readHeader3: isFinalFrame = " + isFinalFrame);
        isControllerFrame = (b0 & OPCODE_FLAG_CONTROL) != 0;
        Log.e("TAG", "readHeader4: isControllerFrame = " + isControllerFrame);
        if (isControllerFrame && !isFinalFrame) {
            throw new IOException("control frame must be final");
        }
        Log.e("TAG", "readHeader5: b0");
        boolean rsv1 = (b0 & B0_FLAG_RSV1) != 0;
        Log.e("TAG", "readHeader6: rsv1 = " + rsv1);
        boolean rsv2 = (b0 & B0_FLAG_RSV2) != 0;
        Log.e("TAG", "readHeader7: rsv2 = " + rsv2);
        boolean rsv3 = (b0 & B0_FLAG_RSV3) != 0;
        Log.e("TAG", "readHeader8: rsv3 = " + rsv3);
        if (rsv1 || rsv2 || rsv3) {
            throw new IOException("rsv is not enabled, rsv1 = " + rsv1 + ", rsv2 = " + rsv2 + ", " +
                    "rsv3 = " + rsv3);
        }
        Log.e("TAG", "readHeader9: b0");
        opcode = b0 & B0_OPCODE;
        Log.e("TAG", "readHeader10: opcode = " + opcode);

        Log.e("TAG", "readHeader1: b1");
        int b1 = source.readByte();
        Log.e("TAG", "readHeader2: b1 = " + b1);
        boolean isMark = (b1 & B1_FLAG_MASK) != 0;
        Log.e("TAG", "readHeader3: isMark = " + isMark);
        if (isMark) {
            throw new IOException("service is not have mark");
        }
        Log.e("TAG", "readHeader4: b1");
        frameLength = b1 & B1_PAYLOAD_LENGTH;
        Log.e("TAG", "readHeader5: b1");
        if (frameLength == PAYLOAD_SHORT) {
            frameLength = source.readShort() & PAYLOAD_SHORT_MAX;
        } else if (frameLength == PAYLOAD_LONG) {
            frameLength = source.readLong();
        }
        Log.e("TAG", "readHeader6: frameLength = " +  frameLength);

        if (isControllerFrame && frameLength > PAYLOAD_BYTE_MAX) {
            throw new IOException("control frame len must be less then 125B");
        }
        Log.e("TAG", "readHeader7: b1 finial");
    }

    public synchronized void close() throws IOException {
        source.close();
        close = true;
    }

    public interface FrameCallback {
        void onMessage(String message) throws IOException;

        void onMessage(ByteString message) throws IOException;

        void onClose(int code, String reason) throws IOException;

        void onPing(ByteString payload) throws IOException;

        void onPong(ByteString payload) throws IOException;
    }
}
